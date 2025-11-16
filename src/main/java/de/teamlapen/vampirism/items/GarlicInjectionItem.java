package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.entity.factions.IFactionPlayerHandler;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.ModSounds;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Nullable;

public class GarlicInjectionItem extends InjectionItem implements IEntityInteractable {

    public GarlicInjectionItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean handleInjection(Level level, BlockPos pos, Player player, IFactionPlayerHandler handler, @Nullable Holder<? extends IPlayableFaction<?>> currentFaction) {
        if (handler.canJoin(ModFactions.HUNTER)) {
            if (level.isClientSide) {
                VampirismModClient.getInstance().getOverlay().makeRenderFullColor(4, 30, 0xBBBBBBFF);
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

    @Override
    public InteractionResult onEntityInteract(ItemStack stack, Entity target, Player player, Level level, InteractionHand hand) {
        if (level.isClientSide) return InteractionResult.CONSUME;

        return ExtendedCreature.getFromEntity(target).map(entity -> {
            if (entity.hasPoisonousBlood()) return InteractionResult.CONSUME;

            entity.setPoisonousBlood(ExtendedCreature.POISONOUS_BLOOD_DOSE_DURATION);

            if (!player.isCreative()) {
                if (stack.getCount() == 1) {
                    player.setItemInHand(hand, stack.getCraftingRemainder());
                } else {
                    stack.shrink(1);
                    ItemHandlerHelper.giveItemToPlayer(player, stack.getCraftingRemainder());
                }
            }

            // TODO: Find some other sound for vaccinating mobs
            level.playSound(null, player.blockPosition(), ModSounds.VAMPIRE_BITE.get(), SoundSource.PLAYERS, 1.0f,  1.0f);

            return InteractionResult.SUCCESS_SERVER;
        }).orElse(InteractionResult.CONSUME);
    }
}
