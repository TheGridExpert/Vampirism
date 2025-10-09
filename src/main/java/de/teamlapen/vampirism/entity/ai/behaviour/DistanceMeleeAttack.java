package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class DistanceMeleeAttack {

    public static OneShot<Hunter> create(int attackCooldown, double preferredDistance, double tooCloseDistance) {
        return create(entity -> true, attackCooldown, preferredDistance, tooCloseDistance);
    }

    public static OneShot<Hunter> create(Predicate<Hunter> canAttack, int attackCooldown, double preferredDistance, double tooCloseDistance) {
        return BehaviorBuilder.create(
                instance -> instance.group(
                                instance.registered(MemoryModuleType.LOOK_TARGET),
                                instance.present(MemoryModuleType.ATTACK_TARGET),
                                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
                        ).apply(instance, (lookTarget, attackTarget, coolingDown) -> (ServerLevel level, Hunter hunter, long gameTime) -> {
                            LivingEntity target = instance.get(attackTarget);

                            if (!canAttack.test(hunter)) return false;

                            double distance = hunter.distanceTo(target);

                            if (distance < tooCloseDistance) {
                                Vec3 dir = hunter.position().subtract(target.position()).normalize();
                                Vec3 retreatPos = hunter.position().add(dir.scale(0.6D));
                                hunter.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.0D);
                            }

                            if (distance < preferredDistance) {
                                hunter.getLookControl().setLookAt(target);
                                hunter.swing(InteractionHand.MAIN_HAND);
                                hunter.doHurtTarget(level, target);
                                coolingDown.setWithExpiry(true, attackCooldown);
                                return true;
                            }

                            return false;
                        })

        );
    }
}