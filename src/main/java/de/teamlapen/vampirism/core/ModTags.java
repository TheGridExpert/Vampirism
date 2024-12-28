package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.api.entity.player.task.Task;
import de.teamlapen.vampirism.api.items.oil.IOil;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import static de.teamlapen.vampirism.api.util.VResourceLocation.common;
import static de.teamlapen.vampirism.api.util.VResourceLocation.mod;

public class ModTags {
    public static class Blocks {
        public static final TagKey<Block> CURSED_EARTH = tag("cursed_earth");
        public static final TagKey<Block> DARK_SPRUCE_LOG = tag("dark_spruce_log");
        public static final TagKey<Block> CURSED_SPRUCE_LOG = tag("cursed_spruce_log");

        public static final TagKey<Block> TOTEM_TOP_CRAFTED = tag("totem_top_crafted");
        public static final TagKey<Block> TOTEM_TOP_FRAGILE = tag("totem_top_fragile");
        public static final TagKey<Block> TOTEM_TOP = tag("totem_top");

        public static final TagKey<Block> COFFIN = tag("coffin");

        public static final TagKey<Block> DARK_STONE = tag("dark_stone");
        public static final TagKey<Block> DARK_STONE_BRICKS = tag("dark_stone_bricks");
        public static final TagKey<Block> POLISHED_DARK_STONE = tag("polished_dark_brick");
        public static final TagKey<Block> COBBLED_DARK_STONE = tag("cobbled_dark_brick");
        public static final TagKey<Block> DARK_STONE_TILES = tag("dark_brick_tiles");
        public static final TagKey<Block> PURPLE_STONE_BRICKS = tag("purple_brick_bricks");
        public static final TagKey<Block> PURPLE_STONE_TILES = tag("purple_brick_tiles");

        public static final TagKey<Block> NO_SPAWN = tag("no_spawn");
        public static final TagKey<Block> VAMPIRE_SPAWN = tag("vampire_spawn");

        public static final TagKey<Block> REMAINS = tag("remains");
        public static final TagKey<Block> ACTIVE_REMAINS = tag("active_remains");
        public static final TagKey<Block> VULNERABLE_REMAINS = tag("vulnerable_remains");
        public static final TagKey<Block> MOTHER_GROWS_ON = tag("mother_grows_on");

        public static final TagKey<Block> VAMPIRE_BEACON_BASE_BLOCKS = tag("vampire_beacon_base_blocks");
        public static final TagKey<Block> VAMPIRE_BEACON_BASE_ENHANCED_BLOCKS = tag("vampire_beacon_base_enhanced_blocks");

        public static final TagKey<Block> GARLIC = tag(common("crops/garlic"));


        private static @NotNull TagKey<Block> tag(@NotNull ResourceLocation resourceLocation) {
            return BlockTags.create(resourceLocation);
        }

        private static @NotNull TagKey<Block> tag(@NotNull String name) {
            return tag(mod(name));
        }

        public static class Compatibility {
            public static final String SERENE_SEASONS = "sereneseasons";
            public static final TagKey<Block> SERENE_SEASONS_AUTUMN_CROPS = compatTag(SERENE_SEASONS, "autumn_crops");
            public static final TagKey<Block> SERENE_SEASONS_SUMMER_CROPS = compatTag(SERENE_SEASONS, "summer_crops");

            public static final String FARMERS_DELIGHT = "farmersdelight";
            public static final TagKey<Block> TRAY_HEAT_SOURCES = compatTag(FARMERS_DELIGHT, "tray_heat_sources");

            private static @NotNull TagKey<Block> compatTag(@NotNull String namespace, @NotNull String name) {
                return tag(ResourceLocation.fromNamespaceAndPath(namespace, name));
            }
        }
    }

    public static class Items {
        public static final TagKey<Item> CURSED_EARTH = tag("cursed_earth");
        public static final TagKey<Item> DARK_SPRUCE_LOG = tag("dark_spruce_log");
        public static final TagKey<Item> CURSED_SPRUCE_LOG = tag("cursed_spruce_log");

        public static final TagKey<Item> DARK_STONE = tag("dark_stone");
        public static final TagKey<Item> DARK_STONE_BRICKS = tag("dark_stone_bricks");
        public static final TagKey<Item> POLISHED_DARK_STONE = tag("polished_dark_brick");
        public static final TagKey<Item> COBBLED_DARK_STONE = tag("cobbled_dark_brick");
        public static final TagKey<Item> DARK_STONE_TILES = tag("dark_brick_tiles");

