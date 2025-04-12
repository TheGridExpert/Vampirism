package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class AppearanceScreen<T extends LivingEntity> extends Screen {

    private static final ResourceLocation BACKGROUND = VResourceLocation.mod("textures/gui/container/appearance.png");
    private static final ResourceLocation SMALL_ARROW_LEFT = VResourceLocation.mod("icon/small_arrow_left");
    private static final ResourceLocation SMALL_ARROW_RIGHT = VResourceLocation.mod("icon/small_arrow_right");
    public static final int DEFAULT_FORE_COLOR = 0x404040;

    public static final int WIDTH = 256;
    public static final int HEIGHT = 157;

    protected final T entity;
    @Nullable
    private final Screen backScreen;
    protected int guiLeft;
    protected int guiTop;

    public AppearanceScreen(@NotNull Component title, T entity, @Nullable Screen backScreen) {
        super(title);
        this.entity = entity;
        this.backScreen = backScreen;
    }

    @Override
    protected void init() {
        this.guiLeft = (this.width - WIDTH) / 2;
        this.guiTop = (this.height - HEIGHT) / 2;

        this.addRenderableWidget(new ExtendedButton(this.guiLeft + 80, this.guiTop + 118, 70, 20, Component.translatable("gui.done"), (context) -> this.onClose()));
        if (this.backScreen != null) {
            this.addRenderableWidget(new ExtendedButton(this.guiLeft + 7, this.guiTop + 118, 70, 20, Component.translatable("gui.back"), (context) -> {
                if (this.minecraft != null) this.minecraft.setScreen(this.backScreen);
            }));
        }
    }

    protected void addPickerButtons(int cornerX, int cornerY, int distanceBetween, Consumer<Integer> clickConsumer) {
        SpriteIconButton leftArrowButton = this.addRenderableWidget(SpriteIconButton
                .builder(Component.empty(), button -> clickConsumer.accept(-1), true)
                .size(14, 14)
                .sprite(SMALL_ARROW_LEFT, 9, 9)
                .build()
        );
        leftArrowButton.setPosition(cornerX, cornerY);

        SpriteIconButton rightArrowButton = this.addRenderableWidget(SpriteIconButton
                .builder(Component.empty(), button -> clickConsumer.accept(1), true)
                .size(14, 14)
                .sprite(SMALL_ARROW_RIGHT, 9, 9)
                .build()
        );
        rightArrowButton.setPosition(cornerX + leftArrowButton.getWidth() + distanceBetween, cornerY);
    }

    protected void drawDisplayButton(@NotNull GuiGraphics guiGraphics, int cornerX, int cornerY, int width, Component displayText) {
        guiGraphics.blitSprite(RenderType::guiTextured, ResourceLocation.withDefaultNamespace("widget/button"), cornerX, cornerY, width, 18);
        guiGraphics.drawCenteredString(this.font, displayText, cornerX + width / 2, cornerY + 5, 16777215);
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        renderLabels(guiGraphics);

        InventoryScreen.renderEntityInInventoryFollowsMouse(guiGraphics, this.guiLeft + 155, this.guiTop + 19, this.guiLeft + 233, this.guiTop + 136, 50, 0.0625f, mouseX, mouseY, this.entity);
    }

    protected void renderLabels(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.drawString(this.font, title, this.guiLeft + 5, this.guiTop + 6, DEFAULT_FORE_COLOR, false);
    }

    @Override
    public void renderBackground(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderGuiBackground(guiGraphics);
    }

    protected void renderGuiBackground(@NotNull GuiGraphics guiGraphics) {
        guiGraphics.blit(RenderType::guiTextured, BACKGROUND, this.guiLeft, this.guiTop, 0, 0, WIDTH, HEIGHT, 256, 256);
    }
}