package de.teamlapen.vampirism.client.gui.screens;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.inventory.AltarInfusionMenu;
import de.teamlapen.vampirism.items.PureBloodItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

public class AltarInfusionScreen extends AbstractContainerScreen<AltarInfusionMenu> {

    private static final ResourceLocation EMPTY_SLOT_PURE_BLOOD_BOTTLE = VResourceLocation.mod("container/slot/pure_blood_bottle");
    private static final ResourceLocation EMPTY_SLOT_HUMAN_HEART = VResourceLocation.mod("container/slot/human_heart");
    private static final ResourceLocation EMPTY_SLOT_VAMPIRE_BOOK = VResourceLocation.mod("container/slot/vampire_book");
    private static final ResourceLocation BACKGROUND_LOCATION = VResourceLocation.mod("textures/gui/container/altar_of_infusion.png");

    private final CyclingSlotBackground pureBloodIcon = new CyclingSlotBackground(0);
    private final CyclingSlotBackground humanHeartIcon = new CyclingSlotBackground(1);
    private final CyclingSlotBackground vampireBookIcon = new CyclingSlotBackground(2);

    public AltarInfusionScreen(AltarInfusionMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        var requirementsOpt = this.menu.getRequirements();
        this.pureBloodIcon.tick(requirementsOpt.filter(requirements -> requirements.pureBloodQuantity() > 0).map(requirements -> List.of(EMPTY_SLOT_PURE_BLOOD_BOTTLE)).orElse(List.of()));
        this.humanHeartIcon.tick(requirementsOpt.filter(requirements -> requirements.humanHeartQuantity() > 0).map(requirements -> List.of(EMPTY_SLOT_HUMAN_HEART)).orElse(List.of()));
        this.vampireBookIcon.tick(requirementsOpt.filter(requirements -> requirements.vampireBookQuantity() > 0).map(requirements -> List.of(EMPTY_SLOT_VAMPIRE_BOOK)).orElse(List.of()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(RenderType::guiTextured, BACKGROUND_LOCATION, this.leftPos, this.topPos, 0.0F, 0.0F, this.imageWidth, this.imageHeight, 256, 256);
        this.pureBloodIcon.render(this.menu, graphics, partialTick, this.leftPos, this.topPos);
        this.humanHeartIcon.render(this.menu, graphics, partialTick, this.leftPos, this.topPos);
        this.vampireBookIcon.render(this.menu, graphics, partialTick, this.leftPos, this.topPos);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        if (this.hoveredSlot != null && this.hoveredSlot.index < 3) {
            ItemStack stack = this.hoveredSlot.getItem();
            var requirementsOpt = this.menu.getRequirements();
            if (requirementsOpt.isPresent()) {
                var requirements = requirementsOpt.get();

                ItemStack expected = ItemStack.EMPTY;
                String expectedName = "";
                int requiredCount = 0;

                switch (this.hoveredSlot.index) {
                    case 0 -> {
                        expected = PureBloodItem.getBloodItemForLevel(requirements.pureBloodLevel()).getDefaultInstance();
                        expectedName = expected.getHoverName().getString() + " " + (requirements.pureBloodLevel() + 1);
                        requiredCount = requirements.pureBloodQuantity();
                    }
                    case 1 -> {
                        expected = ModItems.HUMAN_HEART.get().getDefaultInstance();
                        expectedName = expected.getHoverName().getString();
                        requiredCount = requirements.humanHeartQuantity();
                    }
                    case 2 -> {
                        expected = ModItems.VAMPIRE_BOOK.get().getDefaultInstance();
                        expectedName = expected.getHoverName().getString();
                        requiredCount = requirements.vampireBookQuantity();
                    }
                }

                Optional<Component> tooltip = Optional.empty();

                if (!stack.isEmpty() && !stack.is(expected.getItem())) {
                    tooltip = Optional.of(Component.translatable("text.vampirism.altar_infusion.ritual_wrong_item", expectedName));
                } else if (stack.isEmpty() || stack.getCount() < requiredCount) {
                    tooltip = Optional.of(Component.translatable("text.vampirism.altar_infusion.ritual_missing_items", requiredCount - stack.getCount(), expectedName));
                }

                if (tooltip.isPresent() && requiredCount > 0) {
                    graphics.renderTooltip(this.font, tooltip.get(), mouseX, mouseY);
                    return;
                }
            }
        }

        super.renderTooltip(graphics, mouseX, mouseY);
    }
}