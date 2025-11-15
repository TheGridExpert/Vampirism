package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.entity.IBiteableEntity;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.ModSounds;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.Optional;

public class SyringeItem extends Item implements IEntityInteractable {

    public SyringeItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onEntityInteract(ItemStack stack, Entity target, Player player, Level level, InteractionHand hand) {
        Optional<ExtendedCreature> extendedCreatureOpt = ExtendedCreature.getSafe(target);
        if (extendedCreatureOpt.isPresent() && extendedCreatureOpt.get().hasPoisonousBlood()) {
            player.displayClientMessage(Component.translatable("text.vampirism.syringe.poisonous_blood"), true);
            return InteractionResult.CONSUME;
        }

        if (level.isClientSide) return InteractionResult.CONSUME;

        Optional<? extends IBiteableEntity> biteableOpt = switch (target) {
            case PathfinderMob mob when mob.isAlive() -> ExtendedCreature.getSafe(mob);
            case Player targetPlayer -> Optional.of(VampirePlayer.get(targetPlayer));
            case IBiteableEntity biteableEntity -> Optional.of(biteableEntity);
            default -> Optional.empty();
        };

        return biteableOpt.filter(biteable -> biteable.canBeBitten(null)).map(biteable -> {
            int drained = biteable.onSyringeUse(BloodSyringeFluidHandler.LEVELS_PER_FILL);
            if (drained <= 0) return InteractionResult.CONSUME;

            ItemStack filledStack = new ItemStack(ModItems.SYRINGE_BLOOD.get());

            if (!player.isCreative()) {
                if (stack.getCount() == 1) {
                    player.setItemInHand(hand, filledStack);
                } else {
                    stack.shrink(1);
                    ItemHandlerHelper.giveItemToPlayer(player, filledStack);
                }
            } else {
                ItemHandlerHelper.giveItemToPlayer(player, filledStack);
            }

            level.playSound(null, player.blockPosition(), ModSounds.VAMPIRE_BITE.get(), SoundSource.PLAYERS, 1.0f,  1.0f);

            return InteractionResult.SUCCESS_SERVER;
        }).orElse(InteractionResult.CONSUME);
    }
}
