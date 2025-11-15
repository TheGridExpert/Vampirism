package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.blockentity.BatCageBlockEntity;
import de.teamlapen.vampirism.core.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BatCageItem extends BlockItem implements IEntityInteractable {

    public BatCageItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (stack.has(ModDataComponents.HELD_ENTITY) && (player == null || player.isShiftKeyDown())) {
            if (releaseEntity(stack, context.getLevel(), context.getClickLocation().add(0.0, 0.3, 0.0), context.getHorizontalDirection(), player)) {
                return InteractionResult.SUCCESS_SERVER;
            }
        } else {
            return super.useOn(context);
        }
        return InteractionResult.PASS;
    }

    public boolean releaseEntity(ItemStack stack, Level level, Vec3 pos, Direction direction, @Nullable Player player) {
        CompoundTag entityTag = stack.get(ModDataComponents.HELD_ENTITY);
        if (entityTag == null || level.isClientSide) return false;

        entityTag = sanitizeEntityTag(entityTag);

        EntityType<?> type = EntityType.by(entityTag).orElse(null);
        if (type == null) return false;

        Entity entity = type.create(level, EntitySpawnReason.BUCKET);
        if (entity != null) {
            entity.load(entityTag);
            Quaternionf quaternionf = direction.getRotation();
            entity.moveTo(pos, quaternionf.y(), quaternionf.x());
            level.addFreshEntity(entity);
            if (player == null || !player.getAbilities().instabuild) {
                stack.remove(ModDataComponents.HELD_ENTITY);
            }

            return true;
        }

        return false;
    }

    @Override
    public InteractionResult onEntityInteract(ItemStack stack, Entity target, Player player, Level level, InteractionHand hand) {
        if (!level.isClientSide && BatCageBlockEntity.canContainEntity(target) && !stack.has(ModDataComponents.HELD_ENTITY)) {
            ItemStack capturedStack = stack.copyWithCount(1);
            if (BatCageItem.captureEntity(target, capturedStack)) {
                if (!player.getAbilities().instabuild) {
                    stack.shrink(1);
                }
                ItemHandlerHelper.giveItemToPlayer(player, capturedStack);
            }

            return InteractionResult.SUCCESS_SERVER;
        }

        return InteractionResult.PASS;
    }

    public static boolean captureEntity(Entity entity, ItemStack stack) {
        if (entity.level().isClientSide || stack.has(ModDataComponents.HELD_ENTITY)) return false;

        CompoundTag tag = new CompoundTag();
        if (entity.saveAsPassenger(tag)) {
            entity.remove(Entity.RemovalReason.DISCARDED);
            stack.set(ModDataComponents.HELD_ENTITY, sanitizeEntityTag(tag));
            return true;
        }

        return false;
    }

    public static CompoundTag sanitizeEntityTag(CompoundTag tag) {
        tag = tag.copy();
        Stream.of("UUID", "Pos", "Motion", "Rotation").forEach(tag::remove);
        return tag;
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return stack.has(ModDataComponents.HELD_ENTITY) ? 1 : super.getMaxStackSize(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        CompoundTag entityTag =  stack.get(ModDataComponents.HELD_ENTITY);
        if (entityTag != null) {
            Component name = Component.translatable("text.vampirism.unknown");
            if (entityTag.contains("CustomName")) {
                name = Component.literal(entityTag.getString("CustomName").replace("\"", ""));
            } else {
                Optional<EntityType<?>> entityTypeOpt = EntityType.by(entityTag);
                if (entityTypeOpt.isPresent()) {
                    name = entityTypeOpt.get().getDescription();
                }
            }
            tooltipComponents.add(Component.translatable("tooltip.vampirism.bat_cage.contains_bat", name.copy().withStyle(ChatFormatting.GRAY)).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
