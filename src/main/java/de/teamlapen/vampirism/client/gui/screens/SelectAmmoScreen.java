package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.lib.lib.client.gui.screens.radialmenu.GuiRadialMenu;
import de.teamlapen.lib.lib.client.gui.screens.radialmenu.IRadialMenuSlot;
import de.teamlapen.lib.lib.client.gui.screens.radialmenu.RadialMenu;
import de.teamlapen.lib.lib.client.gui.screens.radialmenu.RadialMenuSlot;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.items.IHunterCrossbow;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.items.crossbow.CrossbowArrowHandler;
import de.teamlapen.vampirism.network.ServerboundSelectAmmoTypePacket;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SelectAmmoScreen extends GuiRadialMenu<SelectAmmoScreen.AmmoType> {

    private static final ResourceLocation NO_RESTRICTION = VResourceLocation.mc("spectator/close");

    public SelectAmmoScreen(Collection<AmmoType> ammoTypes) {
        super(getRadialMenu(ammoTypes), true);
    }

    public static void show() {
        Player player = Minecraft.getInstance().player;
        ItemStack crossbowStack = player.getMainHandItem();
        if (Helper.isHunter(player) && crossbowStack.getItem() instanceof IHunterCrossbow crossbow && crossbow.canSelectAmmunition(crossbowStack)) {
            var ammoTypes = CrossbowArrowHandler.getCrossbowArrows().stream().map(item -> new AmmoType(item, player.getInventory().countItem(item))).collect(Collectors.toList());
            ammoTypes.add(new AmmoType(null, 0));
            Minecraft.getInstance().setScreen(new SelectAmmoScreen(ammoTypes));
        }
    }

    private static RadialMenu<AmmoType> getRadialMenu(Collection<AmmoType> ammoTypes) {
        List<IRadialMenuSlot<AmmoType>> parts = (List<IRadialMenuSlot<AmmoType>>) (Object) ammoTypes.stream().map(a -> new RadialMenuSlot<>(a.getDisplayName(), a)).toList();
        return new RadialMenu<>((i) -> {
            VampirismMod.proxy.sendToServer(ServerboundSelectAmmoTypePacket.of(parts.get(i).primarySlotIcon()));
        }, parts, SelectAmmoScreen::drawAmmoTypePart, 0);
    }

    private static void drawAmmoTypePart(AmmoType action, GuiGraphics graphics, int posX, int posY, int size, boolean transparent) {
        if (action.renderStack != null) {
            graphics.renderItem(action.renderStack, posX, posY);
            graphics.renderItemDecorations(Minecraft.getInstance().screen.font, action.renderStack, posX, posY, String.valueOf(action.count));
        } else {
            graphics.blitSprite(RenderType::guiTextured, NO_RESTRICTION, posX, posY, 16, 16);
        }
    }

    @Override
    public void drawSliceName(GuiGraphics graphics, String sliceName, ItemStack stack, int posX, int posY) {
    }

    public static class AmmoType {
        public final ItemStack renderStack;
        public final int count;

        public AmmoType(@Nullable Item item, int count) {
            this.count = count;
            this.renderStack = item == null ? null : item.getDefaultInstance();
        }

        public Component getDisplayName() {
            return renderStack != null ? renderStack.getHoverName() : Component.translatable("text.vampirism.crossbow.no_restriction");
        }
    }
}
