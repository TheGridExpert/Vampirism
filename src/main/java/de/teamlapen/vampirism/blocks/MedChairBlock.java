package de.teamlapen.vampirism.blocks;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.effects.SanguinareEffect;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.inventory.RevertBackMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Block which represents the top and the bottom part of a "Medical Chair" used for injections
 */
public class MedChairBlock extends VampirismHorizontalBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<ChairPart> PART = EnumProperty.create("part", ChairPart.class);

    private static final VoxelShape SHAPE_TOP = box(2, 6, 0, 14, 16, 16);
    private static final VoxelShape SHAPE_BOTTOM = box(1, 1, 0, 15, 10, 16);

    private final VoxelShape NORTH1;
    private final VoxelShape EAST1;
    private final VoxelShape SOUTH1;
    private final VoxelShape WEST1;
    private final VoxelShape NORTH2;
    private final VoxelShape EAST2;
    private final VoxelShape SOUTH2;
    private final VoxelShape WEST2;

    public MedChairBlock() {
        super(Properties.of().mapColor(MapColor.METAL).pushReaction(PushReaction.DESTROY).strength(1).noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(PART, ChairPart.BOTTOM));
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
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isAlive()) {
            if (handleInjections(player, world, stack, pos)) {
                player.awardStat(ModStats.INTERACT_WITH_INJECTION_CHAIR.get());
                stack.shrink(1);
                if (stack.isEmpty()) {
                    player.getInventory().removeItem(stack);
                }
            }
        } else if (world.isClientSide) {
            player.displayClientMessage(Component.translatable("text.vampirism.need_item_to_use", Component.translatable((new ItemStack(ModItems.INJECTION_GARLIC.get()).getDescriptionId()))), true);
        }
        return ItemInteractionResult.sidedSuccess(world.isClientSide);
    }

    private boolean handleGarlicInjection(Player player, Level world, IFactionPlayerHandler handler, @Nullable IPlayableFaction<?> currentFaction) {
        if (handler.canJoin(VReference.HUNTER_FACTION)) {
            if (world.isClientSide) {
                VampirismModClient.getINSTANCE().getOverlay().makeRenderFullColor(4, 30, 0xBBBBBBFF);
            } else {
                handler.joinFaction(VReference.HUNTER_FACTION);
                player.addEffect(new MobEffectInstance(ModEffects.POISON, 200, 1));
            }
            return true;
        } else if (currentFaction != null) {
            if (!world.isClientSide) {
                player.sendSystemMessage(Component.translatable("text.vampirism.med_chair_other_faction", currentFaction.getName()));
            }
        }
        return false;
    }

    private boolean handleInjections(Player player, Level world, ItemStack stack, BlockPos pos) {
        FactionPlayerHandler handler = FactionPlayerHandler.get(player);
        IPlayableFaction<?> faction = handler.getCurrentFaction();

        if (stack.getItem().equals(ModItems.INJECTION_GARLIC.get())) {
            return handleGarlicInjection(player, world, handler, faction);
        }
        if (stack.getItem().equals(ModItems.INJECTION_SANGUINARE.get())) {
            return handleSanguinareInjection(world, pos, player, handler, faction);
        }
        return false;
    }

    private boolean handleSanguinareInjection(Level level, BlockPos pos, Player player, IFactionPlayerHandler handler, @Nullable IPlayableFaction<?> currentFaction) {
        if (VReference.VAMPIRE_FACTION.equals(currentFaction)) {
            player.displayClientMessage(Component.translatable("text.vampirism.already_vampire"), false);
            return false;
        }
        if (VReference.HUNTER_FACTION.equals(currentFaction)) {
            if (!level.isClientSide) {
                player.openMenu(new SimpleMenuProvider((i, inventory, player1) -> new RevertBackMenu(i, inventory, ContainerLevelAccess.create(level, pos)), Component.empty()));
            }
            return false;
        }
        if (currentFaction == null) {
            if (handler.canJoin(VReference.VAMPIRE_FACTION)) {
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

    public enum ChairPart implements StringRepresentable {
        TOP("top", 0),
        BOTTOM("bottom", 1);

        public final String name;
        public final int meta;

        ChairPart(String name, int meta) {
            this.name = name;
            this.meta = meta;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        @Override
        public String toString() {
            return getSerializedName();
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        boolean main = state.getValue(PART) == ChairPart.BOTTOM;
        return switch (state.getValue(FACING)) {
            case NORTH -> main ? NORTH1 : NORTH2;
            case EAST -> main ? EAST1 : EAST2;
            case SOUTH -> main ? SOUTH1 : SOUTH2;
            case WEST -> main ? WEST1 : WEST2;
            default -> NORTH1;
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection();
        BlockPos blockPos = context.getClickedPos();
        BlockPos blockPos1 = blockPos.relative(direction);
        return context.getLevel().getBlockState(blockPos1).canBeReplaced(context) ? this.defaultBlockState().setValue(FACING, direction.getOpposite()) : null;
    }

    @Override
    protected boolean isPathfindable(BlockState p_60475_, PathComputationType p_60478_) {
        return false;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos blockPos, BlockState blockState, Player player) {
        if (!world.isClientSide && player.isCreative()) {
            ChairPart part = blockState.getValue(PART);
            if (part == ChairPart.TOP) {
                BlockPos blockpos = blockPos.relative(getOtherBlockDirection(blockState));
                BlockState otherState = world.getBlockState(blockpos);
                if (otherState.getBlock() == this && otherState.getValue(PART) == ChairPart.BOTTOM) {
                    world.setBlock(blockpos, Blocks.AIR.defaultBlockState(), 35);
                    world.levelEvent(player, 2001, blockpos, Block.getId(otherState));
                }
            }
        }

        return super.playerWillDestroy(world, blockPos, blockState, player);
    }

    @Override
    public void setPlacedBy(Level world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        if (!world.isClientSide) {
            BlockPos blockpos = pos.relative(getOtherBlockDirection(state));
            BlockState otherState = state.setValue(PART, ChairPart.TOP);
            otherState = otherState.setValue(FACING, otherState.getValue(FACING));
            world.setBlock(blockpos, otherState, 3);
            world.blockUpdated(pos, Blocks.AIR);
            state.updateNeighbourShapes(world, pos, 3);
        }

    }

    @Override
    public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
        if (facing == getOtherBlockDirection(stateIn)) {
            return facingState.getBlock() == this && facingState.getValue(PART) != stateIn.getValue(PART) ? updateFromOther(stateIn, facingState) : Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
        }
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(PART);
    }

    protected Direction getOtherBlockDirection(BlockState blockState) {
        return blockState.getValue(PART) == ChairPart.BOTTOM ? blockState.getValue(FACING).getOpposite() : blockState.getValue(FACING);
    }

    protected BlockState updateFromOther(BlockState thisState, BlockState otherState) {
        return thisState;
    }
}
