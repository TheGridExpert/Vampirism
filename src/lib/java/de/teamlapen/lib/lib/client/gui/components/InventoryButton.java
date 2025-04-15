package de.teamlapen.lib.lib.client.gui.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class InventoryButton extends AbstractButton {

    protected static final WidgetSprites SPRITES = new WidgetSprites(
            ResourceLocation.fromNamespaceAndPath("vampirism", "widget/inventory_button"),
            ResourceLocation.fromNamespaceAndPath("vampirism", "widget/inventory_button_highlighted")
    );

    private final Consumer<InventoryButton> onPress;
    @Nullable
    private final ResourceLocation icon;
    private final int iconWidth;
    private final int iconHeight;

    public static Builder builder(int x, int y, Component message, Consumer<InventoryButton> onPress) {
        return new Builder(x, y, message, onPress);
    }

    private InventoryButton(int x, int y, int width, int height, Component message, Consumer<InventoryButton> onPress, @Nullable ResourceLocation icon, int iconWidth, int iconHeight) {
        super(x, y, width, height, message);
        this.onPress = onPress;
        this.icon = icon;
        this.iconWidth = iconWidth;
        this.iconHeight = iconHeight;
    }

    private InventoryButton(Builder builder) {
        this(builder.x, builder.y, builder.width, builder.height, builder.message, builder.onPress, builder.icon, builder.iconWidth, builder.iconHeight);
        setTooltip(builder.tooltip);
    }

    @Override
    public void onPress() {
        this.onPress.accept(this);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blitSprite(RenderType::guiTextured, SPRITES.get(this.active, this.isHoveredOrFocused()), this.getX(), this.getY(), this.getWidth(), this.getHeight());

        if (this.icon == null) {
            Font font = Minecraft.getInstance().font;
            guiGraphics.drawCenteredString(font, Language.getInstance().getVisualOrder(font.ellipsize(this.getMessage(), this.width - 6)), this.getX() + this.width / 2, this.getY() + (this.height - 8) / 2, getFGColor());
        } else {
            guiGraphics.blitSprite(RenderType::guiTextured, this.icon, this.getX() + this.getWidth() / 2 - this.iconWidth / 2, this.getY() + this.getHeight() / 2 - this.iconHeight / 2, this.iconWidth, this.iconHeight);
        }
    }

    @Override
    protected void updateWidgetNarration(@NotNull NarrationElementOutput narrationElementOutput) {
        this.defaultButtonNarrationText(narrationElementOutput);
    }

    @OnlyIn(Dist.CLIENT)
    public static class Builder {
        private final Component message;
        private final Consumer<InventoryButton> onPress;
        @Nullable
        private ResourceLocation icon;
        private int iconWidth = 15;
        private int iconHeight = 15;
        @Nullable
        private Tooltip tooltip;
        private final int x;
        private final int y;
        private int width = 18;
        private int height = 18;

        public Builder(int x, int y, Component message, Consumer<InventoryButton> onPress) {
            this.x = x;
            this.y = y;
            this.message = message;
            this.onPress = onPress;
        }

        public Builder width(int width) {
            this.width = width;
            return this;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder icon(ResourceLocation icon, int iconWidth, int iconHeight) {
            this.icon = icon;
            this.iconWidth = iconWidth;
            this.iconHeight = iconHeight;
            return this;
        }

        /**
         * The majority of icons are 15x15, so made it the default here
         */
        public Builder icon(ResourceLocation icon) {
            this.icon = icon;
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        /**
         * In case it's an icon button
         */
        public Builder tooltip() {
            this.tooltip = Tooltip.create(this.message);
            return this;
        }

        public InventoryButton build() {
            return new InventoryButton(this);
        }
    }

    public static InventoryButton settingsButton(int x, int y, Component message, Consumer<InventoryButton> onPress) {
        return builder(x, y, message, onPress).icon(ResourceLocation.fromNamespaceAndPath("vampirism", "icon/settings")).tooltip().build();
    }
}
