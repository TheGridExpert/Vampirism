package de.teamlapen.vampirism.client.gui.components;

import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class PlayerHeadButton extends Button {

    public PlayerHeadButton(Component message, OnPress onPress) {
        super(0, 0, 20, 20, message, onPress, DEFAULT_NARRATION);
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) return;

        ResourceLocation skin = minecraft.getSkinManager().getInsecureSkin(player.getGameProfile()).texture();

        int drawX = this.getX() + (this.getWidth() - 16) / 2;
        int drawY = this.getY() + (this.getHeight() - 16) / 2;

        renderTwoLayeredHeadTexture(guiGraphics, skin, drawX, drawY);

        if (Helper.isVampire(player)) {
            ResourceLocation eyeTexture = Helper.getVampireEyesLocation(VampirePlayer.get(player).getEyeType());
            ResourceLocation fangTexture = Helper.getVampireFangLocation(VampirePlayer.get(player).getFangType());

            renderTwoLayeredHeadTexture(guiGraphics, eyeTexture, drawX, drawY);
            renderTwoLayeredHeadTexture(guiGraphics, fangTexture, drawX, drawY);
        }
    }

    private void renderTwoLayeredHeadTexture(GuiGraphics guiGraphics, ResourceLocation texture, int drawX, int drawY) {
        guiGraphics.blit(RenderType::guiTextured, texture, drawX, drawY, 8, 8, 16, 16, 8, 8, 64, 64);
        guiGraphics.blit(RenderType::guiTextured, texture, drawX, drawY, 40, 8, 16, 16, 8, 8, 64, 64);
    }

    @Override
    public void renderString(@NotNull GuiGraphics guiGraphics, @NotNull Font font, int color) {
    }
}
