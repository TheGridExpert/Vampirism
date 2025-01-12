package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.ItemPropertiesExtension;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.items.component.FactionRestriction;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base class for all hunter only armor tileInventory
 */
public abstract class HunterArmorItem extends ArmorItem {

    public HunterArmorItem(@NotNull ArmorMaterial materialIn, @NotNull ArmorType type, Item.@NotNull Properties props) {
        super(materialIn, type, FactionRestriction.builder(ModFactionTags.IS_HUNTER).apply(ItemPropertiesExtension.descriptionWithout(props, "_normal|_enhanced|_ultimate")));
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pEntity.tickCount % 16 == 8 && pSlotId >= 36 && pSlotId <= 39 && pEntity instanceof Player player) {
            Holder<? extends IPlayableFaction<?>> f = VampirismPlayerAttributes.get(player).faction;
            if (f != null && !ModFactions.HUNTER.match(f)) {
                player.addEffect(new MobEffectInstance(ModEffects.POISON, 20, 1));
            }
        }
    }

    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return super.canEquip(stack, armorType, entity) && FactionRestriction.canUse(entity, stack, true);
    }

    protected String getTextureLocation(String name, EquipmentSlot slot, @Nullable String type) {
        return String.format(REFERENCE.MODID + ":textures/models/armor/%s_layer_%d%s.png", name, slot == EquipmentSlot.LEGS ? 2 : 1, type == null ? "" : "_overlay");
    }
}
