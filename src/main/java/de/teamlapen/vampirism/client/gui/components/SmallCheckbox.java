package de.teamlapen.vampirism.client.gui.components;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class SmallCheckbox extends AbstractButton {
    private static final ResourceLocation CHECKBOX_HIGHLIGHTED_SPRITE = VResourceLocation.mod("widget/small_checkbox_highlighted");
    private static final ResourceLocation CHECKBOX_SPRITE = VResourceLocation.mod("widget/small_checkbox");
    private static final ResourceLocation TICK = VResourceLocation.mod("icon/tick");
    private static final int SIZE = 14;

    private final BiConsumer<SmallCheckbox, Boolean> onValueChange;
    private boolean checked;
    private final Font font;
    private final int textColor;

    public SmallCheckbox(int x, int y, boolean checked, Component message, Font font, int textColor, BiConsumer<SmallCheckbox, Boolean> onValueChange) {
        super(x, y, SIZE, SIZE, message);
        this.onValueChange = onValueChange;
        this.checked = checked;
        this.font = font;
        this.textColor = textColor;
    }

    public SmallCheckbox(int x, int y, boolean checked, Component message, Font font, BiConsumer<SmallCheckbox, Boolean> onValueChange) {
        this(x, y, checked, message, font, 0x404040, onValueChange);
    }

    @Override
    public void onPress() {
        this.checked = !this.checked;
        this.onValueChange.accept(this, this.checked);
    }

    public boolean isChecked() {
        return this.checked;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, this.createNarrationMessage());
        if (this.active) {
            if (this.isHoveredOrFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.focused"));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.checkbox.usage.hovered"));
            }
        }
    }

    @Override
    public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        guiGraphics.blitSprite(RenderType::guiTextured, this.isHoveredOrFocused() ? CHECKBOX_HIGHLIGHTED_SPRITE : CHECKBOX_SPRITE, this.getX(), this.getY(), SIZE, SIZE);
        if (isChecked()) {
            int tickWidth = 12;
            int tickHeight = 7;
            guiGraphics.blitSprite(RenderType::guiTextured, TICK, this.getX() + this.getWidth() / 2 - tickWidth / 2, this.getY() + this.getHeight() / 2 - tickHeight / 2, tickWidth, tickHeight);
        }

        guiGraphics.drawString(this.font, getMessage(), this.getX() + SIZE + 5, this.getY() + SIZE / 2 - this.font.lineHeight / 2, textColor, false);
    }
}
