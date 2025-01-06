package de.teamlapen.vampirism.blocks;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.lib.lib.util.UtilLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Stream;

@SuppressWarnings("deprecation")
public class StandingCandelabraBlock extends CandleStickBlock {
    public static final MapCodec<StandingCandelabraBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            candleStickParts(inst).apply(inst, StandingCandelabraBlock::new)
    );

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final Pair<VoxelShape, VoxelShape> SHAPES = UtilLib.getShapeRotatedSymmetrically(Stream.of(Block.box(6, 0, 6, 10, 1, 10), Block.box(2, 0, 7, 14, 8, 9), Block.box(1, 6, 6, 5, 8, 10), Block.box(11, 6, 6, 15, 8, 10), Block.box(6, 8, 6, 10, 10, 10)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get());
    private static final Pair<VoxelShape, VoxelShape> SHAPE_WITH_CANDLES = UtilLib.getShapeRotatedSymmetrically(Stream.of(Block.box(6, 0, 6, 10, 1, 10), Block.box(2, 0, 7, 14, 8, 9), Block.box(1, 6, 6, 5, 8, 10), Block.box(2, 8, 7, 4, 13, 9), Block.box(7, 10, 7, 9, 15, 9), Block.box(12, 8, 7, 14, 13, 9), Block.box(11, 6, 6, 15, 8, 10), Block.box(6, 8, 6, 10, 10, 10)).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get());

    private static final Pair<Iterable<Vec3>, Iterable<Vec3>> PARTICLE_OFFSET = Pair.of(
            ImmutableList.of(new Vec3(13 / 16d, 14.75 / 16d, 8 / 16d), new Vec3(8 / 16d, 16.75 / 16d, 8 / 16d), new Vec3(3 / 16d, 14.75 / 16d, 8 / 16d)),
            ImmutableList.of(new Vec3(8 / 16d, 14.75 / 16d, 13 / 16d), new Vec3(8 / 16d, 16.75 / 16d, 8 / 16d), new Vec3(8 / 16d, 14.75 / 16d, 3 / 16d))
    );

    public static final ToIntFunction<BlockState> LIGHT_EMISSION = (state) -> state.getValue(LIT) ? 12 : 0;

    public StandingCandelabraBlock(Block emptyBlock, Item candle, Properties properties) {
        this(() -> emptyBlock, () -> candle, properties);
    }

    public StandingCandelabraBlock(@Nullable Supplier<? extends Block> emptyBlock, Supplier<Item> candle, Properties properties) {
        super(emptyBlock, candle, properties.lightLevel(LIGHT_EMISSION));
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LIT, false));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pFacing, BlockState pFacingState, LevelAccessor pLevel, BlockPos pCurrentPos, BlockPos pFacingPos) {
        return pFacing == Direction.DOWN && !this.canSurvive(pState, pLevel, pCurrentPos) ? Blocks.AIR.defaultBlockState() : super.updateShape(pState, pFacing, pFacingState, pLevel, pCurrentPos, pFacingPos);
    }

    @Override
    public BlockState rotate(BlockState pState, Rotation pRot) {
        return pState.setValue(FACING, pState.getValue(FACING).getOpposite());
    }

    @Override
    public BlockState mirror(BlockState pState, Mirror pMirror) {
        return pState.setValue(FACING, pMirror.mirror(pState.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        super.createBlockStateDefinition(pBuilder.add(FACING));
    }

    @Override
    public int getNumberOfCandles() {
        return 3;
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        Direction direction = pState.getValue(FACING);
        Pair<VoxelShape, VoxelShape> shapes = isEmpty() ? SHAPES : SHAPE_WITH_CANDLES;
        return direction == Direction.NORTH || direction == Direction.SOUTH ? shapes.getFirst() : shapes.getSecond();
    }

    @Override
    protected MapCodec<? extends AbstractCandleBlock> codec() {
        return CODEC;
    }

    @Override
    protected Iterable<Vec3> getParticleOffsets(BlockState pState) {
        Direction direction = pState.getValue(FACING);
        return direction == Direction.NORTH || direction == Direction.SOUTH ? PARTICLE_OFFSET.getFirst() : PARTICLE_OFFSET.getSecond();
    }

    @Override
    protected BlockState getFilledState(BlockState sourceState, Block block) {
        return super.getFilledState(sourceState, block).setValue(FACING, sourceState.getValue(FACING));
    }

    @Override
    protected BlockState getEmptyState(BlockState sourceState, Block block) {
        return super.getEmptyState(sourceState, block).setValue(FACING, sourceState.getValue(FACING));
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        return new ItemStack(this);
    }
}
