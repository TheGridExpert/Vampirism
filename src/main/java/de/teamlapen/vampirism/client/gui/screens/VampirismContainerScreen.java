package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.lib.lib.client.gui.GuiRenderer;
import de.teamlapen.lib.lib.client.gui.components.VampirismCommonButtons;
import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.ITaskInstance;
import de.teamlapen.vampirism.api.items.IRefinementItem;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModKeys;
import de.teamlapen.vampirism.client.gui.components.PlayerHeadButton;
import de.teamlapen.vampirism.client.gui.screens.skills.SkillsScreen;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.inventory.TaskMenu;
import de.teamlapen.vampirism.inventory.VampirismMenu;
import de.teamlapen.vampirism.mixin.client.accessor.AbstractContainerScreenAccessor;
import de.teamlapen.vampirism.network.ServerboundDeleteRefinementPacket;
import de.teamlapen.vampirism.util.Helper;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class VampirismContainerScreen extends AbstractContainerScreen<VampirismMenu> implements ExtendedScreen {

    private static final ResourceLocation BACKGROUND = VResourceLocation.mod("textures/gui/container/vampirism_menu.png");
    private static final ResourceLocation SLOT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot");
    private static final WidgetSprites APPEARANCE = new WidgetSprites(VResourceLocation.mod("widget/appearance"), VResourceLocation.mod("widget/appearance_highlighted"));
    private static final WidgetSprites SKILLS = new WidgetSprites(VResourceLocation.mod("widget/skills"), VResourceLocation.mod("widget/skills_highlighted"));
    private static final WidgetSprites REMOVE_ACCESSORY = new WidgetSprites(VResourceLocation.mod("widget/remove_accessory"), VResourceLocation.mod("widget/remove_accessory_highlighted"));
    private static final WidgetSprites LOCATE_TASK_MASTER = new WidgetSprites(VResourceLocation.mod("widget/locate_task_master"), VResourceLocation.mod("widget/locate_task_master_highlighted"));

    private static final int WIDTH = 230;
    private static final int HEIGHT = 208;

    private final IFactionPlayer<?> factionPlayer;
    private TaskList list;
    private final Map<Integer, Button> refinementRemoveButtons = new Int2ObjectOpenHashMap<>(3);
    private Component level;

    public VampirismContainerScreen(@NotNull VampirismMenu container, @NotNull Inventory playerInventory, @NotNull Component titleIn) {
        super(container, playerInventory, titleIn);
        this.imageWidth = WIDTH;
        this.imageHeight = HEIGHT;
        this.inventoryLabelX = 35;
        this.inventoryLabelY = this.imageHeight - 95;
        this.menu.setReloadListener(() -> this.list.updateContent());
        this.factionPlayer = FactionPlayerHandler.getCurrentFactionPlayer(playerInventory.player).orElseThrow(() -> new IllegalStateException("Cannot open Vampirism container without faction player"));
    }

    @Override
    public @NotNull TaskMenu getTaskContainer() {
        return this.menu;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ModKeys.VAMPIRISM_MENU.matches(keyCode, scanCode)) {
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        if (!this.isQuickCrafting) {
            this.list.mouseDragged(mouseX, mouseY, button, dragX, dragY);
        }
        return true;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        if (this.menu.areRefinementsAvailable()) {
            for (int i = 0; i < this.menu.getRefinementStacks().size(); i++) {
                ItemStack stack = this.menu.getRefinementStacks().get(i);
                Slot slot = this.menu.getSlot(i);
                int x = slot.x + this.leftPos;
                int y = slot.y + this.topPos;
                guiGraphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, x - 1, y - 1, 18, 18);
                guiGraphics.renderItem(stack, x, y);
                guiGraphics.renderItemDecorations(this.font, stack, x, y, null);
            }
        }

        this.renderAccessorySlots(guiGraphics, mouseX, mouseY, partialTicks);

        this.renderTooltip(guiGraphics, mouseX, mouseY);
        if (this.menu.areRefinementsAvailable()) {
            this.renderHoveredRefinementTooltip(guiGraphics, mouseX, mouseY);
        }
    }

    protected void renderAccessorySlots(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        for (Slot slot : this.menu.slots) {
            if (((AbstractContainerScreenAccessor) this).invoke_isHovering(slot, mouseX, mouseY) && slot instanceof VampirismMenu.RemovingSelectorSlot && !this.menu.getRefinementStacks().get(slot.getSlotIndex()).isEmpty()) {
                this.refinementRemoveButtons.get(slot.getSlotIndex()).render(graphics, mouseX, mouseY, partialTicks);
            }
        }
    }

    @Override
    public void resize(@NotNull Minecraft pMinecraft, int pWidth, int pHeight) {
        super.resize(pMinecraft, pWidth, pHeight);
        this.list.updateContent();
    }

    @Override
    protected void init() {
        super.init();

        if (factionPlayer.getLevel() > 0) {
            FactionPlayerHandler handler = FactionPlayerHandler.get(factionPlayer.asEntity());
            MutableComponent component = Optional.of(handler).filter(x -> x.getLordLevel() > 0).map(FactionPlayerHandler::getLordTitle).map(x -> x.plainCopy().append(" (" + handler.getLordLevel() + ")")).orElseGet(() -> Component.translatable("text.vampirism.level").append(" " + factionPlayer.getLevel()));
            this.level = component.withStyle(style -> style.withColor(factionPlayer.getFaction().value().getChatColor()));
        } else {
            this.level = Component.empty();
        }

        this.list = this.addRenderableWidget(new TaskList(Minecraft.getInstance(), this.menu, factionPlayer, this.leftPos + 82, this.topPos + 8, 146, 101, () -> new ArrayList<>(this.menu.getTaskInfos())));

        int distanceBetweenButtons = 21;

        ImageButton skillsScreenButton = this.addRenderableWidget(new ImageButton(this.leftPos + 7, this.topPos + 90, 20, 20, SKILLS, context -> {
            if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isAlive() && !VampirismPlayerAttributes.get(this.minecraft.player).faction().is(ModFactions.NEUTRAL.getId())) {
                FactionPlayerHandler.get(this.minecraft.player).getCurrentSkillPlayer().ifPresent(f -> Minecraft.getInstance().setScreen(new SkillsScreen(f, this)));
            }
        }, Component.empty()));
        skillsScreenButton.setTooltip(Tooltip.create(Component.translatable("gui.vampirism.vampirism_menu.skill_screen")));

        boolean isAppearanceButtonShown = minecraft != null && Helper.isVampire(minecraft.player);

        PlayerHeadButton appearanceButton = this.addRenderableWidget(new PlayerHeadButton(Component.translatable("gui.vampirism.vampirism_menu.appearance_menu"), button -> Minecraft.getInstance().setScreen(new VampirePlayerAppearanceScreen(this))));
        appearanceButton.setPosition(skillsScreenButton.getX() + distanceBetweenButtons, skillsScreenButton.getY());
        appearanceButton.setTooltip(Tooltip.create(Component.translatable("gui.vampirism.vampirism_menu.appearance_menu")));

        if (!isAppearanceButtonShown) {
            appearanceButton.active = false;
            appearanceButton.visible = false;
        }

        boolean isEditTasksButtonShown = FactionPlayerHandler.get(factionPlayer.asEntity()).getLordLevel() > 0;

        SpriteIconButton editActionsButton = this.addRenderableWidget(VampirismCommonButtons.settings(20, Component.translatable("gui.vampirism.vampirism_menu.edit_actions"), button -> EditSelectActionScreen.show()));
        editActionsButton.setPosition(skillsScreenButton.getX() + (isAppearanceButtonShown ? 0 : distanceBetweenButtons), (isAppearanceButtonShown ? this.topPos + 160 : skillsScreenButton.getY()));

        SpriteIconButton editTasksButton = this.addRenderableWidget(VampirismCommonButtons.settings(20, Component.translatable("gui.vampirism.vampirism_menu.edit_tasks"), button -> EditSelectMinionTaskScreen.show()));
        editTasksButton.setPosition(editActionsButton.getX() + (isAppearanceButtonShown ? 0 : distanceBetweenButtons), editActionsButton.getY() + (isAppearanceButtonShown ? distanceBetweenButtons : 0));
        editTasksButton.visible = isEditTasksButtonShown;

        if (this.menu.areRefinementsAvailable()) {
            NonNullList<ItemStack> refinementList = this.menu.getRefinementStacks();
            for (Slot slot : this.menu.slots) {
                if (slot instanceof VampirismMenu.RemovingSelectorSlot) {
                    Button xButton = this.addRenderableWidget(new ImageButton(this.getGuiLeft() + slot.x + 16 - 5, this.getGuiTop() + slot.y + 16 - 5, 5, 5, REMOVE_ACCESSORY, (button) -> {
                        VampirismMod.proxy.sendToServer(new ServerboundDeleteRefinementPacket(IRefinementItem.AccessorySlotType.values()[slot.index]));
                        refinementList.set(slot.index, ItemStack.EMPTY);
                    }, Component.empty()) {
                        @Override
                        public void renderWidget(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                            if (!refinementList.get(slot.index).isEmpty() && ((AbstractContainerScreenAccessor) VampirismContainerScreen.this).getDraggingItem().isEmpty() && overSlot(slot, pMouseX, pMouseY)) {
                                super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
                            }
                        }

                        private boolean overSlot(@NotNull Slot slot, int mouseX, int mouseY) {
                            mouseX -= VampirismContainerScreen.this.leftPos;
                            mouseY -= VampirismContainerScreen.this.topPos;
                            return slot.x <= mouseX && slot.x + 16 > mouseX && slot.y <= mouseY && slot.y + 16 > mouseY;
                        }
                    });
                    if (slot.getItem() != ItemStack.EMPTY) {
                        xButton.setTooltip(Tooltip.create(Component.translatable("gui.vampirism.vampirism_menu.destroy_item").withStyle(ChatFormatting.RED)));
                    }
                    refinementRemoveButtons.put(slot.getSlotIndex(), xButton);
                }
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(this.font, this.level, Math.max(5, 31 - this.font.width(this.level) / 2), 81, -1, false);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        GuiRenderer.resetColor();
        GuiRenderer.blit(guiGraphics, BACKGROUND, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        if (this.minecraft != null && this.minecraft.player != null) {
            InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.leftPos + 8, this.topPos + 7, this.leftPos + 56, this.topPos + 78, 30, 0.0625f, mouseX, mouseY, this.minecraft.player);
        }
    }

    protected void renderHoveredRefinementTooltip(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null) {
            int index = this.hoveredSlot.index;
            NonNullList<ItemStack> list = this.menu.getRefinementStacks();
            if (index < list.size() && index >= 0) {
                if (this.getMenu().getCarried().isEmpty() && !list.get(index).isEmpty()) {
                    if (!this.refinementRemoveButtons.get(this.hoveredSlot.getSlotIndex()).isHoveredOrFocused()) {
                        guiGraphics.renderTooltip(this.font, list.get(index), mouseX, mouseY);

                    }
                } else {
                    if (!list.get(index).isEmpty() && this.menu.getSlot(index).mayPlace(this.getMenu().getCarried())) {
                        guiGraphics.renderTooltip(this.font, Component.translatable("gui.vampirism.vampirism_menu.destroy_item").withStyle(ChatFormatting.RED), mouseX, mouseY);
                    }
                }
            }
        }
    }

    private static class TaskList extends de.teamlapen.vampirism.client.gui.screens.taskboard.TaskList {

        public TaskList(Minecraft minecraft, TaskMenu menu, IFactionPlayer<?> factionPlayer, int x, int y, int width, int height, Supplier<List<ITaskInstance>> itemSupplier) {
            super(minecraft, menu, factionPlayer, x, y, width, height, itemSupplier);
        }

        @Override
        protected TaskEntry createItem(ITaskInstance item) {
            return new TaskEntry(item);
        }

        @Override
        public void renderWidget(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            if (children().isEmpty()) {
                guiGraphics.drawCenteredString(minecraft.font, Component.translatable("gui.vampirism.vampirism_menu.no_tasks"), this.getX() + width / 2, this.getY() + height / 2, 0x404040);
            }
        }

        private class TaskEntry extends de.teamlapen.vampirism.client.gui.screens.taskboard.TaskList.TaskEntry {

            private @Nullable ImageButton button;

            public TaskEntry(ITaskInstance taskInstance) {
                super(taskInstance);

                if (!taskInstance.isUnique(menu.getRegistry())) {
                    this.button = new ImageButton(0, 0, 8, 11, LOCATE_TASK_MASTER, this::clickLocator, Component.empty());
                    this.button.setTooltip(Tooltip.create(createTooltip()));
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
                if (this.button != null && button.mouseClicked(mouseX, mouseY, mouseButton)) {
                    return true;
                }
                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }

            @Override
            protected void renderToolTips(Minecraft minecraft, int mouseX, int mouseY) {
                if (this.button != null && !button.isMouseOver(mouseX, mouseY)) {
                    super.renderToolTips(minecraft, mouseX, mouseY);
                }
            }

            private void clickLocator(Button button) {
                Player player = factionPlayer.asEntity();
                Component position = ((VampirismMenu) menu).taskWrapper.get(getItem().getTaskBoard()).getLastSeenPos().map(pos -> {
                    int i = Mth.floor(UtilLib.horizontalDistance(player.blockPosition(), pos));
                    MutableComponent itextcomponent = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", pos.getX(), "~", pos.getZ())).withStyle((p_241055_1_) ->
                            p_241055_1_.withColor(ChatFormatting.GREEN).withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + pos.getX() + " ~ " + pos.getZ())).withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.translatable("chat.coordinates.tooltip"))));
                    return itextcomponent.append(Component.translatable("gui.vampirism.vampirism_menu.distance", i));
                }).orElseGet(() -> Component.translatable("gui.vampirism.vampirism_menu.last_known_pos.unknown").withStyle(ChatFormatting.GOLD));
                player.displayClientMessage(Component.translatable("gui.vampirism.vampirism_menu.last_known_pos").append(position), false);
            }

            private Component createTooltip() {
                Component position = ((VampirismMenu) menu).taskWrapper.get(this.getItem().getTaskBoard()).getLastSeenPos().map(pos -> Component.literal("[" + pos.toShortString() + "]").withStyle(ChatFormatting.GREEN)).orElseGet(() -> Component.translatable("gui.vampirism.vampirism_menu.last_known_pos.unknown").withStyle(ChatFormatting.GOLD));
                return Component.translatable("gui.vampirism.vampirism_menu.last_known_pos").append(position);
            }

            @Override
            public void render(GuiGraphics guiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pIsMouseOver, float pPartialTick) {
                super.render(guiGraphics, pIndex, pTop, pLeft, pWidth, pHeight, pMouseX, pMouseY, pIsMouseOver, pPartialTick);
                if (this.button != null) {
                    this.button.setPosition(pLeft + pWidth - this.button.getWidth() - 1, pTop + 1);
                    this.button.render(guiGraphics, pMouseX, pMouseY, pPartialTick);
                }
            }
        }
    }
}
