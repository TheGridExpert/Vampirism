package de.teamlapen.vampirism.core;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.items.IWeaponTableRecipe;
import de.teamlapen.vampirism.recipes.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.jetbrains.annotations.NotNull;

/**
 * Handles all recipe registrations and reference.
 */
@SuppressWarnings("unused")
public class ModRecipes {
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, REFERENCE.MODID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, REFERENCE.MODID);
    private static final DeferredRegister<MapCodec<? extends ICondition>> CONDITION_CODECS = DeferredRegister.create(NeoForgeRegistries.Keys.CONDITION_CODECS, REFERENCE.MODID);
    private static final DeferredRegister<RecipeBookCategory> RECIPE_BOOK_CATEGORIES = DeferredRegister.create(Registries.RECIPE_BOOK_CATEGORY, REFERENCE.MODID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<IWeaponTableRecipe>> WEAPONTABLE_CRAFTING_TYPE = RECIPE_TYPES.register("weapon_table", () -> new RecipeType<>() {
        public @NotNull String toString() {
            return "weapon_table";
        }
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemicalCauldronRecipe>> ALCHEMICAL_CAULDRON_TYPE = RECIPE_TYPES.register("alchemical_cauldron", () -> new RecipeType<>() {
        public @NotNull String toString() {
            return "alchemical_cauldron";
        }
    });
    public static final DeferredHolder<RecipeType<?>, RecipeType<AlchemyTableRecipe>> ALCHEMICAL_TABLE_TYPE = RECIPE_TYPES.register("alchemical_table", () -> new RecipeType<>() {
        public @NotNull String toString() {
            return "alchemical_table";
        }
    });

    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> WEAPON_TABLE_CATEGORY = RECIPE_BOOK_CATEGORIES.register("weapontable", RecipeBookCategory::new);
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ALCHEMICAL_TABLE_CATEGORY = RECIPE_BOOK_CATEGORIES.register("alchemical_table", RecipeBookCategory::new);
    public static final DeferredHolder<RecipeBookCategory, RecipeBookCategory> ALCHEMICAL_CAULDRON_CATEGORY = RECIPE_BOOK_CATEGORIES.register("alchemical_cauldron", RecipeBookCategory::new);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedWeaponTableRecipe>> SHAPED_CRAFTING_WEAPONTABLE = RECIPE_SERIALIZERS.register("shaped_crafting_weapontable", ShapedWeaponTableRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapelessWeaponTableRecipe>> SHAPELESS_CRAFTING_WEAPONTABLE = RECIPE_SERIALIZERS.register("shapeless_crafting_weapontable", ShapelessWeaponTableRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ShapedRecipe>> REPAIR_IITEMWITHTIER = RECIPE_SERIALIZERS.register("repair_iitemwithtier", ShapedItemWithTierRepair.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemicalCauldronRecipe>> ALCHEMICAL_CAULDRON = RECIPE_SERIALIZERS.register("alchemical_cauldron", AlchemicalCauldronRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<AlchemyTableRecipe>> ALCHEMICAL_TABLE = RECIPE_SERIALIZERS.register("alchemical_table", AlchemyTableRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<ApplicableOilRecipe>> APPLICABLE_OIL = RECIPE_SERIALIZERS.register("applicable_oil", () -> new CustomRecipe.Serializer<>(ApplicableOilRecipe::new));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<CleanOilRecipe>> CLEAN_OIL = RECIPE_SERIALIZERS.register("clean_oil", () -> new CustomRecipe.Serializer<>(CleanOilRecipe::new));

    public static final DeferredHolder<MapCodec<? extends ICondition>, MapCodec<ConfigCondition>> CONFIG_CONDITION = CONDITION_CODECS.register("config", () -> ConfigCondition.CODEC);

    static void register(@NotNull IEventBus bus) {
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        CONDITION_CODECS.register(bus);
    }

}
