package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.FishingSpot;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.FishingSpots;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class GoFishing extends Behavior<Hunter> {

    private static final int MAX_DURATION = 6600;
    private static final int MAX_TRAVEL_TICKS = 600;
    private static final int MAX_WALK_ATTEMPTS = 3;
    private static final double ARRIVAL_DISTANCE_SQR = 1.0D;
    private static final double WATER_EDGE_DISTANCE = 0.5D;
    private static final double STAND_BACK_FACTOR = 1.4D;
    private static final double LOOK_BEYOND = 1.0D;
    private static final int MAX_POSITIONING_TICKS = 20;
    private static final double POSITIONED_DISTANCE_SQR = 0.05D * 0.05D;
    private static final double POSITIONING_SPEED = 0.5D;
    private static final int MIN_BREAK_SWINGS = 3;
    private static final int MAX_BREAK_SWINGS = 5;
    private static final int BREAK_SWING_INTERVAL = 8;
    private static final int FREEZE_CHECK_INTERVAL = 20;
    private static final int MIN_CAST_DELAY = 10;
    private static final int MAX_CAST_DELAY = 40;
    private static final int LANDING_DELAY = 10;
    private static final int MIN_WAIT = 60;
    private static final int MAX_WAIT = 600;
    private static final int MIN_RECAST_DELAY = 6;
    private static final int MAX_RECAST_DELAY = 30;

    private enum Phase {
        WALKING, POSITIONING, BREAKING, SETTLING, WAITING, REELING
    }

    private final float speed;
    private @Nullable FishingSpot spot;
    private @Nullable BlockPos breakTarget;
    private Vec3 standPoint = Vec3.ZERO;
    private Vec3 lookPoint = Vec3.ZERO;
    private Phase phase = Phase.WALKING;
    private long phaseEnd;
    private long sessionEnd;
    private long landingAt;
    private int walkAttempts;
    private int breakSwings;
    private boolean finished;

    public GoFishing(float speed) {
        super(ImmutableMap.of(
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                ModMemoryTypes.PASTIME.get(), MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.FISHING_SPOT.get(), MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.SQUAD_POSITION.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.WEAPONS_UNSHEATHED.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.SHOULD_RETREAT.get(), MemoryStatus.VALUE_ABSENT
        ), MAX_DURATION);
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        return !level.isRaining();
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        this.spot = brain.getMemory(ModMemoryTypes.FISHING_SPOT.get()).orElseThrow();
        Vec3 waterDirection = getWaterDirection(this.spot);
        this.standPoint = Vec3.atBottomCenterOf(this.spot.stand()).subtract(waterDirection.scale(WATER_EDGE_DISTANCE * (STAND_BACK_FACTOR - 1.0D)));
        this.lookPoint = this.spot.surface().add(waterDirection.scale(LOOK_BEYOND));
        this.finished = false;
        this.walkAttempts = 0;
        this.landingAt = 0;
        this.sessionEnd = Long.MAX_VALUE;
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        brain.eraseMemory(MemoryModuleType.LOOK_TARGET);
        setPhase(Phase.WALKING, gameTime, MAX_TRAVEL_TICKS);
        walkToSpot(hunter);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        return !this.finished && gameTime < this.sessionEnd && !level.isRaining()
                && brain.hasMemoryValue(ModMemoryTypes.PASTIME.get())
                && brain.hasMemoryValue(ModMemoryTypes.FISHING_SPOT.get())
                && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !brain.hasMemoryValue(ModMemoryTypes.SQUAD_POSITION.get())
                && !brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())
                && !brain.hasMemoryValue(ModMemoryTypes.DANGER.get());
    }

    @Override
    protected void tick(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.phase == Phase.WALKING) {
            tickWalking(level, hunter, gameTime);
            return;
        }

        Vec3 surface = this.spot.surface();
        Vec3 lookAt = this.phase == Phase.BREAKING && this.breakTarget != null ? Vec3.atCenterOf(this.breakTarget) : this.lookPoint;
        hunter.getLookControl().setLookAt(lookAt.x, lookAt.y, lookAt.z);
        if (this.phase == Phase.POSITIONING) {
            tickPositioning(level, hunter, gameTime);
            return;
        }
        if (this.phase == Phase.WAITING && gameTime % FREEZE_CHECK_INTERVAL == 0 && isFrozen(level)) {
            startDigging(hunter, gameTime);
            return;
        }
        if (this.landingAt > 0 && gameTime >= this.landingAt) {
            this.landingAt = 0;
            level.sendParticles(ParticleTypes.SPLASH, surface.x, surface.y, surface.z, 4, 0.1D, 0.0D, 0.1D, 0.0D);
        }
        if (gameTime < this.phaseEnd) return;

        switch (this.phase) {
            case BREAKING -> tickBreaking(level, hunter, gameTime);
            case SETTLING, REELING -> cast(level, hunter, gameTime);
            case WAITING -> reel(hunter, gameTime);
            default -> {
            }
        }
    }

    private void tickWalking(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        Vec3 stand = Vec3.atBottomCenterOf(this.spot.stand());
        double dx = hunter.getX() - stand.x;
        double dz = hunter.getZ() - stand.z;
        if (dx * dx + dz * dz <= ARRIVAL_DISTANCE_SQR && Math.abs(hunter.getY() - stand.y) < 1.0D) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            hunter.getNavigation().stop();
            setPhase(Phase.POSITIONING, gameTime, MAX_POSITIONING_TICKS);
            return;
        }

        if (gameTime >= this.phaseEnd) {
            this.finished = true;
            return;
        }
        if (brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;

        this.walkAttempts++;
        if (brain.hasMemoryValue(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE) || this.walkAttempts >= MAX_WALK_ATTEMPTS) {
            this.finished = true;
            return;
        }
        walkToSpot(hunter);
    }

    private void tickPositioning(ServerLevel level, Hunter hunter, long gameTime) {
        double dx = this.standPoint.x - hunter.getX();
        double dz = this.standPoint.z - hunter.getZ();
        if (dx * dx + dz * dz > POSITIONED_DISTANCE_SQR && gameTime < this.phaseEnd) {
            hunter.getMoveControl().setWantedPosition(this.standPoint.x, this.standPoint.y, this.standPoint.z, POSITIONING_SPEED);
            return;
        }

        this.sessionEnd = gameTime + hunter.getBrain().getMemory(ModMemoryTypes.PASTIME_DURATION.get()).orElse(0);
        if (isFrozen(level)) {
            startDigging(hunter, gameTime);
        } else {
            takeOutRod(level, hunter, gameTime);
        }
    }

    private List<BlockPos> getDigTargets() {
        return this.spot.hole().isEmpty() ? List.of(this.spot.water()) : this.spot.hole();
    }

    private boolean isFrozen(ServerLevel level) {
        return getDigTargets().stream().anyMatch(pos -> FishingSpots.isFishableIce(level, pos));
    }

    private void startDigging(Hunter hunter, long gameTime) {
        hunter.putAwayFishingRod();
        this.landingAt = 0;
        this.breakTarget = null;
        setPhase(Phase.BREAKING, gameTime, BREAK_SWING_INTERVAL);
    }

    private static Vec3 getWaterDirection(FishingSpot spot) {
        Vec3 offset = Vec3.atCenterOf(spot.water()).subtract(Vec3.atCenterOf(spot.stand()));
        return new Vec3(offset.x, 0.0D, offset.z).normalize();
    }

    private void tickBreaking(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.breakTarget == null || !FishingSpots.isFishableIce(level, this.breakTarget)) {
            this.breakTarget = getDigTargets().stream()
                    .filter(pos -> FishingSpots.isFishableIce(level, pos))
                    .min(Comparator.comparingDouble(pos -> pos.distToCenterSqr(hunter.position())))
                    .orElse(null);
            if (this.breakTarget == null) {
                takeOutRod(level, hunter, gameTime);
                return;
            }
            this.breakSwings = nextInt(hunter.getRandom(), MIN_BREAK_SWINGS, MAX_BREAK_SWINGS);
        }

        BlockState ice = level.getBlockState(this.breakTarget);
        hunter.swing(InteractionHand.MAIN_HAND);
        this.breakSwings--;
        if (this.breakSwings > 0) {
            SoundType sound = ice.getSoundType();
            level.playSound(null, this.breakTarget, sound.getHitSound(), SoundSource.NEUTRAL, sound.getVolume() * 0.5F, sound.getPitch() * 0.8F);
        } else {
            level.levelEvent(LevelEvent.PARTICLES_DESTROY_BLOCK, this.breakTarget, Block.getId(ice));
            level.setBlockAndUpdate(this.breakTarget, IceBlock.meltsInto());
            this.breakTarget = null;
        }
        setPhase(Phase.BREAKING, gameTime, BREAK_SWING_INTERVAL);
    }

    private void takeOutRod(ServerLevel level, Hunter hunter, long gameTime) {
        if (!FishingSpots.isOpenWater(level, this.spot.water()) || !hunter.takeOutFishingRod()) {
            this.finished = true;
            return;
        }
        setPhase(Phase.SETTLING, gameTime, nextInt(hunter.getRandom(), MIN_CAST_DELAY, MAX_CAST_DELAY));
    }

    private void cast(ServerLevel level, Hunter hunter, long gameTime) {
        if (isFrozen(level)) {
            startDigging(hunter, gameTime);
            return;
        }
        if (!FishingSpots.isOpenWater(level, this.spot.water())) {
            this.finished = true;
            return;
        }

        RandomSource random = hunter.getRandom();
        hunter.swing(InteractionHand.MAIN_HAND);
        hunter.playSound(SoundEvents.FISHING_BOBBER_THROW, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        this.landingAt = gameTime + LANDING_DELAY;
        setPhase(Phase.WAITING, gameTime, LANDING_DELAY + nextInt(random, MIN_WAIT, MAX_WAIT));
    }

    private void reel(Hunter hunter, long gameTime) {
        RandomSource random = hunter.getRandom();
        hunter.swing(InteractionHand.MAIN_HAND);
        hunter.playSound(SoundEvents.FISHING_BOBBER_RETRIEVE, 1.0F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
        setPhase(Phase.REELING, gameTime, nextInt(random, MIN_RECAST_DELAY, MAX_RECAST_DELAY));
    }

    private void setPhase(Phase phase, long gameTime, int duration) {
        this.phase = phase;
        this.phaseEnd = gameTime + duration;
    }

    private void walkToSpot(Hunter hunter) {
        hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(this.spot.stand(), this.speed, 0));
    }

    @Override
    protected void stop(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        hunter.putAwayFishingRod();
        if (this.phase == Phase.WALKING) brain.eraseMemory(MemoryModuleType.WALK_TARGET);

        boolean interrupted = brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                || brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())
                || brain.hasMemoryValue(ModMemoryTypes.DANGER.get());
        if (!interrupted) {
            brain.eraseMemory(ModMemoryTypes.PASTIME.get());
            brain.eraseMemory(ModMemoryTypes.PASTIME_DURATION.get());
            brain.eraseMemory(ModMemoryTypes.FISHING_SPOT.get());
        }
        this.spot = null;
        this.breakTarget = null;
        this.landingAt = 0;
    }

    private static int nextInt(RandomSource random, int min, int max) {
        return min + random.nextInt(max - min + 1);
    }
}
