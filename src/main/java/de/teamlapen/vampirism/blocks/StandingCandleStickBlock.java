package de.teamlapen.vampirism.blocks;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class StandingCandleStickBlock extends CandleHolderBlock {
    public static final MapCodec<StandingCandleStickBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            candleStickParts(inst).apply(inst, StandingCandleStickBlock::new)
    );

    private static final VoxelShape SHAPE = Stream.of(Block.box(5, 0, 5, 11, 1, 11), Block.box(7, 1, 7, 9, 3, 9), Block.box(6, 3, 6, 10, 5, 10)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_WITH_CANDLE = Shapes.or(SHAPE, Block.box(7, 5, 7, 9, 11, 9));

    private static final ImmutableList<Vec3> PARTICLE_OFFSET = ImmutableList.of(
            new Vec3(8 / 16d, 12.75 / 16d, 8 / 16d)
    );

    private StandingCandleStickBlock(Block emptyBlock, Item candle, Properties properties) {
        this(() -> emptyBlock, () -> candle, properties);
    }

    public StandingCandleStickBlock(@Nullable Supplier<? extends Block> emptyBlock, Supplier<Item> candle, Properties properties) {
        super(emptyBlock, candle, properties);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.below()).isSolid();
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return isEmpty() ? SHAPE : SHAPE_WITH_CANDLE;
    }

    @Override
    protected MapCodec<? extends AbstractCandleBlock> codec() {
        return CODEC;
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        return PARTICLE_OFFSET;
    }
}
