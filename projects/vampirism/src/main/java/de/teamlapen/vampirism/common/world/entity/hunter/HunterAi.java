package de.teamlapen.vampirism.common.world.entity.hunter;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.core.ModSensors;
import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.ai.behaviour.*;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.ActivityData;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.ai.sensing.SensorType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.schedule.Activity;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class HunterAi {

    public static final int PATROL_RADIUS = 16;
    private static final float SPEED_MULTIPLIER_WHEN_PATROLLING = 0.4F;
    private static final int MIN_PATROL_COOLDOWN = 40;
    private static final int MAX_PATROL_COOLDOWN = 120;

    private static final double CURING_RANGE = 1.5D;
    private static final double CURING_RADIUS = 12.0D;
    private static final double MAX_HOSTILE_RADIUS_TO_CURE = 14.0D;
    private static final float SPEED_MULTIPLIER_WHEN_CHASING_CURE_TARGET = 0.7F;
    private static final int STARE_TICKS_AFTER_CURING = 100;

    private static final float SPEED_MULTIPLIER_WHEN_CHASING_TARGET = 0.7F;
    private static final float SPEED_MULTIPLIER_WHEN_DISTANCING_RANGED = 0.85F;
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final double PREFERRED_ATTACK_DISTANCE = 2.5D;
    private static final double TOO_CLOSE_ATTACK_DISTANCE = 1.0D;
    private static final double MIN_RANGE_ATTACK_DISTANCE = 7.5D;
    private static final double MAX_RANGE_ATTACK_DISTANCE = 15.0D;

    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 0.7F;
    private static final float RETREAT_HEALTH_PERCENT = 0.25F;
    private static final float SAFE_HEALTH_PERCENT = 0.75F;
    private static final int MAX_RETREAT_DURATION = 700;
    private static final double RETREAT_DISTANCE = 5.0D;
    private static final double RETREAT_SAFE_DISTANCE = 15.0D;

    private static final ImmutableList<SensorType<? extends Sensor<? super Hunter>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            ModSensors.HUNTER_SPECIFIC_SENSOR.get()
    );
    private static final ImmutableList<MemoryModuleType<?>> MEMORY_TYPES = ImmutableList.of(
            MemoryModuleType.HOME,
            MemoryModuleType.PATH,
            MemoryModuleType.WALK_TARGET,
            MemoryModuleType.LOOK_TARGET,
            MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE,
            MemoryModuleType.DOORS_TO_CLOSE,
            MemoryModuleType.NEAREST_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
            MemoryModuleType.NEAREST_VISIBLE_PLAYER,
            MemoryModuleType.ANGRY_AT,
            MemoryModuleType.ATTACK_TARGET,
            MemoryModuleType.ATTACK_COOLING_DOWN,
            ModMemoryTypes.PATROL_COOLDOWN.get(),
            ModMemoryTypes.REPOSITIONING_COOLDOWN.get(),
            ModMemoryTypes.SHOULD_RETREAT.get(),
            ModMemoryTypes.WEAPONS_UNSHEATHED.get(),
            ModMemoryTypes.WEAPON_SHEATH_COOLDOWN.get(),
            ModMemoryTypes.CURE_TARGET.get(),
            ModMemoryTypes.AIM_TARGET.get(),
            ModMemoryTypes.SEE_TIME.get(),
            ModMemoryTypes.GATES_TO_CLOSE.get(),
            ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get(),
            ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(),
            ModMemoryTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get()
    );

    @SuppressWarnings("deprecation")
    private static final Brain.Provider<Hunter> BRAIN_PROVIDER = Brain.provider(MEMORY_TYPES, SENSOR_TYPES, HunterAi::createActivities);

    public static Brain.Provider<Hunter> brainProvider() {
        return BRAIN_PROVIDER;
    }

    private static List<ActivityData<Hunter>> createActivities(Hunter hunter) {
        return List.of(
                initCoreActivity(),
                initPatrolActivity(),
                initFightActivity(),
                initRetreatActivity()
        );
    }

    protected static void initMemories(Hunter hunter) {
        GlobalPos globalpos = GlobalPos.of(hunter.level().dimension(), hunter.blockPosition());
        hunter.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
    }

    private static ActivityData<Hunter> initCoreActivity() {
        return ActivityData.<Hunter>create(
                Activity.CORE,
                0,
                ImmutableList.of(
                        new FordLikeSwim(0.4F),
                        InteractWithDoor.create(),
                        InteractWithGate.create(),
                        new LookAtTargetSink(45, 90),
                        AvoidBumpingIntoOthers.create(0.4F),
                        new HandleHunterWeapons.Sheathe(),
                        new MoveToPatrolTarget(MIN_PATROL_COOLDOWN, MAX_PATROL_COOLDOWN)
                )
        );
    }

    private static ActivityData<Hunter> initPatrolActivity() {
        return ActivityData.<Hunter>create(
                Activity.IDLE,
                5,
                ImmutableList.of(
                        PatrolAroundHome.create(PATROL_RADIUS, SPEED_MULTIPLIER_WHEN_PATROLLING),
                        new RunOne<Hunter>(
                                ImmutableList.of(
                                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 10.0F), 2),
                                        Pair.of(SetEntityLookTarget.create(ModEntities.NEW_HUNTER.get(), 8.0F), 2),
                                        Pair.of(new RandomLookAround(TimeUtil.rangeOfSeconds(2, 4), 45.0F, -10.0F, 10.0F), 2),
                                        Pair.of(new DoNothing(80, 120), 1)
                                )
                        ),
                        FindCureTarget.create(CURING_RADIUS, MAX_HOSTILE_RADIUS_TO_CURE, SPEED_MULTIPLIER_WHEN_CHASING_CURE_TARGET),
                        CureTargetIfClose.create(CURING_RANGE, STARE_TICKS_AFTER_CURING),
                        StartAttacking.<Hunter>create((level, hunter) -> true, HunterAi::findNearestValidAttackTarget)
                )
        );
    }

    private static ActivityData<Hunter> initFightActivity() {
        return ActivityData.<Hunter>create(
                Activity.FIGHT,
                10,
                ImmutableList.of(
                        new HandleHunterWeapons.Unsheathe(),
                        SwitchAttackTargetIfCloser.create(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()),
                        new CheckHealthAndRetreat(RETREAT_HEALTH_PERCENT, MAX_RETREAT_DURATION),
                        ifMelee(DistanceMeleeAttack.create(MELEE_ATTACK_COOLDOWN, PREFERRED_ATTACK_DISTANCE, TOO_CLOSE_ATTACK_DISTANCE)),
                        ifMelee(SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(SPEED_MULTIPLIER_WHEN_CHASING_TARGET)),
                        new PreciseCrossbowAttack(),
                        ifRanged(MaintainDistanceFromTarget.create(SPEED_MULTIPLIER_WHEN_DISTANCING_RANGED, MIN_RANGE_ATTACK_DISTANCE, MAX_RANGE_ATTACK_DISTANCE))
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    private static ActivityData<Hunter> initRetreatActivity() {
        return ActivityData.<Hunter>create(
                Activity.AVOID,
                15,
                ImmutableList.of(
                        RetreatFromEnemies.create(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), SPEED_MULTIPLIER_WHEN_RETREATING, RETREAT_DISTANCE, RETREAT_SAFE_DISTANCE),
                        new RunOne<Hunter>(
                                ImmutableList.of(
                                        Pair.of(SetEntityLookClosestOfRange.create(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), 12.0F), 3),
                                        Pair.of(SetEntityLookTarget.create(ModEntities.NEW_HUNTER.get(), 8.0F), 2),
                                        Pair.of(new RandomLookAround(TimeUtil.rangeOfSeconds(2, 4), 45.0F, -10.0F, 10.0F), 1),
                                        Pair.of(new DoNothing(80, 120), 1)
                                )
                        ),
                        new CheckIfSafeToStopRetreating(SAFE_HEALTH_PERCENT)
                ),
                ModMemoryTypes.SHOULD_RETREAT.get()
        );
    }

    public static void updateActivity(Hunter hunter) {
        Brain<Hunter> brain = hunter.getBrain();
        Activity previousActivity = brain.getActiveNonCoreActivity().orElse(null);

        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.AVOID,
                Activity.FIGHT,
                Activity.IDLE
        ));

        Activity newActivity = brain.getActiveNonCoreActivity().orElse(null);
        if (previousActivity != Activity.FIGHT && newActivity == Activity.FIGHT) {
            stopWalking(hunter);
        }
    }

    public static boolean isEnemy(LivingEntity target, LivingEntity hunter) {
        return (target instanceof Monster || Helper.isVampire(target)) && hunter.canAttack(target);
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel level, Hunter hunter) {
        Optional<LivingEntity> angryTarget = BehaviorUtils.getLivingEntityFromUUIDMemory(hunter, MemoryModuleType.ANGRY_AT);
        if (angryTarget.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(level, hunter, angryTarget.get())) {
            return angryTarget;
        }

        return findClosestInMemory(hunter, ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), target -> Sensor.isEntityAttackable(level, hunter, target));
    }

    public static Optional<LivingEntity> findClosestInMemory(Hunter hunter, MemoryModuleType<List<LivingEntity>> memory, Predicate<LivingEntity> predicate) {
        Optional<List<LivingEntity>> listOpt = hunter.getBrain().getMemory(memory);
        if (listOpt.isPresent()) {
            for (LivingEntity target : listOpt.get()) {
                if (predicate.test(target)) return Optional.of(target);
            }
        }

        return Optional.empty();
    }

    public static void wasHurtBy(ServerLevel level, Hunter hunter, LivingEntity entity) {
        if (entity instanceof Hunter) return;

        // TODO: Make hunters also support players if neutral or hunter
        maybeRetaliate(level, hunter, entity);
    }

    public static void maybeRetaliate(ServerLevel level, Hunter hunter, LivingEntity entity) {
        if (!hunter.isRetreating()) {
            if (Sensor.isEntityAttackableIgnoringLineOfSight(level, hunter, entity)) {
                if (!BehaviorUtils.isOtherTargetMuchFurtherAwayThanCurrentAttackTarget(hunter, entity, 4.0)) {
                    setAngerTarget(level, hunter, entity);
                    broadcastAngerTarget(level, hunter, entity);
                }
            }
        }
    }

    public static void setAngerTarget(ServerLevel level, Hunter hunter, LivingEntity angerTarget) {
        if (Sensor.isEntityAttackableIgnoringLineOfSight(level, hunter, angerTarget)) {
            Brain<Hunter> brain = hunter.getBrain();
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);
            brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT, angerTarget.getUUID(), 2000L);
        }
    }

    public static void broadcastAngerTarget(ServerLevel level, Hunter hunter, LivingEntity angerTarget) {
        hunter.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()).orElse(ImmutableList.of()).forEach(ally -> {
            if (ally instanceof Hunter allyHunter) {
                setAngerTargetIfCloserThanCurrent(level, allyHunter, angerTarget);
            }
        });
    }

    public static void setAngerTargetIfCloserThanCurrent(ServerLevel level, Hunter hunter, LivingEntity angerTarget) {
        Optional<LivingEntity> currentAngerTarget = getAngerTarget(hunter);
        LivingEntity entity = BehaviorUtils.getNearestTarget(hunter, currentAngerTarget, angerTarget);
        if (currentAngerTarget.isEmpty() || currentAngerTarget.get() != entity) {
            setAngerTarget(level, hunter, entity);
        }
    }

    public static Optional<LivingEntity> getAngerTarget(LivingEntity entity) {
        return BehaviorUtils.getLivingEntityFromUUIDMemory(entity, MemoryModuleType.ANGRY_AT);
    }

    private static void stopWalking(Hunter hunter) {
        hunter.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
        hunter.getNavigation().stop();
    }

    public static OneShot<Hunter> ifMelee(OneShot<Hunter> trigger) {
        return BehaviorBuilder.triggerIf(Hunter::isMeleeClass, trigger);
    }

    public static OneShot<Hunter> ifRanged(OneShot<Hunter> trigger) {
        return BehaviorBuilder.triggerIf(Hunter::isRangedClass, trigger);
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> OneShot<E> ifMelee(BehaviorControl<? extends E> trigger) {
        return BehaviorBuilder.triggerIf(entity -> entity instanceof Hunter hunter && hunter.isMeleeClass(), (OneShot<E>) trigger);
    }

    @SuppressWarnings("unchecked")
    public static <E extends LivingEntity> OneShot<E> ifRanged(BehaviorControl<? extends E> trigger) {
        return BehaviorBuilder.triggerIf(entity -> entity instanceof Hunter hunter && hunter.isRangedClass(), (OneShot<E>) trigger);
    }
}
