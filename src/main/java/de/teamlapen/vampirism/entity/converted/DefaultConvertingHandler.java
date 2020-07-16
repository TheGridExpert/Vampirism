package de.teamlapen.vampirism.entity.converted;

import de.teamlapen.vampirism.api.entity.convertible.IConvertedCreature;
import de.teamlapen.vampirism.api.entity.convertible.IConvertingHandler;
import de.teamlapen.vampirism.config.BalanceMobProps;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.util.SharedMonsterAttributes;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.GlobalEntityTypeAttributes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;

import javax.annotation.Nullable;

/**
 * Default converting handler for entities.
 * Used for some vanilla entities, but can also be used for third party entities.
 * Converts the entity into a {@link ConvertedCreatureEntity}
 */
public class DefaultConvertingHandler<T extends CreatureEntity> implements IConvertingHandler<T> {

    /**
     * Used if no helper is specified
     */
    private final static IDefaultHelper defaultHelper = new IDefaultHelper() {


        @Override
        public double getConvertedDMG(EntityType<? extends CreatureEntity> entityType) {
            AttributeModifierMap map = GlobalEntityTypeAttributes.func_233835_a_(entityType);
            if (map.func_233809_c_(SharedMonsterAttributes.ATTACK_DAMAGE)) {
                return map.func_233807_b_(SharedMonsterAttributes.ATTACK_DAMAGE) * 1.3;
            } else {
                return BalanceMobProps.mobProps.CONVERTED_MOB_DEFAULT_DMG;
            }
        }

        @Override
        public double getConvertedKnockbackResistance(EntityType<? extends CreatureEntity> entityType) {
            AttributeModifierMap map = GlobalEntityTypeAttributes.func_233835_a_(entityType);
            return map.func_233807_b_(SharedMonsterAttributes.KNOCKBACK_RESISTANCE) * 1.3;
        }

        @Override
        public double getConvertedMaxHealth(EntityType<? extends CreatureEntity> entityType) {
            AttributeModifierMap map = GlobalEntityTypeAttributes.func_233835_a_(entityType);
            return map.func_233807_b_(SharedMonsterAttributes.MAX_HEALTH) * 1.5;
        }

        @Override
        public double getConvertedSpeed(EntityType<? extends CreatureEntity> entityType) {
            AttributeModifierMap map = GlobalEntityTypeAttributes.func_233835_a_(entityType);
            return Math.min(map.func_233807_b_(SharedMonsterAttributes.MOVEMENT_SPEED) * 1.2, 2.9D);
        }
    };

    protected final IDefaultHelper helper;

    /**
     * @param helper If null a default one will be used
     */
    public DefaultConvertingHandler(@Nullable IDefaultHelper helper) {
        if (helper == null) {
            this.helper = defaultHelper;
        } else {
            this.helper = helper;
        }
    }

    @Override
    public IConvertedCreature<T> createFrom(T entity) {
        ConvertedCreatureEntity<T> convertedCreature = ModEntities.converted_creature.create(entity.getEntityWorld());
        copyImportantStuff(convertedCreature, entity);
        convertedCreature.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 200, 2));
        return convertedCreature;
    }

    /**
     * @return The helper for this handler
     */
    public IDefaultHelper getHelper() {
        return helper;
    }

    protected void copyImportantStuff(ConvertedCreatureEntity<T> converted, T entity) {
        converted.copyLocationAndAnglesFrom(entity);
        converted.setEntityCreature(entity);
        converted.updateEntityAttributes();
        converted.setHealth(converted.getMaxHealth() / 3 * 2);
    }
}
