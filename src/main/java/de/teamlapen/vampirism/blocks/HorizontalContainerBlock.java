package de.teamlapen.vampirism.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class HorizontalContainerBlock extends VampirismHorizontalBlock implements EntityBlock {
    public HorizontalContainerBlock(Properties properties, VoxelShape shape) {
        super(properties, shape);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected boolean triggerEvent(BlockState state, Level level, BlockPos pos, int id, int param) {
        super.triggerEvent(state, level, pos, id, param);
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity != null && blockentity.triggerEvent(id, param);
    }

    @Nullable
    public MenuProvider getMenuProvider(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos) {
        BlockEntity blockentity = level.getBlockEntity(pos);
        return blockentity instanceof MenuProvider ? (MenuProvider) blockentity : null;
    }

    @Nullable
    public static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> typeA, BlockEntityType<E> typeB, BlockEntityTicker<? super E> ticker) {
        //noinspection unchecked
        return typeB == typeA ? (BlockEntityTicker<A>) ticker : null;
    }
}
