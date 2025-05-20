package de.teamlapen.vampirism.blocks;

import com.mojang.serialization.MapCodec;
import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.blockentity.TombstoneBlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.contents.PlainTextContents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

public class TombstoneBlock extends BaseEntityBlock {

    public static final MapCodec<TombstoneBlock> CODEC = simpleCodec(TombstoneBlock::new);

    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    private final Map<Direction, VoxelShape> shapes;
    private final int textColor;
    private final int highlightColor;

    public TombstoneBlock(Properties properties, int textColor, int highlightColor, VoxelShape shape) {
        super(properties);
        this.textColor = textColor;
        this.highlightColor = highlightColor;
        this.shapes = UtilLib.getShapesRotatedFromNorth(shape);

        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    public TombstoneBlock(Properties properties) {
        this(properties, 0x545454, 0xa3a3a3, Shapes.block());
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public int getTextColor() {
        return this.textColor;
    }

    public int getHighlightColor() {
        return this.highlightColor;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TombstoneBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof TombstoneBlockEntity blockEntity) {
            if (level.isClientSide) {
                Util.pauseInIde(new IllegalStateException("Expected to only call this on server"));
            }

            boolean executedCommand = blockEntity.executeClickCommandsIfPresent(player, level, pos, true);
            if (blockEntity.isWaxed()) {
                level.playSound(null, blockEntity.getBlockPos(), blockEntity.getSignInteractionFailedSoundEvent(), SoundSource.BLOCKS);
                return InteractionResult.SUCCESS_SERVER;
            } else if (executedCommand) {
                return InteractionResult.SUCCESS_SERVER;
            } else if (!this.otherPlayerIsEditingSign(player, blockEntity) && player.mayBuild() && this.hasEditableText(player, blockEntity)) {
                this.openTextEdit(player, blockEntity);
                return InteractionResult.SUCCESS_SERVER;
            } else {
                return InteractionResult.PASS;
            }
        } else {
            return InteractionResult.PASS;
        }
    }

    private boolean hasEditableText(Player player, TombstoneBlockEntity blockEntity) {
        SignText text = blockEntity.getText(true);
        return Arrays.stream(text.getMessages(player.isTextFilteringEnabled()))
                .allMatch(line -> line.equals(CommonComponents.EMPTY) || line.getContents() instanceof PlainTextContents);
    }

    private boolean otherPlayerIsEditingSign(Player player, TombstoneBlockEntity blockEntity) {
        UUID uuid = blockEntity.getPlayerWhoMayEdit();
        return uuid != null && !uuid.equals(player.getUUID());
    }

    public void openTextEdit(Player player, TombstoneBlockEntity blockEntity) {
        blockEntity.setAllowedPlayerEditor(player.getUUID());
        player.openTextEdit(blockEntity, true);
    }

    public float getYRotationDegrees(BlockState state) {
        return state.getValue(FACING).toYRot();
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return Block.canSupportCenter(level, pos.above(), Direction.DOWN);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
