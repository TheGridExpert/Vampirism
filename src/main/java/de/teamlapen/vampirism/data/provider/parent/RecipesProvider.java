package de.teamlapen.vampirism.data.provider.parent;

import de.teamlapen.vampirism.api.items.oil.IOil;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.data.recipebuilder.AlchemicalCauldronRecipeBuilder;
import de.teamlapen.vampirism.data.recipebuilder.AlchemyTableRecipeBuilder;
import de.teamlapen.vampirism.data.recipebuilder.ShapedWeaponTableRecipeBuilder;
import de.teamlapen.vampirism.data.recipebuilder.ShapelessWeaponTableRecipeBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public abstract class RecipesProvider extends RecipeProvider {

    protected HolderLookup.RegistryLookup<Item> itemLookup = this.registries.lookupOrThrow(Registries.ITEM);

    public RecipesProvider(HolderLookup.Provider registries, RecipeOutput output) {
        super(registries, output);
    }

    @SafeVarargs
    protected final @NotNull Ingredient potion(Holder<Potion> @NotNull ... potion) {
        return CompoundIngredient.of(Arrays.stream(potion).map(PotionContents::new).map(s -> DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, s, Items.POTION)).toArray(Ingredient[]::new));
    }

    protected @NotNull Ingredient potion(@NotNull Holder<Potion> potion) {
        return DataComponentIngredient.of(false, DataComponents.POTION_CONTENTS, new PotionContents(potion), Items.POTION);
    }

    protected void coffinFromWoolOrDye(RecipeOutput consumer, ItemLike coffin, ItemLike wool, ItemLike dye, String path) {
        coffinFromWool(consumer, coffin, wool, path);
        shapeless(RecipeCategory.DECORATIONS, coffin).requires(ModBlocks.COFFIN_WHITE.get()).requires(dye).unlockedBy("has_coffin", has(ModBlocks.COFFIN_WHITE.get())).unlockedBy("has_dye", has(dye)).save(consumer, path + "_from_white");
    }

    protected void coffinFromWool(RecipeOutput consumer, ItemLike coffin, ItemLike wool, String path) {
        shaped(RecipeCategory.DECORATIONS, coffin).pattern("XXX").pattern("YYY").pattern("XXX").define('X', ItemTags.PLANKS).define('Y', wool).unlockedBy("has_wool", has(wool)).save(consumer, path);
    }

    private void enchantment(ItemStack stack, int level, @NotNull Holder<Enchantment> enchantment) {
        stack.enchant(enchantment, level);
    }


    protected AlchemyTableRecipeBuilder alchemyTable(@NotNull Holder<IOil> oilStack) {
        return AlchemyTableRecipeBuilder.builder(this.itemLookup, oilStack);
    }

    protected AlchemicalCauldronRecipeBuilder cauldronRecipe(ItemLike item) {
        return AlchemicalCauldronRecipeBuilder.cauldronRecipe(this.itemLookup, item.asItem());
    }

    protected AlchemicalCauldronRecipeBuilder cauldronRecipe(ItemLike item, int count) {
        return AlchemicalCauldronRecipeBuilder.cauldronRecipe(this.itemLookup, item.asItem(), count);
    }

    protected ShapedWeaponTableRecipeBuilder shapedWeaponTable(RecipeCategory category, ItemLike item) {
        return ShapedWeaponTableRecipeBuilder.shapedWeaponTable(this.itemLookup, category, item);
    }

    protected ShapedWeaponTableRecipeBuilder shapedWeaponTable(RecipeCategory category, ItemStack stack) {
        return ShapedWeaponTableRecipeBuilder.shapedWeaponTable(this.itemLookup, category, stack);
    }

    protected ShapedWeaponTableRecipeBuilder shapedWeaponTable(RecipeCategory category, ItemLike item, int count) {
        return ShapedWeaponTableRecipeBuilder.shapedWeaponTable(this.itemLookup, category, item, count);
    }

    protected ShapelessWeaponTableRecipeBuilder shapelessWeaponTable(RecipeCategory category, ItemLike item) {
        return ShapelessWeaponTableRecipeBuilder.shapelessWeaponTable(this.itemLookup, category, item);
    }

    protected ShapelessWeaponTableRecipeBuilder shapelessWeaponTable(RecipeCategory category, ItemLike item, int count) {
        return ShapelessWeaponTableRecipeBuilder.shapelessWeaponTable(this.itemLookup, category, item, count);
    }
}
