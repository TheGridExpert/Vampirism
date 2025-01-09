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
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChandelierBlock extends CandleHolderBlock {
    public static final MapCodec<ChandelierBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            candleStickParts(inst).apply(inst, ChandelierBlock::new)
    );

    private final static VoxelShape SHAPE = Shapes.join(Block.box(1, 1, 1, 15, 6, 15), Block.box(6, 6, 6, 10, 16, 10), BooleanOp.OR);
    private static final VoxelShape SHAPE_WITH_CANDLE = Shapes.or(SHAPE, Stream.of(Block.box(1.6, 6, 1.6, 4.5, 11, 4.5), Block.box(11.5, 6, 1.6, 14.4, 11, 4.5), Block.box(11.5, 6, 11.5, 14.4, 11, 14.4), Block.box(1.6, 6, 11.5, 4.5, 11, 14.4)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get());

    private static final ImmutableList<Vec3> PARTICLE_OFFSET = ImmutableList.of(
            new Vec3(3 / 16d, 11.75 / 16d, 3 / 16d),
            new Vec3(13 / 16d, 11.75 / 16d, 3 / 16d),
            new Vec3(13 / 16d, 11.75 / 16d, 13 / 16d),
            new Vec3(3 / 16d, 11.75 / 16d, 13 / 16d)
    );

    public ChandelierBlock(Block emptyBlock, Item candle, Properties properties) {
        this(() -> emptyBlock, () -> candle, properties);
    }

    public ChandelierBlock(@Nullable Supplier<? extends Block> emptyBlock, Supplier<Item> candle, Properties properties) {
        super(emptyBlock, candle, properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
        return canSupportCenter(worldIn, pos.above(), Direction.DOWN);
    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        return facing == Direction.UP && !this.canSurvive(stateIn, worldIn, currentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
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

    @Override
    public int getNumberOfCandles() {
        return 4;
    }

    @Override
    public int getLitLightLevel() {
        return 15;
    }
}
