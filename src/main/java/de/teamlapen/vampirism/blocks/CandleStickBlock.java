package de.teamlapen.vampirism.blocks;

import com.google.common.collect.Maps;
import com.mojang.datafixers.Products;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.core.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

public abstract class CandleStickBlock extends AbstractCandleBlock implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty LIT = AbstractCandleBlock.LIT;

    public static final ToIntFunction<BlockState> LIGHT_EMISSION = (state) -> state.getValue(LIT) ? 6 : 0;

    protected static <T extends CandleStickBlock> Products.P3<RecordCodecBuilder.Mu<T>, Block, Item, Properties> candleStickParts(RecordCodecBuilder.Instance<T> instance) {
        return instance.group(
                BuiltInRegistries.BLOCK.byNameCodec().fieldOf("empty_block").forGetter(i -> Optional.ofNullable(i.emptyBlock).map(Supplier::get).orElse(null)),
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("candle").forGetter(i -> i.candle.get()),
                propertiesCodec()
        );
    }

    private final Map<ResourceLocation, Supplier<Block>> fullHolderByContent = Maps.newHashMap();
    protected final @Nullable Supplier<? extends Block> emptyBlock;
    protected final Supplier<Item> candle;

    protected CandleStickBlock(@Nullable Supplier<? extends Block> emptyBlock, Supplier<Item> candle, Properties properties) {
        super(properties);
        this.emptyBlock = emptyBlock;
        this.candle = candle;
    }

    public void addCandle(ResourceLocation candle, Supplier<Block> holder) {
        this.fullHolderByContent.put(candle, holder);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        ItemStack stack = pPlayer.getItemInHand(InteractionHand.MAIN_HAND);
        Item item = stack.getItem();
        if (isEmpty()) {
            Block orDefault = this.fullHolderByContent.getOrDefault(BuiltInRegistries.ITEM.getKey(item), () -> Blocks.AIR).get();
            if (orDefault != Blocks.AIR) {
                if (stack.getCount() < getNumberOfCandles()) {
                    pPlayer.displayClientMessage(Component.translatable("text.vampirism.not_enough_candles", getNumberOfCandles()), true);
                } else {
                    pLevel.setBlock(pPos, getFilledState(pState, orDefault), 3);
                    if (!pPlayer.getAbilities().instabuild) {
                        stack.shrink(getNumberOfCandles());
                    }
                    pLevel.playSound(pPlayer, pPos, SoundType.CANDLE.getPlaceSound(), SoundSource.BLOCKS, (SoundType.CANDLE.getVolume() + 1.0F) / 2.0F, SoundType.CANDLE.getPitch() * 0.8F);
                    return InteractionResult.sidedSuccess(pLevel.isClientSide);
                }
            }
        } else if (pPlayer.getAbilities().mayBuild && pPlayer.getItemInHand(InteractionHand.MAIN_HAND).isEmpty()) {
            if (pState.getValue(LIT)) {
                extinguish(pPlayer, pState, pLevel, pPos);
                return InteractionResult.sidedSuccess(pLevel.isClientSide);
            } else {
                if (this.emptyBlock != null) {
                    pLevel.setBlock(pPos, this.getEmptyState(pState, this.emptyBlock.get()), 3);
                    if (!pPlayer.getAbilities().instabuild) {
                        pPlayer.addItem(new ItemStack(candle.get(), getNumberOfCandles()));
                    }
                    pLevel.playSound(pPlayer, pPos, SoundType.CANDLE.getBreakSound(), SoundSource.BLOCKS, (SoundType.CANDLE.getVolume() + 1.0F) / 2.0F, SoundType.CANDLE.getPitch() * 0.8F);
                }
            }
        }

        return InteractionResult.PASS;
    }

    public int getNumberOfCandles() {
        return 1;
    }

    protected BlockState getFilledState(BlockState sourceState, Block block) {
        return block.defaultBlockState().setValue(WATERLOGGED, sourceState.getValue(WATERLOGGED)).setValue(LIT, sourceState.getValue(LIT));
    }

    protected BlockState getEmptyState(BlockState sourceState, Block block) {
        return block.defaultBlockState().setValue(WATERLOGGED, sourceState.getValue(WATERLOGGED)).setValue(LIT, sourceState.getValue(LIT));
    }

    @Override
    public BlockState updateShape(BlockState pState, Direction pDirection, BlockState pNeighborState, LevelAccessor pLevel, BlockPos pPos, BlockPos pNeighborPos) {
        if (pState.getValue(WATERLOGGED)) {
            pLevel.scheduleTick(pPos, Fluids.WATER, Fluids.WATER.getTickDelay(pLevel));
        }

        return super.updateShape(pState, pDirection, pNeighborState, pLevel, pPos, pNeighborPos);
    }

    @Override
    public FluidState getFluidState(BlockState pState) {
        return pState.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(pState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(LIT, WATERLOGGED);
    }

    @Override
    public boolean placeLiquid(LevelAccessor pLevel, BlockPos pPos, BlockState pState, FluidState pFluidState) {
        if (!pState.getValue(WATERLOGGED) && pFluidState.getType() == Fluids.WATER) {
            BlockState blockstate = pState.setValue(WATERLOGGED, Boolean.TRUE);
            if (pState.getValue(LIT)) {
                extinguish(null, blockstate, pLevel, pPos);
            } else {
                pLevel.setBlock(pPos, blockstate, 3);
            }

            pLevel.scheduleTick(pPos, pFluidState.getType(), pFluidState.getType().getTickDelay(pLevel));
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean canBeLit(BlockState pState) {
        return !pState.getValue(WATERLOGGED) && !this.isEmpty() && super.canBeLit(pState);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return Block.canSupportCenter(pLevel, pPos.below(), Direction.UP);
    }

    @NotNull
    public Supplier<@Nullable Item> getCandle() {
        return candle;
    }

    public boolean isEmpty() {
        return this.candle.get() == null;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        Item candle = getCandle().get();
        return candle == null ? ModItems.CANDLE_STICK.get().getDefaultInstance() : candle.getDefaultInstance();
    }
}
