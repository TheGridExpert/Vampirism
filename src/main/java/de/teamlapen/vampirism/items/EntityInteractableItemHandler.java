package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.REFERENCE;
import net.minecraft.world.InteractionResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

@EventBusSubscriber(modid = REFERENCE.MODID)
public class EntityInteractableItemHandler {

    @SubscribeEvent
    public static void onItemInteractWithEntity(PlayerInteractEvent.EntityInteract event) {
        if (event.getItemStack().getItem() instanceof IEntityInteractable interactable) {
            InteractionResult interactionResult = interactable.onEntityInteract(event.getItemStack(), event.getTarget(), event.getEntity(), event.getLevel(), event.getHand());
            if (interactionResult != InteractionResult.PASS) {
                event.setCanceled(true);
                event.setCancellationResult(interactionResult);
            }
        }
    }
}
