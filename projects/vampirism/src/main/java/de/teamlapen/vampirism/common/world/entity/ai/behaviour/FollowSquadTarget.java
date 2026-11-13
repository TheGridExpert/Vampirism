package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterThreats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class FollowSquadTarget {

    public static OneShot<Hunter> create() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryTypes.SQUAD_TARGET.get()),
                instance.registered(MemoryModuleType.ATTACK_TARGET),
                instance.registered(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE),
                instance.absent(ModMemoryTypes.SHOULD_RETREAT.get())
        ).apply(instance, (squadTargetAcc, attackTargetAcc, cantReachAcc, _) -> (level, hunter, _) -> {
            LivingEntity target = instance.get(squadTargetAcc);
            if (!target.isAlive() || target.level() != hunter.level() || !hunter.canAttack(target) || !HunterThreats.canEngage(hunter, target) || !HunterThreats.isWorthEngaging(hunter, target)) {
                squadTargetAcc.erase();
                return false;
            }

            if (instance.tryGet(attackTargetAcc).orElse(null) == target) return false;

            attackTargetAcc.set(target);
            cantReachAcc.erase();
            return true;
        }));
    }
}
