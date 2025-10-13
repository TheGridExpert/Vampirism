package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import de.teamlapen.vampirism.mixin.accessor.CrossbowItemAccessor;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.Optional;

public class PreciseCrossbowAttack extends Behavior<Hunter> {

    private static final int TIMEOUT = 1200;
    private int attackDelay;
    private CrossbowState crossbowState = CrossbowState.UNCHARGED;

    public PreciseCrossbowAttack() {
        super(ImmutableMap.of(
                MemoryModuleType.LOOK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.VALUE_PRESENT,
                ModMemoryModuleTypes.AIM_TARGET.get(),
                MemoryStatus.REGISTERED
        ), TIMEOUT);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter shooter) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && shooter.isHolding(item -> item.getItem() instanceof CrossbowItem) && shooter.isRangedClass();
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Hunter shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive();
    }

    @Override
    protected void tick(ServerLevel level, Hunter shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive()) return;

        boolean seesTarget = shooter.hasLineOfSight(target);
        boolean isCharging = this.crossbowState == CrossbowState.CHARGING || shooter.isUsingItem();

        if (seesTarget && !isCharging) {
            Vec3 aimPos = calculateAimPosition(shooter, target);
            shooter.getLookControl().setLookAt(aimPos.x, aimPos.y, aimPos.z, 45.0F, 45.0F);
            shooter.getBrain().setMemory(ModMemoryModuleTypes.AIM_TARGET.get(), aimPos);
        }

        crossbowAttack(shooter, target, seesTarget);
    }

    @Override
    protected void stop(ServerLevel level, Hunter entity, long gameTime) {
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }

        if (entity.isHolding(is -> is.getItem() instanceof CrossbowItem)) {
            entity.setChargingCrossbow(false);
            entity.getUseItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }
    }

    private void crossbowAttack(Hunter shooter, LivingEntity target, boolean seesTarget) {
        switch (this.crossbowState) {
            case UNCHARGED -> {
                shooter.startUsingItem(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));
                this.crossbowState = CrossbowState.CHARGING;
                shooter.setChargingCrossbow(true);
            }
            case CHARGING -> {
                if (!shooter.isUsingItem()) {
                    this.crossbowState = CrossbowState.UNCHARGED;
                    return;
                }

                int useTicks = shooter.getTicksUsingItem();
                ItemStack stack = shooter.getUseItem();
                if (useTicks >= CrossbowItem.getChargeDuration(stack, shooter)) {
                    shooter.releaseUsingItem();
                    this.crossbowState = CrossbowState.CHARGED;
                    this.attackDelay = 20 + shooter.getRandom().nextInt(20);
                    shooter.setChargingCrossbow(false);
                }
            }
            case CHARGED -> {
                this.attackDelay--;
                if (this.attackDelay <= 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            }
            case READY_TO_ATTACK -> {
                if (seesTarget && shooter.hasLineOfSight(target)) {
                    shooter.performRangedAttack(target, 1.0F);
                    this.crossbowState = CrossbowState.UNCHARGED;
                    shooter.getBrain().eraseMemory(ModMemoryModuleTypes.REPOSITIONING_COOLDOWN.get());
                }
            }
        }
    }

    private Vec3 calculateAimPosition(Hunter shooter, LivingEntity target) {
        Vec3 shooterEye = shooter.position().add(0, shooter.getEyeHeight(), 0);
        Vec3 targetEye = target.position().add(0, target.getEyeHeight(), 0);

        Vec3 targetVel = target.getDeltaMovement();
        Vec3 delta = targetEye.subtract(shooterEye);
        double horizontalDist = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double arrowVelocity = Hunter.ARROW_VELOCITY;
        double travelTime = horizontalDist / arrowVelocity;

        Vec3 predictedPos = targetEye.add(targetVel.scale(travelTime));

        double gravity = getArrowGravity(shooter.getProjectile(shooter.getMainHandItem()).getItem());
        double verticalDrop = 0.5 * gravity * travelTime * travelTime;

        return new Vec3(predictedPos.x, predictedPos.y + verticalDrop, predictedPos.z);
    }

    public static boolean shootAimedProjectile(CrossbowItem crossbowItem, LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle) {
        Optional<Vec3> aimTargetOpt = shooter.getBrain().getMemory(ModMemoryModuleTypes.AIM_TARGET.get());
        if (aimTargetOpt.isEmpty()) return false;

        Vec3 aimTarget = aimTargetOpt.get();
        Vec3 delta = aimTarget.subtract(shooter.position().add(0, shooter.getEyeHeight(), 0));
        Vector3f shotVector = ((CrossbowItemAccessor) crossbowItem).invokeGetProjectileShotVector(shooter, delta, angle);

        projectile.shoot(shotVector.x(), shotVector.y(), shotVector.z(), velocity, inaccuracy);
        float pitch = ((CrossbowItemAccessor) crossbowItem).invokeGetShotPitch(shooter.getRandom(), index);
        shooter.level().playSound(null, shooter.getX(), shooter.getY(), shooter.getZ(), SoundEvents.CROSSBOW_SHOOT, shooter.getSoundSource(), 1.0F, pitch);

        return true;
    }

    public static double getArrowGravity(Item arrowItem) {
        return 0.05D;
    }

    public enum CrossbowState {
        UNCHARGED,
        CHARGING,
        CHARGED,
        READY_TO_ATTACK;
    }
}
