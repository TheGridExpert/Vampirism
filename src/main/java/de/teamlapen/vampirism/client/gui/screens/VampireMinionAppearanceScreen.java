package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.client.gui.components.SmallCheckbox;
import de.teamlapen.vampirism.client.renderer.entity.VampireMinionRenderer;
import de.teamlapen.vampirism.entity.minion.VampireMinionEntity;
import de.teamlapen.vampirism.entity.minion.management.MinionData;
import de.teamlapen.vampirism.network.ServerboundAppearancePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class VampireMinionAppearanceScreen extends AppearanceScreen<VampireMinionEntity> {

    private static final Component NAME = Component.translatable("gui.vampirism.minion_appearance");

    private int skinType;
    private boolean useLordSkin;
    private boolean isMinionSpecificSkin;

    private EditBox nameWidget;

    private int normalSkinCount;
    @SuppressWarnings("FieldCanBeLocal")
    private int minionSkinCount;
    private int generalSkinCount;

    public VampireMinionAppearanceScreen(VampireMinionEntity minion, Screen backScreen) {
        super(NAME, minion, backScreen);
    }

    @Override
    public void removed() {
        String name = nameWidget.getValue();
        if (name.isEmpty()) {
            name = Component.translatable("text.vampirism.minion").getString() + entity.getMinionId().orElse(0);
        }
        VampirismMod.proxy.sendToServer(new ServerboundAppearancePacket(this.entity.getId(), name, this.skinType, (isMinionSpecificSkin ? 0b10 : 0b0) | (useLordSkin ? 0b1 : 0b0)));
        super.removed();
    }

    @Override
    protected void init() {
        super.init();

        this.skinType = this.entity.getVampireType();
        this.useLordSkin = this.entity.shouldRenderLordSkin();

        this.normalSkinCount = ((VampireMinionRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.entity)).getVampireTextureCount();
        this.minionSkinCount = ((VampireMinionRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.entity)).getMinionSpecificTextureCount(); //can be 0
        this.generalSkinCount = this.normalSkinCount + minionSkinCount;

        this.isMinionSpecificSkin = this.entity.hasMinionSpecificSkin();
        if (this.isMinionSpecificSkin && this.minionSkinCount > 0) {
            this.skinType = this.skinType % this.minionSkinCount;
        } else {
            this.skinType = this.skinType % this.normalSkinCount;
            this.isMinionSpecificSkin = false; //If this.isMinionSpecificSkin && this.minionSkinCount==0
        }

        this.nameWidget = this.addTextField(this.guiLeft + 39, this.guiTop + 31, entity.getMinionData().map(MinionData::getName).orElse("Minion"), MinionData.MAX_NAME_LENGTH, this::onNameChanged, Component.translatable("gui.vampirism.minion_appearance.name"));

        this.addPickerButtons(this.guiLeft + 13, this.guiTop + 50, 104, this::skin);

        this.addRenderableWidget(new SmallCheckbox(this.guiLeft + 13, this.guiTop + 72, this.useLordSkin, Component.translatable("gui.vampirism.minion_appearance.use_lord_skin"), this.font, (checkBox, checked) -> {
            this.useLordSkin = checked;
            this.entity.setUseLordSkin(this.useLordSkin);
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawNameField(guiGraphics, this.guiLeft + 36, this.guiTop + 27);
        this.drawDisplayButton(guiGraphics, this.guiLeft + 29, this.guiTop + 48, 100, Component.translatable("gui.vampirism.minion_appearance.skin").append(" " + (skinType + 1)));
    }

    @Override
    protected float getEntityGuiYOffset() {
        return 0.3f;
    }

    private void onNameChanged(String newName) {
        this.entity.changeMinionName(newName);
    }

    private void skin(int difference) {
        this.skinType = (this.skinType + difference) % generalSkinCount;
        if (this.skinType < 0) this.skinType = (generalSkinCount - 1);

        this.isMinionSpecificSkin = this.skinType + difference >= this.normalSkinCount;

        this.entity.setVampireType(this.skinType, this.isMinionSpecificSkin);
    }
}