package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.lib.util.Color;
import de.teamlapen.vampirism.inventory.dialog.DialogNode;
import de.teamlapen.vampirism.inventory.dialog.DialogOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogScreen extends Screen {

    private final Player player;
    private final LivingEntity interactionEntity;
    private DialogNode currentNode;
    
    public DialogScreen(Player player, LivingEntity interactionEntity, DialogNode startNode) {
        super(Component.literal("Dialog"));
        this.player = player;
        this.interactionEntity = interactionEntity;
        this.currentNode = startNode;
    }

    @Override
    protected void init() {
        super.init();
        rebuildOptions();
    }

    private void rebuildOptions() {
        this.clearWidgets();

        if (currentNode == null) return;

        List<DialogOption> options = currentNode.getOptions();

        int startX = (int) (this.width * 0.73);
        int startY = (int) (this.height * 0.67);
        int spacing = this.font.lineHeight + 8;

        if (options.isEmpty()) {
            this.addRenderableWidget(new OptionButton(this.width / 2 - 5, (int) (this.height * 0.84) + this.font.lineHeight + 10, Component.literal("▼"), button -> goToNextNode(), this.font));
        } else {
            for (int i = 0; i < options.size(); i++) {
                DialogOption option = options.get(i);
                Component text = option.getText();
                int y = startY + (options.size() - 1 - i) * spacing;

                this.addRenderableWidget(new OptionButton(startX, y, text, button -> onOptionSelected(option), this.font));
            }
        }
    }

    private void goToNextNode() {
        if (currentNode == null) return;
        Optional<DialogNode> next = currentNode.getNextNode();

        if (next.isEmpty()) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        this.currentNode = next.get();
        rebuildOptions();
    }

    private void onOptionSelected(DialogOption option) {
        option.onSelect(player);
        this.currentNode = option.getNextNode();

        if (currentNode == null) {
            Minecraft.getInstance().setScreen(null);
            return;
        }

        rebuildOptions();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (currentNode != null) {
            Component npcText = currentNode.getText();
            int textX = (this.width - this.font.width(npcText)) / 2;
            int textY = (int) (this.height * 0.84);

            drawTextWithBackground(guiGraphics, this.font, npcText, textX, textY, 0xFFFFFFFF, 5, 3);
        }
    }

    public static void drawTextWithBackground(GuiGraphics guiGraphics, Font font, Component text, int x, int y, int color, int paddingX, int paddingY) {
        int textWidth = font.width(text);
        int textHeight = font.lineHeight;
        int backgroundColor = Color.getRgb(0, 0, 0, 127);

        guiGraphics.fill(x - paddingX, y - paddingY - 1, x + textWidth + paddingX, y + textHeight + paddingY, backgroundColor);
        guiGraphics.drawString(font, text, x, y, color, false);
    }

    public Player getPlayer() {
        return player;
    }

    public LivingEntity getInteractionEntity() {
        return interactionEntity;
    }

    public static class OptionButton extends Button {

        private final Font font;

        public OptionButton(int x, int y, Component message, OnPress onPress, Font font) {
            super(x, y, font.width(message) + 8, font.lineHeight + 4, message, onPress, DEFAULT_NARRATION);
            this.font = font;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int color = isHovered() ? 0xFFFF55 : 0xFFFFFFFF;
            drawTextWithBackground(guiGraphics, this.font, getMessage(), getX(), getY(), color, 4, 2);
        }
    }
}
