package de.teamlapen.vampirism.data.recipebuilder;

import de.teamlapen.vampirism.recipes.InfuserRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class InfuserRecipeBuilder implements RecipeBuilder {
    private final HolderLookup.RegistryLookup<Item> itemLookup;
    @Nullable
    private final ItemStack result;
    protected String group = "";
    protected final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    private final List<Ingredient> ingredients = Arrays.asList(new Ingredient[4]);
    private Ingredient input;
    private final List<ItemStack> results = Arrays.asList(new ItemStack[3]);
    private int burnTime = 200;

    public InfuserRecipeBuilder(HolderLookup.RegistryLookup<Item> itemLookup, ItemStack result) {
        this.itemLookup = itemLookup;
        this.result = result;
    }

    public InfuserRecipeBuilder(HolderLookup.RegistryLookup<Item> itemLookup) {
        this.itemLookup = itemLookup;
        this.result = null;
    }

    @Override
    public @NotNull InfuserRecipeBuilder unlockedBy(@NotNull String name, @NotNull Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public @NotNull InfuserRecipeBuilder group(@Nullable String groupName) {
        this.group = groupName;
        return this;
    }

    public InfuserRecipeBuilder ingredient(Ingredient ingredient) {
        int i = ingredients.indexOf(null);
        if (i == -1) {
            throw new IllegalArgumentException("Ingredients are already filled");
        }
        this.ingredients.set(i, ingredient);
        return this;
    }

    public InfuserRecipeBuilder ingredient(int number, Ingredient ingredient) {
        this.ingredients.set(number, ingredient);
        return this;
    }

    public InfuserRecipeBuilder ingredients(Ingredient ingredient) {
        Collections.fill(this.ingredients, ingredient);
        return this;
    }

    public InfuserRecipeBuilder input(Ingredient input) {
        this.input = input;
        return this;
    }

    public InfuserRecipeBuilder burnTime(int burnTime) {
        this.burnTime = burnTime;
        return this;
    }

    public InfuserRecipeBuilder result(ItemStack result) {
        int i = results.indexOf(null);
        if (i == -1) {
            throw new IllegalArgumentException("Results are already filled");
        }
        this.results.set(i, result);
        return this;
    }

    public InfuserRecipeBuilder result(int number, ItemStack result) {
        this.results.set(number, result);
        return this;
    }

    public InfuserRecipeBuilder results(ItemStack result) {
        Collections.fill(this.results, result);
        return this;
    }

    @Override
    public @NotNull Item getResult() {
        return this.result.getItem();
    }

    @Override
    public void save(@NotNull RecipeOutput recipeOutput, @NotNull ResourceKey<Recipe<?>> key) {
        Advancement.Builder advancement = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(key))
                .rewards(AdvancementRewards.Builder.recipe(key))
                .requirements(AdvancementRequirements.Strategy.OR);
        this.criteria.forEach(advancement::addCriterion);
        var recipe = new InfuserRecipe(this.group, this.ingredients.get(0), this.ingredients.get(1), this.ingredients.get(2), this.ingredients.get(3), this.input, this.results.get(0), this.results.get(1), this.results.get(2), Optional.ofNullable(this.result), this.burnTime);
        recipeOutput.accept(key, recipe, advancement.build(key.location().withPrefix("recipes/infuser/")));
    }

    public static InfuserRecipeBuilder infuserRecipe(HolderLookup.RegistryLookup<Item> itemLookup, ItemStack result) {
        return new InfuserRecipeBuilder(itemLookup, result);
    }

    public static InfuserRecipeBuilder infuserRecipe(HolderLookup.RegistryLookup<Item> itemLookup) {
        return new InfuserRecipeBuilder(itemLookup);
    }
}
