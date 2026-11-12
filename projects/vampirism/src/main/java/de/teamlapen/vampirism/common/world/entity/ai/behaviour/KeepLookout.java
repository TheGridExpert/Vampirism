package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.LookoutSpots;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KeepLookout extends Behavior<Hunter> {

    private static final int MAX_DURATION = 3600;
    private static final int MAX_TRAVEL_TICKS = 600;
    private static final int MAX_WALK_ATTEMPTS = 3;
    private static final double ARRIVAL_DISTANCE_SQR = 1.5D * 1.5D;
    private static final int MIN_WATCH_TICKS = 600;
    private static final int MAX_WATCH_TICKS = 1400;
    private static final int MIN_GLANCE_TICKS = 60;
    private static final int MAX_GLANCE_TICKS = 140;
    private static final int SPYGLASS_RAISE_DELAY = 15;
    private static final float SPYGLASS_CHANCE = 0.6F;
    private static final float WATCH_MOB_CHANCE = 0.25F;
    private static final double GLANCE_DISTANCE = 32.0D;
    private static final double MAX_GLANCE_PITCH = 0.15D;
    private static final int PATROL_HOLD_TICKS = 40;
    private static final int MIN_COOLDOWN = 1200;
    private static final int MAX_COOLDOWN = 2400;
    private static final int FAILED_COOLDOWN = 200;
    private static final int NO_SPOT_COOLDOWN = 400;
    private static final int MAX_PATH_CHECKS = 4;

    private final float speed;
    private @Nullable LookoutSpots spots;
    private @Nullable BlockPos favourite;
    private @Nullable BlockPos spot;
    private boolean watching;
    private boolean failed;
    private int walkAttempts;
    private long phaseEnd;
    private long glanceEnd;
    private long spyglassAt;
    private @Nullable Vec3 glancePoint;
    private @Nullable LivingEntity watchedMob;

    public KeepLookout(float speed) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                ModMemoryTypes.LOOKOUT_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.SQUAD_POSITION.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.CURE_TARGET.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.WEAPONS_UNSHEATHED.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.SHOULD_RETREAT.get(), MemoryStatus.VALUE_ABSENT
        ), MAX_DURATION);
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        if (!hunter.isRangedClass()) return false;

        PatrolArea area = hunter.getPatrolArea();
        if (area == null) return giveUp(hunter, "no patrol area");

        if (this.spots == null || this.spots.isOutdated(area, level.getGameTime())) {
            this.spots = LookoutSpots.scan(level, area, hunter.getPatrolZone());
        }
        hunter.getBrain().setMemory(ModMemoryTypes.LOOKOUT_CANDIDATES.get(), List.copyOf(this.spots.spots()));
        if (this.spots.spots().isEmpty()) return giveUp(hunter, "no spots in r" + area.radius());

        this.spot = pickSpot(hunter);
        return this.spot != null || giveUp(hunter, "no free reachable spot");
    }

    private @Nullable BlockPos pickSpot(Hunter hunter) {
        HunterSquad squad = hunter.getSquad();
        List<BlockPos> candidates = new ArrayList<>(this.spots.spots());
        if (this.favourite != null && candidates.remove(this.favourite)) {
            candidates.addFirst(this.favourite);
        }
        int pathChecks = 0;
        for (BlockPos candidate : candidates) {
            if (pathChecks >= MAX_PATH_CHECKS) break;
            if (squad != null && !squad.claimLookoutSpot(hunter, candidate)) continue;

            pathChecks++;
            Path path = hunter.getNavigation().createPath(candidate, 0);
            if (path != null && path.canReach()) return candidate;

            if (squad != null) squad.releaseLookoutSpot(hunter);
            this.spots.reject(candidate);
            if (candidate.equals(this.favourite)) this.favourite = null;
        }
        return null;
    }

    private static boolean giveUp(Hunter hunter, String reason) {
        Brain<Hunter> brain = hunter.getBrain();
        brain.setMemory(ModMemoryTypes.LOOKOUT_STATUS.get(), reason);
        brain.setMemoryWithExpiry(ModMemoryTypes.LOOKOUT_COOLDOWN.get(), Unit.INSTANCE, NO_SPOT_COOLDOWN);
        return false;
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        this.watching = false;
        this.failed = false;
        this.walkAttempts = 0;
        this.phaseEnd = gameTime + MAX_TRAVEL_TICKS;
        Brain<Hunter> brain = hunter.getBrain();
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.setMemory(ModMemoryTypes.LOOKOUT_SPOT.get(), this.spot);
        brain.setMemory(ModMemoryTypes.LOOKOUT_STATUS.get(), "walking to spot");
        walkToSpot(hunter);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        return !this.failed && gameTime < this.phaseEnd && hunter.isRangedClass()
                && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !brain.hasMemoryValue(ModMemoryTypes.SQUAD_POSITION.get())
                && !brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get());
    }

    @Override
    protected void tick(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.watching) {
            tickWatching(level, hunter, gameTime);
        } else {
            tickTravelling(level, hunter, gameTime);
        }
    }

    private void tickTravelling(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        if (hunter.position().distanceToSqr(Vec3.atBottomCenterOf(this.spot)) <= ARRIVAL_DISTANCE_SQR) {
            this.watching = true;
            this.favourite = this.spot;
            brain.setMemory(ModMemoryTypes.LOOKOUT_STATUS.get(), "watching");
            this.phaseEnd = gameTime + nextInt(hunter.getRandom(), MIN_WATCH_TICKS, MAX_WATCH_TICKS);
            this.glanceEnd = gameTime;
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            hunter.getNavigation().stop();
            return;
        }

        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;
        this.walkAttempts++;
        if (brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE) || this.walkAttempts >= MAX_WALK_ATTEMPTS) {
            this.spots.reject(this.spot);
            if (this.spot.equals(this.favourite)) this.favourite = null;
            this.failed = true;
            return;
        }
        walkToSpot(hunter);
    }

    private void tickWatching(ServerLevel level, Hunter hunter, long gameTime) {
        hunter.getBrain().setMemoryWithExpiry(ModMemoryTypes.PATROL_COOLDOWN.get(), Unit.INSTANCE, PATROL_HOLD_TICKS);

        if (gameTime >= this.glanceEnd || (this.watchedMob != null && !this.watchedMob.isAlive())) {
            startGlance(hunter, gameTime);
        }

        if (this.spyglassAt > 0 && gameTime >= this.spyglassAt) {
            this.spyglassAt = 0;
            hunter.raiseSpyglass();
        }

        Vec3 target = this.watchedMob != null ? this.watchedMob.getEyePosition() : this.glancePoint;
        if (target != null) {
            hunter.getLookControl().setLookAt(target.x, target.y, target.z);
        }
    }

    private void startGlance(Hunter hunter, long gameTime) {
        RandomSource random = hunter.getRandom();
        hunter.lowerSpyglass();
        this.glanceEnd = gameTime + nextInt(random, MIN_GLANCE_TICKS, MAX_GLANCE_TICKS);
        this.spyglassAt = random.nextFloat() < SPYGLASS_CHANCE ? gameTime + SPYGLASS_RAISE_DELAY : 0;
        this.watchedMob = random.nextFloat() < WATCH_MOB_CHANCE ? pickMob(hunter) : null;

        double angle = random.nextDouble() * Math.PI * 2.0D;
        double pitch = (random.nextDouble() * 2.0D - 1.0D) * MAX_GLANCE_PITCH;
        this.glancePoint = hunter.getEyePosition().add(Math.cos(angle) * GLANCE_DISTANCE, pitch * GLANCE_DISTANCE, Math.sin(angle) * GLANCE_DISTANCE);
    }

    private static @Nullable LivingEntity pickMob(Hunter hunter) {
        List<LivingEntity> mobs = hunter.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES)
                .orElse(NearestVisibleLivingEntities.empty())
                .find(entity -> !(entity instanceof Hunter))
                .toList();
        return mobs.isEmpty() ? null : mobs.get(hunter.getRandom().nextInt(mobs.size()));
    }

    private void walkToSpot(Hunter hunter) {
        hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.spot, this.speed, 0));
    }

    @Override
    protected void stop(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        hunter.lowerSpyglass();
        if (!this.watching) brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        HunterSquad squad = hunter.getSquad();
        if (squad != null) squad.releaseLookoutSpot(hunter);

        int cooldown = this.failed ? FAILED_COOLDOWN : nextInt(hunter.getRandom(), MIN_COOLDOWN, MAX_COOLDOWN);
        brain.setMemoryWithExpiry(ModMemoryTypes.LOOKOUT_COOLDOWN.get(), Unit.INSTANCE, cooldown);
        brain.eraseMemory(ModMemoryTypes.LOOKOUT_SPOT.get());
        brain.setMemory(ModMemoryTypes.LOOKOUT_STATUS.get(), this.failed ? "spot unreachable" : this.watching ? "done watching" : "interrupted");
        this.spot = null;
        this.glancePoint = null;
        this.watchedMob = null;
        this.spyglassAt = 0;
    }

    private static int nextInt(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
