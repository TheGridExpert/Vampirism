package de.teamlapen.vampirism.blocks;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.blockentity.PedestalBlockEntity;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.core.ModBlockEntities;
import de.teamlapen.vampirism.items.VampireSwordItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class PedestalBlock extends VampirismBlockContainer {

    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    private static final VoxelShape SHAPE = Shapes.join(Block.box(1, 0, 1, 15, 2, 15), Block.box(2, 2, 2, 14, 10, 14), BooleanOp.OR);

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!player.getMainHandItem().isEmpty()) return InteractionResult.PASS;

        return getBlockEntity(level, pos)
                .map(pedestal -> {
                    ItemStack wasInside = pedestal.removeStack();
                    giveItemToPlayer(player, InteractionHand.MAIN_HAND, wasInside);
                    return (InteractionResult) InteractionResult.SUCCESS;
                })
                .orElse(InteractionResult.PASS);
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        return getBlockEntity(level, pos)
                .filter(pedestal -> stack.getItem() instanceof VampireSwordItem)
                .map(pedestal -> {
                    ItemStack wasInside = pedestal.setStack(stack);
                    giveItemToPlayer(player, hand, wasInside);
                    return (InteractionResult) InteractionResult.SUCCESS;
                })
                .orElse(InteractionResult.TRY_WITH_EMPTY_HAND);
    }

    private static void giveItemToPlayer(Player player, InteractionHand hand, ItemStack stack) {
        player.setItemInHand(hand, stack);
        player.awardStat(ModStats.ITEMS_FILLED_ON_BLOOD_PEDESTAL.get());

        if (stack.getItem() instanceof VampireSwordItem vampireSwordItem && vampireSwordItem.isFullyCharged(stack)) {
            vampireSwordItem.tryName(stack, player);
        }
    }

    @Override
    protected void clearContainer(BlockState state, Level level, BlockPos pos) {
        getBlockEntity(level, pos)
                .filter(PedestalBlockEntity::hasStack)
                .ifPresent(pedestal -> dropItem(level, pos, pedestal.removeStack()));
    }

    private Optional<PedestalBlockEntity> getBlockEntity(BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestalBlockEntity) {
            return Optional.of(pedestalBlockEntity);
        }
        return Optional.empty();
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BLOOD_PEDESTAL.get(), level.isClientSide() ? PedestalBlockEntity::clientTick : PedestalBlockEntity::serverTick);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return getBlockEntity(level, pos).map(PedestalBlockEntity::getChargeProgress).orElse(0);
    }
}
