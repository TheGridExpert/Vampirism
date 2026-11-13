package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.common.core.ModAttributes;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.QuarrelEntity;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterThreats;
import de.teamlapen.vampirism.misc.mixin.accessor.CrossbowItemAccessor;
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
                ModMemoryTypes.AIM_TARGET.get(),
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

        boolean seesTarget = shooter.hasLineOfSight(target) && !HunterThreats.isSubmerged(target);
        boolean isCharging = this.crossbowState == CrossbowState.CHARGING || shooter.isUsingItem();

        if (seesTarget && !isCharging) {
            Vec3 aimPos = calculateAimPosition(shooter, target);
            shooter.getLookControl().setLookAt(aimPos.x, aimPos.y, aimPos.z, 45.0F, 45.0F);
            shooter.getBrain().setMemory(ModMemoryTypes.AIM_TARGET.get(), aimPos);
        }

        if (seesTarget || this.crossbowState != CrossbowState.READY_TO_ATTACK) {
            crossbowAttack(shooter, target);
        }
    }

    @Override
    protected void stop(ServerLevel level, Hunter entity, long gameTime) {
        if (entity.isUsingItem()) {
            entity.stopUsingItem();
        }

        if (entity.isHolding(stack -> stack.getItem() instanceof CrossbowItem)) {
            entity.setChargingCrossbow(false);
            ItemStack crossbow = entity.getItemInHand(ProjectileUtil.getWeaponHoldingHand(entity, item -> item instanceof CrossbowItem));
            crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
        }

        this.crossbowState = CrossbowState.UNCHARGED;
        entity.getBrain().eraseMemory(ModMemoryTypes.SEE_TIME.get());
    }

    private void crossbowAttack(Hunter shooter, LivingEntity target) {
        switch (this.crossbowState) {
            case UNCHARGED -> {
                if (CrossbowItem.isCharged(heldCrossbow(shooter))) {
                    finishCharging(shooter);
                    return;
                }

                shooter.startUsingItem(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));                this.crossbowState = CrossbowState.CHARGING;
                shooter.setChargingCrossbow(true);
            }
            case CHARGING -> {
                if (CrossbowItem.isCharged(heldCrossbow(shooter))) {
                    finishCharging(shooter);
                    return;
                }

                if (!shooter.isUsingItem()) {                    this.crossbowState = CrossbowState.UNCHARGED;
                    return;
                }

                if (shooter.getTicksUsingItem() >= CrossbowItem.getChargeDuration(shooter.getUseItem(), shooter)) {
                    shooter.releaseUsingItem();
                    if (CrossbowItem.isCharged(heldCrossbow(shooter))) {
                        finishCharging(shooter);
                    } else {
                        this.crossbowState = CrossbowState.UNCHARGED;
                    }
                }
            }
            case CHARGED -> {
                this.attackDelay--;
                if (this.attackDelay <= 0) {
                    this.crossbowState = CrossbowState.READY_TO_ATTACK;
                }
            }
            case READY_TO_ATTACK -> {
                int seeTime = shooter.getBrain().getMemory(ModMemoryTypes.SEE_TIME.get()).orElse(0);
                if (seeTime < 20) {
                    shooter.getBrain().setMemory(ModMemoryTypes.SEE_TIME.get(), seeTime + 1);
                    return;
                }

                shooter.performRangedAttack(target, 1.0F);
                this.crossbowState = CrossbowState.UNCHARGED;
                shooter.getBrain().eraseMemory(ModMemoryTypes.REPOSITIONING_COOLDOWN.get());
                shooter.getBrain().eraseMemory(ModMemoryTypes.SEE_TIME.get());
            }
        }
    }

    private void finishCharging(Hunter shooter) {
        if (shooter.isUsingItem()) {
            shooter.releaseUsingItem();
        }
        this.crossbowState = CrossbowState.CHARGED;
        this.attackDelay = 20 + shooter.getRandom().nextInt(20);
        shooter.setChargingCrossbow(false);
    }

    private static ItemStack heldCrossbow(Hunter shooter) {
        return shooter.getItemInHand(ProjectileUtil.getWeaponHoldingHand(shooter, item -> item instanceof CrossbowItem));
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

    public static boolean shootAimedProjectile(LivingEntity shooter, Projectile projectile, int index, float velocity, float inaccuracy, float angle) {
        if (!(shooter instanceof Hunter hunter)) return false;

        LivingEntity target = hunter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        Optional<Vec3> aimTargetOpt = hunter.getBrain().getMemory(ModMemoryTypes.AIM_TARGET.get());
        if (target == null && aimTargetOpt.isEmpty()) return false;

        Vec3 delta;
        if (target != null) {
            float gravityFactor = projectile instanceof QuarrelEntity quarrel ? quarrel.getGravityFactor() : 1.0F;
            delta = BallisticAim.calculateShotDirection(shooter.level(), projectile, projectile.position(), target, velocity, gravityFactor);
        } else {
            delta = aimTargetOpt.get().subtract(shooter.position().add(0, shooter.getEyeHeight(), 0));
        }

        Vector3f shotVector = CrossbowItemAccessor.invokeGetProjectileShotVector(shooter, delta, angle);

        double accuracyMod = 1.0;
        if (shooter.getAttributes().hasAttribute(ModAttributes.ACCURACY)) {
            accuracyMod = shooter.getAttributeValue(ModAttributes.ACCURACY);
        }

        float focus = target != null ? hunter.getAimFocus(target) : 1.0f;
        float adjustedInaccuracy = Math.max(0.0f, inaccuracy / (float) accuracyMod * focus);
        projectile.shoot(shotVector.x(), shotVector.y(), shotVector.z(), velocity, adjustedInaccuracy);

        float pitch = CrossbowItemAccessor.invokeGetShotPitch(shooter.getRandom(), index);
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
        READY_TO_ATTACK
    }
}
