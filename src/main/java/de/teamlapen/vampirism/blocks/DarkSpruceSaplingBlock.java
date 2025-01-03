package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.core.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class DarkSpruceSaplingBlock extends SaplingBlock {
    private final TreeGrower darkTreeGrower;
    private final TreeGrower cursedTreeGrower;

    public DarkSpruceSaplingBlock(TreeGrower darkTreeGrower, TreeGrower cursedTreeGrower, Properties pProperties) {
        super(darkTreeGrower, pProperties);
        this.darkTreeGrower = darkTreeGrower;
        this.cursedTreeGrower = cursedTreeGrower;
    }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (stack.is(ModTags.Items.PURE_BLOOD)) {
            stack.shrink(1);
            pLevel.setBlockAndUpdate(pPos, ModBlocks.CURSED_SPRUCE_SAPLING.get().defaultBlockState());
            return ItemInteractionResult.sidedSuccess(pLevel.isClientSide);
        }
        return super.useItemOn(stack, pState, pLevel, pPos, pPlayer, pHand, pHit);
    }

    public void advanceTree(ServerLevel pLevel, BlockPos pPos, BlockState pState, RandomSource pRandom) {
        if (pState.getValue(STAGE) == 0) {
            pLevel.setBlock(pPos, pState.cycle(STAGE), 4);
        } else {
            var grower = this.darkTreeGrower;
            if (pLevel.getBlockState(pPos.below()).is(ModTags.Blocks.CURSED_EARTH) && pRandom.nextFloat() < 0.3) {
                grower = cursedTreeGrower;
            }
            grower.growTree(pLevel, pLevel.getChunkSource().getGenerator(), pPos, pState, pRandom);
        }
    }
}
