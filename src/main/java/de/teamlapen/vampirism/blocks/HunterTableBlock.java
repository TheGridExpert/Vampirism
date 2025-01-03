package de.teamlapen.vampirism.blocks;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.inventory.HunterTableMenu;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Table for creating Hunter Intels used for leveling as a hunter.
 */
public class HunterTableBlock extends VampirismHorizontalBlock {
    public static final EnumProperty<TableVariant> VARIANT = EnumProperty.create("variant", TableVariant.class);

    private static final VoxelShape NORTH = makeShape();
    private static final VoxelShape NORTH_HAMMER = Shapes.or(makeShape(), makeHammerShape());
    private static final VoxelShape EAST = UtilLib.rotateShape(NORTH, UtilLib.RotationAmount.NINETY);
    private static final VoxelShape EAST_HAMMER = UtilLib.rotateShape(NORTH_HAMMER, UtilLib.RotationAmount.NINETY);
    private static final VoxelShape SOUTH = UtilLib.rotateShape(NORTH, UtilLib.RotationAmount.HUNDRED_EIGHTY);
    private static final VoxelShape SOUTH_HAMMER = UtilLib.rotateShape(NORTH_HAMMER, UtilLib.RotationAmount.HUNDRED_EIGHTY);
    private static final VoxelShape WEST = UtilLib.rotateShape(NORTH, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);
    private static final VoxelShape WEST_HAMMER = UtilLib.rotateShape(NORTH_HAMMER, UtilLib.RotationAmount.TWO_HUNDRED_SEVENTY);

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0.125, 0.625, 0.375, 0.5, 0.6875, 0.875));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.75, 0.375, 0.5, 0.8125, 0.875));
        shape = Shapes.or(shape, Shapes.box(0.125, 0.6875, 0.375, 0.1875, 0.75, 0.875));
        shape = Shapes.or(shape, Shapes.box(0.1875, 0.6875, 0.40625, 0.46875, 0.75, 0.84375));
        shape = Shapes.or(shape, Shapes.box(0, 0.5, 0, 1, 0.625, 1));
        shape = Shapes.or(shape, Shapes.box(0.0625, 0, 0.75, 0.25, 0.5, 0.9375));
        shape = Shapes.or(shape, Shapes.box(0.75, 0, 0.75, 0.9375, 0.5, 0.9375));
        shape = Shapes.or(shape, Shapes.box(0.75, 0, 0.0625, 0.9375, 0.5, 0.25));
        shape = Shapes.or(shape, Shapes.box(0.0625, 0, 0.0625, 0.25, 0.5, 0.25));

        return shape;
    }

    private static VoxelShape makeHammerShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Shapes.box(0.4375, 0, 0.5625, 0.75, 0.1875, 0.75));
        shape = Shapes.or(shape, Shapes.box(0.5625, 0.1875, 0.625, 0.625, 0.624375, 0.6875));

        return shape;
    }

    public static HunterTableBlock.TableVariant getTierFor(boolean weapon_table, boolean potion_table, boolean cauldron) {
        return weapon_table ? (potion_table ? (cauldron ? TableVariant.COMPLETE : TableVariant.WEAPON_POTION) : (cauldron ? TableVariant.WEAPON_CAULDRON : TableVariant.WEAPON)) : (potion_table ? (cauldron ? TableVariant.POTION_CAULDRON : TableVariant.POTION) : (cauldron ? TableVariant.CAULDRON : TableVariant.SIMPLE));
    }

    public HunterTableBlock() {
        super(Properties.of().mapColor(MapColor.WOOD).strength(0.5f).ignitedByLava().noOcclusion());
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(VARIANT, TableVariant.SIMPLE));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(VARIANT)) {
            default -> switch (state.getValue(FACING)) {
                case EAST -> EAST;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                default -> NORTH;
            };
            case WEAPON_CAULDRON, WEAPON_POTION, COMPLETE, WEAPON -> switch (state.getValue(FACING)) {
                case EAST -> EAST_HAMMER;
                case SOUTH -> SOUTH_HAMMER;
                case WEST -> WEST_HAMMER;
                default -> NORTH_HAMMER;
            };
        };
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        return this.defaultBlockState().setValue(FACING, facing).setValue(VARIANT, determineTier(context.getLevel(), context.getClickedPos(), facing));
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if (fromPos.getY() != pos.getY()) return;
        TableVariant newVariant = determineTier(worldIn, pos, state.getValue(FACING));
        if (newVariant != state.getValue(VARIANT)) {
            worldIn.setBlock(pos, state.setValue(VARIANT, newVariant), 2);
        }
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        if (!worldIn.isClientSide) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.awardStat(ModStats.INTERACT_WITH_RESEARCH_TABLE.get());
                if (Helper.isHunter(serverPlayer)) {
                    player.openMenu(new SimpleMenuProvider((id, playerInventory, playerIn) -> new HunterTableMenu(id, playerInventory, ContainerLevelAccess.create(playerIn.level(), pos)), Component.translatable("container.crafting")), pos);
                } else {
                    player.displayClientMessage(Component.translatable("text.vampirism.unfamiliar"), true);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, VARIANT);
    }

    protected TableVariant determineTier(LevelReader world, BlockPos pos, Direction facing) {
        Block behind = world.getBlockState(pos.relative(facing)).getBlock();
        Block left = world.getBlockState(pos.relative(facing.getClockWise())).getBlock();
        Block right = world.getBlockState(pos.relative(facing.getCounterClockWise())).getBlock();
        Block front = world.getBlockState(pos.relative(facing.getOpposite())).getBlock();
        boolean weapon_table = left == ModBlocks.WEAPON_TABLE.get() || right == ModBlocks.WEAPON_TABLE.get() || behind == ModBlocks.WEAPON_TABLE.get() || front == ModBlocks.WEAPON_TABLE.get();
        boolean potion_table = left == ModBlocks.POTION_TABLE.get() || right == ModBlocks.POTION_TABLE.get() || behind == ModBlocks.POTION_TABLE.get() || front == ModBlocks.POTION_TABLE.get();
        boolean cauldron = left == ModBlocks.ALCHEMICAL_CAULDRON.get() || right == ModBlocks.ALCHEMICAL_CAULDRON.get() || behind == ModBlocks.ALCHEMICAL_CAULDRON.get() || front == ModBlocks.ALCHEMICAL_CAULDRON.get();

        return getTierFor(weapon_table, potion_table, cauldron);
    }

    public enum TableVariant implements StringRepresentable {
        SIMPLE("simple", 0), WEAPON("weapon", 1), CAULDRON("cauldron", 1), POTION("potion", 1), WEAPON_CAULDRON("weapon_cauldron", 2), WEAPON_POTION("weapon_potion", 2), POTION_CAULDRON("potion_cauldron", 2), COMPLETE("complete", 3);
        public final String name;
        public final int tier;

        TableVariant(String n, int tier) {
            this.name = n;
            this.tier = tier;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