        public static final TagKey<Item> NO_SPAWN = tag("no_spawn");
        public static final TagKey<Item> VAMPIRE_SPAWN = tag("vampire_spawn");

        public static final TagKey<Item> PURE_BLOOD = tag("pure_blood");
        public static final TagKey<Item> GARLIC = tag(common("crops/garlic"));
        public static final TagKey<Item> HEART = tag("heart");

        public static final TagKey<Item> HUNTER_INTEL = tag("hunter_intel");
        public static final TagKey<Item> HOLY_WATER = tag("holy_water");
        public static final TagKey<Item> HOLY_WATER_SPLASH = tag("holy_water_splash");
        public static final TagKey<Item> VAMPIRE_BEACON_PAYMENT_ITEM = tag("vampire_beacon_payment_item");

        public static final TagKey<Item> VAMPIRE_SLAYER_ITEMS = tag("enchantable/vampire_slayer");
        public static final TagKey<Item> CROSSBOW_ENCHANTABLE = tag("enchantable/crossbow");

        public static final TagKey<Item> APPLICABLE_OIL_SWORD = tag("applicable_oil/sword");
        public static final TagKey<Item> APPLICABLE_OIL_PICKAXE = tag("applicable_oil/pickaxe");
        public static final TagKey<Item> APPLICABLE_OIL_ARMOR = tag("applicable_oil/armor");

        public static final TagKey<Item> HEART_SEEKER = tag("swords/heart_seeker");
        public static final TagKey<Item> HEART_STRIKER = tag("swords/heart_striker");

        public static final TagKey<Item> CROSSBOW_ARROW = tag("crossbow_arrow");
        public static final TagKey<Item> SINGLE_CROSSBOWS = tag("crossbows/single");
        public static final TagKey<Item> DOUBLE_CROSSBOWS = tag("crossbows/double");
        public static final TagKey<Item> TECH_CROSSBOWS = tag("crossbows/tech");
        public static final TagKey<Item> BASIC_CROSSBOWS = tag("crossbows/basic");
        public static final TagKey<Item> ENHANCED_CROSSBOWS = tag("crossbows/enhanced");
        public static final TagKey<Item> CROSSBOWS = tag("crossbows");

        public static final TagKey<Item> ARMOR_OF_SWIFTNESS = tag("armors/armor_of_swiftness");
        public static final TagKey<Item> ARMOR_OF_SWIFTNESS_NORMAL = tag("armors/armor_of_swiftness/normal");
        public static final TagKey<Item> ARMOR_OF_SWIFTNESS_ENHANCED = tag("armors/armor_of_swiftness/enhanced");
        public static final TagKey<Item> ARMOR_OF_SWIFTNESS_ULTIMATE = tag("armors/armor_of_swiftness/ultimate");

        public static final TagKey<Item> HUNTER_COAT = tag("armors/hunter_coat");
        public static final TagKey<Item> HUNTER_COAT_NORMAL = tag("armors/hunter_coat/normal");
        public static final TagKey<Item> HUNTER_COAT_ENHANCED = tag("armors/hunter_coat/enhanced");
        public static final TagKey<Item> HUNTER_COAT_ULTIMATE = tag("armors/hunter_coat/ultimate");

        public static final TagKey<Item> POWDER_SNOW_WALKABLE_BOOTS = tag("armors/powder_snow_walkable_boots");

        public static final TagKey<Item> VAMPIRE_CLOTHING = tag("armors/vampire_clothing");
        public static final TagKey<Item> VAMPIRE_CLOAK = tag("armors/vampire_clothing/cloak");


        private static @NotNull TagKey<Item> tag(@NotNull ResourceLocation resourceLocation) {
            return ItemTags.create(resourceLocation);
        }

        private static @NotNull TagKey<Item> tag(@NotNull String name) {
            return tag(mod(name));
        }

        public static class Compatibility {
            public static final String COMMON = "c";
            public static final TagKey<Item> CROPS_GARLIC = compatTag(COMMON, "crops/garlic");
            public static final TagKey<Item> FOODS_GARLIC = compatTag(COMMON, "foods/garlic");

            public static final String SERENE_SEASONS = "sereneseasons";
            public static final TagKey<Item> SERENE_SEASONS_AUTUMN_CROPS = compatTag(SERENE_SEASONS, "autumn_crops");
            public static final TagKey<Item> SERENE_SEASONS_SUMMER_CROPS = compatTag(SERENE_SEASONS, "summer_crops");

