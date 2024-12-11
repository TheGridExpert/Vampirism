package de.teamlapen.vampirism.mixin;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.items.IFactionExclusiveItem;
import de.teamlapen.vampirism.entity.player.IVampirismPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Equippable.class)
public class EquippableMixin {

    @Inject(method = "swapWithEquipmentSlot" , at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getItemBySlot(Lnet/minecraft/world/entity/EquipmentSlot;)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void checkFactionCheck(ItemStack newItem, Player player, CallbackInfoReturnable<InteractionResult> cir) {
        if (newItem.getItem() instanceof IFactionExclusiveItem item) {
            @NotNull TagKey<IFaction<?>> exclusiveFaction = item.getExclusiveFaction(newItem);
            if (!IFaction.is(((IVampirismPlayer) player).getVampAtts().faction(), exclusiveFaction)) {
                cir.setReturnValue(InteractionResult.FAIL);
            }
        }
    }
}
