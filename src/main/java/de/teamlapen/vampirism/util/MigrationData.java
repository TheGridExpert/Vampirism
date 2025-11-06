package de.teamlapen.vampirism.util;

import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.entity.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.entity.player.vampire.skills.VampireSkills;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;

import java.util.Map;

public class MigrationData {

    private static final Map<String, String> VAMPIRE_SKILL_REMAPS = Map.ofEntries(
            Map.entry("vampirism:bat", "vampirism:fledgling")
    );

    private static final Map<String, String> HUNTER_SKILL_REMAPS = Map.ofEntries(
            Map.entry("vampirism:garlic_beacon_improved", "vampirism:garlic_diffuser_improved"),
            Map.entry("vampirism:garlic_beacon", "vampirism:garlic_diffuser"),
            Map.entry("vampirism:holy_water_enhanced", "vampirism:enhanced_blessing")
    );

    private static final Map<String, String> POTION_REMAPS = Map.ofEntries(
            Map.entry("vampirism:long_strong_resistance", "vampirism:long_resistance"),
            Map.entry("vampirism:very_long_resistance", "vampirism:long_resistance"),
            Map.entry("vampirism:very_strong_resistance", "vampirism:strong_resistance"),
            Map.entry("vampirism:very_strong_harming", "strong_harming")
    );

    private static final Map<String, String> BLOCK_ENTITY_REMAPS = Map.ofEntries(
            Map.entry("vampirism:garlic_beacon", "vampirism:garlic_diffuser"),
            Map.entry("vampirism:sieve", "vampirism:blood_sieve")
    );

    private static final Map<String, String> BLOCK_REMAPS = Map.ofEntries(
            Map.entry("vampirism:blood_potion_table", "vampirism:potion_table"),
            Map.entry("vampirism:garlic_beacon_normal", "vampirism:totem_top_vampirism_hunter_crafted"),
            Map.entry("vampirism:garlic_beacon_weak", "vampirism:garlic_diffuser_weak"),
            Map.entry("vampirism:garlic_beacon_improved", "vampirism:garlic_diffuser_improved"),
            Map.entry("vampirism:church_altar", "vampirism:altar_cleansing"),
            Map.entry("vampirism:vampire_spruce_leaves", "vampirism:dark_spruce_leaves"),
            Map.entry("vampirism:bloody_spruce_leaves", "vampirism:dark_spruce_leaves"),
            Map.entry("vampirism:bloody_spruce_log", "vampirism:cursed_spruce_log"),
            Map.entry("vampirism:cursed_grass_block", "vampirism:cursed_grass"),
            Map.entry("castle_block_dark_brick", "vampirism:dark_stone_bricks"),
            Map.entry("castle_block_dark_brick_bloody", "vampirism:bloody_dark_stone_bricks"),
            Map.entry("castle_block_dark_stone", "vampirism:dark_stone"),
            Map.entry("castle_block_normal_brick", "stone_bricks"),
            Map.entry("castle_slab_dark_brick", "vampirism:dark_stone_brick_slab"),
            Map.entry("castle_slab_dark_stone", "vampirism:dark_stone_slab"),
            Map.entry("castle_stairs_dark_brick", "vampirism:dark_stone_brick_stairs"),
            Map.entry("castle_stairs_dark_stone", "vampirism:dark_stone_stairs"),
            Map.entry("castle_block_dark_brick_cracked", "vampirism:cracked_dark_stone_bricks"),
            Map.entry("castle_block_dark_brick_wall", "vampirism:dark_stone_brick_wall"),
            Map.entry("castle_block_purple_brick", "vampirism:purple_stone_bricks"),
            Map.entry("castle_slab_purple_brick", "vampirism:purple_stone_brick_slab"),
            Map.entry("castle_stairs_purple_brick", "vampirism:purple_stone_brick_stairs"),
            Map.entry("castle_block_purple_brick_wall", "vampirism:purple_stone_brick_wall"),
            Map.entry("dark_spruce_pressure_place", "vampirism:dark_spruce_pressure_plate"),
            Map.entry("cursed_spruce_pressure_place", "vampirism:cursed_spruce_pressure_plate"),
            Map.entry("vampirism:candelabra_wall", "vampirism:wall_candelabra_normal"),
            Map.entry("vampirism:cursed_spruce_log_cured", "vampirism:cursed_spruce_log"),
            Map.entry("vampirism:cursed_spruce_wood_cured", "vampirism:cursed_spruce_wood"),
            Map.entry("vampirism:tombstone1", "vampirism:tombstone_short"),
            Map.entry("vampirism:tombstone2", "vampirism:tombstone_normal"),
            Map.entry("vampirism:tombstone3", "vampirism:tombstone_cross")
    );

