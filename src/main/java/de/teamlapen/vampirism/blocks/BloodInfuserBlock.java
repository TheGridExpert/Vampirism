package de.teamlapen.vampirism.blocks;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.blockentity.InfuserBlockEntity;
import de.teamlapen.vampirism.core.ModTiles;
import de.teamlapen.vampirism.util.BlockVoxelshapes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BloodInfuserBlock extends BaseEntityBlock {

    public static final MapCodec<BloodInfuserBlock> CODEC = simpleCodec(BloodInfuserBlock::new);
    public static final BooleanProperty IS_ACTIVE = BooleanProperty.create("active");

    public BloodInfuserBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(IS_ACTIVE, false));
    }

    @Override
    protected @NotNull MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        return new InfuserBlockEntity(pos, state);
    }

    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, @NotNull BlockState state, @NotNull BlockEntityType<T> type){
        return level.isClientSide() ? null : createTickerHelper(type, ModTiles.INFUSER.get(), InfuserBlockEntity::serverTick);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            openContainer(level, pos, player);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BlockVoxelshapes.BLOOD_INFUSER;
    }

    protected void openContainer(Level level, BlockPos pos, Player player) {
        level.getBlockEntity(pos, ModTiles.INFUSER.get()).ifPresent(blockEntity -> {
            player.openMenu(blockEntity);
        });
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IS_ACTIVE);
    }
}
