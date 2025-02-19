package de.teamlapen.vampirism.items;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.ItemPropertiesExtension;
import de.teamlapen.vampirism.api.VampirismAPI;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * Simple headwear that look like a hunter head
 */
public class HunterHatItem extends HunterArmorItem {
    private final HatType type;

    public HunterHatItem(HatType type, ArmorMaterial armorMaterial, Item.Properties properties) {
        super(armorMaterial, ArmorType.HELMET, properties.overrideDescription("item.vampirism.hunter_hat_head"));
        this.type = type;
    }

    public HatType getHateType() {
        return type;
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        super.inventoryTick(pStack, pLevel, pEntity, pSlotId, pIsSelected);
        if (pSlotId >= 36 && pSlotId <= 39 && pEntity instanceof Player living) {
            if (pStack.has(DataComponents.CUSTOM_NAME) && "10000000".equals(pStack.getHoverName().getString()) && VampirismAPI.settings().isSettingTrue("vampirism:10000000d")) {
                UtilLib.spawnParticlesAroundEntity(living, ParticleTypes.ELECTRIC_SPARK, 0.5, 4);
                if (living.tickCount % 16 == 4) {
                    living.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 30, 0));
                    living.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 100, 2));
                }
            }
        }
    }

    public enum HatType {
        TYPE_1,
        TYPE_2
    }
}
