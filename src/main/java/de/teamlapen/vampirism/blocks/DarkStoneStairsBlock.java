package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.ModTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DarkStoneStairsBlock extends StairBlock {
    public DarkStoneStairsBlock(@NotNull Holder<Block> block, Properties properties) {
        super(block.value().defaultBlockState(), properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
        super.appendHoverText(stack, context, tooltip, advanced);
        if (stack.is(ModTags.Items.NO_SPAWN)) {
            tooltip.add(Component.translatable("block.vampirism.castle_block.no_spawn").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        } else if (stack.is(ModTags.Items.VAMPIRE_SPAWN)) {
            tooltip.add(Component.translatable("block.vampirism.castle_block.vampire_spawn").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }
}
