package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterPost;
import de.teamlapen.vampirism.common.world.entity.hunter.LookoutSpots;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class HunterSquad {

    private static final int STABLE_SIZE = 5;
    private static final double MEMBER_BASE_STRENGTH = 5.0D;
    private static final double TRANSFER_COST = 4.0D;
    private static final double COMPOSITION_PENALTY = 6.0D;
    private static final double MIN_REBALANCE_GAIN = 4.0D;
    private static final int THINK_INTERVAL = 10;
    private static final int ORDER_TTL = 30; // TTL = time to live: ticks before a squad order expires from a hunter's memory
    private static final double MAX_LEADER_DISTANCE = 40.0D;
    private static final double HEADING_MIN_SPEED_SQR = 0.0004D;
    private static final int GUARD_DURATION = 600;
    private static final int THREAT_MEMORY = 60;
    private static final double POST_STRAGGLER_MARGIN = 8.0D;
    private static final int BANNER_MIN_SIZE = 5;
    private static final int MAX_CACHED_ZONES = 4;
    private static final int BANNER_SUMMON_CHANCE = 60;
    private static final int BANNER_COOLDOWN = 6000;
    private static final double BANNER_RECOVERY_RANGE = 32.0D;
    private static final int BANNER_RECOVERY_TIMEOUT = 600;

    private final UUID id;
    private final int thinkOffset;
    private final List<Hunter> members = new ArrayList<>();
    private @Nullable Hunter leader;
    private SquadStrategy strategy = GeneralStrategy.INSTANCE;
    private Vec3 heading = new Vec3(1, 0, 0);
    private boolean retreating;
    private boolean disbanded;
    private long guardUntil;
    private final Map<LivingEntity, Long> rememberedThreats = new HashMap<>();
    private int threatCount;
    private long bannerCooldownUntil;
    private @Nullable ItemEntity droppedBanner;
    private int bannerPursuitTicks;
    private final Set<UUID> abandonedBanners = new HashSet<>();
    private final Map<Hunter, BlockPos> lookoutClaims = new HashMap<>();
    private final Map<PatrolArea, PatrolZone> patrolZones = new HashMap<>();

    private HunterSquad(UUID id) {
        this.id = id;
        this.thinkOffset = Math.floorMod(id.hashCode(), THINK_INTERVAL);
    }

    public static void tickMember(ServerLevel level, Hunter hunter) {
        HunterSquad squad = hunter.getSquad();
        if (squad == null || squad.disbanded || !squad.members.contains(hunter)) {
            UUID id = squad == null && hunter.getSquadId() != null ? hunter.getSquadId() : UUID.randomUUID();
            squad = new HunterSquad(id);
            squad.addMember(hunter);
        }

        squad.tickForMember(level, hunter);
    }

    private void tickForMember(ServerLevel level, Hunter hunter) {
        if (this.leader == null || !isActiveMember(this.leader, level)) {
            this.members.removeIf(member -> !isActiveMember(member, level));
            electLeader();
        }

        if (hunter != this.leader) return;
        if ((level.getGameTime() + this.thinkOffset) % THINK_INTERVAL != 0) return;

        planOrders(level);
    }

    private void planOrders(ServerLevel level) {
        this.members.removeIf(member -> !isActiveMember(member, level));
        if (this.leader == null || !this.members.contains(this.leader)) electLeader();
        if (this.leader == null) return;

        updateHeading();
        kickStragglers();
        dismissExcessMembers();
        mergeWithVisibleSquads();
        promoteStrongerLeader();
        updateBanner(level);

        if (this.members.size() < 2) return;

        SquadContext context = SquadContext.create(level, this);
        rememberThreats(context.getReportedThreats(), level.getGameTime());
        this.threatCount = context.threats().size();
        if (context.hasThreats()) {
            this.guardUntil = level.getGameTime() + GUARD_DURATION;
        }
        this.strategy.tick(context);
        context.applyOrders(ORDER_TTL);
    }

    private void rememberThreats(List<LivingEntity> threats, long gameTime) {
        for (LivingEntity threat : threats) {
            this.rememberedThreats.put(threat, gameTime);
        }
    }

    public Set<LivingEntity> getRememberedThreats(long gameTime) {
        this.rememberedThreats.entrySet().removeIf(entry -> gameTime - entry.getValue() > THREAT_MEMORY || !entry.getKey().isAlive());
        return this.rememberedThreats.keySet();
    }

    public int threatCount() {
        return this.threatCount;
    }

    private void updateHeading() {
        Vec3 movement = this.leader.getDeltaMovement();
        Vec3 horizontal = new Vec3(movement.x, 0, movement.z);
        if (horizontal.lengthSqr() > HEADING_MIN_SPEED_SQR) {
            this.heading = horizontal.normalize();
        }
    }

    private void kickStragglers() {
        if (this.leader.isFighting()) return;

        HunterPost post = post();
        for (Hunter member : List.copyOf(this.members)) {
            if (member == this.leader || member.distanceTo(this.leader) <= MAX_LEADER_DISTANCE) continue;
            if (post != null && post.equals(member.getPost()) && post.contains(member.blockPosition(), POST_STRAGGLER_MARGIN)) continue;

            detachMember(member);
        }
    }

    private void dismissExcessMembers() {
        int excess = this.members.size() - maxSize();
        if (excess <= 0) return;

        this.members.stream()
                .filter(member -> member != this.leader)
                .sorted(Comparator.comparingInt(HunterSquad::calculateLeadershipScore))
                .limit(excess)
                .toList()
                .forEach(this::detachMember);
    }

    public int maxSize() {
        return this.leader != null ? this.leader.getMaxSquadSize() : Hunter.BASE_SQUAD_SIZE;
    }

    private void mergeWithVisibleSquads() {
        Set<HunterSquad> visible = new LinkedHashSet<>();
        for (Hunter member : this.members) {
            for (LivingEntity seen : member.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()).orElse(List.of())) {
                if (!(seen instanceof Hunter other) || !other.isAlive()) continue;

                HunterSquad otherSquad = other.getSquad();
                if (otherSquad == null || otherSquad == this || otherSquad.disbanded || !otherSquad.members.contains(other)) continue;
                visible.add(otherSquad);
            }
        }

        for (HunterSquad other : visible) {
            if (this.disbanded || this.leader == null) return;
            if (other.disbanded) continue;

            if (shouldAbsorb(other)) {
                absorbSquad(other);
            } else if (!other.shouldAbsorb(this)) {
                rebalanceWith(other);
            }
        }
    }

    private void rebalanceWith(HunterSquad other) {
        if (other.leader == null || this.leader.isFighting() || other.leader.isFighting()) return;

        List<List<Hunter>> outgoing = listTransfers(this.members, other.leader);
        List<List<Hunter>> incoming = listTransfers(other.members, this.leader);
        double bestCost = calculateSplitCost(this.members, other.members, 0) - MIN_REBALANCE_GAIN;
        List<Hunter> bestOut = null;
        List<Hunter> bestIn = null;

        for (List<Hunter> out : outgoing) {
            for (List<Hunter> in : incoming) {
                if (out.isEmpty() && in.isEmpty()) continue;

                List<Hunter> here = new ArrayList<>(this.members);
                here.removeAll(out);
                here.addAll(in);
                List<Hunter> there = new ArrayList<>(other.members);
                there.removeAll(in);
                there.addAll(out);
                if (!isValidSplit(here, this.members.size()) || !isValidSplit(there, other.members.size())) continue;

                double cost = calculateSplitCost(here, there, out.size() + in.size());
                if (cost < bestCost) {
                    bestCost = cost;
                    bestOut = out;
                    bestIn = in;
                }
            }
        }

        if (bestOut == null) return;
        transfer(bestOut, this, other);
        transfer(bestIn, other, this);
        electLeader();
        other.electLeader();
    }

    private static List<List<Hunter>> listTransfers(List<Hunter> members, Hunter destinationLeader) {
        List<Hunter> movable = members.stream()
                .filter(member -> !member.getBrain().hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get()))
                .filter(member -> member.distanceTo(destinationLeader) < MAX_LEADER_DISTANCE)
                .toList();

        List<List<Hunter>> transfers = new ArrayList<>();
        transfers.add(List.of());
        for (int i = 0; i < movable.size(); i++) {
            transfers.add(List.of(movable.get(i)));
            for (int j = i + 1; j < movable.size(); j++) {
                transfers.add(List.of(movable.get(i), movable.get(j)));
            }
        }
        return transfers;
    }

    private static boolean isValidSplit(List<Hunter> squad, int originalSize) {
        if (squad.isEmpty() || squad.size() < Math.min(originalSize, STABLE_SIZE)) return false;
        Hunter leader = squad.stream().max(Comparator.comparingInt(HunterSquad::calculateLeadershipScore)).orElseThrow();
        return squad.size() <= leader.getMaxSquadSize();
    }

    private static double calculateSplitCost(List<Hunter> first, List<Hunter> second, int transfers) {
        return Math.abs(calculateStrength(first) - calculateStrength(second)) + transfers * TRANSFER_COST + calculateCompositionPenalty(first) + calculateCompositionPenalty(second);
    }

    private static double calculateStrength(List<Hunter> squad) {
        double strength = 0;
        for (Hunter member : squad) {
            strength += MEMBER_BASE_STRENGTH + member.getFactionLevel();
        }
        return strength;
    }

    private static double calculateCompositionPenalty(List<Hunter> squad) {
        if (squad.size() < 2) return 0;
        boolean hasRanged = squad.stream().anyMatch(Hunter::isRangedClass);
        boolean hasMelee = squad.stream().anyMatch(Hunter::isMeleeClass);
        return hasRanged && hasMelee ? 0 : COMPOSITION_PENALTY;
    }

    private static void transfer(List<Hunter> hunters, HunterSquad from, HunterSquad to) {
        for (Hunter hunter : hunters) {
            from.members.remove(hunter);
            clearOrders(hunter);
            to.addMember(hunter);
        }
    }

    private boolean shouldAbsorb(HunterSquad other) {
        boolean sameId = other.id.equals(this.id);
        if (!sameId && this.members.size() + other.members.size() > Math.max(maxSize(), other.maxSize())) {
            return false;
        }

        if (this.members.size() != other.members.size()) {
            return this.members.size() > other.members.size();
        }

        return sameId || this.id.compareTo(other.id) < 0;
    }

    private void absorbSquad(HunterSquad other) {
        for (Hunter hunter : other.members) {
            addMember(hunter);
        }
        other.members.clear();
        other.leader = null;
        other.disbanded = true;
    }

    private void addMember(Hunter hunter) {
        if (!this.members.contains(hunter)) {
            this.members.add(hunter);
        }
        hunter.setSquad(this);
        this.bannerCooldownUntil = Math.max(this.bannerCooldownUntil, hunter.getBannerCooldownUntil());
    }

    private void detachMember(Hunter hunter) {
        this.members.remove(hunter);
        clearOrders(hunter);
        new HunterSquad(UUID.randomUUID()).addMember(hunter);
    }

    private static void clearOrders(Hunter hunter) {
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_TARGET.get());
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_POSITION.get());
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_RETREAT.get());
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_PATROL_AREA.get());
    }

    private void electLeader() {
        this.leader = this.members.stream().max(Comparator.comparingInt(HunterSquad::calculateLeadershipScore)).orElse(null);
    }

    private void promoteStrongerLeader() {
        for (Hunter member : this.members) {
            if (calculateLeadershipScore(member) > calculateLeadershipScore(this.leader)) {
                this.leader = member;
            }
        }
    }

    private void updateBanner(ServerLevel level) {
        handBannersToLeader();

        long gameTime = level.getGameTime();
        if (this.leader.hasBanner()) {
            setDroppedBanner(null);
            if (gameTime >= this.bannerCooldownUntil) this.leader.makeBannerOminous();
            return;
        }

        setDroppedBanner(isRecoverable(level, this.droppedBanner) ? this.droppedBanner : searchForDroppedBanner(level));
        if (this.droppedBanner != null) return;

        if (this.members.size() >= BANNER_MIN_SIZE && gameTime >= this.bannerCooldownUntil && !this.retreating && level.getRandom().nextInt(BANNER_SUMMON_CHANCE) == 0) {
            this.leader.equipBanner(Hunter.createSquadBanner(level), true);
        }
    }

    private void handBannersToLeader() {
        for (Hunter member : this.members) {
            if (member == this.leader || !member.hasBanner()) continue;

            boolean ominous = member.hasOminousBanner();
            ItemStack banner = member.removeBanner();
            if (!this.leader.hasBanner()) {
                this.leader.equipBanner(banner, ominous);
            } else if (ominous) {
                this.leader.makeBannerOminous();
            }
        }
    }

    public void startBannerCooldown(long gameTime) {
        this.bannerCooldownUntil = gameTime + BANNER_COOLDOWN;
        for (Hunter member : this.members) {
            member.setBannerCooldownUntil(this.bannerCooldownUntil);
        }
    }

    public void onBannerDropped(ItemEntity banner) {
        setDroppedBanner(banner);
    }

    private void setDroppedBanner(@Nullable ItemEntity banner) {
        if (banner != this.droppedBanner) this.bannerPursuitTicks = 0;
        this.droppedBanner = banner;
    }

    public void tickBannerPursuit() {
        if (this.droppedBanner == null || ++this.bannerPursuitTicks < BANNER_RECOVERY_TIMEOUT) return;

        this.abandonedBanners.add(this.droppedBanner.getUUID());
        setDroppedBanner(null);
    }

    public @Nullable ItemEntity findDroppedBanner(ServerLevel level) {
        return isRecoverable(level, this.droppedBanner) ? this.droppedBanner : null;
    }

    private @Nullable ItemEntity searchForDroppedBanner(ServerLevel level) {
        this.abandonedBanners.removeIf(id -> level.getEntity(id) == null);
        return level.getEntitiesOfClass(ItemEntity.class, this.leader.getBoundingBox().inflate(BANNER_RECOVERY_RANGE), banner -> isRecoverable(level, banner)).stream()
                .min(Comparator.comparingDouble(this.leader::distanceToSqr))
                .orElse(null);
    }

    private boolean isRecoverable(ServerLevel level, @Nullable ItemEntity banner) {
        if (banner == null || this.leader == null) return false;
        if (banner.isRemoved() || level.getEntity(banner.getId()) != banner || !Hunter.isBanner(banner.getItem())) return false;
        if (banner.getOwner() instanceof Player || this.abandonedBanners.contains(banner.getUUID())) return false;
        return banner.distanceTo(this.leader) <= BANNER_RECOVERY_RANGE;
    }

    public long bannerCooldownUntil() {
        return this.bannerCooldownUntil;
    }

    private static int calculateLeadershipScore(Hunter hunter) {
        return hunter.getFactionLevel();
    }

    private static boolean isActiveMember(Hunter hunter, ServerLevel level) {
        return hunter.isAlive() && !hunter.isRemoved() && hunter.level() == level;
    }

    public UUID id() {
        return this.id;
    }

    public List<Hunter> members() {
        return Collections.unmodifiableList(this.members);
    }

    public @Nullable Hunter leader() {
        return this.leader;
    }

    public Vec3 heading() {
        return this.heading;
    }

    public boolean isRetreating() {
        return this.retreating;
    }

    public boolean isOnGuard(long gameTime) {
        return gameTime < this.guardUntil;
    }

    public PatrolZone getPatrolZone(ServerLevel level, PatrolArea area) {
        long gameTime = level.getGameTime();
        this.patrolZones.values().removeIf(zone -> zone.isExpired(gameTime));
        PatrolZone zone = this.patrolZones.get(area);
        if (zone == null) {
            if (this.patrolZones.size() >= MAX_CACHED_ZONES) this.patrolZones.clear();
            zone = PatrolZone.scan(level, area);
            this.patrolZones.put(area, zone);
        }
        return zone;
    }

    public boolean claimLookoutSpot(Hunter hunter, BlockPos spot) {
        this.lookoutClaims.keySet().removeIf(member -> !this.members.contains(member));
        for (Map.Entry<Hunter, BlockPos> claim : this.lookoutClaims.entrySet()) {
            if (claim.getKey() != hunter && claim.getValue().closerThan(spot, LookoutSpots.MIN_SPOT_SPACING)) return false;
        }
        this.lookoutClaims.put(hunter, spot);
        return true;
    }

    public void releaseLookoutSpot(Hunter hunter) {
        this.lookoutClaims.remove(hunter);
    }

    public @Nullable HunterPost post() {
        if (this.leader != null && this.leader.getPost() != null) return this.leader.getPost();
        for (Hunter member : this.members) {
            HunterPost post = member.getPost();
            if (post != null) return post;
        }
        return null;
    }

    public void setRetreating(boolean retreating) {
        this.retreating = retreating;
    }

    public SquadStrategy strategy() {
        return this.strategy;
    }

    public void setStrategy(SquadStrategy strategy) {
        this.strategy = strategy;
    }
}
