package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.api.blocks.HolyWaterEffectConsumer;
import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.items.HolyWaterBottleItem;
import de.teamlapen.vampirism.items.HolyWaterSplashBottleItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;

public class CursedEarthBlock extends Block implements HolyWaterEffectConsumer {

    public CursedEarthBlock() {
        super(Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(0.5f, 2.0f).sound(SoundType.GRAVEL));

    }

    @Override
    public @NotNull ItemInteractionResult useItemOn(ItemStack stack, @NotNull BlockState state, @NotNull Level worldIn, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand handIn, @NotNull BlockHitResult hit) {
        Item heldItem = stack.getItem();
        if (heldItem instanceof HolyWaterBottleItem && !(heldItem instanceof HolyWaterSplashBottleItem)) {
            int uses = heldItem == ModItems.HOLY_WATER_BOTTLE_ULTIMATE.get() ? 100 : (heldItem == ModItems.HOLY_WATER_BOTTLE_ENHANCED.get() ? 50 : 25);
            if (!player.getAbilities().instabuild && player.getRandom().nextInt(uses) == 0) {
                stack.setCount(stack.getCount() - 1);
            }
            worldIn.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, worldIn, pos, player, handIn, hit);
    }

    @Override
    public void onHolyWaterEffect(Level level, BlockState state, BlockPos pos, ItemStack holyWaterStack, IItemWithTier.TIER tier) {
        level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
    }
}
