package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.tags.ModItemTags;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeartSeekerItem extends VampireSwordItem implements IItemWithTier {

    public static final ToolMaterial NORMAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 500, -3.6f, 1.7F, 14, ModItemTags.VAMPIRE_SWORD_REPAIRABLE_SIMPLE);
    public static final ToolMaterial ENHANCED = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1750, -3.5f, 2.7F, 14, ModItemTags.VAMPIRE_SWORD_REPAIRABLE_ENHANCED);
    public static final ToolMaterial ULTIMATE = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2500, -3.4f, 3.7F, 14, ModItemTags.VAMPIRE_SWORD_REPAIRABLE_ENHANCED);

    private final @NotNull TIER tier;

    public HeartSeekerItem(@NotNull ToolMaterial material, @NotNull TIER tier, float trainIncrease, Item.Properties properties) {
        super(material, 3, trainIncrease, properties);
        this.tier = tier;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        addTierInformation(tooltip);
        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    @Override
    public TIER getVampirismTier() {
        return tier;
    }

    @Override
    public float getXpRepairRatio(ItemStack stack) {
        return this.getVampirismTier() == TIER.ULTIMATE ? super.getXpRepairRatio(stack) / 2f : super.getXpRepairRatio(stack);
    }

    @Override
    protected float getChargeUsage() {
        return (float) ((VampirismConfig.BALANCE.vampireSwordBloodUsageFactor.get() / 100f) * (getVampirismTier().ordinal() + 2) / 2f);
    }

    @Override
    protected float getChargingFactor(ItemStack stack) {
        return (float) (VampirismConfig.BALANCE.vampireSwordChargingFactor.get() * 2f / (getVampirismTier().ordinal() + 2f));
    }
}
