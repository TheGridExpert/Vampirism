package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
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

    private final UUID id;
    private final int thinkOffset;
    private final List<Hunter> members = new ArrayList<>();
    private @Nullable Hunter leader;
    private SquadStrategy strategy = GeneralStrategy.INSTANCE;
    private Vec3 heading = new Vec3(1, 0, 0);
    private boolean retreating;
    private boolean disbanded;

    private HunterSquad(UUID id) {
        this.id = id;
        this.thinkOffset = Math.floorMod(id.hashCode(), THINK_INTERVAL);
    }

    public static void tickMember(ServerLevel level, Hunter hunter) {
        HunterSquad squad = hunter.getSquad();
        if (squad == null || squad.disbanded || !squad.members.contains(hunter)) {
            UUID id = squad == null && hunter.getSquadId() != null ? hunter.getSquadId() : UUID.randomUUID();
            squad = new HunterSquad(id);
            squad.add(hunter);
        }

        squad.tickFrom(level, hunter);
    }

    private void tickFrom(ServerLevel level, Hunter hunter) {
        if (this.leader == null || !isActive(this.leader, level)) {
            this.members.removeIf(member -> !isActive(member, level));
            electLeader();
        }

        if (hunter != this.leader) return;
        if ((level.getGameTime() + this.thinkOffset) % THINK_INTERVAL != 0) return;

        think(level);
    }

    private void think(ServerLevel level) {
        this.members.removeIf(member -> !isActive(member, level));
        if (this.leader == null || !this.members.contains(this.leader)) electLeader();
        if (this.leader == null) return;

        updateHeading();
        kickStragglers();
        mergeWithVisibleSquads();
        promoteStrongerLeader();

        if (this.members.size() < 2) return;

        SquadContext context = SquadContext.create(level, this);
        this.strategy.tick(context);
        context.apply(ORDER_TTL);
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

        for (Hunter member : List.copyOf(this.members)) {
            if (member != this.leader && member.distanceTo(this.leader) > MAX_LEADER_DISTANCE) {
                leave(member);
            }
        }
    }

    private void mergeWithVisibleSquads() {
        for (Hunter member : List.copyOf(this.members)) {
            for (LivingEntity seen : member.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()).orElse(List.of())) {
                if (!(seen instanceof Hunter other) || !other.isAlive()) continue;

                HunterSquad otherSquad = other.getSquad();
                if (otherSquad == null || otherSquad == this || otherSquad.disbanded || !otherSquad.members.contains(other)) continue;

                if (shouldAbsorb(otherSquad)) {
                    absorb(otherSquad);
                } else if (this.members.size() > otherSquad.members.size()) {
                    reinforce(otherSquad);
                } else {
                    otherSquad.reinforce(this);
                }
            }
        }
    }

    private void reinforce(HunterSquad recipient) {
        int needed = MIN_SIZE - recipient.members.size();
        if (needed <= 0 || recipient.members.isEmpty() || this.members.size() - needed < MIN_SIZE) return;

        Hunter anchor = recipient.leader != null ? recipient.leader : recipient.members.getFirst();
        List<Hunter> candidates = new ArrayList<>(this.members.stream()
                .filter(member -> member != this.leader)
                .filter(member -> !member.getBrain().hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get()))
                .filter(member -> member.distanceTo(anchor) < MAX_LEADER_DISTANCE)
                .toList());

        for (int i = 0; i < needed && !candidates.isEmpty(); i++) {
            boolean wantRanged = recipient.countRanged(true) < recipient.countRanged(false);
            Hunter pick = candidates.stream()
                    .min(Comparator.comparingInt((Hunter member) -> transferPenalty(member, wantRanged))
                            .thenComparingDouble(member -> member.distanceToSqr(anchor)))
                    .orElseThrow();
            candidates.remove(pick);
            this.members.remove(pick);
            clearOrders(pick);
            recipient.add(pick);
        }
    }

    private int transferPenalty(Hunter member, boolean wantRanged) {
        int penalty = member.isRangedClass() == wantRanged ? 0 : 1;
        if (countRanged(member.isRangedClass()) <= 1) penalty += 2;
        return penalty;
    }

    private int countRanged(boolean ranged) {
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

    private void absorb(HunterSquad other) {
        for (Hunter hunter : other.members) {
            add(hunter);
        }
        other.members.clear();
        other.leader = null;
        other.disbanded = true;
    }

    private void add(Hunter hunter) {
        if (!this.members.contains(hunter)) {
            this.members.add(hunter);
        }
        hunter.setSquad(this);
    }

    private void leave(Hunter hunter) {
        this.members.remove(hunter);
        clearOrders(hunter);
        new HunterSquad(UUID.randomUUID()).add(hunter);
    }

    private static void clearOrders(Hunter hunter) {
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_TARGET.get());
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_POSITION.get());
        hunter.getBrain().eraseMemory(ModMemoryTypes.SQUAD_RETREAT.get());
    }

    private void electLeader() {
        this.leader = this.members.stream().max(Comparator.comparingInt(HunterSquad::leadershipScore)).orElse(null);
    }

    private void promoteStrongerLeader() {
        for (Hunter member : this.members) {
            if (leadershipScore(member) > leadershipScore(this.leader)) {
                this.leader = member;
            }
        }
    }

    private static int leadershipScore(Hunter hunter) {
        return hunter.getFactionLevel();
    }

    private static boolean isActive(Hunter hunter, ServerLevel level) {
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
