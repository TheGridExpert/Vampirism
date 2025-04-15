package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.lib.lib.client.gui.GuiRenderer;
import de.teamlapen.lib.lib.client.gui.components.InventoryButton;
import de.teamlapen.lib.lib.client.gui.components.SimpleList;
import de.teamlapen.vampirism.api.entity.minion.IMinionTask;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.inventory.MinionContainer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.LockIconButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MinionScreen extends AbstractContainerScreen<MinionContainer> {

    private static final ResourceLocation BACKGROUND = VResourceLocation.mod("textures/gui/container/minion_inventory.png");
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");

    private final int extraSlots;
    private SimpleList<?> taskList;
    private Button taskButton;
    private LockIconButton lockActionButton;

    public MinionScreen(@NotNull MinionContainer screenContainer, @NotNull Inventory inventory, @NotNull Component title) {
        super(screenContainer, inventory, title);
        this.imageWidth = 214;
        this.imageHeight = 185;
        this.extraSlots = screenContainer.getExtraSlots();
    }

    @Override
    protected void init() {
        super.init();

        this.addRenderableWidget(InventoryButton.settingsButton(this.leftPos + 6, this.topPos + 18, Component.translatable("gui.vampirism.minion.appearance"), button -> menu.openConfigurationScreen()));
        this.addRenderableWidget(InventoryButton.builder(this.leftPos + 6, this.topPos + 36, Component.translatable("gui.vampirism.minion_stats"), button -> menu.openStatsScreen()).icon(VResourceLocation.mod("icon/plus")).tooltip().build());

        this.lockActionButton = this.addRenderableWidget(new LockIconButton(this.leftPos + 99, this.topPos + 19, this::toggleActionLock));

        this.lockActionButton.setLocked(this.menu.isTaskLocked());
        List<Component> taskNames = Arrays.stream(menu.getAvailableTasks()).map(IMinionTask::getName).toList();

        this.taskList = this.addRenderableWidget(SimpleList.builder(this.leftPos + 119, this.topPos + 19 + 19, 88, Math.min(3 * 18, taskNames.size() * 18) + 2).componentsWithClick(taskNames, this::selectTask).build());
        this.taskButton = this.addRenderableWidget(new ExtendedButton(this.leftPos + 119, this.topPos + 19, 88, 20, getActiveTaskName(), button -> taskList.visible = !taskList.visible));
        this.taskList.visible = false;
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        GuiRenderer.resetColor();

        int centreX = (this.width - this.imageWidth) / 2;
        int centreY = (this.height - this.imageHeight) / 2;

        GuiRenderer.blit(guiGraphics, BACKGROUND, centreX, centreY, this.imageWidth, this.imageHeight);

        for (int i = 0; i < extraSlots / 3; i++) {
            for (int j = 0; j < 3; j++) {
                guiGraphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, this.leftPos + 117 + (j * 18), this.topPos + 36 + (i * 18), 18, 18);
            }
        }

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 45, this.topPos + 19, this.leftPos + 93, this.topPos + 88, 30, 0.3f, mouseX, mouseY, this.menu.getMinionEntity());
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, title, 5, 6, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("gui.vampirism.minion.active_task"), 120, 10, 0x404040, false);
    }

    @Override
    public boolean mouseDragged(double p_mouseDragged_1_, double p_mouseDragged_3_, int p_mouseDragged_5_, double p_mouseDragged_6_, double p_mouseDragged_8_) {
        this.taskList.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
        return super.mouseDragged(p_mouseDragged_1_, p_mouseDragged_3_, p_mouseDragged_5_, p_mouseDragged_6_, p_mouseDragged_8_);
    }

    @Override
    protected void renderTooltip(@NotNull GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.lockActionButton.isMouseOver(mouseX, mouseY)) {
            drawButtonTip(graphics, Component.translatable("gui.vampirism.minion.lock_action"), mouseX, mouseY);
        } else {
            super.renderTooltip(graphics, mouseX, mouseY);
        }
    }

    private void drawButtonTip(@NotNull GuiGraphics graphics, Component text, int mouseX, int mouseY) {
        graphics.renderTooltip(this.font, Collections.singletonList(text), Optional.empty(), mouseX, mouseY);
    }

    private Component getActiveTaskName() {
        return menu.getSelectedTask().getName();
    }

    private void selectTask(int id) {
        this.taskList.visible = false;
        this.menu.setTaskToActivate(id);
        this.taskButton.setMessage(getActiveTaskName());
    }

    private void toggleActionLock(Button b) {
        lockActionButton.setLocked(!lockActionButton.isLocked());
        menu.setTaskLocked(lockActionButton.isLocked());
    }
}