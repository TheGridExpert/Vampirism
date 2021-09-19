package de.teamlapen.vampirism.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.inventory.container.HunterTableContainer;
import de.teamlapen.vampirism.items.PureBloodItem;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

/**
 * Gui for the hunter table
 */
@OnlyIn(Dist.CLIENT)
public class HunterTableScreen extends AbstractContainerScreen<HunterTableContainer> {
    private static final ResourceLocation altarGuiTextures = new ResourceLocation(REFERENCE.MODID, "textures/gui/hunter_table.png");
    @SuppressWarnings("FieldCanBeLocal")
    private final ContainerLevelAccess worldPos;

    public HunterTableScreen(HunterTableContainer inventorySlotsIn, Inventory playerInventory, Component name) {
        this(inventorySlotsIn, playerInventory, name, ContainerLevelAccess.NULL);
    }

    public HunterTableScreen(HunterTableContainer inventorySlotsIn, Inventory playerInventory, Component name, ContainerLevelAccess worldPosIn) {
        super(inventorySlotsIn, playerInventory, name);
        this.worldPos = worldPosIn;
    }

    @Override
    public void render(@Nonnull PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);

    }

    @Override
    protected void renderBg(@Nonnull PoseStack stack, float var1, int var2, int var3) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, altarGuiTextures);
        this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(@Nonnull PoseStack stack, int mouseX, int mouseY) {
        super.renderLabels(stack, mouseX, mouseY);

        Component text = null;
        if (!menu.isLevelValid(false)) {
            text = new TranslatableComponent("container.vampirism.hunter_table.level_wrong");
        } else if (!menu.isLevelValid(true)) {
            text = new TranslatableComponent("container.vampirism.hunter_table.structure_level_wrong");
        } else if (!menu.getMissingItems().isEmpty()) {
            ItemStack missing = menu.getMissingItems();
            Component item = missing.getItem() instanceof PureBloodItem ? ((PureBloodItem) missing.getItem()).getCustomName() : new TranslatableComponent(missing.getDescriptionId());
            text = new TranslatableComponent("text.vampirism.hunter_table.ritual_missing_items", missing.getCount(), item);
        }
        if (text != null) this.font.drawWordWrap(text, 8, 50, this.imageWidth - 10, 0x000000);
    }
}
