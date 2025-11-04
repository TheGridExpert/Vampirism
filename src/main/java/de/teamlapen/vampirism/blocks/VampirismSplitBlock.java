package de.teamlapen.vampirism.blocks;

import de.teamlapen.lib.lib.util.UtilLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

public class VampirismSplitBlock extends Block {

    public static final EnumProperty<Direction> FACING = HORIZONTAL_FACING;
    public static final EnumProperty<Part> PART = EnumProperty.create("part", Part.class);

    private final VoxelShape NORTH_MAIN, EAST_MAIN, SOUTH_MAIN, WEST_MAIN;
    private final VoxelShape NORTH_SUB,  EAST_SUB,  SOUTH_SUB,  WEST_SUB;

    private final boolean vertical;

    public VampirismSplitBlock(Properties properties, VoxelShape mainShape, VoxelShape subShape, boolean vertical) {
        super(properties);
        this.vertical = vertical;
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(PART, Part.MAIN));

        NORTH_MAIN = mainShape;
        EAST_MAIN  = UtilLib.rotateShape(mainShape, UtilLib.RotationAmount.NINETY);
        SOUTH_MAIN = UtilLib.rotateShape(mainShape, UtilLib.RotationAmount.HUNDRED_EIGHTY);
        WEST_MAIN  = UtilLib.rotateShape(mainShape, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);

        NORTH_SUB = subShape;
        EAST_SUB  = UtilLib.rotateShape(subShape, UtilLib.RotationAmount.NINETY);
        SOUTH_SUB = UtilLib.rotateShape(subShape, UtilLib.RotationAmount.HUNDRED_EIGHTY);
        WEST_SUB  = UtilLib.rotateShape(subShape, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return state.getValue(PART).isMain() ? RenderShape.MODEL : RenderShape.INVISIBLE;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean main = state.getValue(PART).isMain();
        return switch (state.getValue(FACING)) {
            case NORTH -> main ? NORTH_MAIN : NORTH_SUB;
            case EAST  -> main ? EAST_MAIN  : EAST_SUB;
            case SOUTH -> main ? SOUTH_MAIN : SOUTH_SUB;
            case WEST  -> main ? WEST_MAIN  : WEST_SUB;
            default -> NORTH_MAIN;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Level level = context.getLevel();
        Direction direction = context.getHorizontalDirection();
        BlockPos subPos = context.getClickedPos().relative(this.vertical ? Direction.UP : direction);

        if (!level.getBlockState(subPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(subPos)) {
            return null;
        }

        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.isClientSide) return;

        BlockPos subPos = pos.relative(getOtherBlockDirection(state));
        BlockState subState = state.setValue(PART, Part.SUB);

        if (!this.vertical) {
            subState = subState.setValue(FACING, subState.getValue(FACING).getOpposite());
        }

        level.setBlock(subPos, subState, Block.UPDATE_ALL);
        state.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        return direction == getOtherBlockDirection(state) && !(neighborState.getBlock() == this && neighborState.getValue(PART) != state.getValue(PART))
                ? Blocks.AIR.defaultBlockState()
                : super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative() && state.getValue(PART).isSub()) {
            BlockPos mainPos = pos.relative(getOtherBlockDirection(state));
            BlockState mainState = level.getBlockState(mainPos);

            if (mainState.getBlock() == this && mainState.getValue(PART).isMain()) {
                level.setBlock(mainPos, Blocks.AIR.defaultBlockState(), 35);
                level.levelEvent(player, LevelEvent.PARTICLES_DESTROY_BLOCK, mainPos, Block.getId(mainState));
            }
        }

        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    protected Direction getOtherBlockDirection(BlockState state) {
        return vertical
                ? (state.getValue(PART).isMain() ? Direction.UP : Direction.DOWN)
                : state.getValue(FACING);
    }

    public enum Part implements StringRepresentable {
        MAIN("main"),
        SUB("sub");

        private final String name;

        Part(String name) {
            this.name = name;
        }

        public boolean isMain() {
            return this == MAIN;
        }

        public boolean isSub() {
            return this == SUB;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
