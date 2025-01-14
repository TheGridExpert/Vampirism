package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.ModRegistryItems;
import de.teamlapen.vampirism.api.VEnums;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.api.items.IRefinementItem;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.items.*;
import de.teamlapen.vampirism.items.crossbow.ArrowContainer;
import de.teamlapen.vampirism.items.crossbow.DoubleCrossbowItem;
import de.teamlapen.vampirism.items.crossbow.SingleCrossbowItem;
import de.teamlapen.vampirism.items.crossbow.TechCrossbowItem;
import de.teamlapen.vampirism.misc.VampirismCreativeTab;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.dispenser.BoatDispenseItemBehavior;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.common.EffectCure;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Handles all item registrations and reference.
 */
@SuppressWarnings("unused")
public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(REFERENCE.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, REFERENCE.MODID);

    private static final Set<DeferredHolder<Item, ? extends Item>> VAMPIRISM_TAB_ITEMS = new HashSet<>();
    private static final Map<ResourceKey<CreativeModeTab>, Set<DeferredHolder<Item, ? extends Item>>> CREATIVE_TAB_ITEMS = new HashMap<>();

    public static final ResourceKey<CreativeModeTab> VAMPIRISM_TAB_KEY = ResourceKey.create(Registries.CREATIVE_MODE_TAB, VResourceLocation.mod("default"));
    public static final DeferredHolder<CreativeModeTab,CreativeModeTab> VAMPIRISM_TAB = CREATIVE_TABS.register(VAMPIRISM_TAB_KEY.location().getPath(), () -> VampirismCreativeTab.builder(VAMPIRISM_TAB_ITEMS.stream().map(DeferredHolder::get).collect(Collectors.toSet())).build());
    public static final EffectCure GARLIC_CURE = EffectCure.get("vampirism:garlic");

    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_NORMAL = register("armor_of_swiftness_chest_normal", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_ENHANCED = register("armor_of_swiftness_chest_enhanced", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_CHEST_ULTIMATE = register("armor_of_swiftness_chest_ultimate", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_NORMAL = register("armor_of_swiftness_feet_normal", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorItem.Type.BOOTS, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_ENHANCED = register("armor_of_swiftness_feet_enhanced", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorItem.Type.BOOTS, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_FEET_ULTIMATE = register("armor_of_swiftness_feet_ultimate", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorItem.Type.BOOTS, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_NORMAL = register("armor_of_swiftness_head_normal", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorItem.Type.HELMET, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_ENHANCED = register("armor_of_swiftness_head_enhanced", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorItem.Type.HELMET, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_HEAD_ULTIMATE = register("armor_of_swiftness_head_ultimate", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorItem.Type.HELMET, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_NORMAL = register("armor_of_swiftness_legs_normal", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.NORMAL_SWIFTNESS, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_ENHANCED = register("armor_of_swiftness_legs_enhanced", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ENHANCED_SWIFTNESS, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<ArmorOfSwiftnessItem> ARMOR_OF_SWIFTNESS_LEGS_ULTIMATE = register("armor_of_swiftness_legs_ultimate", () -> new ArmorOfSwiftnessItem(ModArmorMaterials.ULTIMATE_SWIFTNESS, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.ULTIMATE));

    public static final DeferredItem<SingleCrossbowItem> BASIC_CROSSBOW = register("basic_crossbow", () -> new SingleCrossbowItem(props().durability(465), 1, 20, Tiers.WOOD));
    public static final DeferredItem<DoubleCrossbowItem> BASIC_DOUBLE_CROSSBOW = register("basic_double_crossbow", () -> new DoubleCrossbowItem(props().durability(465), 1, 20, Tiers.WOOD));
    public static final DeferredItem<TechCrossbowItem> BASIC_TECH_CROSSBOW = register("basic_tech_crossbow", () -> new TechCrossbowItem(props().durability(930), 1.6F, 40, Tiers.DIAMOND));

    public static final DeferredItem<BloodBottleItem> BLOOD_BOTTLE = ITEMS.register("blood_bottle", () -> new BloodBottleItem(props().stacksTo(1)));
    public static final DeferredItem<BucketItem> BLOOD_BUCKET = register("blood_bucket", CreativeModeTabs.TOOLS_AND_UTILITIES, () -> new BucketItem(ModFluids.BLOOD.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<Item> BLOOD_INFUSED_IRON_INGOT = register("blood_infused_iron_ingot", () -> new Item(props()));
    public static final DeferredItem<Item> BLOOD_INFUSED_ENHANCED_IRON_INGOT = register("blood_infused_enhanced_iron_ingot", () -> new Item(props()));

    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_NORMAL = register("crossbow_arrow_normal", () -> new CrossbowArrowItem(CrossbowArrowItem.EnumArrowType.NORMAL));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_SPITFIRE = register("crossbow_arrow_spitfire", () -> new CrossbowArrowItem(CrossbowArrowItem.EnumArrowType.SPITFIRE));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_VAMPIRE_KILLER = register("crossbow_arrow_vampire_killer", () -> new CrossbowArrowItem(CrossbowArrowItem.EnumArrowType.VAMPIRE_KILLER));
    public static final DeferredItem<CrossbowArrowItem> CROSSBOW_ARROW_TELEPORT = register("crossbow_arrow_teleport", () -> new CrossbowArrowItem(CrossbowArrowItem.EnumArrowType.TELEPORT));

    public static final DeferredItem<SingleCrossbowItem> ENHANCED_CROSSBOW = register("enhanced_crossbow", () -> new SingleCrossbowItem(props().durability(930), 1.5F, 15, Tiers.IRON));
    public static final DeferredItem<DoubleCrossbowItem> ENHANCED_DOUBLE_CROSSBOW = register("enhanced_double_crossbow", () -> new DoubleCrossbowItem(props().durability(930),1.5F, 15, Tiers.IRON));
    public static final DeferredItem<TechCrossbowItem> ENHANCED_TECH_CROSSBOW = register("enhanced_tech_crossbow", () -> new TechCrossbowItem(props().durability(1860), 1.7F, 30, Tiers.DIAMOND));

    public static final DeferredItem<Item> GARLIC_DIFFUSER_CORE = register("garlic_diffuser_core", () -> new Item(props()));
    public static final DeferredItem<Item> GARLIC_DIFFUSER_CORE_IMPROVED = register("garlic_diffuser_core_improved", () -> new Item(props()));

    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_NORMAL = register("heart_seeker_normal", () -> new HeartSeekerItem(HeartSeekerItem.NORMAL));
    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_ENHANCED = register("heart_seeker_enhanced", () -> new HeartSeekerItem(HeartSeekerItem.ENHANCED));
    public static final DeferredItem<HeartSeekerItem> HEART_SEEKER_ULTIMATE = register("heart_seeker_ultimate", () -> new HeartSeekerItem(HeartSeekerItem.ULTIMATE));

    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_NORMAL = register("heart_striker_normal", () -> new HeartStrikerItem(HeartStrikerItem.NORMAL));
    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_ENHANCED = register("heart_striker_enhanced", () -> new HeartStrikerItem(HeartStrikerItem.ENHANCED));
    public static final DeferredItem<HeartStrikerItem> HEART_STRIKER_ULTIMATE = register("heart_striker_ultimate", () -> new HeartStrikerItem(HeartStrikerItem.ULTIMATE));

    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_NORMAL = register("holy_water_bottle_normal", () -> new HolyWaterBottleItem(IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_ENHANCED = register("holy_water_bottle_enhanced", () -> new HolyWaterBottleItem(IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HolyWaterBottleItem> HOLY_WATER_BOTTLE_ULTIMATE = register("holy_water_bottle_ultimate", () -> new HolyWaterBottleItem(IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_NORMAL = register("holy_water_splash_bottle_normal", () -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_ENHANCED = register("holy_water_splash_bottle_enhanced", () -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HolyWaterSplashBottleItem> HOLY_WATER_SPLASH_BOTTLE_ULTIMATE = register("holy_water_splash_bottle_ultimate", () -> new HolyWaterSplashBottleItem(IItemWithTier.TIER.ULTIMATE));

    public static final DeferredItem<BlessableItem> PURE_SALT_WATER = register("pure_salt_water", () -> new BlessableItem(new Item.Properties().stacksTo(1), HOLY_WATER_BOTTLE_NORMAL::get, HOLY_WATER_BOTTLE_ENHANCED::get) {
        @Override
        public boolean isFoil(@NotNull ItemStack stack) {
            return true;
        }
    });

    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_NORMAL = register("hunter_axe_normal", () -> new HunterAxeItem(HunterAxeItem.NORMAL));
    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_ENHANCED = register("hunter_axe_enhanced", () -> new HunterAxeItem(HunterAxeItem.ENHANCED));
    public static final DeferredItem<HunterAxeItem> HUNTER_AXE_ULTIMATE = register("hunter_axe_ultimate", () -> new HunterAxeItem(HunterAxeItem.ULTIMATE));

    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_NORMAL = register("hunter_coat_chest_normal", () -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_ENHANCED = register("hunter_coat_chest_enhanced", () -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_CHEST_ULTIMATE = register("hunter_coat_chest_ultimate", () -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorItem.Type.CHESTPLATE, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_NORMAL = register("hunter_coat_feet_normal", () -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorItem.Type.BOOTS, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_ENHANCED = register("hunter_coat_feet_enhanced", () -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorItem.Type.BOOTS, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_FEET_ULTIMATE = register("hunter_coat_feet_ultimate", () -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorItem.Type.BOOTS, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_NORMAL = register("hunter_coat_head_normal", () -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorItem.Type.HELMET, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_ENHANCED = register("hunter_coat_head_enhanced", () -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorItem.Type.HELMET, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_HEAD_ULTIMATE = register("hunter_coat_head_ultimate", () -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorItem.Type.HELMET, IItemWithTier.TIER.ULTIMATE));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_NORMAL = register("hunter_coat_legs_normal", () -> new HunterCoatItem(ModArmorMaterials.NORMAL_HUNTER_COAT, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_ENHANCED = register("hunter_coat_legs_enhanced", () -> new HunterCoatItem(ModArmorMaterials.ENHANCED_HUNTER_COAT, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<HunterCoatItem> HUNTER_COAT_LEGS_ULTIMATE = register("hunter_coat_legs_ultimate", () -> new HunterCoatItem(ModArmorMaterials.ULTIMATE_HUNTER_COAT, ArmorItem.Type.LEGGINGS, IItemWithTier.TIER.ULTIMATE));

    public static final DeferredItem<HunterHatItem> HUNTER_HAT_HEAD_0 = register("hunter_hat_head_0", () -> new HunterHatItem(HunterHatItem.HatType.TYPE_1, ModArmorMaterials.HUNTER_HAT_0));
    public static final DeferredItem<HunterHatItem> HUNTER_HAT_HEAD_1 = register("hunter_hat_head_1", () -> new HunterHatItem(HunterHatItem.HatType.TYPE_2, ModArmorMaterials.HUNTER_HAT_1));

    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_0 = register("hunter_intel_0", () -> new HunterIntelItem(0));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_1 = register("hunter_intel_1", () -> new HunterIntelItem(1));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_2 = register("hunter_intel_2", () -> new HunterIntelItem(2));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_3 = register("hunter_intel_3", () -> new HunterIntelItem(3));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_4 = register("hunter_intel_4", () -> new HunterIntelItem(4));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_5 = register("hunter_intel_5", () -> new HunterIntelItem(5));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_6 = register("hunter_intel_6", () -> new HunterIntelItem(6));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_7 = register("hunter_intel_7", () -> new HunterIntelItem(7));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_8 = register("hunter_intel_8", () -> new HunterIntelItem(8));
    public static final DeferredItem<HunterIntelItem> HUNTER_INTEL_9 = register("hunter_intel_9", () -> new HunterIntelItem(9));

    public static final DeferredItem<VampirismItemBloodFoodItem> HUMAN_HEART = register("human_heart", () -> new VampirismItemBloodFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(1f).build()), new FoodProperties.Builder().nutrition(20).saturationModifier(1.5F).build()));

    public static final DeferredItem<InjectionItem> INJECTION_EMPTY = register("injection_empty", () -> new InjectionItem(InjectionItem.TYPE.EMPTY));
    public static final DeferredItem<InjectionItem> INJECTION_GARLIC = register("injection_garlic", () -> new InjectionItem(InjectionItem.TYPE.GARLIC));
    public static final DeferredItem<InjectionItem> INJECTION_SANGUINARE = register("injection_sanguinare", () -> new InjectionItem(InjectionItem.TYPE.SANGUINARE));

    public static final DeferredItem<BucketItem> IMPURE_BLOOD_BUCKET = register("impure_blood_bucket", CreativeModeTabs.TOOLS_AND_UTILITIES, () -> new BucketItem(ModFluids.IMPURE_BLOOD.get(), new Item.Properties().craftRemainder(Items.BUCKET).stacksTo(1)));
    public static final DeferredItem<GarlicBreadItem> GARLIC_BREAD = register("garlic_bread", GarlicBreadItem::new);
    public static final DeferredItem<AlchemicalFireItem> ITEM_ALCHEMICAL_FIRE = register("item_alchemical_fire", AlchemicalFireItem::new);

    public static final DeferredItem<TentItem> ITEM_TENT = register("item_tent", () -> new TentItem(false));
    public static final DeferredItem<TentItem> ITEM_TENT_SPAWNER = register("item_tent_spawner", () -> new TentItem(true));

    public static final DeferredItem<PitchforkItem> PITCHFORK = register("pitchfork", PitchforkItem::new);

    public static final DeferredItem<PureBloodItem> PURE_BLOOD_0 = register("pure_blood_0", () -> new PureBloodItem(0));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_1 = register("pure_blood_1", () -> new PureBloodItem(1));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_2 = register("pure_blood_2", () -> new PureBloodItem(2));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_3 = register("pure_blood_3", () -> new PureBloodItem(3));
    public static final DeferredItem<PureBloodItem> PURE_BLOOD_4 = register("pure_blood_4", () -> new PureBloodItem(4));

    public static final DeferredItem<Item> PURIFIED_GARLIC = register("purified_garlic", () -> new Item(props()));
    public static final DeferredItem<Item> PURE_SALT = register("pure_salt", () -> new Item(props()));
    public static final DeferredItem<Item> SOUL_ORB_VAMPIRE = register("soul_orb_vampire", () -> new Item(props()));

    public static final DeferredItem<StakeItem> STAKE = register("stake", StakeItem::new);
    public static final DeferredItem<ArrowContainer> ARROW_CLIP = register("tech_crossbow_ammo_package", () -> new ArrowContainer(props().stacksTo(1), 12, (stack) -> stack.is(CROSSBOW_ARROW_NORMAL.get())) {
        @Override
        public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> texts, @NotNull TooltipFlag flag) {
            texts.add(Component.translatable("item.vampirism.tech_crossbow_ammo_package.tooltip", Component.translatable(BASIC_TECH_CROSSBOW.get().getDescriptionId())).withStyle(ChatFormatting.GRAY));
            texts.add(Component.translatable("item.vampirism.arrow_clip.right_click").withStyle(ChatFormatting.GRAY));
            super.appendHoverText(stack, context, texts, flag);
        }

    });

    public static final DeferredItem<ColoredVampireClothingItem> VAMPIRE_CLOAK_BLACK_BLUE = register("vampire_cloak_black_blue", () -> new ColoredVampireClothingItem(ArmorItem.Type.CHESTPLATE, ColoredVampireClothingItem.EnumClothingColor.BLACKBLUE));
    public static final DeferredItem<ColoredVampireClothingItem> VAMPIRE_CLOAK_BLACK_RED = register("vampire_cloak_black_red", () -> new ColoredVampireClothingItem(ArmorItem.Type.CHESTPLATE, ColoredVampireClothingItem.EnumClothingColor.BLACKRED));
    public static final DeferredItem<ColoredVampireClothingItem> VAMPIRE_CLOAK_BLACK_WHITE = register("vampire_cloak_black_white", () -> new ColoredVampireClothingItem(ArmorItem.Type.CHESTPLATE, ColoredVampireClothingItem.EnumClothingColor.BLACKWHITE));
    public static final DeferredItem<ColoredVampireClothingItem> VAMPIRE_CLOAK_RED_BLACK = register("vampire_cloak_red_black", () -> new ColoredVampireClothingItem(ArmorItem.Type.CHESTPLATE, ColoredVampireClothingItem.EnumClothingColor.REDBLACK));
    public static final DeferredItem<ColoredVampireClothingItem> VAMPIRE_CLOAK_WHITE_BLACK = register("vampire_cloak_white_black", () -> new ColoredVampireClothingItem(ArmorItem.Type.CHESTPLATE, ColoredVampireClothingItem.EnumClothingColor.WHITEBLACK));

    public static final DeferredItem<VampireBloodBottleItem> VAMPIRE_BLOOD_BOTTLE = register("vampire_blood_bottle", VampireBloodBottleItem::new);
    public static final DeferredItem<VampireBookItem> VAMPIRE_BOOK = register("vampire_book", VampireBookItem::new);
    public static final DeferredItem<VampireFangItem> VAMPIRE_FANG = register("vampire_fang", VampireFangItem::new);
    public static final DeferredItem<VampirismItemBloodFoodItem> WEAK_HUMAN_HEART = register("weak_human_heart", () -> new VampirismItemBloodFoodItem(new Item.Properties().food(new FoodProperties.Builder().nutrition(3).saturationModifier(1f).build()), new FoodProperties.Builder().nutrition(10).saturationModifier(0.9F).build()));

    public static final DeferredItem<SpawnEggItem> VAMPIRE_SPAWN_EGG = register("vampire_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.VAMPIRE, 0x881d99, 0x5e1975, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ADVANCED_VAMPIRE_SPAWN_EGG = register("advanced_vampire_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.ADVANCED_VAMPIRE, 0x881d99, 0xedbb24, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> VAMPIRE_BARON_SPAWN_EGG = register("vampire_baron_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.VAMPIRE_BARON, 0x9a1b1b, 0x252525, new Item.Properties()));

    public static final DeferredItem<SpawnEggItem> VAMPIRE_HUNTER_SPAWN_EGG = register("vampire_hunter_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.HUNTER, 0x173f9c, 142163, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> ADVANCED_VAMPIRE_HUNTER_SPAWN_EGG = register("advanced_vampire_hunter_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.ADVANCED_HUNTER, 0x173f9c, 0xedbb24, new Item.Properties()));
    public static final DeferredItem<SpawnEggItem> HUNTER_TRAINER_SPAWN_EGG = register("hunter_trainer_spawn_egg", null, () -> new DeferredSpawnEggItem(ModEntities.HUNTER_TRAINER, 0xc1bebe, 0x252525, new Item.Properties()));

    public static final DeferredItem<UmbrellaItem> UMBRELLA = register("umbrella", UmbrellaItem::new);

    public static final DeferredItem<Item> HUNTER_MINION_EQUIPMENT = register("hunter_minion_equipment", () -> new Item(props()));
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_SIMPLE = register("hunter_minion_upgrade_simple", () -> new MinionUpgradeItem(1, 2, VReference.HUNTER_FACTION));
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_ENHANCED = register("hunter_minion_upgrade_enhanced", () -> new MinionUpgradeItem(3, 4, VReference.HUNTER_FACTION));
    public static final DeferredItem<MinionUpgradeItem> HUNTER_MINION_UPGRADE_SPECIAL = register("hunter_minion_upgrade_special", () -> new MinionUpgradeItem(5, 6, VReference.HUNTER_FACTION));
    public static final DeferredItem<FeedingAdapterItem> FEEDING_ADAPTER = register("feeding_adapter", FeedingAdapterItem::new);
    public static final DeferredItem<Item> VAMPIRE_MINION_BINDING = register("vampire_minion_binding", () -> new Item(props()));
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_SIMPLE = register("vampire_minion_upgrade_simple", () -> new MinionUpgradeItem(1, 2, VReference.VAMPIRE_FACTION));
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_ENHANCED = register("vampire_minion_upgrade_enhanced", () -> new MinionUpgradeItem(3, 4, VReference.VAMPIRE_FACTION));
    public static final DeferredItem<MinionUpgradeItem> VAMPIRE_MINION_UPGRADE_SPECIAL = register("vampire_minion_upgrade_special", () -> new MinionUpgradeItem(5, 6, VReference.VAMPIRE_FACTION));

    public static final DeferredItem<OblivionItem> OBLIVION_POTION = register("oblivion_potion", () -> new OblivionItem(props()));

    public static final DeferredItem<RefinementItem> AMULET = register("amulet", () -> new VampireRefinementItem(props(), IRefinementItem.AccessorySlotType.AMULET));
    public static final DeferredItem<RefinementItem> RING = register("ring", () -> new VampireRefinementItem(props(), IRefinementItem.AccessorySlotType.RING));
    public static final DeferredItem<RefinementItem> OBI_BELT = register("obi_belt", () -> new VampireRefinementItem(props(), IRefinementItem.AccessorySlotType.OBI_BELT));

    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_CROWN = register("vampire_clothing_crown", () -> new VampireClothingItem(ArmorItem.Type.HELMET, ModArmorMaterials.VAMPIRE_CLOTH_CROWN));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_LEGS = register("vampire_clothing_legs", () -> new VampireClothingItem(ArmorItem.Type.LEGGINGS, ModArmorMaterials.VAMPIRE_CLOTH_LEGS));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_BOOTS = register("vampire_clothing_boots", () -> new VampireClothingItem(ArmorItem.Type.BOOTS, ModArmorMaterials.VAMPIRE_CLOTH_BOOTS));
    public static final DeferredItem<VampireClothingItem> VAMPIRE_CLOTHING_HAT = register("vampire_clothing_hat", () -> new VampireClothingItem(ArmorItem.Type.HELMET, ModArmorMaterials.VAMPIRE_CLOTH_HAT));

    public static final DeferredItem<Item> GARLIC_FINDER = register("garlic_finder", () -> new Item(props().rarity(Rarity.RARE)));

    public static final DeferredItem<SignItem> DARK_SPRUCE_SIGN = register("dark_spruce_sign", () -> new SignItem((new Item.Properties()).stacksTo(16), ModBlocks.DARK_SPRUCE_SIGN.get(), ModBlocks.DARK_SPRUCE_WALL_SIGN.get()));
    public static final DeferredItem<SignItem> CURSED_SPRUCE_SIGN = register("cursed_spruce_sign", () -> new SignItem((new Item.Properties()).stacksTo(16), ModBlocks.CURSED_SPRUCE_SIGN.get(), ModBlocks.CURSED_SPRUCE_WALL_SIGN.get()));

    public static final DeferredItem<CrucifixItem> CRUCIFIX_NORMAL = register("crucifix_normal", () -> new CrucifixItem(IItemWithTier.TIER.NORMAL));
    public static final DeferredItem<CrucifixItem> CRUCIFIX_ENHANCED = register("crucifix_enhanced", () -> new CrucifixItem(IItemWithTier.TIER.ENHANCED));
    public static final DeferredItem<CrucifixItem> CRUCIFIX_ULTIMATE = register("crucifix_ultimate", () -> new CrucifixItem(IItemWithTier.TIER.ULTIMATE));

    public static final DeferredItem<BoatItem> DARK_SPRUCE_BOAT = register(ModRegistryItems.DARK_SPRUCE_BOAT.getId().getPath(), () -> new BoatItem(false, VEnums.DARK_SPRUCE_BOAT_TYPE.getValue(), props().stacksTo(1)));
    public static final DeferredItem<BoatItem> CURSED_SPRUCE_BOAT = register(ModRegistryItems.CURSED_SPRUCE_BOAT.getId().getPath(), () -> new BoatItem(false, VEnums.CURSED_SPRUCE_BOAT_TYPE.getValue(), props().stacksTo(1)));
    public static final DeferredItem<BoatItem> DARK_SPRUCE_CHEST_BOAT = register(ModRegistryItems.DARK_SPRUCE_CHEST_BOAT.getId().getPath(), () -> new BoatItem(true, VEnums.DARK_SPRUCE_BOAT_TYPE.getValue(), props().stacksTo(1)));
    public static final DeferredItem<BoatItem> CURSED_SPRUCE_CHEST_BOAT = register(ModRegistryItems.CURSED_SPRUCE_CHEST_BOAT.getId().getPath(), () -> new BoatItem(true, VEnums.CURSED_SPRUCE_BOAT_TYPE.getValue(), props().stacksTo(1)));

    public static final DeferredItem<OilBottleItem> OIL_BOTTLE = register("oil_bottle", () -> new OilBottleItem(props().stacksTo(1)));
    public static final DeferredItem<HangingSignItem> DARK_SPRUCE_HANGING_SIGN = register("dark_spruce_hanging_sign", () -> new HangingSignItem(ModBlocks.DARK_SPRUCE_HANGING_SIGN.get(), ModBlocks.DARK_SPRUCE_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));
    public static final DeferredItem<HangingSignItem> CURSED_SPRUCE_HANGING_SIGN = register("cursed_spruce_hanging_sign", () -> new HangingSignItem(ModBlocks.CURSED_SPRUCE_HANGING_SIGN.get(), ModBlocks.CURSED_SPRUCE_WALL_HANGING_SIGN.get(), new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> MOTHER_CORE = register("mother_core", () -> new Item(props().rarity(Rarity.UNCOMMON)));

    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK = register("candle_stick", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK.get(), ModBlocks.WALL_CANDLE_STICK.get(), Helper.STANDING_AND_WALL_CANDLE_STICKS));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_NORMAL = ITEMS.register("candle_stick_normal", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_NORMAL.get(), ModBlocks.WALL_CANDLE_STICK_NORMAL.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_WHITE = ITEMS.register("candle_stick_white", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_WHITE.get(), ModBlocks.WALL_CANDLE_STICK_WHITE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_ORANGE = ITEMS.register("candle_stick_orange", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_ORANGE.get(), ModBlocks.WALL_CANDLE_STICK_ORANGE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_MAGENTA = ITEMS.register("candle_stick_magenta", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_MAGENTA.get(), ModBlocks.WALL_CANDLE_STICK_MAGENTA.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_LIGHT_BLUE = ITEMS.register("candle_stick_light_blue", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_LIGHT_BLUE.get(), ModBlocks.WALL_CANDLE_STICK_LIGHT_BLUE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_YELLOW = ITEMS.register("candle_stick_yellow", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_YELLOW.get(), ModBlocks.WALL_CANDLE_STICK_YELLOW.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_LIME = ITEMS.register("candle_stick_lime", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_LIME.get(), ModBlocks.WALL_CANDLE_STICK_LIME.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_PINK = ITEMS.register("candle_stick_pink", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_PINK.get(), ModBlocks.WALL_CANDLE_STICK_PINK.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_GRAY = ITEMS.register("candle_stick_gray", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_GRAY.get(), ModBlocks.WALL_CANDLE_STICK_GRAY.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_LIGHT_GRAY = ITEMS.register("candle_stick_light_gray", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_LIGHT_GRAY.get(), ModBlocks.WALL_CANDLE_STICK_LIGHT_GRAY.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_CYAN = ITEMS.register("candle_stick_cyan", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_CYAN.get(), ModBlocks.WALL_CANDLE_STICK_CYAN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_PURPLE = ITEMS.register("candle_stick_purple", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_PURPLE.get(), ModBlocks.WALL_CANDLE_STICK_PURPLE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_BLUE = ITEMS.register("candle_stick_blue", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_BLUE.get(), ModBlocks.WALL_CANDLE_STICK_BLUE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_BROWN = ITEMS.register("candle_stick_brown", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_BROWN.get(), ModBlocks.WALL_CANDLE_STICK_BROWN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_GREEN = ITEMS.register("candle_stick_green", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_GREEN.get(), ModBlocks.WALL_CANDLE_STICK_GREEN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_RED = ITEMS.register("candle_stick_red", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_RED.get(), ModBlocks.WALL_CANDLE_STICK_RED.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDLE_STICK_BLACK = ITEMS.register("candle_stick_black", () -> new PairCandleHolderItem(ModBlocks.CANDLE_STICK_BLACK.get(), ModBlocks.WALL_CANDLE_STICK_BLACK.get()));

    public static final DeferredItem<PairCandleHolderItem> CANDELABRA = ITEMS.register("candelabra", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA.get(), ModBlocks.WALL_CANDELABRA.get(), Helper.STANDING_AND_WALL_CANDELABRAS));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_NORMAL = ITEMS.register("candelabra_normal", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_NORMAL.get(), ModBlocks.WALL_CANDELABRA_NORMAL.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_WHITE = ITEMS.register("candelabra_white", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_WHITE.get(), ModBlocks.WALL_CANDELABRA_WHITE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_ORANGE = ITEMS.register("candelabra_orange", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_ORANGE.get(), ModBlocks.WALL_CANDELABRA_ORANGE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_MAGENTA = ITEMS.register("candelabra_magenta", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_MAGENTA.get(), ModBlocks.WALL_CANDELABRA_MAGENTA.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_LIGHT_BLUE = ITEMS.register("candelabra_light_blue", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_LIGHT_BLUE.get(), ModBlocks.WALL_CANDELABRA_LIGHT_BLUE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_YELLOW = ITEMS.register("candelabra_yellow", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_YELLOW.get(), ModBlocks.WALL_CANDELABRA_YELLOW.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_LIME = ITEMS.register("candelabra_lime", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_LIME.get(), ModBlocks.WALL_CANDELABRA_LIME.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_PINK = ITEMS.register("candelabra_pink", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_PINK.get(), ModBlocks.WALL_CANDELABRA_PINK.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_GRAY = ITEMS.register("candelabra_gray", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_GRAY.get(), ModBlocks.WALL_CANDELABRA_GRAY.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_LIGHT_GRAY = ITEMS.register("candelabra_light_gray", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_LIGHT_GRAY.get(), ModBlocks.WALL_CANDELABRA_LIGHT_GRAY.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_CYAN = ITEMS.register("candelabra_cyan", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_CYAN.get(), ModBlocks.WALL_CANDELABRA_CYAN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_PURPLE = ITEMS.register("candelabra_purple", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_PURPLE.get(), ModBlocks.WALL_CANDELABRA_PURPLE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_BLUE = ITEMS.register("candelabra_blue", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_BLUE.get(), ModBlocks.WALL_CANDELABRA_BLUE.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_BROWN = ITEMS.register("candelabra_brown", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_BROWN.get(), ModBlocks.WALL_CANDELABRA_BROWN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_GREEN = ITEMS.register("candelabra_green", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_GREEN.get(), ModBlocks.WALL_CANDELABRA_GREEN.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_RED = ITEMS.register("candelabra_red", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_RED.get(), ModBlocks.WALL_CANDELABRA_RED.get()));
    public static final DeferredItem<PairCandleHolderItem> CANDELABRA_BLACK = ITEMS.register("candelabra_black", () -> new PairCandleHolderItem(ModBlocks.CANDELABRA_BLACK.get(), ModBlocks.WALL_CANDELABRA_BLACK.get()));

    public static final DeferredItem<CandleHolderItem> CHANDELIER = ITEMS.register("chandelier", () -> new CandleHolderItem(ModBlocks.CHANDELIER.get(), Helper.HANGING_CHANDELIERS));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_NORMAL = ITEMS.register("chandelier_normal", () -> new CandleHolderItem(ModBlocks.CHANDELIER_NORMAL.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_WHITE = ITEMS.register("chandelier_white", () -> new CandleHolderItem(ModBlocks.CHANDELIER_WHITE.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_ORANGE = ITEMS.register("chandelier_orange", () -> new CandleHolderItem(ModBlocks.CHANDELIER_ORANGE.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_MAGENTA = ITEMS.register("chandelier_magenta", () -> new CandleHolderItem(ModBlocks.CHANDELIER_MAGENTA.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_LIGHT_BLUE = ITEMS.register("chandelier_light_blue", () -> new CandleHolderItem(ModBlocks.CHANDELIER_LIGHT_BLUE.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_YELLOW = ITEMS.register("chandelier_yellow", () -> new CandleHolderItem(ModBlocks.CHANDELIER_YELLOW.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_LIME = ITEMS.register("chandelier_lime", () -> new CandleHolderItem(ModBlocks.CHANDELIER_LIME.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_PINK = ITEMS.register("chandelier_pink", () -> new CandleHolderItem(ModBlocks.CHANDELIER_PINK.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_GRAY = ITEMS.register("chandelier_gray", () -> new CandleHolderItem(ModBlocks.CHANDELIER_GRAY.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_LIGHT_GRAY = ITEMS.register("chandelier_light_gray", () -> new CandleHolderItem(ModBlocks.CHANDELIER_LIGHT_GRAY.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_CYAN = ITEMS.register("chandelier_cyan", () -> new CandleHolderItem(ModBlocks.CHANDELIER_CYAN.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_PURPLE = ITEMS.register("chandelier_purple", () -> new CandleHolderItem(ModBlocks.CHANDELIER_PURPLE.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_BLUE = ITEMS.register("chandelier_blue", () -> new CandleHolderItem(ModBlocks.CHANDELIER_BLUE.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_BROWN = ITEMS.register("chandelier_brown", () -> new CandleHolderItem(ModBlocks.CHANDELIER_BROWN.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_GREEN = ITEMS.register("chandelier_green", () -> new CandleHolderItem(ModBlocks.CHANDELIER_GREEN.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_RED = ITEMS.register("chandelier_red", () -> new CandleHolderItem(ModBlocks.CHANDELIER_RED.get()));
    public static final DeferredItem<CandleHolderItem> CHANDELIER_BLACK = ITEMS.register("chandelier_black", () -> new CandleHolderItem(ModBlocks.CHANDELIER_BLACK.get()));

    static <I extends Item> DeferredItem<I> register(final String name, ResourceKey<CreativeModeTab> tab, final Supplier<? extends I> sup) {
        DeferredItem<I> item = ITEMS.register(name, sup);
        if (tab == VAMPIRISM_TAB_KEY) {
            VAMPIRISM_TAB_ITEMS.add(item);
        } else {
            CREATIVE_TAB_ITEMS.computeIfAbsent(tab, (a) -> new HashSet<>()).add(item);
        }
        return item;
    }

    static <I extends Item> DeferredItem<I> register(final String name, final Supplier<? extends I> sup) {
        return register(name, VAMPIRISM_TAB_KEY, sup);
    }

    static void register(IEventBus bus) {
        CREATIVE_TABS.register(bus);
        ITEMS.register(bus);
        if (VampirismMod.inDataGen) {
            DeferredRegister.Items DUMMY_ITEMS = DeferredRegister.createItems("guideapi_vp");
            DeferredItem<DummyItem> dummy_item = DUMMY_ITEMS.register("vampirism-guidebook", DummyItem::new);
            DUMMY_ITEMS.register(bus);
            VAMPIRISM_TAB_ITEMS.add(dummy_item);
        }
    }

    private static Item.@NotNull Properties props() {
        return new Item.Properties();
    }

    static void registerOtherCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        CREATIVE_TAB_ITEMS.forEach((tab, items) -> {
            if (event.getTabKey() == tab) {
                items.forEach(item -> event.accept(item.get()));
            }
        });
    }

    public static void registerDispenserBehaviourUnsafe() {
        DispenserBlock.registerBehavior(ModItems.DARK_SPRUCE_BOAT.get(), new BoatDispenseItemBehavior(VEnums.DARK_SPRUCE_BOAT_TYPE.getValue()));
        DispenserBlock.registerBehavior(ModItems.CURSED_SPRUCE_BOAT.get(), new BoatDispenseItemBehavior(VEnums.CURSED_SPRUCE_BOAT_TYPE.getValue()));
        DispenserBlock.registerBehavior(ModItems.DARK_SPRUCE_CHEST_BOAT.get(), new BoatDispenseItemBehavior(VEnums.DARK_SPRUCE_BOAT_TYPE.getValue(), true));
        DispenserBlock.registerBehavior(ModItems.CURSED_SPRUCE_CHEST_BOAT.get(), new BoatDispenseItemBehavior(VEnums.CURSED_SPRUCE_BOAT_TYPE.getValue(), true));
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_NORMAL.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_SPITFIRE.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_TELEPORT.get());
        DispenserBlock.registerProjectileBehavior(ModItems.CROSSBOW_ARROW_VAMPIRE_KILLER.get());
    }
}
