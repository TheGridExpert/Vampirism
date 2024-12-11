package de.teamlapen.lib.lib.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.gui.GuiSpriteScaling;
import net.minecraft.resources.ResourceLocation;

public class GuiRenderer {

    private static final int DEFAULT_IMAGE_WIDTH = 256;
    private static final int DEFAULT_IMAGE_HEIGHT = 256;

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height) {
        graphics.blit(RenderType::guiTextured, texture, x, y, 0, 0, width, height, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    public static void blit(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int imageWidth, int imageHeight) {
        graphics.blit(RenderType::guiTextured, texture, x, y, 0, 0, width, height, imageWidth, imageHeight);
    }

    public static void blitWithOffset(GuiGraphics graphics, ResourceLocation texture, int x, int y, int xOffset, int yOffset, int width, int height) {
        graphics.blit(RenderType::guiTextured, texture, x, y, xOffset, yOffset, width, height, DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_HEIGHT);
    }

    public static void blitWithOffset(GuiGraphics graphics, ResourceLocation texture, int x, int y, int xOffset, int yOffset, int width, int height, int imageWidth, int imageHeight) {
        graphics.blit(RenderType::guiTextured, texture, x, y, xOffset, yOffset, width, height, imageWidth, imageHeight);
    }

    public static void blitTiled(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int color) {
        graphics.blitSprite(RenderType::guiTextured, texture, x, y, width, height, color);
    }

    public static void blitTiledOffset(GuiGraphics graphics, ResourceLocation texture, int x, int y, int width, int height, int xOffset, int yOffset, int color) {
        x += xOffset;
        y += yOffset;
        width -= xOffset;
        height -= yOffset;
        TextureAtlasSprite textureatlassprite = graphics.sprites.getSprite(texture);
        GuiSpriteScaling guispritescaling = graphics.sprites.getSpriteScaling(textureatlassprite);
        if (guispritescaling instanceof GuiSpriteScaling.Tile(int tileWidth, int tileHeight)) {
            graphics.blitTiledSprite(RenderType::guiTextured, textureatlassprite, x, y, width, height, xOffset, yOffset, tileWidth, tileHeight, tileWidth, tileHeight, color);
        } else {
            graphics.blitSprite(RenderType::guiTextured, texture, x, y, width, height, color);
        }

    }

    public static void resetColor() {
        RenderSystem.setShaderColor(1,1,1,1);
    }
}
