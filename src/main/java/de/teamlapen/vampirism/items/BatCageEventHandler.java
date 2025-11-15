package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.blockentity.BatCageBlockEntity;
import de.teamlapen.vampirism.core.ModDataComponents;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = REFERENCE.MODID)
public class BatCageEventHandler {

    @SubscribeEvent
    public static void onCageMobInteraction(PlayerInteractEvent.EntityInteract event) {
        Level level = event.getLevel();

        Player player = event.getEntity();
        ItemStack heldStack = event.getItemStack();
        Entity target = event.getTarget();

        if (level.isClientSide) return;

        if (heldStack.getItem() instanceof BatCageItem && BatCageBlockEntity.canContainEntity(target) && !heldStack.has(ModDataComponents.HELD_ENTITY)) {
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.SUCCESS);

            ItemStack capturedStack = heldStack.copyWithCount(1);
            if (BatCageItem.captureEntity(target, capturedStack)) {
                if (!player.getAbilities().instabuild) {
                    heldStack.shrink(1);
                }
                if (!player.getInventory().add(capturedStack)) {
                    player.drop(capturedStack, false);
                }
            }
        }
    }
}
