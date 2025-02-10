package de.teamlapen.vampirism.blocks.candle;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public abstract class CandleHolderBlock extends AbstractCandleBlock implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;

    protected static <T extends CandleHolderBlock> Products.P3<RecordCodecBuilder.Mu<T>, Block, Item, Properties> candleStickParts(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("empty_block").forGetter(i -> Optional.ofNullable(i.emptyBlock).map(Supplier::get).orElse(null)),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("candle").forGetter(i -> i.candle.get()),
                propertiesCodec()
        );
    }

    private final Map<ResourceLocation, Supplier<Block>> fullHolderByContent = Maps.newHashMap();
    protected final @Nullable Supplier<? extends Block> emptyBlock;
    protected final @NotNull Supplier<Item> candle;

    protected CandleHolderBlock(@Nullable Supplier<? extends Block> emptyBlock, @NotNull Supplier<Item> candle, Properties properties) {
        super(properties);
        this.emptyBlock = emptyBlock;
        this.candle = candle;
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(WATERLOGGED, false).setValue(LIT, false));
    }

    public void addCandle(ResourceLocation candle, Supplier<Block> holder) {
        if (candle == null) {
            throw new IllegalArgumentException("Cannot add plant to non-empty candle mount");
        }
        this.fullHolderByContent.put(candle, holder);
    }

    @Override
    public @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = stack.getItem();
        if (isEmpty()) {
            Block orDefault = this.fullHolderByContent.getOrDefault(BuiltInRegistries.ITEM.getKey(item), () -> Blocks.AIR).get();
            if (orDefault != Blocks.AIR) {
                if (stack.getCount() < getNumberOfCandles()) {
                    player.displayClientMessage(Component.translatable("text.vampirism.candle_holder.not_enough_candles", getNumberOfCandles()), true);
                } else {
                    level.setBlock(pos, getFilledState(state, orDefault), 3);
                    if (!player.getAbilities().instabuild) {
                        stack.shrink(getNumberOfCandles());
                    }
                    level.playSound(player, pos, SoundType.CANDLE.getPlaceSound(), SoundSource.BLOCKS, (SoundType.CANDLE.getVolume() + 1.0F) / 2.0F, SoundType.CANDLE.getPitch() * 0.8F);
                    return InteractionResult.SUCCESS_SERVER;
                }
            }
        } else if (player.getAbilities().mayBuild && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            if (state.getValue(LIT)) {
                extinguish(player, state, level, pos);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                if (this.emptyBlock != null) {
                    level.setBlock(pos, this.getEmptyState(state, this.emptyBlock.get()), 3);
                    if (!player.getAbilities().instabuild) {
                        player.addItem(new ItemStack(candle.get(), getNumberOfCandles()));
                    }
                    level.playSound(player, pos, SoundType.CANDLE.getBreakSound(), SoundSource.BLOCKS, (SoundType.CANDLE.getVolume() + 1.0F) / 2.0F, SoundType.CANDLE.getPitch() * 0.8F);
                }
            }
        }

        return InteractionResult.PASS;
    }

    public int getNumberOfCandles() {
        return 1;
    }

    @Override
    public int getLightEmission(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos) {
        return state.getValue(LIT) ? getLitLightLevel() : 0;
    }

    public int getLitLightLevel() {
        return 6;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite()).setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
    }

    /**
     * Used in child methods for wall candle holders.
     */
    @Nullable
    public BlockState getStateForWallPlacement(BlockPlaceContext pContext) {
        BlockState blockstate = this.defaultBlockState();
        FluidState fluidstate = pContext.getLevel().getFluidState(pContext.getClickedPos());
        LevelReader levelreader = pContext.getLevel();
        BlockPos blockpos = pContext.getClickedPos();
        Direction[] adirection = pContext.getNearestLookingDirections();

        for (Direction direction : adirection) {
            if (direction.getAxis().isHorizontal()) {
                Direction direction1 = direction.getOpposite();
                blockstate = blockstate.setValue(FACING, direction1);
                if (blockstate.canSurvive(levelreader, blockpos)) {
                    return blockstate.setValue(WATERLOGGED, fluidstate.getType() == Fluids.WATER);
                }
            }
        }

        return null;
    }

    protected BlockState getFilledState(BlockState sourceState, Block block) {
        return block.defaultBlockState().setValue(FACING, sourceState.getValue(FACING)).setValue(WATERLOGGED, sourceState.getValue(WATERLOGGED)).setValue(LIT, sourceState.getValue(LIT));
    }

    protected BlockState getEmptyState(BlockState sourceState, Block block) {
        return block.defaultBlockState().setValue(FACING, sourceState.getValue(FACING)).setValue(WATERLOGGED, sourceState.getValue(WATERLOGGED)).setValue(LIT, sourceState.getValue(LIT));
    }

    @Override
    public @NotNull BlockState updateShape(@NotNull BlockState state, @NotNull LevelReader level, @NotNull ScheduledTickAccess tickAccess, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource randomSource) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }

        return super.updateShape(state, level, tickAccess, pos, direction, neighborPos, neighborState, randomSource);
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, WATERLOGGED);
    }

    @Override
    public boolean placeLiquid(@NotNull LevelAccessor level, @NotNull BlockPos pos, BlockState state, @NotNull FluidState fluidState) {
        if (!state.getValue(WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            BlockState blockstate = state.setValue(WATERLOGGED, Boolean.TRUE);
            if (state.getValue(LIT)) {
                extinguish(null, blockstate, level, pos);
            } else {
                level.setBlock(pos, blockstate, 3);
            }

            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean canBeLit(BlockState state) {
        return !state.getValue(WATERLOGGED) && !this.isEmpty() && super.canBeLit(state);
    }

    @Override
    public boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader level, BlockPos pPos) {
        return Block.canSupportCenter(level, pPos.below(), Direction.UP);
    }

    @Override
    public @NotNull BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public @NotNull BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)));
    }

    @Override
    protected boolean isPathfindable(@NotNull BlockState state, @NotNull PathComputationType pathComputationType) {
        return false;
    }

    @NotNull
    public Supplier<@Nullable Item> getCandle() {
        return candle;
    }

    public boolean isEmpty() {
        return this.candle.get() == null;
    }
}
