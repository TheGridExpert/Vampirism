package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.sit.SitEntity;
import de.teamlapen.vampirism.sit.SitHandler;
import de.teamlapen.vampirism.sit.SitUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.stream.Stream;

public class ThroneBlock extends VampirismSplitBlock implements SimpleWaterloggedBlock {

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public static final VoxelShape BOTTOM_SHAPE = Stream.of(
            Block.box(1, 0, 1, 15, 10, 16),
            Block.box(0, 7, 2, 3, 15, 16),
            Block.box(13, 7, 2, 16, 15, 16),
            Block.box(1, 10, 14, 15, 16, 16)
    ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
    public static final VoxelShape TOP_SHAPE = Block.box(1, 0, 14, 15, 11, 16);

    public ThroneBlock(Properties properties) {
        super(properties, BOTTOM_SHAPE, TOP_SHAPE, true);
        this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false));
    }

    // TODO: If the main part is placed inside water, while the top is not, it will flood the top one above water.
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        BlockState state = super.getStateForPlacement(context);

        if (state != null) {
            state.setValue(WATERLOGGED, fluidState.getType() == Fluids.WATER);
        }

        return state;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WATERLOGGED);
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess scheduledTickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            scheduledTickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, scheduledTickAccess, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    protected FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        player.awardStat(ModStats.INTERACT_WITH_THRONE.get());

        Part part = state.getValue(PART);
        Direction backDirection = state.getValue(FACING).getOpposite();
        Direction hitDirection = hitResult.getDirection();

        if (part.isMain() && (hitDirection == Direction.UP || hitDirection == backDirection)) {
            SitHandler.startSitting(player, level, pos, 0.5);
            return InteractionResult.SUCCESS;
        }

        if (part.isSub() && hitDirection == backDirection && level.getBlockState(pos.below()).is(this)) {
            SitHandler.startSitting(player, level, pos.below(), 0.5);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);

        SitEntity sit = SitUtil.getSitEntity(level, pos);
        if (sit != null) {
            sit.discard();
        }
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        entity.causeFallDamage(fallDistance, state.getValue(PART).isMain() ? 0.5f : 1.0f, entity.damageSources().fall());
    }

    @Override
    public void updateEntityMovementAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            super.updateEntityMovementAfterFallOn(level, entity);
        } else {
            this.bounceUp(entity);
        }
    }

    private void bounceUp(Entity entity) {
        Vec3 vec3 = entity.getDeltaMovement();
        if (vec3.y < 0.0) {
            entity.setDeltaMovement(vec3.x, -vec3.y * 0.35F, vec3.z);
        }
    }
}
