package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class DistanceMeleeAttack {

    public static <T extends Mob> OneShot<T> create(int attackCooldown, double preferredDistance, double tooCloseDistance) {
        return create(entity -> true, attackCooldown, preferredDistance, tooCloseDistance);
    }

    public static <T extends Mob> OneShot<T> create(Predicate<T> canAttack, int attackCooldown, double preferredDistance, double tooCloseDistance) {
        return BehaviorBuilder.create(
                instance -> instance.group(
                                instance.registered(MemoryModuleType.LOOK_TARGET),
                                instance.present(MemoryModuleType.ATTACK_TARGET),
                                instance.absent(MemoryModuleType.ATTACK_COOLING_DOWN)
                        ).apply(instance, (lookTarget, attackTarget, coolingDown) -> (ServerLevel level, T mob, long gameTime) -> {
                            LivingEntity target = instance.get(attackTarget);

                            if (!canAttack.test(mob)) return false;
                            if (isHoldingUsableProjectileWeapon(mob)) return false;

                            double distance = mob.distanceTo(target);

                            // Maintain preferred distance
                            if (distance < tooCloseDistance) {
                                // Step backwards slightly
                                Vec3 dir = mob.position().subtract(target.position()).normalize();
                                Vec3 retreatPos = mob.position().add(dir.scale(0.6D)); // small step back
                                mob.getNavigation().moveTo(retreatPos.x, retreatPos.y, retreatPos.z, 1.0D);
                            }

                            if (distance < preferredDistance) {
                                // In good range — face and attack
                                mob.getLookControl().setLookAt(target);
                                mob.swing(InteractionHand.MAIN_HAND);
                                mob.doHurtTarget(level, target);
                                coolingDown.setWithExpiry(true, attackCooldown);
                                return true;
                            }

                            return false;
                        })

        );
    }

    private static boolean isHoldingUsableProjectileWeapon(Mob mob) {
        return mob.isHolding(stack -> {
            var item = stack.getItem();
            return item instanceof ProjectileWeaponItem weapon && mob.canFireProjectileWeapon(weapon);
        });
    }
}