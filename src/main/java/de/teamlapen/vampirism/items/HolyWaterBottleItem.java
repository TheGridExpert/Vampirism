package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.ItemPropertiesExtension;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.items.IFactionExclusiveItem;
import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * HolyWaterBottle
 * Exists in different tiers and as splash versions.
 */
public class HolyWaterBottleItem extends Item implements IItemWithTier, IFactionExclusiveItem {
    private final TIER tier;

    public HolyWaterBottleItem(TIER tier, @NotNull Properties props) {
        super(ItemPropertiesExtension.descriptionWithout(props, "_normal|_enhanced|_ultimate"));
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        addTierInformation(tooltip);
    }

    @Override
    public @NotNull TagKey<IFaction<?>> getExclusiveFaction(@NotNull ItemStack stack) {
        return ModFactionTags.IS_HUNTER;
    }

    /**
     * Converts the tier of this bottle into the strength of the applied holy water
     */
    public @NotNull EnumStrength getStrength(@NotNull TIER tier) {
        return switch (tier) {
            case NORMAL -> EnumStrength.WEAK;
            case ENHANCED -> EnumStrength.MEDIUM;
            case ULTIMATE -> EnumStrength.STRONG;
        };
    }

    @Override
    public TIER getVampirismTier() {
        return tier;
    }

}
