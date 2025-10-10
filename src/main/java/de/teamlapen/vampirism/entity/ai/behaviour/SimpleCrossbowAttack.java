package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.entity.hunter.Hunter;
import de.teamlapen.vampirism.mixin.accessor.CrossbowAttackAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class SimpleCrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends CrossbowAttack<E, T> {

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E shooter) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && shooter.isHolding(item -> item.getItem() instanceof CrossbowItem);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive();
    }

    @Override
    protected void tick(ServerLevel level, E shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive()) return;

        Vec3 shooterPos = shooter.position().add(0, shooter.getEyeHeight(), 0);
        Vec3 targetPos = target.position().add(0, target.getBbHeight(), 0);
        Vec3 targetVel = target.getDeltaMovement();

        Vec3 delta = targetPos.subtract(shooterPos);
        double horizontalDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double arrowVelocity = Hunter.ARROW_VELOCITY;
        double gravity = getArrowGravity(shooter.getProjectile(shooter.getMainHandItem()).getItem());
        double travelTime = horizontalDist / arrowVelocity;

        Vec3 futurePos = targetPos.add(targetVel.scale(travelTime));

        Vec3 diff = futurePos.subtract(shooterPos);
        double dx = diff.x;
        double dz = diff.z;
        double dy = diff.y;
        double h = Math.sqrt(dx * dx + dz * dz);

        double arrowVelocitySq = arrowVelocity * arrowVelocity;
        double discriminant = arrowVelocitySq * arrowVelocitySq - gravity * (gravity * h * h + 2 * dy * arrowVelocitySq);

        double aimY;
        if (discriminant <= 0) {
            aimY = futurePos.y;
        } else {
            double sqrt = Math.sqrt(discriminant);
            double angle = Math.atan((arrowVelocitySq - sqrt) / (gravity * h));

            double t = h / (arrowVelocity * Math.cos(angle));
            double heightOffset = arrowVelocity * Math.sin(angle) * t - 0.5 * gravity * t * t;
            aimY = shooterPos.y + heightOffset;
        }
        Vec3 aimPos = new Vec3(futurePos.x, aimY, futurePos.z);

        shooter.getLookControl().setLookAt(aimPos.x, aimPos.y, aimPos.z, 45.0F, 45.0F);

        if (shooter.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;

        ((CrossbowAttackAccessor) this).invokeCrossbowAttack(shooter, target);
    }

    public static double getArrowGravity(Item arrowItem) {
        return 0.05D;
    }
}
