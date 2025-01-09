package de.teamlapen.vampirism.blocks;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.lib.lib.util.UtilLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
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

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class WallCandleStickBlock extends CandleHolderBlock {
    public static final MapCodec<WallCandleStickBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            candleStickParts(inst).apply(inst, WallCandleStickBlock::new)
    );

    private static final VoxelShape SHAPE = Stream.of(Block.box(6, 1, 15, 10, 5, 16), Block.box(7, 2, 13, 9, 4, 15), Block.box(7, 2, 11, 9, 5, 13), Block.box(6, 5, 10, 10, 7, 14)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    private static final VoxelShape SHAPE_WITH_CANDLE = Shapes.or(SHAPE, Block.box(7, 7, 11, 9, 13, 13));

    private static final Map<Direction, VoxelShape> SHAPES = UtilLib.getShapeRotatedFromNorth(SHAPE);
    private static final Map<Direction, VoxelShape> SHAPES_WITH_CANDLE = UtilLib.getShapeRotatedFromNorth(SHAPE_WITH_CANDLE);

    private static final Map<Direction, Iterable<Vec3>> PARTICLE_OFFSET = new EnumMap<>(Direction.class) {{
        put(Direction.NORTH, ImmutableList.of(new Vec3(8 / 16d, 14.75 / 16d, 12 / 16d)));
        put(Direction.WEST, ImmutableList.of(new Vec3(12 / 16d, 14.75 / 16d, 8 / 16d)));
        put(Direction.SOUTH, ImmutableList.of(new Vec3(8 / 16d, 14.75 / 16d, 4 / 16d)));
        put(Direction.EAST, ImmutableList.of(new Vec3(4 / 16d, 14.75 / 16d, 8 / 16d)));
    }};

    private WallCandleStickBlock(Block emptyBlock, Item candle, Properties properties) {
        this(() -> emptyBlock, () -> candle, properties);
    }

    public WallCandleStickBlock(@Nullable Supplier<? extends Block> emptyBlock, Supplier<Item> candle, Properties properties) {
        super(emptyBlock, candle, properties);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return pLevel.getBlockState(pPos.relative(pState.getValue(FACING).getOpposite())).isSolid();
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return getStateForWallPlacement(pContext);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing.getOpposite() == pState.getValue(FACING) && !pState.canSurvive(pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return isEmpty() ? SHAPES.get(pState.getValue(FACING)) : SHAPES_WITH_CANDLE.get(pState.getValue(FACING));
    }

    @Override
    protected MapCodec<? extends AbstractCandleBlock> codec() {
        return CODEC;
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        return PARTICLE_OFFSET.get(pState.getValue(FACING));
    }
}
