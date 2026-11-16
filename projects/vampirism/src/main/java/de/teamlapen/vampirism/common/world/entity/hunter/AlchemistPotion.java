package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterComrades;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownSplashPotion;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

public class AlchemistPotion extends ThrownSplashPotion {

    public enum Audience {
        ALLIES, ENEMIES
    }

    private final Hunter thrower;
    private final Audience audience;

    public AlchemistPotion(ServerLevel level, Hunter thrower, ItemStack potion, Audience audience) {
        super(level, thrower, potion);
        this.thrower = thrower;
        this.audience = audience;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return super.canHitEntity(entity) && (!(entity instanceof LivingEntity living) || affects(living));
    }

    @Override
    public void onHitAsPotion(ServerLevel level, ItemStack potionItem, HitResult hitResult) {
        PotionContents contents = potionItem.getOrDefault(DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        float durationScale = potionItem.getOrDefault(DataComponents.POTION_DURATION_SCALE, 1.0F);
        AABB potionBox = getBoundingBox().move(hitResult.getLocation().subtract(position()));
        float margin = ProjectileUtil.computeMargin(this);
        Entity effectSource = getEffectSource();

        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, potionBox.inflate(SPLASH_RANGE, 2.0D, SPLASH_RANGE), this::affects)) {
            if (!entity.isAffectedByPotions()) continue;

            double distanceSqr = potionBox.distanceToSqr(entity.getBoundingBox().inflate(margin));
            if (distanceSqr >= SPLASH_RANGE_SQ) continue;

            double scale = 1.0D - Math.sqrt(distanceSqr) / SPLASH_RANGE;
            for (MobEffectInstance instance : contents.getAllEffects()) {
                Holder<MobEffect> effect = instance.getEffect();
                if (effect.value().isInstantenous()) {
                    effect.value().applyInstantenousEffect(level, this, getOwner(), entity, instance.getAmplifier(), scale);
                    continue;
                }

                int duration = instance.mapDuration(base -> (int) (scale * base * durationScale + 0.5D));
                MobEffectInstance scaled = new MobEffectInstance(effect, duration, instance.getAmplifier(), instance.isAmbient(), instance.isVisible());
                if (!scaled.endsWithin(20)) entity.addEffect(scaled, effectSource);
            }
        }
    }

    private boolean affects(LivingEntity entity) {
        return switch (this.audience) {
            case ALLIES -> entity == this.thrower || HunterComrades.isComrade(this.thrower, entity);
            case ENEMIES -> HunterComrades.isFoe(this.thrower, entity);
        };
    }
}