            private static @NotNull TagKey<Item> compatTag(@NotNull String namespace, @NotNull String name) {
                return tag(ResourceLocation.fromNamespaceAndPath(namespace, name));
            }
        }
    }

    public static class Entities {
        /**
         * All hunter mobs (including _imob)
         */
        public static final TagKey<EntityType<?>> HUNTER = tag("hunter");
        /**
         * All vampire mobs (including _imob)
         */
        public static final TagKey<EntityType<?>> VAMPIRE = tag("vampire");
        /**
         * Both advanced hunter mobs (normal + _imob)
         */
        public static final TagKey<EntityType<?>> ADVANCED_HUNTER = tag("advanced_hunter");
        /**
         * Both advanced vampire mobs (normal + _imob)
         */
        public static final TagKey<EntityType<?>> ADVANCED_VAMPIRE = tag("advanced_vampire");
        public static final TagKey<EntityType<?>> CONVERTED_CREATURES = tag("converted_creatures");
        /**
         * Hunters that stand up for comrades when their attacked
         */
        public static final TagKey<EntityType<?>> COMPARE_DEFENDING_HUNTERS = tag("comrade_defending_hunters");
        /**
         * Vampires that stand up for comrades when their attacked
         */
        public static final TagKey<EntityType<?>> COMPARE_DEFENDING_VAMPIRES = tag("comrade_defending_vampires");

        /**
         * Vanilla zombies
         */
        public static final TagKey<EntityType<?>> ZOMBIES = tag("zombies");
        public static final TagKey<EntityType<?>> IGNORE_VAMPIRE_SWORD_FINISHER = tag("ignore_vampire_sword_finisher");

        private static @NotNull TagKey<EntityType<?>> tag(@NotNull ResourceLocation resourceLocation) {
            return TagKey.create(Registries.ENTITY_TYPE, resourceLocation);
        }

        private static @NotNull TagKey<EntityType<?>> tag(@NotNull String name) {
            return tag(mod(name));
        }
    }

    public static class Fluids {
        public static final TagKey<Fluid> BLOOD = tag("vampirism_blood");
        public static final TagKey<Fluid> IMPURE_BLOOD = tag("vampirism_impure_blood");

        private static @NotNull TagKey<Fluid> tag(@NotNull ResourceLocation resourceLocation) {
            return FluidTags.create(resourceLocation);
        }

        private static @NotNull TagKey<Fluid> tag(@NotNull String name) {
            return tag(mod(name));
        }
    }

    public static class Biomes {

        public static final TagKey<Biome> IS_FACTION_BIOME = tag("has_faction");
        public static final TagKey<Biome> IS_VAMPIRE_BIOME = tag("has_faction/vampire");
        public static final TagKey<Biome> IS_HUNTER_BIOME = tag("has_faction/hunter");

        public static class HasStructure {
            public static final TagKey<Biome> HUNTER_TENT = tag("has_structure/hunter_tent");
            public static final TagKey<Biome> VAMPIRE_DUNGEON = tag("has_structure/vampire_dungeon");
            public static final TagKey<Biome> VAMPIRE_HUT = tag("has_structure/vampire_hut");
            public static final TagKey<Biome> HUNTER_OUTPOST_PLAINS = tag("has_structure/outpost/plains");
            public static final TagKey<Biome> HUNTER_OUTPOST_DESERT = tag("has_structure/outpost/desert");
            public static final TagKey<Biome> HUNTER_OUTPOST_VAMPIRE_FOREST = tag("has_structure/outpost/vampire_forest");
            public static final TagKey<Biome> HUNTER_OUTPOST_BADLANDS = tag("has_structure/outpost/badlands");
            public static final TagKey<Biome> VAMPIRE_ALTAR = tag("has_structure/vampire_altar");
            public static final TagKey<Biome> MOTHER = tag("has_structure/mother");
            public static final TagKey<Biome> CRYPT = tag("has_structure/crypt");
        }

        public static class HasSpawn {
            public static final TagKey<Biome> VAMPIRE = tag("has_spawn/vampire");
            public static final TagKey<Biome> ADVANCED_VAMPIRE = tag("has_spawn/advanced_vampire");
            public static final TagKey<Biome> HUNTER = tag("has_spawn/hunter");
            public static final TagKey<Biome> ADVANCED_HUNTER = tag("has_spawn/advanced_hunter");
        }