    private static final Map<String, String> ITEM_REMAPS = Map.ofEntries(
            Map.entry("vampirism:vampire_clothing_head", "vampirism:vampire_clothing_crown"),
            Map.entry("vampirism:vampire_clothing_feet", "vampirism:vampire_clothing_boots"),
            Map.entry("vampirism:garlic_beacon_core", "vampirism:garlic_diffuser_core"),
            Map.entry("vampirism:garlic_beacon_core_improved", "vampirism:garlic_diffuser_core_improved"),
            Map.entry("vampirism:garlic_beacon_normal", "vampirism:garlic_diffuser_normal"),
            Map.entry("vampirism:garlic_beacon_weak", "vampirism:garlic_diffuser_weak"),
            Map.entry("vampirism:garlic_beacon_improved", "vampirism:garlic_diffuser_improved"),
            Map.entry("vampirism:church_altar", "vampirism:altar_cleansing"),
            Map.entry("vampirism:item_med_chair", "vampirism:med_chair"),
            Map.entry("vampirism:bloody_spruce_log", "vampirism:cursed_spruce_log"),
            Map.entry("vampirism:bloody_spruce_leaves", "vampirism:dark_spruce_leaves"),
            Map.entry("vampirism:coffin", "vampirism:coffin_red"),
            Map.entry("vampirism:holy_salt_water", "vampirism:pure_salt_water"),
            Map.entry("vampirism:holy_salt", "vampirism:pure_salt"),
            Map.entry("vampirism:injection_zombie_blood", "apple"),
            Map.entry("vampirism:cure_apple", "golden_apple"),
            Map.entry("vampirism:obsidian_armor_head_normal", "vampirism:hunter_coat_head_normal"),
            Map.entry("vampirism:obsidian_armor_chest_normal", "vampirism:hunter_coat_chest_normal"),
            Map.entry("vampirism:obsidian_armor_legs_normal", "vampirism:hunter_coat_legs_normal"),
            Map.entry("vampirism:obsidian_armor_feet_normal", "vampirism:hunter_coat_feet_normal"),
            Map.entry("vampirism:obsidian_armor_head_enhanced", "vampirism:hunter_coat_head_enhanced"),
            Map.entry("vampirism:obsidian_armor_chest_enhanced", "vampirism:hunter_coat_chest_enhanced"),
            Map.entry("vampirism:obsidian_armor_legs_enhanced", "vampirism:hunter_coat_legs_enhanced"),
            Map.entry("vampirism:obsidian_armor_feet_enhanced", "vampirism:hunter_coat_feet_enhanced"),
            Map.entry("vampirism:obsidian_armor_head_ultimate", "vampirism:hunter_coat_head_ultimate"),
            Map.entry("vampirism:obsidian_armor_chest_ultimate", "vampirism:hunter_coat_chest_ultimate"),
            Map.entry("vampirism:obsidian_armor_legs_ultimate", "vampirism:hunter_coat_legs_ultimate"),
            Map.entry("vampirism:obsidian_armor_feet_ultimate", "vampirism:hunter_coat_feet_ultimate"),
            Map.entry("vampirism:item_garlic", "vampirism:garlic"),
            Map.entry("vampirism:item_candelabra", "vampirism:candelabra_normal"),
            Map.entry("vampirism:vampire_cloak_black_blue", "vampirism:vampire_cloak_blue"),
            Map.entry("vampirism:vampire_cloak_black_red", "vampirism:vampire_cloak_black"),
            Map.entry("vampirism:vampire_cloak_black_white", "vampirism:vampire_cloak_black"),
            Map.entry("vampirism:vampire_cloak_red_black", "vampirism:vampire_cloak_red"),
            Map.entry("vampirism:vampire_cloak_white_black", "vampirism:vampire_cloak_white"),
            Map.entry("vampirism:hunter_hat_head_0", "vampirism:hunter_hat_tall"),
            Map.entry("vampirism:hunter_hat_head_1", "vampirism:hunter_hat_broad"),
            Map.entry("vampirism:injection_empty", "vampirism:syringe_empty"),
            Map.entry("vampirism:cursed_spruce_log_cured", "vampirism:cursed_spruce_log"),
            Map.entry("vampirism:cursed_spruce_wood_cured", "vampirism:cursed_spruce_wood"),
            Map.entry("vampirism:tombstone1", "vampirism:tombstone_short"),
            Map.entry("vampirism:tombstone2", "vampirism:tombstone_normal"),
            Map.entry("vampirism:tombstone3", "vampirism:tombstone_cross")
    );

    private static final Map<String, String> ENTITY_REMAPS = Map.ofEntries(
            Map.entry("vampirism:vampire_hunter", "vampirism:hunter"),
            Map.entry("vampirism:vampire_hunter_imob", "vampirism:hunter_imob"),
            Map.entry("vampirism:boat", "boat"),
            Map.entry("vampirism:chest_boat", "chest_boat")
    );

    private static final Map<String, String> FLUID_REMAPS = Map.ofEntries(
            Map.entry("vampirism:impure_blood", "vampirism:blood")
    );

    private static final Map<String, String> FLUID_TYPE_REMAPS = Map.ofEntries(
            Map.entry("vampirism:impure_blood", "vampirism:blood")
    );

    private static final Map<String, String> EFFECT_REMAPS = Map.ofEntries(
            Map.entry("vampirism:thirst", "hunger")
    );

    private static final Map<String, String> POI_REMAPS = Map.ofEntries(
            Map.entry("vampirism:church_altar", "vampirism:altar_cleansing")
    );

    @SubscribeEvent
    public static void remapMissing(NewRegistryEvent event) {
        remap(VAMPIRE_SKILL_REMAPS, VampireSkills.SKILLS);
        remap(HUNTER_SKILL_REMAPS, HunterSkills.SKILLS);
        remap(POTION_REMAPS, ModPotions.POTIONS);
        remap(BLOCK_ENTITY_REMAPS, ModBlockEntities.BLOCK_ENTITY_TYPES);
        remap(BLOCK_REMAPS, ModBlocks.BLOCKS);
        remap(ITEM_REMAPS, ModItems.ITEMS);
        remap(ENTITY_REMAPS, ModEntities.ENTITY_TYPES);
        remap(FLUID_REMAPS, ModFluids.FLUIDS);
        remap(FLUID_TYPE_REMAPS, ModFluids.FLUID_TYPES);
        remap(EFFECT_REMAPS, ModEffects.EFFECTS);
        remap(POI_REMAPS, ModVillage.POI_TYPES);
    }

    public static void remap(Map<String, String> map, DeferredRegister<?> register) {
        map.forEach((id, newId) -> register.addAlias(ResourceLocation.parse(id), ResourceLocation.parse(newId)));
    }
}
