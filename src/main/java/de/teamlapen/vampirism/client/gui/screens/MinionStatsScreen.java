package de.teamlapen.vampirism.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.lib.lib.inventory.InventoryHelper;
import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.minion.MinionEntity;
import de.teamlapen.vampirism.entity.minion.management.MinionData;
import de.teamlapen.vampirism.network.ServerboundUpgradeMinionStatPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.WidgetSprites;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class MinionStatsScreen<T extends MinionData, Q extends MinionEntity<T>> extends Screen {

    private static final ResourceLocation BACKGROUND = VResourceLocation.mod("textures/gui/appearance.png");
    private static final WidgetSprites RESET = new WidgetSprites(VResourceLocation.mod("widget/reset"), VResourceLocation.mod("widget/reset_highlighted"));

    protected final Q entity;
    protected final int xSize = 256;
    protected final int ySize = 177;
    protected final int statCount;
    @Nullable
    protected final Screen backScreen;
    private final MutableComponent textLevel = Component.translatable("text.vampirism.level");
    private final List<Button> statButtons = new ArrayList<>();
    protected int guiLeft;
    protected int guiTop;
    private Button reset;

    protected MinionStatsScreen(Q entity, int statCount, @Nullable Screen backScreen) {
        super(Component.translatable("gui.vampirism.minion_stats"));
        assert statCount > 0;
        this.entity = entity;
        this.statCount = statCount;
        this.backScreen = backScreen;
    }


    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.drawTitle(graphics);
        entity.getMinionData().ifPresent(d -> renderStats(graphics, d));
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.renderBackground(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderGuiBackground(pGuiGraphics);
    }

    @Override
    public void tick() {
        for (int i = 0; i < statCount; i++) {
            int finalI = i;
            statButtons.get(i).active = entity.getMinionData().map(d -> isActive(d, finalI)).orElse(false);
            statButtons.get(i).visible = entity.getMinionData().map(this::areButtonsVisible).orElse(false);
        }

        reset.active = entity.getMinionData().map(MinionData::hasUsedSkillPoints).orElse(false) && getOblivionPotion().isPresent();
    }

    protected abstract boolean areButtonsVisible(T d);

    protected abstract int getRemainingStatPoints(T d);

    @Override
    protected void init() {
        this.statButtons.clear();
        this.guiLeft = (this.width - this.xSize) / 2;
        this.guiTop = (this.height - this.ySize) / 2;
        this.addRenderableWidget(new ExtendedButton(this.guiLeft + this.xSize - 80 - 20, this.guiTop + 152, 80, 20, Component.translatable("gui.done"), (context) -> {
            this.onClose();
        }));
        if (backScreen != null) {
            this.addRenderableWidget(new ExtendedButton(this.guiLeft + 20, this.guiTop + 152, 80, 20, Component.translatable("gui.back"), (context) -> {
                Minecraft.getInstance().setScreen(this.backScreen);
            }));
        }
        for (int i = 0; i < statCount; i++) {
            int finalI = i;
            Button button = this.addRenderableWidget(new ExtendedButton(guiLeft + 225, guiTop + 43 + 26 * i, 20, 20, Component.literal("+"), (b) -> VampirismMod.proxy.sendToServer(new ServerboundUpgradeMinionStatPacket(entity.getId(), finalI))));
            statButtons.add(button);
            button.visible = false;
        }

        reset = this.addRenderableWidget(new ImageButton(this.guiLeft + 225, this.guiTop + 8, 20, 20, RESET, pButton -> {
            VampirismMod.proxy.sendToServer(new ServerboundUpgradeMinionStatPacket(entity.getId(), -1));
            getOblivionPotion().ifPresent(stack -> stack.shrink(1));//server syncs after the screen is closed
        }, Component.translatable("text.vampirism.minion_screen.reset_stats", Component.translatable(ModItems.OBLIVION_POTION.get().getDescriptionId()))) {
            @Override
            public void renderWidget(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
                if (!this.active) {
                    RenderSystem.setShaderColor(0.65f, 0.65f, 0.65f, 1);
                }
                super.renderWidget(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
            }
        });
        reset.setTooltip(Tooltip.create(Component.translatable("text.vampirism.minion_screen.reset_stats", Component.translatable(ModItems.OBLIVION_POTION.get().getDescriptionId()))));
        reset.active = false;
    }

    protected abstract boolean isActive(T data, int i);

    protected void renderGuiBackground(@NotNull GuiGraphics graphics) {
        graphics.blit(RenderType::guiTextured, BACKGROUND, this.guiLeft, this.guiTop, 0, 0, 0, this.xSize, this.ySize, 300, 256);
    }

    protected void renderLevelRow(@NotNull GuiGraphics graphics, int current, int max) {
        graphics.drawString(this.font, textLevel, guiLeft + 10, guiTop + 30, 0x0, false);
        graphics.drawString(this.font, current + "/" + max, guiLeft + 145, guiTop + 30, 0x404040, false);
        int remainingPoints = entity.getMinionData().map(this::getRemainingStatPoints).orElse(0);
        if (remainingPoints > 0) {
            graphics.drawString(this.font, "(" + remainingPoints + ")", guiLeft + 228, guiTop + 30, 0x404040, false);
        }
        graphics.hLine(guiLeft + 10, guiLeft + xSize - 10, guiTop + 40, 0xF0303030);
    }

    protected void renderStatRow(@NotNull GuiGraphics graphics, int i, @NotNull MutableComponent name, @NotNull Component value, int currentLevel, int maxLevel) {
        graphics.drawString(this.font, name.append(":"), guiLeft + 10, guiTop + 50 + 26 * i, 0x404040, false);
        graphics.drawString(this.font, value, guiLeft + 145, guiTop + 50 + 26 * i, 0x404040, false);
        graphics.drawString(this.font, Component.translatable("text.vampirism.level_short").append(": " + currentLevel + "/" + maxLevel), guiLeft + 175, guiTop + 50 + 26 * i, 0x404040, false);
    }

    protected void renderStats(GuiGraphics graphics, T data) {

    }

    private void drawTitle(@NotNull GuiGraphics graphics) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        graphics.drawString(this.font, this.title, this.guiLeft + 10, this.guiTop + 10, -1, true);
    }

    private @NotNull Optional<ItemStack> getOblivionPotion() {
        return Optional.ofNullable(entity.getMinionData().flatMap(data -> Optional.ofNullable(InventoryHelper.getFirst(data.getInventory(), ModItems.OBLIVION_POTION.get()))).orElse(InventoryHelper.getFirst(this.minecraft.player.getInventory(), ModItems.OBLIVION_POTION.get())));
    }


}