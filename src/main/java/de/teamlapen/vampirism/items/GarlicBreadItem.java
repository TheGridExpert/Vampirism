package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import de.teamlapen.vampirism.items.consume.FactionBasedConsumeEffect;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

import java.util.List;

public class GarlicBreadItem extends Item {

    public static final Consumable GARLIC = Consumables.defaultFood()
            .onConsume(new RemoveStatusEffectsConsumeEffect(ModEffects.SANGUINARE))
            .onConsume(FactionBasedConsumeEffect.build(ModFactionTags.IS_VAMPIRE, new ApplyStatusEffectsConsumeEffect(
                    List.of(
                            new MobEffectInstance(ModEffects.GARLIC, 20*30),
                            new MobEffectInstance(MobEffects.WEAKNESS, 20*30)
                    )))
            )
            .build();

    public GarlicBreadItem(Item.Properties properties) {
        super(properties.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build()).component(DataComponents.CONSUMABLE, GARLIC));
    }
}
