package de.teamlapen.vampirism.client.gui.screens;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.lib.lib.client.gui.GuiRenderer;
import de.teamlapen.lib.lib.util.MultilineTooltip;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.entity.player.hunter.HunterPlayer;
import de.teamlapen.vampirism.inventory.AlchemicalCauldronMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.tooltip.BelowOrAboveWidgetTooltipPositioner;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AlchemicalCauldronScreen extends AbstractContainerScreen<AlchemicalCauldronMenu> {

    public static final ResourceLocation LIT_PROGRESS_SPRITE = VResourceLocation.mod("container/alchemical_cauldron/lit_progress");
    public static final ResourceLocation BURN_PROGRESS_SPRITE = VResourceLocation.mod("container/alchemical_cauldron/burn_progress");
    public static final ResourceLocation BUBBLES_PROGRESS_SPRITE = VResourceLocation.mod("container/alchemical_cauldron/bubbles_progress");
    private static final ResourceLocation ERROR_SPRITE = ResourceLocation.withDefaultNamespace("container/anvil/error");
    public static final ResourceLocation BACKGROUND_LOCATION = VResourceLocation.mod("textures/gui/container/alchemical_cauldron.png");

    public AlchemicalCauldronScreen(AlchemicalCauldronMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(@NotNull GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShaderColor(1, 1, 1, 1);
        GuiRenderer.resetColor();

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        GuiRenderer.blit(graphics, BACKGROUND_LOCATION, x, y, this.imageWidth, this.imageHeight);

        if (this.menu.isLit()) {
            int lit = Mth.ceil(this.menu.getLitProgress() * 13) + 1;
            graphics.blitSprite(RenderType::guiTextured, LIT_PROGRESS_SPRITE, 14, 14, 0, 14 - lit, x + 56, y + 36 + 14 - lit, 14, lit);
        }

        int burn = Mth.ceil(this.menu.getBurnProgress() * 24.0F);
        graphics.blitSprite(RenderType::guiTextured, BURN_PROGRESS_SPRITE, 24, 16, 0, 0, x + 79, y + 35, burn, 16);

        int bubbles = Mth.ceil(menu.getBurnProgress() * 29F);
        graphics.blitSprite(RenderType::guiTextured, BUBBLES_PROGRESS_SPRITE, 12, 29, 0, 29 - bubbles, x + 142, y + 28 + 30 - bubbles, 12, bubbles);

        this.menu.checkRecipeNoSkills().ifPresent(holder -> {
            boolean allSkills = HunterPlayer.get(this.minecraft.player).getSkillHandler().areSkillsEnabled(holder.value().getRequiredSkills());
            if (!allSkills) {
                graphics.blitSprite(RenderType::guiTextured, ERROR_SPRITE, x + 77, y + 32, 28, 21);
            }
        });
    }

    @Override
    protected void renderTooltip(GuiGraphics gfx, int mouseX, int mouseY) {
        super.renderTooltip(gfx, mouseX, mouseY);

        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        if (mouseX > x + 77 && mouseX < x + 105 && mouseY > y + 32 && mouseY < y + 53) {
            menu.checkRecipeNoSkills().ifPresent(holder -> {
                List<Holder<ISkill<?>>> missingSkills = holder.value().getRequiredSkills().stream().filter(skill -> !HunterPlayer.get(this.minecraft.player).getSkillHandler().isSkillEnabled(skill)).toList();

                if (!missingSkills.isEmpty()) {
                    List<Component> components = Stream.concat(
                            Stream.of(Component.translatable("gui.vampirism.alchemical_cauldron.missing_skills").withStyle(ChatFormatting.RED)),
                            missingSkills.stream().map(skill -> Component.literal("• ").append(skill.value().getName()).withStyle(ChatFormatting.RED))
                    ).collect(Collectors.toUnmodifiableList());

                    setTooltipForNextRenderPass(new MultilineTooltip(components), new BelowOrAboveWidgetTooltipPositioner(new ScreenRectangle(x + 77, y + 32, 28, 21)), false);
                }
            });
        }
    }
}
