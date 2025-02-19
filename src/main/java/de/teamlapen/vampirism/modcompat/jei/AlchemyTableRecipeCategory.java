package de.teamlapen.vampirism.modcompat.jei;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.items.component.OilContent;
import de.teamlapen.vampirism.recipes.AlchemyTableRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.List;

public class AlchemyTableRecipeCategory implements IRecipeCategory<RecipeHolder<AlchemyTableRecipe>> {

    private static final ResourceLocation location = VResourceLocation.mod("textures/gui/container/alchemy_table.png");

    private final @NotNull Component localizedName;
    private final @NotNull IDrawable icon;
    private final @NotNull IDrawable background;
    private final @NotNull IDrawableStatic blazeHeat;
    private final @NotNull IDrawableAnimated arrow;
    private final @NotNull IDrawableAnimated pool;

    public AlchemyTableRecipeCategory(@NotNull IGuiHelper helper) {
        this.localizedName = ModBlocks.ALCHEMY_TABLE.get().getName();
        this.background = helper.drawableBuilder(location, 11, 12, 149, 80).addPadding(0, 30, 0, 0).build();
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(ModBlocks.ALCHEMY_TABLE.get()));
        this.blazeHeat = helper.createDrawable(location, 176, 9, 18, 4);
        this.arrow = helper.drawableBuilder(location, 176, 1, 28, 8).buildAnimated(600, IDrawableAnimated.StartDirection.LEFT, false);
        this.pool = helper.drawableBuilder(location, 176, 13, 32, 32).buildAnimated(600, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public @NotNull RecipeType<RecipeHolder<AlchemyTableRecipe>> getRecipeType() {
        return VampirismJEIPlugin.ALCHEMY_TABLE;
    }

    @NotNull
    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public int getWidth() {
        return 149;
    }

    @Override
    public int getHeight() {
        return 110;
    }

    @NotNull
    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull RecipeHolder<AlchemyTableRecipe> holder, @NotNull IFocusGroup focuses) {
        AlchemyTableRecipe recipe = holder.value();
        builder.addSlot(RecipeIngredientRole.INPUT, 4, 13).addIngredients(recipe.getInput());
        builder.addSlot(RecipeIngredientRole.INPUT, 44, 4).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.INPUT, 68, 4).addIngredients(recipe.getIngredient());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 101, 60).addItemStack(RecipeUtil.getResultItem(recipe));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 129, 32).addItemStack(RecipeUtil.getResultItem(recipe));
        builder.addSlot(RecipeIngredientRole.INPUT, 23, 57).addItemStack(new ItemStack(Items.BLAZE_POWDER));
    }

    @Override
    public void draw(@NotNull RecipeHolder<AlchemyTableRecipe> holder, IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics graphics, double mouseX, double mouseY) {
        this.background.draw(graphics);
        graphics.pose().pushPose();
        AlchemyTableRecipe recipe = holder.value();
        this.blazeHeat.draw(graphics, 33 - 9 - 2, 60 - 10 - 2);
        this.arrow.draw(graphics, 73 - 9 - 2, 57 - 10 - 2);

        int color = OilContent.getOil(RecipeUtil.getResultItem(recipe)).value().getColor();
        graphics.setColor(((color >> 16) & 0xFF) / 255f, ((color >> 8) & 0xFF) / 255f, ((color) & 0xFF) / 255f, 1F);
        this.pool.draw(graphics, 104 - 9 - 2, 36 - 10 - 2);
        graphics.setColor(1, 1, 1, 1);

        int x = 2;
        int y = 80;
        Minecraft minecraft = Minecraft.getInstance();

        List<ISkill<?>> requiredSkills = recipe.getRequiredSkills();
        if (!requiredSkills.isEmpty()) {
            MutableComponent skillText = Component.translatable("gui.vampirism.skill_required", " ");

            for (ISkill<?> skill : recipe.getRequiredSkills()) {
                skillText.append(skill.getName()).append(" ");

            }
            y += UtilLib.renderMultiLine(minecraft.font, graphics, skillText, 132, x, y, Color.gray.getRGB());

        }

        graphics.pose().popPose();
    }
}