        public static class NoSpawn {
            public static final TagKey<Biome> VAMPIRE = tag("no_spawn/vampire");
            public static final TagKey<Biome> ADVANCED_VAMPIRE = tag("no_spawn/advanced_vampire");
            public static final TagKey<Biome> HUNTER = tag("no_spawn/hunter");
            public static final TagKey<Biome> ADVANCED_HUNTER = tag("no_spawn/advanced_hunter");
        }

        private static @NotNull TagKey<Biome> tag(@NotNull String name) {
            return TagKey.create(Registries.BIOME, mod(name));
        }
    }

    public static class PoiTypes {
        public static final TagKey<PoiType> HAS_FACTION = tag("has_faction");
        public static final TagKey<PoiType> IS_VAMPIRE = tag("is_vampire");
        public static final TagKey<PoiType> IS_HUNTER = tag("is_hunter");

        private static @NotNull TagKey<PoiType> tag(@NotNull String name) {
            return TagKey.create(Registries.POINT_OF_INTEREST_TYPE, mod(name));
        }
    }

    public static class Professions {
        public static final TagKey<VillagerProfession> HAS_FACTION = tag("has_faction");
        public static final TagKey<VillagerProfession> IS_VAMPIRE = tag("is_vampire");
        public static final TagKey<VillagerProfession> IS_HUNTER = tag("is_hunter");

        private static @NotNull TagKey<VillagerProfession> tag(@NotNull String name) {
            return TagKey.create(Registries.VILLAGER_PROFESSION, mod(name));
        }
    }

    public static class DamageTypes {

        public static final TagKey<DamageType> ENTITY_PHYSICAL = tag("entity_physical");
        public static final TagKey<DamageType> REMAINS_INVULNERABLE = tag("remains_invulnerable");
        public static final TagKey<DamageType> MOTHER_RESISTANT_TO = tag("mother_resistant_to");
        public static final TagKey<DamageType> VAMPIRE_IMMORTAL = tag("vampire_immortal");

        private static @NotNull TagKey<DamageType> tag(@NotNull String name) {
            return TagKey.create(Registries.DAMAGE_TYPE, mod(name));
        }
    }

    public static class Tasks {

        public static final TagKey<Task> HAS_FACTION = tag("has_faction");
        public static final TagKey<Task> IS_VAMPIRE = tag("has_faction/vampire");
        public static final TagKey<Task> IS_HUNTER = tag("has_faction/hunter");
        public static final TagKey<Task> IS_UNIQUE = tag("is_unique");
        public static final TagKey<Task> AWARDS_LORD_LEVEL = tag("awards_lord_level");

        private static @NotNull TagKey<Task> tag(@NotNull String name) {
            return TagKey.create(VampirismRegistries.Keys.TASK, mod(name));
        }
    }

    public static class Structures {
        public static final TagKey<Structure> ON_ANCIENT_REMAINS_MAPS = tag("on_ancient_remains_maps");
        public static final TagKey<Structure> ON_CRYPT_MAPS = tag("on_crypt_maps");
        public static final TagKey<Structure> HUNTER_OUTPOST = tag("hunter_outpost");

        private static @NotNull TagKey<Structure> tag(@NotNull String name) {
            return TagKey.create(Registries.STRUCTURE, mod(name));
        }

    }

    public static class SkillTrees {
        public static final TagKey<ISkillTree> HUNTER = tag("faction/hunter");
        public static final TagKey<ISkillTree> VAMPIRE = tag("faction/vampire");
        public static final TagKey<ISkillTree> LEVEL = tag("type/level");
        public static final TagKey<ISkillTree> LORD = tag("type/lord");

        private static @NotNull TagKey<ISkillTree> tag(@NotNull String name) {
            return TagKey.create(VampirismRegistries.Keys.SKILL_TREE, mod(name));
        }

    }

    public static class Effects {
        public static final TagKey<MobEffect> HUNTER_POTION_RESISTANCE = tag("hunter_potion_resistance");

        private static @NotNull TagKey<MobEffect> tag(@NotNull String name) {
            return TagKey.create(Registries.MOB_EFFECT, mod(name));
        }

    }

    public static class Enchantments {
        public static final TagKey<Enchantment> CROSSBOW_INCOMPATIBLE = tag("crossbow_incompatible");

        private static @NotNull TagKey<Enchantment> tag(@NotNull String name) {
            return TagKey.create(Registries.ENCHANTMENT, mod(name));
        }

    }

    public static class Oils {
        public static final TagKey<IOil> NON_TREASURE = tag("non_treasure");

        private static @NotNull TagKey<IOil> tag(@NotNull String name) {
            return TagKey.create(VampirismRegistries.Keys.OIL, mod(name));
        }

    }

}
