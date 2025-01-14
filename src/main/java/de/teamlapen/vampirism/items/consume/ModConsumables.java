package de.teamlapen.vampirism.items.consume;

import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.consume_effects.RemoveStatusEffectsConsumeEffect;

public class ModConsumables {
    public static final Consumable GARLIC = net.minecraft.world.item.component.Consumables.defaultFood()
            .onConsume(new RemoveStatusEffectsConsumeEffect(ModEffects.SANGUINARE))
            .onConsume(FactionBasedConsumeEffect.build(ModFactionTags.IS_VAMPIRE, new AffectGarlic(EnumStrength.MEDIUM))).build();
}
