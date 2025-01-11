package de.teamlapen.vampirism.blocks;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.effects.SanguinareEffect;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.inventory.RevertBackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.MenuConstructor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING;

/**
 * Block which represents the top and the bottom part of a "Medical Chair" used for injections
 */
public class MedChairBlock extends VampirismHorizontalBlock {
    public static final EnumProperty<EnumPart> PART = EnumProperty.create("part", EnumPart.class);
    private static final @NotNull VoxelShape SHAPE_TOP = box(2, 6, 0, 14, 16, 16);
    private static final @NotNull VoxelShape SHAPE_BOTTOM = box(1, 1, 0, 15, 10, 16);
    private final VoxelShape NORTH1;
    private final @NotNull VoxelShape EAST1;
    private final @NotNull VoxelShape SOUTH1;
    private final @NotNull VoxelShape WEST1;
    private final VoxelShape NORTH2;
    private final @NotNull VoxelShape EAST2;
    private final @NotNull VoxelShape SOUTH2;
    private final @NotNull VoxelShape WEST2;

    public MedChairBlock(BlockBehaviour.Properties properties) {
        super(properties.mapColor(MapColor.METAL).pushReaction(PushReaction.DESTROY).strength(1).noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(PART, EnumPart.BOTTOM));
        NORTH1 = SHAPE_BOTTOM;
        EAST1 = UtilLib.rotateShape(NORTH1, UtilLib.RotationAmount.NINETY);
        SOUTH1 = UtilLib.rotateShape(NORTH1, UtilLib.RotationAmount.HUNDRED_EIGHTY);
        WEST1 = UtilLib.rotateShape(NORTH1, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);
        NORTH2 = SHAPE_TOP;
        EAST2 = UtilLib.rotateShape(NORTH2, UtilLib.RotationAmount.NINETY);
        SOUTH2 = UtilLib.rotateShape(NORTH2, UtilLib.RotationAmount.HUNDRED_EIGHTY);
        WEST2 = UtilLib.rotateShape(NORTH2, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);
    }

