package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.tags.ModItemTags;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.equipment.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.Tags;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class ModArmorMaterials {

    public static class Asset {
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOTH = createId("vampire_cloth");
        public static final ResourceKey<EquipmentAsset> HUNTER_COAT_NORMAL = createId("hunter_coat");
        public static final ResourceKey<EquipmentAsset> HUNTER_COAT_ENHANCED = createId("hunter_coat_enhanced");
        public static final ResourceKey<EquipmentAsset> HUNTER_COAT_ULTIMATE = createId("hunter_coat_ultimate");
        public static final ResourceKey<EquipmentAsset> SWIFTNESS_NORMAL = createId("armor_of_swiftness_normal");
        public static final ResourceKey<EquipmentAsset> SWIFTNESS_ENHANCED = createId("armor_of_swiftness_enhanced");
        public static final ResourceKey<EquipmentAsset> SWIFTNESS_ULTIMATE = createId("armor_of_swiftness_ultimate");
        public static final ResourceKey<EquipmentAsset> HUNTER_HAT_0 = createId("hunter_hat_0");
        public static final ResourceKey<EquipmentAsset> HUNTER_HAT_1 = createId("hunter_hat_1");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOAK_BLACK_BLUE = createId("vampire_cloak_black_blue");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOAK_BLACK_RED = createId("vampire_cloak_black_red");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOAK_BLACK_WHITE = createId("vampire_cloak_black_white");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOAK_RED_BLACK = createId("vampire_cloak_red_black");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOAK_WHITE_BLACK = createId("vampire_cloak_white_black");

        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOTH_CROWN = createId("vampire_clothing_crown");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOTH_HAT = createId("vampire_clothing_hat");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOTH_LEGS = createId("vampire_clothing_legs");
        public static final ResourceKey<EquipmentAsset> VAMPIRE_CLOTH_BOOTS = createId("vampire_clothing_boots");

        private static ResourceKey<EquipmentAsset> createId(String id) {
            return ResourceKey.create(EquipmentAssets.ROOT_ID, VResourceLocation.mod(id));
        }
    }

    private static final ArmorMaterial VAMPIRE_CLOTH = register(4, createReduction(1, 3, 2, 1), 15, SoundEvents.ARMOR_EQUIP_LEATHER, 0,0 ,ModItemTags.HEART, Asset.VAMPIRE_CLOTH);
    public static final ArmorMaterial VAMPIRE_CLOTH_CROWN = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOTH_CROWN);
    public static final ArmorMaterial VAMPIRE_CLOTH_HAT = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOTH_HAT);
    public static final ArmorMaterial VAMPIRE_CLOTH_LEGS = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOTH_LEGS);
    public static final ArmorMaterial VAMPIRE_CLOTH_BOOTS = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOTH_BOOTS);
    public static final ArmorMaterial VAMPIRE_CLOAK_BLACK_BLUE = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOAK_BLACK_BLUE);
    public static final ArmorMaterial VAMPIRE_CLOAK_BLACK_RED = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOAK_BLACK_RED);
    public static final ArmorMaterial VAMPIRE_CLOAK_BLACK_WHITE = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOAK_BLACK_WHITE);
    public static final ArmorMaterial VAMPIRE_CLOAK_RED_BLACK = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOAK_RED_BLACK);
    public static final ArmorMaterial VAMPIRE_CLOAK_WHITE_BLACK = copyWithNewLayer(VAMPIRE_CLOTH, Asset.VAMPIRE_CLOAK_WHITE_BLACK);
    public static final ArmorMaterial NORMAL_HUNTER_COAT = register(4, createReduction(2, 6, 5, 2), 10, SoundEvents.ARMOR_EQUIP_IRON, 2, 0, Tags.Items.INGOTS_IRON, Asset.HUNTER_COAT_NORMAL );
    public static final ArmorMaterial ENHANCED_HUNTER_COAT = register(4, createReduction(3, 8, 6, 3), 10, SoundEvents.ARMOR_EQUIP_IRON, 2, 0, Tags.Items.INGOTS_IRON, Asset.HUNTER_COAT_ENHANCED);
    public static final ArmorMaterial ULTIMATE_HUNTER_COAT = register(4, createReduction(3, 9, 9, 3), 10, SoundEvents.ARMOR_EQUIP_IRON, 2, 0, Tags.Items.GEMS_DIAMOND, Asset.HUNTER_COAT_ULTIMATE);
    public static final ArmorMaterial NORMAL_SWIFTNESS = register(4, createReduction(1, 3, 2, 1), 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0,Tags.Items.LEATHERS, Asset.SWIFTNESS_NORMAL);
    public static final ArmorMaterial ENHANCED_SWIFTNESS = register(4, createReduction(2, 6, 5, 2), 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, Tags.Items.LEATHERS, Asset.SWIFTNESS_ENHANCED);
    public static final ArmorMaterial ULTIMATE_SWIFTNESS = register(4, createReduction(3, 8, 6, 3), 12, SoundEvents.ARMOR_EQUIP_LEATHER, 0, 0, Tags.Items.LEATHERS, Asset.SWIFTNESS_ULTIMATE);
    public static final ArmorMaterial HUNTER_HAT_0 = copyWithNewLayer(ArmorMaterials.IRON, Asset.HUNTER_HAT_0);
    public static final ArmorMaterial HUNTER_HAT_1 = copyWithNewLayer(ArmorMaterials.IRON, Asset.HUNTER_HAT_1);

    private static ArmorMaterial copyWithNewLayer(ArmorMaterial source, ResourceKey<EquipmentAsset> asset) {
        return new ArmorMaterial(source.durability(), source.defense(), source.enchantmentValue(), source.equipSound(), source.toughness(), source.knockbackResistance(), source.repairIngredient(), asset);
    }

    private static ArmorMaterial register(int durability, Map<ArmorType, Integer> defense, int enchantmentValue, Holder<SoundEvent> equipSound, float toughness, float knockbackResistance, TagKey<Item> repairIngredient, ResourceKey<EquipmentAsset> equipmentAsset) {
        return new ArmorMaterial(durability, defense, enchantmentValue, equipSound, toughness, knockbackResistance, repairIngredient, equipmentAsset);
    }

    private static EnumMap<ArmorType, Integer> createReduction(int helmet, int chestplate, int leggings, int boots) {
        return Util.make(new EnumMap<>(ArmorType.class), (map) -> {
            map.put(ArmorType.BOOTS, boots);
            map.put(ArmorType.LEGGINGS, leggings);
            map.put(ArmorType.CHESTPLATE, chestplate);
            map.put(ArmorType.HELMET, helmet);
        });
    }


}
