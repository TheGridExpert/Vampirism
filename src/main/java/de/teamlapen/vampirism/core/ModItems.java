package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.api.items.IRefinementItem;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import de.teamlapen.vampirism.entity.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.items.*;
import de.teamlapen.vampirism.items.component.FactionRestriction;
import de.teamlapen.vampirism.items.consume.*;
import de.teamlapen.vampirism.items.crossbow.ArrowContainer;
import de.teamlapen.vampirism.items.crossbow.DoubleCrossbowItem;
import de.teamlapen.vampirism.items.crossbow.SingleCrossbowItem;
import de.teamlapen.vampirism.items.crossbow.TechCrossbowItem;
import de.teamlapen.vampirism.items.crossbow.arrow.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.holdersets.NotHolderSet;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Stream;

/**
 * Handles all item registrations and reference.
 */
@SuppressWarnings("unused")
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(REFERENCE.MODID);
    public static final DeferredRegister<ConsumeEffect.Type<?>> CONSUME_EFFECTS = DeferredRegister.create(Registries.CONSUME_EFFECT_TYPE, REFERENCE.MODID);

    // Consume Effects
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<OblivionEffect>> OBLIVION = CONSUME_EFFECTS.register("oblivious", () -> new ConsumeEffect.Type<>(OblivionEffect.CODEC, OblivionEffect.STREAM_CODEC));
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<FactionBasedConsumeEffect>> FACTION_BASED = CONSUME_EFFECTS.register("faction_based", () -> new ConsumeEffect.Type<>(FactionBasedConsumeEffect.CODEC, FactionBasedConsumeEffect.STREAM_CODEC));
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<BloodConsume>> CONSUME_BLOOD_EFFECT = CONSUME_EFFECTS.register("blood_consume", () -> new ConsumeEffect.Type<>(BloodConsume.CODEC, BloodConsume.STREAM_CODEC));
    public static final DeferredHolder<ConsumeEffect.Type<?>, ConsumeEffect.Type<AffectGarlic>> AFFECT_GARLIC = CONSUME_EFFECTS.register("affect_garlic", () -> new ConsumeEffect.Type<>(AffectGarlic.CODEC, AffectGarlic.STREAM_CODEC));

    // Weapons
    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_NORMAL = ITEMS.registerItem("iron_heart_seeker", properties -> new HeartSeekerItem(HeartSeekerItem.IRON, IItemWithTier.TIER.NORMAL, 1.3f, properties));
    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_ENHANCED = ITEMS.registerItem("diamond_heart_seeker", properties -> new HeartSeekerItem(HeartSeekerItem.DIAMOND, IItemWithTier.TIER.ENHANCED, 1.4f, properties));
    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_ULTIMATE = ITEMS.registerItem("netherite_heart_seeker", properties -> new HeartSeekerItem(HeartSeekerItem.NETHERITE, IItemWithTier.TIER.ULTIMATE, 1.5f, properties));

    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_NORMAL = ITEMS.registerItem("iron_heart_striker", properties -> new HeartStrikerItem(HeartStrikerItem.IRON, IItemWithTier.TIER.NORMAL,1.25f, properties));
    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_ENHANCED = ITEMS.registerItem("diamond_heart_striker", properties -> new HeartStrikerItem(HeartStrikerItem.DIAMOND, IItemWithTier.TIER.ENHANCED, 1.3f, properties));
    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_ULTIMATE = ITEMS.registerItem("netherite_heart_striker", properties -> new HeartStrikerItem(HeartStrikerItem.NETHERITE, IItemWithTier.TIER.ULTIMATE, 1.35f, properties));

    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_NORMAL = ITEMS.registerItem("hunter_axe_normal", properties -> new HunterAxeItem(HunterAxeItem.NORMAL, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_ENHANCED = ITEMS.registerItem("hunter_axe_enhanced", properties -> new HunterAxeItem(HunterAxeItem.ENHANCED, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_ULTIMATE = ITEMS.registerItem("hunter_axe_ultimate", properties -> new HunterAxeItem(HunterAxeItem.ULTIMATE, IItemWithTier.TIER.ULTIMATE, properties));

    public static final DeferredItem<SingleCrossbowItem> BASIC_CROSSBOW = ITEMS.registerItem("basic_crossbow", properties -> new SingleCrossbowItem(properties.durability(465), 1, 20, ToolMaterial.WOOD, HunterSkills.WEAPON_TABLE));
    public static final DeferredItem<DoubleCrossbowItem> BASIC_DOUBLE_CROSSBOW = ITEMS.registerItem("basic_double_crossbow", properties -> new DoubleCrossbowItem(properties.durability(465), 1, 20, ToolMaterial.WOOD, HunterSkills.WEAPON_TABLE));
    public static final DeferredItem<SingleCrossbowItem> ENHANCED_CROSSBOW = ITEMS.registerItem("enhanced_crossbow", properties -> new SingleCrossbowItem(properties.durability(930), 1.5F, 15, ToolMaterial.IRON, HunterSkills.MASTER_CRAFTSMANSHIP));
    public static final DeferredItem<DoubleCrossbowItem> ENHANCED_DOUBLE_CROSSBOW = ITEMS.registerItem("enhanced_double_crossbow", properties -> new DoubleCrossbowItem(properties.durability(930), 1.5F, 15, ToolMaterial.IRON, HunterSkills.MASTER_CRAFTSMANSHIP));
    public static final DeferredItem<TechCrossbowItem> BASIC_TECH_CROSSBOW = ITEMS.registerItem("basic_tech_crossbow", properties -> new TechCrossbowItem(properties.durability(930), 1.6F, 40, ToolMaterial.DIAMOND, HunterSkills.WEAPON_TABLE));
    public static final DeferredItem<TechCrossbowItem> ENHANCED_TECH_CROSSBOW = ITEMS.registerItem("enhanced_tech_crossbow", properties -> new TechCrossbowItem(properties.durability(1860), 1.7F, 30, ToolMaterial.DIAMOND, HunterSkills.MASTER_CRAFTSMANSHIP));

    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_NORMAL = ITEMS.registerItem("crossbow_arrow_normal", properties -> new CrossbowArrowItem(new NormalBehavior(), properties));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_SPITFIRE = ITEMS.registerItem("crossbow_arrow_spitfire", properties -> new CrossbowArrowItem(new SpitfireBehavior(), properties));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_GARLIC = ITEMS.registerItem("crossbow_arrow_garlic", properties -> new CrossbowArrowItem(new GarlicBehavior(), properties));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_VAMPIRE_KILLER = ITEMS.registerItem("crossbow_arrow_vampire_killer", properties -> new CrossbowArrowItem(new VampireKillerBehavior(), properties));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_TELEPORT = ITEMS.registerItem("crossbow_arrow_teleport", properties -> new CrossbowArrowItem(new TeleportBehavior(), properties));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_BLEEDING = ITEMS.registerItem("crossbow_arrow_bleeding", properties -> new CrossbowArrowItem(new BleedingBehavior(), properties));

    public static final DeferredItem<ArrowContainer> ARROW_CLIP = ITEMS.registerItem("tech_crossbow_ammo_package", properties -> new ArrowContainer(properties.stacksTo(1), 12, (stack) -> stack.is(CROSSBOW_ARROW_NORMAL.get())));
    public static final DeferredItem<Item> QUARREL_POUCH = ITEMS.registerItem("quarrel_pouch", properties -> new QuarrelPouch(properties.stacksTo(1)));

    public static final DeferredItem<SwordItem> PITCHFORK = ITEMS.registerItem("pitchfork", props -> new SwordItem(ToolMaterial.IRON, 6, -3, props));
    public static final DeferredItem<StakeItem> STAKE = ITEMS.registerItem("stake", StakeItem::new);

    public static final DeferredItem<CrucifixItem> CRUCIFIX_NORMAL = ITEMS.registerItem("crucifix_normal", properties -> new CrucifixItem(IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<CrucifixItem> CRUCIFIX_ENHANCED = ITEMS.registerItem("crucifix_enhanced", properties -> new CrucifixItem(IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<CrucifixItem> CRUCIFIX_ULTIMATE = ITEMS.registerItem("crucifix_ultimate", properties -> new CrucifixItem(IItemWithTier.TIER.ULTIMATE, properties));

    // Armor
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_NORMAL = ITEMS.registerItem("armor_of_swiftness_chest_normal", properties ->new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorType.CHESTPLATE, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_ENHANCED = ITEMS.registerItem("armor_of_swiftness_chest_enhanced", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorType.CHESTPLATE, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_ULTIMATE = ITEMS.registerItem("armor_of_swiftness_chest_ultimate", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorType.CHESTPLATE, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_NORMAL = ITEMS.registerItem("armor_of_swiftness_feet_normal", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorType.BOOTS, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_ENHANCED = ITEMS.registerItem("armor_of_swiftness_feet_enhanced", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorType.BOOTS, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_ULTIMATE = ITEMS.registerItem("armor_of_swiftness_feet_ultimate", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorType.BOOTS, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_NORMAL = ITEMS.registerItem("armor_of_swiftness_head_normal", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorType.HELMET, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_ENHANCED = ITEMS.registerItem("armor_of_swiftness_head_enhanced", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorType.HELMET, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_ULTIMATE = ITEMS.registerItem("armor_of_swiftness_head_ultimate", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorType.HELMET, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_NORMAL = ITEMS.registerItem("armor_of_swiftness_legs_normal", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorType.LEGGINGS, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_ENHANCED = ITEMS.registerItem("armor_of_swiftness_legs_enhanced", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorType.LEGGINGS, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_ULTIMATE = ITEMS.registerItem("armor_of_swiftness_legs_ultimate", properties -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorType.LEGGINGS, IItemWithTier.TIER.ULTIMATE, properties));

    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_NORMAL = ITEMS.registerItem("hunter_coat_chest_normal", properties -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorType.CHESTPLATE, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_ENHANCED = ITEMS.registerItem("hunter_coat_chest_enhanced", properties -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorType.CHESTPLATE, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_ULTIMATE = ITEMS.registerItem("hunter_coat_chest_ultimate", properties -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorType.CHESTPLATE, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_NORMAL = ITEMS.registerItem("hunter_coat_feet_normal", properties -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorType.BOOTS, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_ENHANCED = ITEMS.registerItem("hunter_coat_feet_enhanced", properties -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorType.BOOTS, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_ULTIMATE = ITEMS.registerItem("hunter_coat_feet_ultimate", properties -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorType.BOOTS, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_NORMAL = ITEMS.registerItem("hunter_coat_head_normal", properties -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorType.HELMET, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_ENHANCED = ITEMS.registerItem("hunter_coat_head_enhanced", properties -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorType.HELMET, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_ULTIMATE = ITEMS.registerItem("hunter_coat_head_ultimate", properties -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorType.HELMET, IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_NORMAL = ITEMS.registerItem("hunter_coat_legs_normal", properties -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorType.LEGGINGS, IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_ENHANCED = ITEMS.registerItem("hunter_coat_legs_enhanced", properties -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorType.LEGGINGS, IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_ULTIMATE = ITEMS.registerItem("hunter_coat_legs_ultimate", properties -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorType.LEGGINGS, IItemWithTier.TIER.ULTIMATE, properties));

    public static final DeferredItem<HunterHatItem> HUNTER_HAT_TALL = ITEMS.registerItem("hunter_hat_tall", properties -> new HunterHatItem(ModArmorMaterials.HUNTER_HAT_TALL, properties));
    public static final DeferredItem<HunterHatItem> HUNTER_HAT_BROAD = ITEMS.registerItem("hunter_hat_broad", properties -> new HunterHatItem(ModArmorMaterials.HUNTER_HAT_BROAD, properties));

    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_CROWN = ITEMS.registerItem("vampire_clothing_crown", properties -> new VampireClothingItem(ArmorType.HELMET, ModArmorMaterials.VAMPIRE_CLOTH_CROWN, properties));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_LEGS = ITEMS.registerItem("vampire_clothing_legs", properties -> new VampireClothingItem(ArmorType.LEGGINGS, ModArmorMaterials.VAMPIRE_CLOTH_LEGS, properties));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_BOOTS = ITEMS.registerItem("vampire_clothing_boots", properties -> new VampireClothingItem(ArmorType.BOOTS, ModArmorMaterials.VAMPIRE_CLOTH_BOOTS, properties));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_HAT = ITEMS.registerItem("vampire_clothing_hat", properties -> new VampireClothingItem(ArmorType.HELMET, ModArmorMaterials.VAMPIRE_CLOTH_HAT, properties));

    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_WHITE = ITEMS.registerItem("vampire_cloak_white", properties -> new VampireCloakItem(DyeColor.WHITE, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_ORANGE = ITEMS.registerItem("vampire_cloak_orange", properties -> new VampireCloakItem(DyeColor.ORANGE, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_MAGENTA = ITEMS.registerItem("vampire_cloak_magenta", properties -> new VampireCloakItem(DyeColor.MAGENTA, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_LIGHT_BLUE = ITEMS.registerItem("vampire_cloak_light_blue", properties -> new VampireCloakItem(DyeColor.LIGHT_BLUE, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_YELLOW = ITEMS.registerItem("vampire_cloak_yellow", properties -> new VampireCloakItem(DyeColor.YELLOW, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_LIME = ITEMS.registerItem("vampire_cloak_lime", properties -> new VampireCloakItem(DyeColor.LIME, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_PINK = ITEMS.registerItem("vampire_cloak_pink", properties -> new VampireCloakItem(DyeColor.PINK, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_GRAY = ITEMS.registerItem("vampire_cloak_gray", properties -> new VampireCloakItem(DyeColor.GRAY, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_LIGHT_GRAY = ITEMS.registerItem("vampire_cloak_light_gray", properties -> new VampireCloakItem(DyeColor.LIGHT_GRAY, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_CYAN = ITEMS.registerItem("vampire_cloak_cyan", properties -> new VampireCloakItem(DyeColor.CYAN, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_PURPLE = ITEMS.registerItem("vampire_cloak_purple", properties -> new VampireCloakItem(DyeColor.PURPLE, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_BLUE = ITEMS.registerItem("vampire_cloak_blue", properties -> new VampireCloakItem(DyeColor.BLUE, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_BROWN = ITEMS.registerItem("vampire_cloak_brown", properties -> new VampireCloakItem(DyeColor.BROWN, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_GREEN = ITEMS.registerItem("vampire_cloak_green", properties -> new VampireCloakItem(DyeColor.GREEN, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_RED = ITEMS.registerItem("vampire_cloak_red", properties -> new VampireCloakItem(DyeColor.RED, properties));
    public static final DeferredItem<VampireCloakItem> VAMPIRE_CLOAK_BLACK = ITEMS.registerItem("vampire_cloak_black", properties -> new VampireCloakItem(DyeColor.BLACK, properties));

    public static final DeferredItem<RefinementItem> AMULET = ITEMS.registerItem("amulet", properties -> new RefinementItem(FactionRestriction.builder(ModFactionTags.IS_VAMPIRE).apply(properties), IRefinementItem.AccessorySlotType.AMULET));
    public static final DeferredItem<RefinementItem> RING = ITEMS.registerItem("ring", properties -> new RefinementItem(FactionRestriction.builder(ModFactionTags.IS_VAMPIRE).apply(properties), IRefinementItem.AccessorySlotType.RING));
    public static final DeferredItem<RefinementItem> OBI_BELT = ITEMS.registerItem("obi_belt", properties -> new RefinementItem(FactionRestriction.builder(ModFactionTags.IS_VAMPIRE).apply(properties), IRefinementItem.AccessorySlotType.OBI_BELT));

    // General
    public static final DeferredItem<BloodBottleItem> BLOOD_BOTTLE = ITEMS.registerItem("blood_bottle", properties -> new BloodBottleItem(properties.stacksTo(1).component(DataComponents.CONSUMABLE, Consumables.defaultDrink().build())));
    public static final DeferredItem<BucketItem> BLOOD_BUCKET = ITEMS.registerItem("blood_bucket", properties -> new BucketItem(ModFluids.BLOOD.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<BucketItem> IMPURE_BLOOD_BUCKET = ITEMS.registerItem("impure_blood_bucket", properties -> new BucketItem(ModFluids.IMPURE_BLOOD.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_RAW_IRON = ITEMS.registerItem("blood_infused_raw_iron", PureLevelItem::new);
    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_RAW_GOLD = ITEMS.registerItem("blood_infused_raw_gold", PureLevelItem::new);
    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_IRON_INGOT = ITEMS.registerItem("blood_infused_iron_ingot", PureLevelItem::new);
    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_GOLD_INGOT = ITEMS.registerItem("blood_infused_gold_ingot", PureLevelItem::new);
    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_DIAMOND = ITEMS.registerItem("blood_infused_diamond", PureLevelItem::new);
    public static final DeferredItem<PureLevelItem> BLOOD_INFUSED_NETHERITE_INGOT = ITEMS.registerItem("blood_infused_netherite_ingot", PureLevelItem::new);

    public static final DeferredItem<Item> GARLIC_DIFFUSER_CORE = ITEMS.registerItem("garlic_diffuser_core", Item::new);
    public static final DeferredItem<Item> GARLIC_DIFFUSER_CORE_IMPROVED = ITEMS.registerItem("garlic_diffuser_core_improved", Item::new);

    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_NORMAL = ITEMS.registerItem("holy_water_bottle_normal", properties -> new HolyWaterBottleItem(IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_ENHANCED = ITEMS.registerItem("holy_water_bottle_enhanced", properties -> new HolyWaterBottleItem(IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_ULTIMATE = ITEMS.registerItem("holy_water_bottle_ultimate", properties -> new HolyWaterBottleItem(IItemWithTier.TIER.ULTIMATE, properties));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_NORMAL = ITEMS.registerItem("holy_water_splash_bottle_normal", properties -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.NORMAL, properties));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_ENHANCED = ITEMS.registerItem("holy_water_splash_bottle_enhanced", properties -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.ENHANCED, properties));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_ULTIMATE = ITEMS.registerItem("holy_water_splash_bottle_ultimate", properties -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.ULTIMATE, properties));

    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_0 = ITEMS.registerItem("hunter_intel_0", properties -> new HunterIntelItem(0, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_1 = ITEMS.registerItem("hunter_intel_1", properties -> new HunterIntelItem(1, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_2 = ITEMS.registerItem("hunter_intel_2", properties -> new HunterIntelItem(2, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_3 = ITEMS.registerItem("hunter_intel_3", properties -> new HunterIntelItem(3, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_4 = ITEMS.registerItem("hunter_intel_4", properties -> new HunterIntelItem(4, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_5 = ITEMS.registerItem("hunter_intel_5", properties -> new HunterIntelItem(5, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_6 = ITEMS.registerItem("hunter_intel_6", properties -> new HunterIntelItem(6, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_7 = ITEMS.registerItem("hunter_intel_7", properties -> new HunterIntelItem(7, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_8 = ITEMS.registerItem("hunter_intel_8", properties -> new HunterIntelItem(8, properties));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_9 = ITEMS.registerItem("hunter_intel_9", properties -> new HunterIntelItem(9, properties));

    public static final DeferredItem<PureBloodItem> PURE_BLOOD_0 = ITEMS.registerItem("pure_blood_0", properties -> new PureBloodItem(0, properties));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_1 = ITEMS.registerItem("pure_blood_1", properties -> new PureBloodItem(1, properties));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_2 = ITEMS.registerItem("pure_blood_2", properties -> new PureBloodItem(2, properties));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_3 = ITEMS.registerItem("pure_blood_3", properties -> new PureBloodItem(3, properties));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_4 = ITEMS.registerItem("pure_blood_4", properties -> new PureBloodItem(4, properties));

    public static final DeferredItem<Item> GARLIC_BREAD = ITEMS.registerItem("garlic_bread", props -> new Item(props.food(new FoodProperties.Builder().nutrition(6).saturationModifier(0.7F).build()).component(DataComponents.CONSUMABLE, ModConsumables.GARLIC)));
    public static final DeferredItem<Item> HUMAN_HEART = ITEMS.registerItem("human_heart", properties -> new Item(properties.component(DataComponents.FOOD, new FoodProperties.Builder().nutrition(5).saturationModifier(1f).build()).component(ModDataComponents.VAMPIRE_FOOD, new BloodFoodProperties.Builder().blood(20).saturationModifier(1.5F).build()).component(DataComponents.CONSUMABLE, Consumables.defaultFood().onConsume(new FactionBasedConsumeEffect(new NotHolderSet<>(ModRegistries.FACTIONS, HolderSet.direct((Holder<IFaction<?>>) (Object) ModFactions.VAMPIRE)), new ApplyStatusEffectsConsumeEffect(List.of(new MobEffectInstance(MobEffects.CONFUSION, 20*20))))).build())));
    public static final DeferredItem<VampirismItemBloodFoodItem> WEAK_HUMAN_HEART = ITEMS.registerItem("weak_human_heart", properties -> new VampirismItemBloodFoodItem(properties.food(new FoodProperties.Builder().nutrition(3).saturationModifier(1f).build()), new BloodFoodProperties.Builder().blood(10).saturationModifier(0.9F).build()));

    public static final DeferredItem<InjectionItem> INJECTION_EMPTY = ITEMS.registerItem("injection_empty", properties -> new InjectionItem(InjectionItem.TYPE.EMPTY, properties));
    public static final DeferredItem<InjectionItem> INJECTION_GARLIC = ITEMS.registerItem("injection_garlic", properties -> new InjectionItem(InjectionItem.TYPE.GARLIC, properties));
    public static final DeferredItem<InjectionItem> INJECTION_SANGUINARE = ITEMS.registerItem("injection_sanguinare", properties -> new InjectionItem(InjectionItem.TYPE.SANGUINARE, properties));

    public static final DeferredItem<AlchemicalFireItem> ITEM_ALCHEMICAL_FIRE = ITEMS.registerItem("item_alchemical_fire", AlchemicalFireItem::new);

    public static final DeferredItem<TentItem> ITEM_TENT = ITEMS.registerItem("item_tent", properties -> new TentItem(false, properties));
    public static final DeferredItem<TentItem> ITEM_TENT_SPAWNER = ITEMS.registerItem("item_tent_spawner", properties -> new TentItem(true, properties));

    public static final DeferredItem<Item> PURIFIED_GARLIC = ITEMS.registerItem("purified_garlic", properties -> new Item(properties.stacksTo(16)));
    public static final DeferredItem<Item> PURE_SALT = ITEMS.registerItem("pure_salt", Item::new);
    public static final DeferredItem<BlessableItem> PURE_SALT_WATER = ITEMS.registerItem("pure_salt_water", properties -> new BlessableItem(properties.stacksTo(1), HOLY_WATER_BOTTLE_NORMAL::get, HOLY_WATER_BOTTLE_ENHANCED::get) {
        @Override
        public boolean isFoil(@NotNull ItemStack stack) {
            return true;
        }
    });

    public static final DeferredItem<Item> SOUL_ORB_VAMPIRE = ITEMS.registerItem("soul_orb_vampire", Item::new);
    public static final DeferredItem<Item> MOTHER_CORE = ITEMS.registerItem("mother_core", properties -> new Item(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> VAMPIRE_BLOOD_BOTTLE = ITEMS.registerItem("vampire_blood_bottle", Item::new);
    public static final DeferredItem<VampireBookItem> VAMPIRE_BOOK = ITEMS.registerItem("vampire_book", properties -> new VampireBookItem(properties.rarity(Rarity.UNCOMMON).stacksTo(1)));
    public static final DeferredItem<VampireFangItem> VAMPIRE_FANG = ITEMS.registerItem("vampire_fang", VampireFangItem::new);

    public static final DeferredItem<UmbrellaItem> UMBRELLA = ITEMS.registerItem("umbrella", UmbrellaItem::new);

    public static final DeferredItem<Item> HUNTER_MINION_EQUIPMENT = ITEMS.registerItem("hunter_minion_equipment", Item::new);
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_SIMPLE = ITEMS.registerItem("hunter_minion_upgrade_simple", properties -> new MinionUpgradeItem(1, 2, ModFactions.HUNTER, properties));
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_ENHANCED = ITEMS.registerItem("hunter_minion_upgrade_enhanced", properties -> new MinionUpgradeItem(3, 4, ModFactions.HUNTER, properties));
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_SPECIAL = ITEMS.registerItem("hunter_minion_upgrade_special", properties -> new MinionUpgradeItem(5, 6, ModFactions.HUNTER, properties));

    public static final DeferredItem<Item> VAMPIRE_MINION_BINDING = ITEMS.registerItem("vampire_minion_binding", Item::new);
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_SIMPLE = ITEMS.registerItem("vampire_minion_upgrade_simple", properties -> new MinionUpgradeItem(1, 2, ModFactions.VAMPIRE, properties));
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_ENHANCED = ITEMS.registerItem("vampire_minion_upgrade_enhanced", properties -> new MinionUpgradeItem(3, 4, ModFactions.VAMPIRE, properties));
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_SPECIAL = ITEMS.registerItem("vampire_minion_upgrade_special", properties -> new MinionUpgradeItem(5, 6, ModFactions.VAMPIRE, properties));

    public static final DeferredItem<FeedingAdapterItem> FEEDING_ADAPTER = ITEMS.registerItem("feeding_adapter", FeedingAdapterItem::new);
    public static final DeferredItem<OblivionItem> OBLIVION_POTION = ITEMS.registerItem("oblivion_potion", OblivionItem::new);
    public static final DeferredItem<Item> GARLIC_FINDER = ITEMS.registerItem("garlic_finder", properties -> new Item(properties.rarity(Rarity.RARE)));

    public static final DeferredItem<OilBottleItem> OIL_BOTTLE = ITEMS.registerItem("oil_bottle", properties -> new OilBottleItem(properties.stacksTo(1)));

    public static final DeferredItem<BoatItem> DARK_SPRUCE_BOAT = ITEMS.registerItem("dark_spruce_boat", properties -> new BoatItem(ModEntities.DARK_SPRUCE_BOAT.get(), properties.stacksTo(1)));
    public static final DeferredItem<BoatItem> CURSED_SPRUCE_BOAT = ITEMS.registerItem("cursed_spruce_boat", properties -> new BoatItem(ModEntities.CURSED_SPRUCE_BOAT.get(), properties.stacksTo(1)));
    public static final DeferredItem<BoatItem> DARK_SPRUCE_CHEST_BOAT = ITEMS.registerItem("dark_spruce_chest_boat", properties -> new BoatItem(ModEntities.DARK_SPRUCE_CHEST_BOAT.get(), properties.stacksTo(1)));
    public static final DeferredItem<BoatItem> CURSED_SPRUCE_CHEST_BOAT = ITEMS.registerItem("cursed_spruce_chest_boat", properties -> new BoatItem(ModEntities.CURSED_SPRUCE_CHEST_BOAT.get(), properties.stacksTo(1)));

    public static final DeferredItem<SignItem> DARK_SPRUCE_SIGN = ITEMS.registerItem("dark_spruce_sign", properties -> new SignItem(ModBlocks.DARK_SPRUCE_SIGN.get(), ModBlocks.DARK_SPRUCE_WALL_SIGN.get(), properties.useBlockDescriptionPrefix().stacksTo(16)));
    public static final DeferredItem<SignItem> CURSED_SPRUCE_SIGN = ITEMS.registerItem("cursed_spruce_sign", properties -> new SignItem(ModBlocks.CURSED_SPRUCE_SIGN.get(), ModBlocks.CURSED_SPRUCE_WALL_SIGN.get(), properties.useBlockDescriptionPrefix().stacksTo(16)));
    public static final DeferredItem<HangingSignItem> DARK_SPRUCE_HANGING_SIGN = ITEMS.registerItem("dark_spruce_hanging_sign", properties -> new HangingSignItem(ModBlocks.DARK_SPRUCE_HANGING_SIGN.get(), ModBlocks.DARK_SPRUCE_WALL_HANGING_SIGN.get(), properties.useBlockDescriptionPrefix().stacksTo(16)));
    public static final DeferredItem<HangingSignItem> CURSED_SPRUCE_HANGING_SIGN = ITEMS.registerItem("cursed_spruce_hanging_sign", properties -> new HangingSignItem(ModBlocks.CURSED_SPRUCE_HANGING_SIGN.get(), ModBlocks.CURSED_SPRUCE_WALL_HANGING_SIGN.get(), properties.useBlockDescriptionPrefix().stacksTo(16)));

    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK = ITEMS.registerItem("candle_stick", properties -> new StandingAndWallBlockItem(ModBlocks.CANDLE_STICK.get(), ModBlocks.WALL_CANDLE_STICK.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_NORMAL = ITEMS.registerItem("candle_stick_normal", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_NORMAL.get(), ModBlocks.WALL_CANDLE_STICK_NORMAL.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_WHITE = ITEMS.registerItem("candle_stick_white", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_WHITE.get(), ModBlocks.WALL_CANDLE_STICK_WHITE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_ORANGE = ITEMS.registerItem("candle_stick_orange", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_ORANGE.get(), ModBlocks.WALL_CANDLE_STICK_ORANGE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_MAGENTA = ITEMS.registerItem("candle_stick_magenta", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_MAGENTA.get(), ModBlocks.WALL_CANDLE_STICK_MAGENTA.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_LIGHT_BLUE = ITEMS.registerItem("candle_stick_light_blue", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_LIGHT_BLUE.get(), ModBlocks.WALL_CANDLE_STICK_LIGHT_BLUE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_YELLOW = ITEMS.registerItem("candle_stick_yellow", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_YELLOW.get(), ModBlocks.WALL_CANDLE_STICK_YELLOW.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_LIME = ITEMS.registerItem("candle_stick_lime", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_LIME.get(), ModBlocks.WALL_CANDLE_STICK_LIME.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_PINK = ITEMS.registerItem("candle_stick_pink", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_PINK.get(), ModBlocks.WALL_CANDLE_STICK_PINK.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_GRAY = ITEMS.registerItem("candle_stick_gray", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_GRAY.get(), ModBlocks.WALL_CANDLE_STICK_GRAY.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_LIGHT_GRAY = ITEMS.registerItem("candle_stick_light_gray", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_LIGHT_GRAY.get(), ModBlocks.WALL_CANDLE_STICK_LIGHT_GRAY.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_CYAN = ITEMS.registerItem("candle_stick_cyan", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_CYAN.get(), ModBlocks.WALL_CANDLE_STICK_CYAN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_PURPLE = ITEMS.registerItem("candle_stick_purple", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_PURPLE.get(), ModBlocks.WALL_CANDLE_STICK_PURPLE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_BLUE = ITEMS.registerItem("candle_stick_blue", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_BLUE.get(), ModBlocks.WALL_CANDLE_STICK_BLUE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_BROWN = ITEMS.registerItem("candle_stick_brown", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_BROWN.get(), ModBlocks.WALL_CANDLE_STICK_BROWN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_GREEN = ITEMS.registerItem("candle_stick_green", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_GREEN.get(), ModBlocks.WALL_CANDLE_STICK_GREEN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_RED = ITEMS.registerItem("candle_stick_red", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_RED.get(), ModBlocks.WALL_CANDLE_STICK_RED.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDLE_STICK_BLACK = ITEMS.registerItem("candle_stick_black", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDLE_STICK_BLACK.get(), ModBlocks.WALL_CANDLE_STICK_BLACK.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));

    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA = ITEMS.registerItem("candelabra", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA.get(), ModBlocks.WALL_CANDELABRA.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_NORMAL = ITEMS.registerItem("candelabra_normal", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_NORMAL.get(), ModBlocks.WALL_CANDELABRA_NORMAL.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_WHITE = ITEMS.registerItem("candelabra_white", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_WHITE.get(), ModBlocks.WALL_CANDELABRA_WHITE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_ORANGE = ITEMS.registerItem("candelabra_orange", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_ORANGE.get(), ModBlocks.WALL_CANDELABRA_ORANGE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_MAGENTA = ITEMS.registerItem("candelabra_magenta", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_MAGENTA.get(), ModBlocks.WALL_CANDELABRA_MAGENTA.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_LIGHT_BLUE = ITEMS.registerItem("candelabra_light_blue", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_LIGHT_BLUE.get(), ModBlocks.WALL_CANDELABRA_LIGHT_BLUE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_YELLOW = ITEMS.registerItem("candelabra_yellow", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_YELLOW.get(), ModBlocks.WALL_CANDELABRA_YELLOW.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_LIME = ITEMS.registerItem("candelabra_lime", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_LIME.get(), ModBlocks.WALL_CANDELABRA_LIME.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_PINK = ITEMS.registerItem("candelabra_pink", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_PINK.get(), ModBlocks.WALL_CANDELABRA_PINK.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_GRAY = ITEMS.registerItem("candelabra_gray", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_GRAY.get(), ModBlocks.WALL_CANDELABRA_GRAY.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_LIGHT_GRAY = ITEMS.registerItem("candelabra_light_gray", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_LIGHT_GRAY.get(), ModBlocks.WALL_CANDELABRA_LIGHT_GRAY.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_CYAN = ITEMS.registerItem("candelabra_cyan", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_CYAN.get(), ModBlocks.WALL_CANDELABRA_CYAN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_PURPLE = ITEMS.registerItem("candelabra_purple", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_PURPLE.get(), ModBlocks.WALL_CANDELABRA_PURPLE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_BLUE = ITEMS.registerItem("candelabra_blue", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_BLUE.get(), ModBlocks.WALL_CANDELABRA_BLUE.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_BROWN = ITEMS.registerItem("candelabra_brown", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_BROWN.get(), ModBlocks.WALL_CANDELABRA_BROWN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_GREEN = ITEMS.registerItem("candelabra_green", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_GREEN.get(), ModBlocks.WALL_CANDELABRA_GREEN.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_RED = ITEMS.registerItem("candelabra_red", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_RED.get(), ModBlocks.WALL_CANDELABRA_RED.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<StandingAndWallBlockItem> CANDELABRA_BLACK = ITEMS.registerItem("candelabra_black", properties -> new  StandingAndWallBlockItem(ModBlocks.CANDELABRA_BLACK.get(), ModBlocks.WALL_CANDELABRA_BLACK.get(), Direction.DOWN, properties.useBlockDescriptionPrefix()));

    public static final DeferredItem<BlockItem> CHANDELIER = ITEMS.registerItem("chandelier", properties -> new BlockItem(ModBlocks.CHANDELIER.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_NORMAL = ITEMS.registerItem("chandelier_normal", properties -> new BlockItem(ModBlocks.CHANDELIER_NORMAL.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_WHITE = ITEMS.registerItem("chandelier_white", properties -> new BlockItem(ModBlocks.CHANDELIER_WHITE.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_ORANGE = ITEMS.registerItem("chandelier_orange", properties -> new BlockItem(ModBlocks.CHANDELIER_ORANGE.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_MAGENTA = ITEMS.registerItem("chandelier_magenta", properties -> new BlockItem(ModBlocks.CHANDELIER_MAGENTA.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_LIGHT_BLUE = ITEMS.registerItem("chandelier_light_blue", properties -> new BlockItem(ModBlocks.CHANDELIER_LIGHT_BLUE.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_YELLOW = ITEMS.registerItem("chandelier_yellow", properties -> new BlockItem(ModBlocks.CHANDELIER_YELLOW.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_LIME = ITEMS.registerItem("chandelier_lime", properties -> new BlockItem(ModBlocks.CHANDELIER_LIME.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_PINK = ITEMS.registerItem("chandelier_pink", properties -> new BlockItem(ModBlocks.CHANDELIER_PINK.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_GRAY = ITEMS.registerItem("chandelier_gray", properties -> new BlockItem(ModBlocks.CHANDELIER_GRAY.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_LIGHT_GRAY = ITEMS.registerItem("chandelier_light_gray", properties -> new BlockItem(ModBlocks.CHANDELIER_LIGHT_GRAY.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_CYAN = ITEMS.registerItem("chandelier_cyan", properties -> new BlockItem(ModBlocks.CHANDELIER_CYAN.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_PURPLE = ITEMS.registerItem("chandelier_purple", properties -> new BlockItem(ModBlocks.CHANDELIER_PURPLE.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_BLUE = ITEMS.registerItem("chandelier_blue", properties -> new BlockItem(ModBlocks.CHANDELIER_BLUE.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_BROWN = ITEMS.registerItem("chandelier_brown", properties -> new BlockItem(ModBlocks.CHANDELIER_BROWN.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_GREEN = ITEMS.registerItem("chandelier_green", properties -> new BlockItem(ModBlocks.CHANDELIER_GREEN.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_RED = ITEMS.registerItem("chandelier_red", properties -> new BlockItem(ModBlocks.CHANDELIER_RED.get(), properties.useBlockDescriptionPrefix()));
    public static final DeferredItem<BlockItem> CHANDELIER_BLACK = ITEMS.registerItem("chandelier_black", properties -> new BlockItem(ModBlocks.CHANDELIER_BLACK.get(), properties.useBlockDescriptionPrefix()));

    // Spawn Eggs
    public static final DeferredItem<SpawnEggItem> VAMPIRE_SPAWN_EGG = ITEMS.registerItem("vampire_spawn_egg", properties -> new SpawnEggItem(ModEntities.VAMPIRE.get(), properties));
    public static final DeferredItem<SpawnEggItem> ADVANCED_VAMPIRE_SPAWN_EGG = ITEMS.registerItem("advanced_vampire_spawn_egg", properties -> new SpawnEggItem(ModEntities.ADVANCED_VAMPIRE.get(), properties));
    public static final DeferredItem<SpawnEggItem> VAMPIRE_BARON_SPAWN_EGG = ITEMS.registerItem("vampire_baron_spawn_egg", properties -> new SpawnEggItem(ModEntities.VAMPIRE_BARON.get(), properties));
    public static final DeferredItem<SpawnEggItem> TASK_MASTER_VAMPIRE_SPAWN_EGG = ITEMS.registerItem("task_master_vampire_spawn_egg", properties -> new SpawnEggItem(ModEntities.TASK_MASTER_VAMPIRE.get(), properties));

    public static final DeferredItem<SpawnEggItem> VAMPIRE_HUNTER_SPAWN_EGG = ITEMS.registerItem("vampire_hunter_spawn_egg", properties -> new SpawnEggItem(ModEntities.HUNTER.get(), properties));
    public static final DeferredItem<SpawnEggItem> ADVANCED_VAMPIRE_HUNTER_SPAWN_EGG = ITEMS.registerItem("advanced_vampire_hunter_spawn_egg", properties -> new SpawnEggItem(ModEntities.ADVANCED_HUNTER.get(), properties));
    public static final DeferredItem<SpawnEggItem> HUNTER_TRAINER_SPAWN_EGG = ITEMS.registerItem("hunter_trainer_spawn_egg", properties -> new SpawnEggItem(ModEntities.HUNTER_TRAINER.get(), properties));
    public static final DeferredItem<SpawnEggItem> TASK_MASTER_HUNTER_SPAWN_EGG = ITEMS.registerItem("task_master_hunter_spawn_egg", properties -> new SpawnEggItem(ModEntities.TASK_MASTER_HUNTER.get(), properties));

    public static final DeferredItem<SpawnEggItem> GHOST_SPAWN_EGG = ITEMS.registerItem("ghost_spawn_egg", properties -> new SpawnEggItem(ModEntities.GHOST.get(), properties));


    @SuppressWarnings("unchecked")
    public static Stream<Holder<Item>> listElements() {
        return ((Collection<Holder<Item>>) (Object) ITEMS.getEntries()).stream();
    }

    private static Item.@NotNull Properties props() {
        return new Item.Properties();
    }

    static void register(IEventBus bus) {
        ITEMS.register(bus);

        if (VampirismMod.inDataGen) {
            DeferredRegister.Items GUIDEAPI_ITEMS = DeferredRegister.createItems(REFERENCE.GUIDEAPI_MODID);
            DeferredItem<DummyItem> guidebook = GUIDEAPI_ITEMS.registerItem(REFERENCE.GUIDEBOOK_ID, DummyItem::new);
            GUIDEAPI_ITEMS.register(bus);
        }
    }

    public static void registerDispenserBehaviourUnsafe() {
        DispenserBlock.registerBehavior(ModItems.DARK_SPRUCE_BOAT.get(), new BoatDispenseItemBehavior(ModEntities.DARK_SPRUCE_BOAT.get()));
        DispenserBlock.registerBehavior(ModItems.CURSED_SPRUCE_BOAT.get(), new BoatDispenseItemBehavior(ModEntities.CURSED_SPRUCE_BOAT.get()));
        DispenserBlock.registerBehavior(ModItems.DARK_SPRUCE_CHEST_BOAT.get(), new BoatDispenseItemBehavior(ModEntities.DARK_SPRUCE_CHEST_BOAT.get()));
        DispenserBlock.registerBehavior(ModItems.CURSED_SPRUCE_CHEST_BOAT.get(), new BoatDispenseItemBehavior(ModEntities.CURSED_SPRUCE_CHEST_BOAT.get()));
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_NORMAL.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_SPITFIRE.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_TELEPORT.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_VAMPIRE_KILLER.get());
        DispenserBlock.registerProjectileBehavior(ModItems.HOLY_WATER_SPLASH_BOTTLE_NORMAL.get());
        DispenserBlock.registerProjectileBehavior(ModItems.HOLY_WATER_SPLASH_BOTTLE_ENHANCED.get());
        DispenserBlock.registerProjectileBehavior(ModItems.HOLY_WATER_SPLASH_BOTTLE_ULTIMATE.get());
    }
}
