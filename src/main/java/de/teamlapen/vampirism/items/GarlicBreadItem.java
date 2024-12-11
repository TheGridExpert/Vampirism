package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.items.IFactionExclusiveItem;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ClearAllStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GarlicBreadItem extends Item implements IFactionExclusiveItem {

    public static final Consumable GARLIC = Consumables.defaultFood()
            .onConsume(new RemoveStatusEffectsConsumeEffect(ModEffects.SANGUINARE)).build();

    public GarlicBreadItem(Item.Properties properties) {
        super(properties.food((new FoodProperties.Builder()).nutrition(6).saturationModifier(0.7F).build()).component(DataComponents.CONSUMABLE, GARLIC));
    }

    @Override
    public @NotNull TagKey<IFaction<?>> getExclusiveFaction(@NotNull ItemStack stack) {
        return ModFactionTags.USE_GARLIC_BREAD;
    }

}
