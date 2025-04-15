package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.client.gui.components.SmallCheckbox;
import de.teamlapen.vampirism.client.renderer.entity.HunterMinionRenderer;
import de.teamlapen.vampirism.entity.minion.HunterMinionEntity;
import de.teamlapen.vampirism.entity.minion.management.MinionData;
import de.teamlapen.vampirism.network.ServerboundAppearancePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class HunterMinionAppearanceScreen extends AppearanceScreen<HunterMinionEntity> {

    private static final Component NAME = Component.translatable("gui.vampirism.minion_appearance");

    private int skinType;
    private int hatType;
    private boolean useLordSkin;
    private boolean isMinionSpecificSkin;

    private EditBox nameWidget;

    private int normalSkinCount;
    @SuppressWarnings("FieldCanBeLocal")
    private int minionSkinCount;
    private int generalSkinCount;

    public HunterMinionAppearanceScreen(HunterMinionEntity minion, Screen backScreen) {
        super(NAME, minion, backScreen);
    }

    @Override
    public void removed() {
        String name = nameWidget.getValue();
        if (name.isEmpty()) {
            name = Component.translatable("text.vampirism.minion").toString() + entity.getMinionId().orElse(0);
        }
        VampirismMod.proxy.sendToServer(new ServerboundAppearancePacket(this.entity.getId(), name, this.skinType, this.hatType, (this.isMinionSpecificSkin ? 0b10 : 0b0) | (this.useLordSkin ? 0b1 : 0b0)));
        super.removed();
    }

    @Override
    protected void init() {
        super.init();

        this.hatType = this.entity.getHatType();
        this.skinType = this.entity.getHunterType();
        this.useLordSkin = this.entity.shouldRenderLordSkin();

        this.normalSkinCount = ((HunterMinionRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.entity)).getHunterTextureCount();
        this.minionSkinCount = ((HunterMinionRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(this.entity)).getMinionSpecificTextureCount(); //Can be 0
        this.generalSkinCount = this.normalSkinCount + this.minionSkinCount;

        this.isMinionSpecificSkin = this.entity.hasMinionSpecificSkin();
        if (this.isMinionSpecificSkin && this.minionSkinCount > 0) {
            this.skinType = this.skinType % this.minionSkinCount;
        } else {
            this.skinType = this.skinType % this.normalSkinCount;
            this.isMinionSpecificSkin = false; //If this.isMinionSpecificSkin && this.minionSkinCount==0
        }

        this.nameWidget = this.addTextField(this.guiLeft + 39, this.guiTop + 31, entity.getMinionData().map(MinionData::getName).orElse("Minion"), MinionData.MAX_NAME_LENGTH, this::onNameChanged, Component.translatable("gui.vampirism.minion_appearance.name"));

        this.addPickerButtons(this.guiLeft + 13, this.guiTop + 50, 104, this::hat);
        this.addPickerButtons(this.guiLeft + 13, this.guiTop + 72, 104, this::skin);

        this.addRenderableWidget(new SmallCheckbox(this.guiLeft + 13, this.guiTop + 92, this.useLordSkin, Component.translatable("gui.vampirism.minion_appearance.use_lord_skin"), this.font, (checkBox, checked) -> {
            useLordSkin = checked;
            entity.setUseLordSkin(checked);
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawNameField(guiGraphics, this.guiLeft + 36, this.guiTop + 27);
        this.drawDisplayButton(guiGraphics, this.guiLeft + 29, this.guiTop + 48, 100, Component.translatable("gui.vampirism.minion_appearance.hat").append(" " + (this.hatType + 1)));
        this.drawDisplayButton(guiGraphics, this.guiLeft + 29, this.guiTop + 70, 100, Component.translatable("gui.vampirism.minion_appearance.skin").append(" " + (this.skinType + 1)));
    }

    @Override
    protected float getEntityGuiYOffset() {
        return 0.3f;
    }

    private void onNameChanged(String newName) {
        this.entity.changeMinionName(newName);
    }

    private void hat(int difference) {
        this.hatType = (this.hatType + difference) % 3;
        if (this.hatType < 0) this.hatType = 2;

        this.entity.setHatType(this.hatType);
    }

    private void skin(int difference) {
        this.skinType = (this.skinType + difference) % generalSkinCount;
        if (this.skinType < 0) this.skinType = (generalSkinCount - 1);

        this.isMinionSpecificSkin = this.skinType + difference >= this.normalSkinCount;

        this.entity.setHunterType(this.skinType, this.isMinionSpecificSkin);
    }
}
