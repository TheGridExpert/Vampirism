package de.teamlapen.vampirism.blocks;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.blockentity.VampireBeaconBlockEntity;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.core.ModTiles;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BeaconBeamBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class VampireBeaconBlock extends VampirismBlockContainer implements BeaconBeamBlock {
    public static final MapCodec<VampireBeaconBlock> CODEC = simpleCodec(VampireBeaconBlock::new);

    public VampireBeaconBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    @SuppressWarnings("deprecation")
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public DyeColor getColor() {
        return DyeColor.RED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new VampireBeaconBlockEntity(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ModTiles.VAMPIRE_BEACON.get(), VampireBeaconBlockEntity::tick);
    }

    @Override
    public InteractionResult useWithoutItem(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, BlockHitResult pHit) {
        if (!pLevel.isClientSide) {
            if (pPlayer instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(ModStats.INTERACT_WITH_ANCIENT_BEACON.get());
                if (Helper.isHunter(serverPlayer)) {
                    if (pLevel.getBlockEntity(pPos) instanceof VampireBeaconBlockEntity blockentity) {
                        pPlayer.openMenu(blockentity);
                    }
                } else {
                    pPlayer.displayClientMessage(Component.translatable("text.vampirism.unfamiliar"), true);
                }
                return InteractionResult.CONSUME;
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void setPlacedBy(Level pLevel, BlockPos pPos, BlockState pState, @Nullable LivingEntity pPlacer, ItemStack pStack) {
        if (pStack.has(DataComponents.CUSTOM_NAME) && pLevel.getBlockEntity(pPos) instanceof VampireBeaconBlockEntity blockEntity) {
            blockEntity.setCustomName(pStack.getHoverName());
        }
    }
}
