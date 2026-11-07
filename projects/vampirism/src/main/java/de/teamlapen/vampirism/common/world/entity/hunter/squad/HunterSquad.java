package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterPost;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class HunterSquad {

    public static final int MAX_SIZE = 9;
    private static final int MIN_SIZE = 3;
    private static final int THINK_INTERVAL = 10;
    private static final int ORDER_TTL = 30; // TTL = time to live: ticks before a squad order expires from a hunter's memory
    private static final double MAX_LEADER_DISTANCE = 40.0D;
    private static final double HEADING_MIN_SPEED_SQR = 0.0004D;
    private static final int GUARD_DURATION = 600;
    private static final int THREAT_MEMORY = 60;
    private static final double POST_STRAGGLER_MARGIN = 8.0D;
    private static final int BANNER_MIN_SIZE = 5;
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

    private void mergeWithVisibleSquads() {
        for (Hunter member : List.copyOf(this.members)) {
            for (LivingEntity seen : member.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()).orElse(List.of())) {
                if (!(seen instanceof Hunter other) || !other.isAlive()) continue;

                HunterSquad otherSquad = other.getSquad();
                if (otherSquad == null || otherSquad == this || otherSquad.disbanded || !otherSquad.members.contains(other)) continue;

                if (shouldAbsorb(otherSquad)) {
                    absorbSquad(otherSquad);
                } else if (this.members.size() > otherSquad.members.size()) {
                    sendReinforcements(otherSquad);
                } else {
                    otherSquad.sendReinforcements(this);
                }
            }
        }
    }

    private void sendReinforcements(HunterSquad recipient) {
        int needed = MIN_SIZE - recipient.members.size();
        if (needed <= 0 || recipient.members.isEmpty() || this.members.size() - needed < MIN_SIZE) return;

        Hunter anchor = recipient.leader != null ? recipient.leader : recipient.members.getFirst();
        List<Hunter> candidates = new ArrayList<>(this.members.stream()
                .filter(member -> member != this.leader)
                .filter(member -> !member.getBrain().hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get()))
                .filter(member -> member.distanceTo(anchor) < MAX_LEADER_DISTANCE)
                .toList());

        for (int i = 0; i < needed && !candidates.isEmpty(); i++) {
            boolean wantRanged = recipient.countByClass(true) < recipient.countByClass(false);
            Hunter pick = candidates.stream()
                    .min(Comparator.comparingInt((Hunter member) -> calculateTransferPenalty(member, wantRanged))
                            .thenComparingDouble(member -> member.distanceToSqr(anchor)))
                    .orElseThrow();
            candidates.remove(pick);
            this.members.remove(pick);
            clearOrders(pick);
            recipient.addMember(pick);
        }
    }

    private int calculateTransferPenalty(Hunter member, boolean wantRanged) {
        int penalty = member.isRangedClass() == wantRanged ? 0 : 1;
        if (countByClass(member.isRangedClass()) <= 1) penalty += 2;
        return penalty;
    }

    private int countByClass(boolean ranged) {
        int count = 0;
        for (Hunter member : this.members) {
            if (member.isRangedClass() == ranged) count++;
        }
        return count;
    }

    private boolean shouldAbsorb(HunterSquad other) {
        boolean sameId = other.id.equals(this.id);
        if (!sameId && this.members.size() + other.members.size() > MAX_SIZE) {
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
