package de.teamlapen.lib.lib.client.gui.components;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class VampirismCommonButtons {

    public static SpriteIconButton settings(int width, Component message, Button.OnPress onPress) {
        SpriteIconButton button = SpriteIconButton.builder(message, onPress, true)
                .width(width)
                .sprite(ResourceLocation.fromNamespaceAndPath("vampirism", "icon/settings"), 15, 15)
                .build();
        button.setTooltip(Tooltip.create(message));
        return button;
    }
}