    @Override
    public InteractionResult useItemOn(ItemStack stack, @NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Player player, @NotNull InteractionHand hand, @NotNull BlockHitResult hit) {
        if (player.isAlive()) {
            if (handleInjections(player, world, stack, pos)) {
                player.awardStat(ModStats.INTERACT_WITH_INJECTION_CHAIR.get());
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().removeItem(stack);
                }
            }
        } else if (world.isClientSide) {
            player.displayClientMessage(Component.translatable("text.vampirism.need_item_to_use", Component.translatable(ModItems.INJECTION_GARLIC.get().getDescriptionId())), true);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private boolean handleGarlicInjection(@NotNull Player player, @NotNull Level world, @NotNull IFactionPlayerHandler handler, @Nullable Holder<? extends IPlayableFaction<?>> currentFaction) {
        if (handler.canJoin(ModFactions.HUNTER)) {
            if (world.isClientSide) {
                VampirismModClient.getINSTANCE().getOverlay().makeRenderFullColor(4, 30, 0xBBBBBBFF);
            } else {
                handler.joinFaction(ModFactions.HUNTER);
                player.addEffect(new MobEffectInstance(ModEffects.POISON, 200, 1));
            }
            return true;
        } else if (currentFaction != null) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.translatable("text.vampirism.med_chair_other_faction", currentFaction.value().getName()));
            }
        }
        return false;
    }

    private boolean handleInjections(@NotNull Player player, @NotNull Level world, @NotNull ItemStack stack, @NotNull BlockPos pos) {
        FactionPlayerHandler handler = FactionPlayerHandler.get(player);
        Holder<? extends IPlayableFaction<?>> faction = handler.getFaction();

        if (stack.getItem().equals(ModItems.INJECTION_GARLIC.get())) {
            return handleGarlicInjection(player, world, handler, faction);
        }
        if (stack.getItem().equals(ModItems.INJECTION_SANGUINARE.get())) {
            return handleSanguinareInjection(world, pos, player, handler, faction);
        }
        return false;
    }

    private boolean handleSanguinareInjection(@NotNull Level level, @NotNull BlockPos pos, @NotNull Player player, @NotNull IFactionPlayerHandler handler, @Nullable Holder<? extends IPlayableFaction<?>> currentFaction) {
        if (IFaction.is(ModFactions.VAMPIRE, currentFaction)) {
            player.displayClientMessage(Component.translatable("text.vampirism.already_vampire"), false);
            return false;
        }
        if (ModFactions.HUNTER.match(currentFaction)) {
            if (!level.isClientSide) {
                player.openMenu(new SimpleMenuProvider(new MenuConstructor() {
                    @Override
                    public @NotNull AbstractContainerMenu createMenu(int i, @NotNull Inventory inventory, @NotNull Player player) {
                        return new RevertBackMenu(i, inventory, ContainerLevelAccess.create(level, pos));
                    }
                }, Component.empty()));
            }
            return false;
        }
        if (currentFaction == null) {
            if (handler.canJoin(ModFactions.VAMPIRE)) {
                if (VampirismConfig.SERVER.disableFangInfection.get()) {
                    player.displayClientMessage(Component.translatable("text.vampirism.deactivated_by_serveradmin"), true);
                } else {
                    SanguinareEffect.addRandom(player, true, true);
                    player.addEffect(new MobEffectInstance(ModEffects.POISON, 60));
                    return true;
                }
            }
        }
        return false;
    }

    public enum EnumPart implements StringRepresentable {
        TOP("top", 0), BOTTOM("bottom", 1);

        public static @NotNull EnumPart fromMeta(int meta) {
            if (meta == 1) {
                return BOTTOM;
            }
            return TOP;
        }

        public final String name;
        public final int meta;

        EnumPart(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public @NotNull String toString() {
            return getSerializedName();
        }


    }

    public @NotNull RenderShape getRenderShape(@NotNull BlockState p_149645_1_) {
        return RenderShape.MODEL;
    }


    @Override
    public @NotNull VoxelShape getShape(@NotNull BlockState state, @NotNull BlockGetter worldIn, @NotNull BlockPos pos, @NotNull CollisionContext context) {
        boolean main = state.getValue(PART) == EnumPart.BOTTOM;
        return switch (state.getValue(FACING)) {
            case NORTH -> main ? NORTH1 : NORTH2;
            case EAST -> main ? EAST1 : EAST2;
            case SOUTH -> main ? SOUTH1 : SOUTH2;
            case WEST -> main ? WEST1 : WEST2;
            default -> NORTH1;
        };
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(@NotNull BlockPlaceContext context) {
        Direction enumfacing = context.getHorizontalDirection();
        BlockPos blockpos = context.getClickedPos();
        BlockPos blockpos1 = blockpos.relative(enumfacing);
        return context.getLevel().getBlockState(blockpos1).canBeReplaced(context) ? this.defaultBlockState().setValue(HORIZONTAL_FACING, enumfacing.getOpposite()) : null;
    }

    @Override
    protected boolean isPathfindable(BlockState p_60475_, PathComputationType p_60478_) {
        return false;
    }

    @Override
    public BlockState playerWillDestroy(@NotNull Level world, @NotNull BlockPos blockPos, @NotNull BlockState blockState, @NotNull Player player) {
        if (!world.isClientSide && player.isCreative()) {
            EnumPart part = blockState.getValue(PART);
            if (part == EnumPart.TOP) {
                BlockPos blockpos = blockPos.relative(getOtherBlockDirection(blockState));
                BlockState otherState = world.getBlockState(blockpos);
                if (otherState.getBlock() == this && otherState.getValue(PART) == EnumPart.BOTTOM) {
                    world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    world.levelEvent(player, 2001, blockpos, Block.getId(otherState));
                }
            }
        }

        return super.playerWillDestroy(world, blockPos, blockState, player);
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide) {
            BlockPos blockpos = pos.relative(getOtherBlockDirection(state));
            BlockState otherState = state.setValue(PART, EnumPart.TOP);
            otherState = otherState.setValue(FACING, otherState.getValue(FACING));
            world.setBlock(blockpos, otherState, 3);
            world.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(world, pos, 3);
        }

    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos, Direction direction, BlockPos pos1, BlockState state1, RandomSource randomSource) {
        if (direction == getOtherBlockDirection(state)) {
            return state1.getBlock() == this && state1.getValue(PART) != state.getValue(PART) ? updateFromOther(state, state1) : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, level, ticks, pos, direction, pos1, state1, randomSource);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.@NotNull Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    protected @NotNull Direction getOtherBlockDirection(@NotNull BlockState blockState) {
        return blockState.getValue(PART) == EnumPart.BOTTOM ? blockState.getValue(FACING).getOpposite() : blockState.getValue(FACING);
    }

    protected BlockState updateFromOther(BlockState thisState, BlockState otherState) {
        return thisState;
    }
}
