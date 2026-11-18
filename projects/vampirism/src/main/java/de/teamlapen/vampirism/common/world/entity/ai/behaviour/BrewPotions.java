package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.BrewingStation;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.BrewingStations;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BrewPotions extends Behavior<Hunter> {

    private static final int MAX_DURATION = 6000;
    private static final int MAX_TRAVEL_TICKS = 400;
    private static final double REACH_DISTANCE_SQR = 2.5D * 2.5D;
    private static final double MAX_HEIGHT_DIFFERENCE = 1.5D;
    private static final double STAND_ARRIVAL_DISTANCE_SQR = 1.0D;
    private static final int MAX_POSITIONING_TICKS = 20;
    private static final double POSITIONED_DISTANCE_SQR = 0.15D * 0.15D;
    private static final double POSITIONING_SPEED = 0.5D;
    private static final int MIN_STIR_INTERVAL = 40;
    private static final int MAX_STIR_INTERVAL = 100;

    private enum Phase {
        WALKING, POSITIONING, BREWING
    }

    private final float speed;
    private @Nullable BrewingStation station;
    private Phase phase = Phase.WALKING;
    private long phaseEnd;
    private boolean finished;

    public BrewPotions(float speed) {
        super(ImmutableMap.of(
                ModMemoryTypes.BREWING_STATION.get(), MemoryStatus.VALUE_PRESENT,
                ModMemoryTypes.BREWING.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET, MemoryStatus.REGISTERED,
                ModMemoryTypes.SQUAD_POSITION.get(), MemoryStatus.VALUE_ABSENT,
                ModMemoryTypes.SHOULD_RETREAT.get(), MemoryStatus.VALUE_ABSENT
        ), MAX_DURATION);
        this.speed = speed;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        return hunter.isAlchemistClass() && BrewingStations.isRelaxed(level, hunter);
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        this.station = brain.getMemory(ModMemoryTypes.BREWING_STATION.get()).orElseThrow();
        this.finished = false;
        brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
        setPhase(Phase.WALKING, gameTime, MAX_TRAVEL_TICKS);
        WalkTarget target = this.station.portable() ? new WalkTarget(this.station.stand(), this.speed, 0) : new WalkTarget(this.station.pos(), this.speed, 1);
        brain.setMemory(MemoryModuleType.WALK_TARGET, target);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        return !this.finished && hunter.isAlchemistClass()
                && brain.hasMemoryValue(ModMemoryTypes.BREWING_STATION.get())
                && !brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)
                && !brain.hasMemoryValue(ModMemoryTypes.SQUAD_POSITION.get())
                && !brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())
                && !brain.hasMemoryValue(ModMemoryTypes.DANGER.get())
                && BrewingStations.isRelaxed(level, hunter);
    }

    @Override
    protected void tick(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.phase == Phase.WALKING) {
            tickWalking(level, hunter, gameTime);
            return;
        }
        if (this.phase == Phase.POSITIONING) {
            tickPositioning(level, hunter, gameTime);
            return;
        }

        BlockPos pos = this.station.pos();
        Vec3 center = Vec3.atCenterOf(pos);
        hunter.getLookControl().setLookAt(center.x, center.y, center.z);
        if (!BrewingStations.isWorkstation(level.getBlockState(pos)) || !hunter.needsPotions()) {
            this.finished = true;
            return;
        }
        if (gameTime < this.phaseEnd) return;

        hunter.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.NEUTRAL, 0.6F, 0.9F + hunter.getRandom().nextFloat() * 0.2F);
        setPhase(Phase.BREWING, gameTime, nextStirInterval(hunter.getRandom()));
    }

    private void tickWalking(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        boolean arrived = this.station.portable() ? isNear(hunter, this.station.stand(), STAND_ARRIVAL_DISTANCE_SQR) : isNear(hunter, this.station.pos(), REACH_DISTANCE_SQR);
        if (arrived) {
            brain.eraseMemory(MemoryModuleType.WALK_TARGET);
            hunter.getNavigation().stop();
            if (this.station.portable()) {
                setPhase(Phase.POSITIONING, gameTime, MAX_POSITIONING_TICKS);
            } else {
                arrive(level, hunter, gameTime);
            }
            return;
        }
        if (gameTime >= this.phaseEnd || !brain.hasMemoryValue(MemoryModuleType.WALK_TARGET)) {
            this.finished = true;
        }
    }

    private void tickPositioning(ServerLevel level, Hunter hunter, long gameTime) {
        Vec3 stand = Vec3.atBottomCenterOf(this.station.stand());
        double dx = stand.x - hunter.getX();
        double dz = stand.z - hunter.getZ();
        if (dx * dx + dz * dz > POSITIONED_DISTANCE_SQR && gameTime < this.phaseEnd) {
            hunter.getMoveControl().setWantedPosition(stand.x, stand.y, stand.z, POSITIONING_SPEED);
            return;
        }
        arrive(level, hunter, gameTime);
    }

    private void arrive(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        BlockPos pos = this.station.pos();
        if (this.station.portable() && !this.station.placed()) {
            if (!BrewingStations.canPlaceAt(level, pos)) {
                this.finished = true;
                return;
            }
            BrewingStations.placeStand(level, hunter, pos);
            this.station = this.station.markPlaced();
            brain.setMemory(ModMemoryTypes.BREWING_STATION.get(), this.station);
        } else if (!BrewingStations.isWorkstation(level.getBlockState(pos))) {
            this.finished = true;
            return;
        }

        brain.setMemory(ModMemoryTypes.BREWING.get(), Unit.INSTANCE);
        setPhase(Phase.BREWING, gameTime, nextStirInterval(hunter.getRandom()));
    }

    private static boolean isNear(Hunter hunter, BlockPos pos, double distanceSqr) {
        double dx = hunter.getX() - (pos.getX() + 0.5D);
        double dz = hunter.getZ() - (pos.getZ() + 0.5D);
        return dx * dx + dz * dz <= distanceSqr && Math.abs(hunter.getY() - pos.getY()) <= MAX_HEIGHT_DIFFERENCE;
    }

    private void setPhase(Phase phase, long gameTime, int duration) {
        this.phase = phase;
        this.phaseEnd = gameTime + duration;
    }

    private static int nextStirInterval(RandomSource random) {
        return MIN_STIR_INTERVAL + random.nextInt(MAX_STIR_INTERVAL - MIN_STIR_INTERVAL + 1);
    }

    @Override
    protected void stop(ServerLevel level, Hunter hunter, long gameTime) {
        Brain<Hunter> brain = hunter.getBrain();
        if (this.phase == Phase.WALKING) brain.eraseMemory(MemoryModuleType.WALK_TARGET);
        if (this.station != null && this.station.placed()) BrewingStations.takeBack(level, hunter, this.station.pos());

        brain.eraseMemory(ModMemoryTypes.BREWING.get());
        brain.eraseMemory(ModMemoryTypes.BREWING_STATION.get());
        this.station = null;
    }
}
