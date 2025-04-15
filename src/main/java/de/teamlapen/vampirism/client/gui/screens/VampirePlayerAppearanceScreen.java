package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.client.gui.components.SmallCheckbox;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayerSpecialAttributes;
import de.teamlapen.vampirism.network.ServerboundAppearancePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VampirePlayerAppearanceScreen extends AppearanceScreen<Player> {

    private static final Component NAME = Component.translatable("gui.vampirism.appearance");

    private int fangType;
    private int eyeType;
    private boolean glowingEyes;
    private boolean titleGender;

    public VampirePlayerAppearanceScreen(@Nullable Screen backScreen) {
        super(NAME, Minecraft.getInstance().player, backScreen);
    }

    @Override
    public void removed() {
        VampirismMod.proxy.sendToServer(new ServerboundAppearancePacket(this.entity.getId(), "", fangType, eyeType, glowingEyes ? 1 : 0, titleGender ? 1 : 0));
        super.removed();
    }

    @Override
    protected void init() {
        super.init();

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        VampirismPlayerAttributes playerAttributes = VampirismPlayerAttributes.get(player);
        VampirePlayerSpecialAttributes vampireAttributes = playerAttributes.getVampSpecial();

        this.fangType = vampireAttributes.fangType;
        this.eyeType = vampireAttributes.eyeType;
        this.glowingEyes = vampireAttributes.glowingEyes;
        this.titleGender = FactionPlayerHandler.get(player).titleGender() == IPlayableFaction.TitleGender.FEMALE;

        this.addPickerButtons(this.guiLeft + 13, this.guiTop + 28, 104, this::eye);
        this.addPickerButtons(this.guiLeft + 13, this.guiTop + 50, 104, this::fang);

        this.addRenderableWidget(new SmallCheckbox(this.guiLeft + 13, this.guiTop + 72, this.glowingEyes, Component.translatable("gui.vampirism.appearance.glowing_eye"), this.font, (checkBox, checked) -> {
            this.glowingEyes = checked;
            VampirePlayer.get(this.entity).setGlowingEyes(this.glowingEyes);
        }));
        this.addRenderableWidget(new SmallCheckbox(this.guiLeft + 13, this.guiTop + 92, this.titleGender, Component.translatable("gui.vampirism.appearance.title_gender"), this.font, (checkBox, checked) -> {
            this.titleGender = checked;
            FactionPlayerHandler.get(this.entity).setTitleGender(this.titleGender);
        }));
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        this.drawDisplayButton(guiGraphics, this.guiLeft + 29, this.guiTop + 26, 100, Component.translatable("gui.vampirism.appearance.eye").append(" " + (eyeType + 1)));
        this.drawDisplayButton(guiGraphics, this.guiLeft + 29, this.guiTop + 48, 100, Component.translatable("gui.vampirism.appearance.fang").append(" " + (fangType + 1)));
    }

    private void eye(int difference) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        VampirePlayer vampire = VampirePlayer.get(this.minecraft.player);

        this.eyeType = (this.eyeType + difference) % REFERENCE.EYE_TYPE_COUNT;
        if (this.eyeType < 0) this.eyeType = (REFERENCE.EYE_TYPE_COUNT - 1);

        vampire.setEyeType(this.eyeType);
    }

    private void fang(int difference) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        VampirePlayer vampire = VampirePlayer.get(this.minecraft.player);

        this.fangType = (this.fangType + difference) % REFERENCE.FANG_TYPE_COUNT;
        if (this.fangType < 0) this.fangType = (REFERENCE.FANG_TYPE_COUNT - 1);

        vampire.setFangType(this.fangType);
    }
}