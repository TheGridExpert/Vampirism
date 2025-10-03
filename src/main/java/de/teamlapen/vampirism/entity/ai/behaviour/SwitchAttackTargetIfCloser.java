package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SwitchAttackTargetIfCloser {

    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> BehaviorControl<E> create(MemoryModuleType<List<LivingEntity>> candidateMemory) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.registered(candidateMemory),
                instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        ).apply(instance, (attackTargetMemory, candidatesAcc, cantReachAcc) -> (level, mob, gameTime) -> {
            LivingEntity currentTarget = instance.get(attackTargetMemory);

            if (!mob.canAttack(currentTarget)
                    || !currentTarget.isAlive()
                    || currentTarget.level() != mob.level()
                    || isTiredOfTryingToReachTarget(mob, instance.tryGet(cantReachAcc))) {
                attackTargetMemory.erase();
                return true;
            }

            Optional<List<LivingEntity>> candidatesOpt = instance.tryGet(candidatesAcc);
            if (candidatesOpt.isPresent() && !candidatesOpt.get().isEmpty()) {
                LivingEntity best = candidatesOpt.get().stream()
                        .min(Comparator.comparingDouble(mob::distanceToSqr))
                        .orElse(null);

                if (best != currentTarget) {
                    double distCurrent = mob.distanceToSqr(currentTarget);
                    double distNew = mob.distanceToSqr(best);

                    if (distNew < distCurrent * 0.65D) {
                        attackTargetMemory.set(best);
                        return true;
                    }
                }
            }

            return true;
        }));
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity entity, Optional<Long> timeSinceInvalidTarget) {
        return timeSinceInvalidTarget.isPresent() && entity.level().getGameTime() - timeSinceInvalidTarget.get() > TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE;
    }
}
