package de.teamlapen.vampirism.entity.hunter;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.core.ModSensorTypes;
import de.teamlapen.vampirism.entity.ai.behaviour.*;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.TimeUtil;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.*;
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

    private static final float SPEED_MULTIPLIER_WHEN_CHASING_TARGET = 0.6F;
    private static final int MELEE_ATTACK_COOLDOWN = 20;
    private static final double PREFERRED_ATTACK_DISTANCE = 2.0D;
    private static final double TOO_CLOSE_ATTACK_DISTANCE = 1.0D;

    private static final float SPEED_MULTIPLIER_WHEN_RETREATING = 0.7F;
    private static final float RETREAT_HEALTH_PERCENT = 0.35F;
    private static final float SAFE_HEALTH_PERCENT = 0.75F;
    private static final int MAX_RETREAT_DURATION = 700;

    private static final ImmutableList<SensorType<? extends Sensor<? super Hunter>>> SENSOR_TYPES = ImmutableList.of(
            SensorType.NEAREST_LIVING_ENTITIES,
            SensorType.NEAREST_PLAYERS,
            ModSensorTypes.HUNTER_SPECIFIC_SENSOR.get()
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
            ModMemoryModuleTypes.PATROL_COOLDOWN.get(),
            ModMemoryModuleTypes.RETREAT_COOLDOWN.get(),
            ModMemoryModuleTypes.SHOULD_RETREAT.get(),
            ModMemoryModuleTypes.WEAPONS_UNSHEATHED.get(),
            ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get(),
            ModMemoryModuleTypes.GATES_TO_CLOSE.get(),
            ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get(),
            ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get()
    );

    public static Brain.Provider<Hunter> brainProvider() {
        return Brain.provider(MEMORY_TYPES, SENSOR_TYPES);
    }

    protected static Brain<Hunter> makeBrain(Hunter hunter, Brain<Hunter> brain) {
        initCoreActivity(brain);
        initPatrolActivity(brain);
        initFightActivity(brain);
        initRetreatActivity(brain);

        brain.setCoreActivities(ImmutableSet.of(Activity.CORE));
        brain.setDefaultActivity(Activity.IDLE);
        updateActivity(brain);

        return brain;
    }

    protected static void initMemories(Hunter hunter) {
        GlobalPos globalpos = GlobalPos.of(hunter.level().dimension(), hunter.blockPosition());
        hunter.getBrain().setMemory(MemoryModuleType.HOME, globalpos);
    }

    private static void initCoreActivity(Brain<Hunter> brain) {
        brain.addActivity(
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

    private static void initPatrolActivity(Brain<Hunter> brain) {
        brain.addActivity(
                Activity.IDLE,
                5,
                ImmutableList.of(
                        new PatrolAroundHome(PATROL_RADIUS, SPEED_MULTIPLIER_WHEN_PATROLLING),
                        new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(SetEntityLookTarget.create(EntityType.PLAYER, 10.0F), 2),
                                        Pair.of(SetEntityLookTarget.create(ModEntities.NEW_HUNTER.get(), 8.0F), 2),
                                        Pair.of(new RandomLookAround(TimeUtil.rangeOfSeconds(2, 4), 45.0F, -10.0F, 10.0F), 2),
                                        Pair.of(new DoNothing(80, 120), 1)
                                )
                        ),
                        StartAttacking.create((level, hunter) -> true, HunterAi::findNearestValidAttackTarget)
                )
        );
    }

    private static void initFightActivity(Brain<Hunter> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.FIGHT,
                10,
                ImmutableList.of(
                        new HandleHunterWeapons.Unsheathe(),
                        SwitchAttackTargetIfCloser.create(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get()),
                        SetWalkTargetFromAttackTargetIfTargetOutOfReach.create(SPEED_MULTIPLIER_WHEN_CHASING_TARGET),
                        DistanceMeleeAttack.create(MELEE_ATTACK_COOLDOWN, PREFERRED_ATTACK_DISTANCE, TOO_CLOSE_ATTACK_DISTANCE),
                        new CheckHealthAndRetreat(RETREAT_HEALTH_PERCENT, MAX_RETREAT_DURATION)
                ),
                MemoryModuleType.ATTACK_TARGET
        );
    }

    private static void initRetreatActivity(Brain<Hunter> brain) {
        brain.addActivityAndRemoveMemoryWhenStopped(
                Activity.AVOID,
                15,
                ImmutableList.of(
                        // TODO: Finding a spot to retreat is still pretty broken, requires fixing
                        RetreatFromEnemies.create(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get(), SPEED_MULTIPLIER_WHEN_RETREATING, 5),
                        new RunOne<>(
                                ImmutableList.of(
                                        Pair.of(SetEntityLookClosestOfRange.create(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get(), 12.0F), 3),
                                        Pair.of(SetEntityLookTarget.create(ModEntities.NEW_HUNTER.get(), 8.0F), 2),
                                        Pair.of(new RandomLookAround(TimeUtil.rangeOfSeconds(2, 4), 45.0F, -10.0F, 10.0F), 1),
                                        Pair.of(new DoNothing(80, 120), 1)
                                )
                        ),
                        new CheckIfSafeToStopRetreating(SAFE_HEALTH_PERCENT)
                ),
                ModMemoryModuleTypes.SHOULD_RETREAT.get()
        );
    }

    public static void updateActivity(Brain<Hunter> brain) {
        brain.setActiveActivityToFirstValid(ImmutableList.of(
                Activity.AVOID,
                Activity.FIGHT,
                Activity.IDLE
        ));
    }

    public static boolean isEnemy(LivingEntity target, LivingEntity hunter) {
        return (target instanceof Monster || Helper.isVampire(target)) && hunter.canAttack(target);
    }

    private static Optional<? extends LivingEntity> findNearestValidAttackTarget(ServerLevel level, Hunter hunter) {
        Optional<LivingEntity> angryTarget = BehaviorUtils.getLivingEntityFromUUIDMemory(hunter, MemoryModuleType.ANGRY_AT);
        if (angryTarget.isPresent() && Sensor.isEntityAttackableIgnoringLineOfSight(level, hunter, angryTarget.get())) {
            return angryTarget;
        }

        return findClosestInMemory(hunter, ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get(), target -> Sensor.isEntityAttackable(level, hunter, target));
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
}
