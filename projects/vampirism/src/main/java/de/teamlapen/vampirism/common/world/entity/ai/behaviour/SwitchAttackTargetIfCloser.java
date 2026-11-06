package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SwitchAttackTargetIfCloser {

    private static final int TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE = 200;

    public static <E extends Mob> OneShot<E> create(MemoryModuleType<List<LivingEntity>> candidateMemory) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.registered(candidateMemory),
                instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE)
        ).apply(instance, (attackTargetMemory, candidatesAcc, cantReachAcc) -> (level, mob, _) -> {
            LivingEntity currentTarget = instance.get(attackTargetMemory);

            if (!mob.canAttack(currentTarget)
                    || !currentTarget.isAlive()
                    || currentTarget.level() != mob.level()
                    || isTiredOfTryingToReachTarget(mob, instance.tryGet(cantReachAcc))
                    || !isWorthEngaging(mob, currentTarget)) {
                attackTargetMemory.erase();
                return true;
            }

            Optional<List<LivingEntity>> candidatesOpt = instance.tryGet(candidatesAcc);
            if (candidatesOpt.isPresent() && !candidatesOpt.get().isEmpty()) {
                double switchDistance = mob.distanceToSqr(currentTarget) * 0.65D;
                List<LivingEntity> byDistance = candidatesOpt.get().stream().sorted(Comparator.comparingDouble(mob::distanceToSqr)).toList();
                for (LivingEntity candidate : byDistance) {
                    if (mob.distanceToSqr(candidate) >= switchDistance) break;
                    if (HunterThreats.isExplosive(candidate) && mob.distanceTo(candidate) < HunterThreats.getMinShootDistance(candidate)) continue;
                    if (!isWorthEngaging(mob, candidate)) continue;

                    if (candidate != currentTarget) attackTargetMemory.set(candidate);
                    break;
                }
            }

            return true;
        }));
    }

    private static boolean isWorthEngaging(Mob mob, LivingEntity target) {
        return !(mob instanceof Hunter hunter) || HunterThreats.isWorthEngaging(hunter, target);
    }

    private static boolean isTiredOfTryingToReachTarget(LivingEntity entity, Optional<Long> timeSinceInvalidTarget) {
        return timeSinceInvalidTarget.isPresent() && entity.level().getGameTime() - timeSinceInvalidTarget.get() > TIMEOUT_TO_GET_WITHIN_ATTACK_RANGE;
    }
}
