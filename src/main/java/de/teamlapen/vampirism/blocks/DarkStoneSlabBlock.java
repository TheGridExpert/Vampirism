package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.tags.ModItemTags;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.SlabBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class DarkStoneSlabBlock extends SlabBlock {

    public DarkStoneSlabBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, Item.TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag advanced) {
        super.appendHoverText(stack, context, tooltip, advanced);
        if (stack.is(ModItemTags.NO_SPAWN)) {
            tooltip.add(Component.translatable("block.vampirism.castle_block.no_spawn").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        } else if (stack.is(ModItemTags.VAMPIRE_SPAWN)) {
            tooltip.add(Component.translatable("block.vampirism.castle_block.vampire_spawn").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY));
        }
    }
}
