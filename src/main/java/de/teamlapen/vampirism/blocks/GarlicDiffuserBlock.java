package de.teamlapen.vampirism.blocks;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.blockentity.GarlicDiffuserBlockEntity;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.core.ModTiles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GarlicDiffuserBlock extends VampirismBlockContainer {
    public static final MapCodec<GarlicDiffuserBlock> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    StringRepresentable.fromEnum(Type::values).fieldOf("type").forGetter(p -> p.type),
                    propertiesCodec()
            ).apply(inst, GarlicDiffuserBlock::new)
    );

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private static final VoxelShape shape = makeShape();

    private static VoxelShape makeShape() {
        VoxelShape a = Block.box(1, 0, 1, 15, 2, 15);
        VoxelShape b = Block.box(3, 2, 3, 13, 12, 13);
        return Shapes.or(a, b);
    }

    private final Type type;

    public GarlicDiffuserBlock(Type type) {
        this(type, Properties.of().mapColor(MapColor.STONE).strength(40.0F, 1200.0F).sound(SoundType.STONE).noOcclusion());
    }

    public GarlicDiffuserBlock(Type type, BlockBehaviour.Properties properties) {
        super(properties);
        this.type = type;
        this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag advanced) {
        if (type == Type.WEAK || type == Type.IMPROVED) {
            tooltip.add(Component.translatable(getDescriptionId() + "." + type.getSerializedName()).withStyle(ChatFormatting.AQUA));
        }

        tooltip.add(Component.translatable("block.vampirism.garlic_diffuser.tooltip1").withStyle(ChatFormatting.GRAY));
        int c = VampirismConfig.BALANCE.hsGarlicDiffuserEnhancedDist == null /* During game start config is not yet set*/ ? 1 : 1 + 2 * (type == Type.IMPROVED ? VampirismConfig.BALANCE.hsGarlicDiffuserEnhancedDist.get() : (type == Type.WEAK ? VampirismConfig.BALANCE.hsGarlicDiffuserWeakDist.get() : VampirismConfig.BALANCE.hsGarlicDiffuserNormalDist.get()));
        tooltip.add(Component.translatable("block.vampirism.garlic_diffuser.tooltip2", c, c).withStyle(ChatFormatting.GRAY));
    }

    @Override
    public void attack(BlockState state, Level worldIn, BlockPos pos, Player playerIn) {
        GarlicDiffuserBlockEntity tile = getTile(worldIn, pos);
        if (tile != null) {
            tile.onTouched(playerIn);
        }
    }

    @Override
    public String getDescriptionId() {
        return "block.vampirism.garlic_diffuser";
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
        return shape;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        GarlicDiffuserBlockEntity tile = new GarlicDiffuserBlockEntity(pos, state);
        tile.setType(type);
        tile.initiateBootTimer();
        return tile;
    }

    @Override
    public void playerDestroy(Level worldIn, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity te, ItemStack stack) {
        super.playerDestroy(worldIn, player, pos, state, te, stack);
        if (te instanceof GarlicDiffuserBlockEntity) {
            ((GarlicDiffuserBlockEntity) te).onTouched(player);
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level world, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.isEmpty() && ModItems.PURIFIED_GARLIC.get() == stack.getItem()) {
            if (!world.isClientSide) {
                player.awardStat(ModStats.INTERACT_WITH_GARLIC_DIFFUSER.get());
                GarlicDiffuserBlockEntity t = getTile(world, pos);
                if (t != null) {
                    if (t.getFuelTime() > 0) {
                        player.sendSystemMessage(Component.translatable("block.vampirism.garlic_diffuser.already_fueled"));
                    } else {
                        t.onFueled();
                        if (!player.isCreative()) stack.shrink(1);
                        player.sendSystemMessage(Component.translatable("block.vampirism.garlic_diffuser.successfully_fueled"));
                    }

                }
            }
            return ItemInteractionResult.sidedSuccess(world.isClientSide);
        }
        return super.useItemOn(stack, state, world, pos, player, hand, hit);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide) {
            GarlicDiffuserBlockEntity t = getTile(world, pos);
            if (t != null) {
                VampirismMod.proxy.displayGarlicBeaconScreen(t, getName());
            }
        }
        return InteractionResult.sidedSuccess(world.isClientSide);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    private GarlicDiffuserBlockEntity getTile(BlockGetter world, BlockPos pos) {
        BlockEntity t = world.getBlockEntity(pos);
        if (t instanceof GarlicDiffuserBlockEntity) {
            return (GarlicDiffuserBlockEntity) t;
        }
        return null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModTiles.GARLIC_DIFFUSER.get(), GarlicDiffuserBlockEntity::tick);
    }

    public enum Type implements StringRepresentable {
        NORMAL("normal"),
        IMPROVED("improved"),
        WEAK("weak");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return this.name;
        }
    }
}
