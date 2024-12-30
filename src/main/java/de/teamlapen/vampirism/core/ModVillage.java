package de.teamlapen.vampirism.core;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.entity.villager.VampirismTrades;
import de.teamlapen.vampirism.util.MapUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.entity.schedule.Schedule;
import net.minecraft.world.entity.schedule.ScheduleBuilder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;

public class ModVillage {
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, REFERENCE.MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, REFERENCE.MODID);
    public static final DeferredRegister<Schedule> SCHEDULES = DeferredRegister.create(Registries.SCHEDULE, REFERENCE.MODID);

    public static final DeferredHolder<PoiType, PoiType> HUNTER_TOTEM = POI_TYPES.register("hunter_totem", () -> new PoiType(getAllStates(ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER.get(), ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER_CRAFTED.get()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> VAMPIRE_TOTEM = POI_TYPES.register("vampire_totem", () -> new PoiType(getAllStates(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE.get(), ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE_CRAFTED.get()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> NO_FACTION_TOTEM = POI_TYPES.register("no_faction_totem", () -> new PoiType(getAllStates(ModBlocks.TOTEM_TOP.get(), ModBlocks.TOTEM_TOP_CRAFTED.get()), 1, 1));
    public static final DeferredHolder<PoiType, PoiType> ALTAR_CLEANSING = POI_TYPES.register("church_altar", () -> new PoiType(getAllStates(ModBlocks.ALTAR_CLEANSING.get()), 1, 1));

    public static final DeferredHolder<Schedule, Schedule> CONVERTED_DEFAULT = SCHEDULES.register("converted_default", () ->
            new ScheduleBuilder(new Schedule()).changeActivityAt(12000, Activity.IDLE).changeActivityAt(10, Activity.REST).changeActivityAt(14000, Activity.WORK).changeActivityAt(21000, Activity.MEET).changeActivityAt(23000, Activity.IDLE).build());

    public static final DeferredHolder<VillagerProfession, VillagerProfession> VAMPIRE_EXPERT = PROFESSIONS.register("vampire_expert", () -> new VillagerProfession(REFERENCE.MODID + ":vampire_expert", (holder) -> holder.is(ModTags.PoiTypes.IS_VAMPIRE), (holder) -> holder.is(ModTags.PoiTypes.IS_VAMPIRE), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> HUNTER_EXPERT = PROFESSIONS.register("hunter_expert", () -> new VillagerProfession(REFERENCE.MODID + ":hunter_expert", (holder) -> holder.is(ModTags.PoiTypes.IS_HUNTER), (holder) -> holder.is(ModTags.PoiTypes.IS_HUNTER), ImmutableSet.of(), ImmutableSet.of(), SoundEvents.VILLAGER_WORK_CARTOGRAPHER));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> PRIEST = PROFESSIONS.register("priest", () -> new VillagerProfession(REFERENCE.MODID + ":priest", holder -> ALTAR_CLEANSING.getKey() != null && holder.is(ALTAR_CLEANSING.getKey()), holder -> ALTAR_CLEANSING.getKey() != null && holder.is(ALTAR_CLEANSING.getKey()), ImmutableSet.of(), ImmutableSet.of(), ModSounds.BLESSING_MUSIC.get()));

    static void register(IEventBus bus) {
        POI_TYPES.register(bus);
        PROFESSIONS.register(bus);
        SCHEDULES.register(bus);
    }

    public static void villagerTradeSetup() {
        for (Map.Entry<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> entry : getTrades().entrySet()) {
            VillagerTrades.TRADES.computeIfAbsent(entry.getKey(), trades -> new Int2ObjectOpenHashMap<>()).putAll(entry.getValue());
        }
        VillagerTrades.WANDERING_TRADER_TRADES.computeIfAbsent(1, getWanderingTraderTrades());
    }

    private static Set<BlockState> getAllStates(Block @NotNull ... blocks) {
        return Arrays.stream(blocks).flatMap(block -> block.getStateDefinition().getPossibleStates().stream()).collect(ImmutableSet.toImmutableSet());
    }

    public static Map<VillagerProfession, Int2ObjectMap<VillagerTrades.ItemListing[]>> getTrades() {
        return Util.make(Maps.newHashMap(), map -> {
            map.put(
                    VAMPIRE_EXPERT.get(), new Int2ObjectOpenHashMap<>(ImmutableMap.of(
                            1,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(10, 15), ModItems.PURE_BLOOD_0.get(), new VampirismTrades.Price(1, 1), 2, 2),
                                    // When buying, the player only gets one bottle and not the amount set in the offer for some reason
                                    //new VampirismTrades.BloodBottleForHeart(new VampirismTrades.Price(3, 12), new VampirismTrades.Price(1, 15), 9, 4, 1),
                                    new VillagerTrades.EmeraldForItems(ModBlocks.VAMPIRE_ORCHID.get(), 8, 8, 2),
                                    new VampirismTrades.VampireForestMapTrade(5, MapUtil.getModTranslation("vampire_forest"), MapDecorationTypes.TARGET_POINT, 12, 2)
                            },
                            2,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(25, 30), ModItems.PURE_BLOOD_1.get(), new VampirismTrades.Price(1, 1), 2, 5),
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(3, 10), ModBlocks.COFFIN_RED.get(), new VampirismTrades.Price(1, 1), 4, 5),
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(10, 25), ModItems.BLOOD_INFUSED_IRON_INGOT.get(), new VampirismTrades.Price(1, 3), 8, 5),
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(10, 30), new ItemStack[]{
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_BLUE.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_RED.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_WHITE.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_RED_BLACK.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_WHITE_BLACK.get())}, new VampirismTrades.Price(1, 1), 4, 10)
                            },
                            3,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(30, 40), ModItems.PURE_BLOOD_2.get(), new VampirismTrades.Price(1, 1), 2, 10),
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(15, 30), ModItems.BLOOD_INFUSED_ENHANCED_IRON_INGOT.get(), new VampirismTrades.Price(1, 2), 12, 10),
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(10, 30), new ItemStack[]{
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_BLUE.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_RED.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_BLACK_WHITE.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_RED_BLACK.get()),
                                            new ItemStack(ModItems.VAMPIRE_CLOAK_WHITE_BLACK.get())}, new VampirismTrades.Price(1, 1), 4, 10)
                            },
                            4,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(20, 30), ModItems.PURE_BLOOD_3.get(), new VampirismTrades.Price(1, 1), 2, 10),
                                    new VampirismTrades.TreasureMapForEmeralds(14, ModTags.Structures.ON_CRYPT_MAPS, MapUtil.getModTranslation("crypt"), ModMapDecorations.CRYPT, 12, 15)
                            },
                            5,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForHeart(new VampirismTrades.Price(30, 40), ModItems.PURE_BLOOD_4.get(), new VampirismTrades.Price(1, 1), 2, 30),
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HEART_SEEKER_ULTIMATE.get(), 40, 1, 30),
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HEART_STRIKER_ULTIMATE.get(), 40, 1, 30),
                            }
                    ))
            );
            map.put(
                    HUNTER_EXPERT.get(), new Int2ObjectOpenHashMap<>(ImmutableMap.of(
                            1,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 20), ModBlocks.GARLIC.get(), new VampirismTrades.Price(2, 5)),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 40), ModItems.ARMOR_OF_SWIFTNESS_CHEST_NORMAL.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 35), ModItems.ARMOR_OF_SWIFTNESS_LEGS_NORMAL.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 15), ModItems.ARMOR_OF_SWIFTNESS_FEET_NORMAL.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 30), ModItems.ARMOR_OF_SWIFTNESS_HEAD_NORMAL.get(), new VampirismTrades.Price(1, 1), 6, 1),
                                    new VampirismTrades.VampireForestMapTrade(5, MapUtil.getModTranslation("vampire_forest"), MapDecorationTypes.TARGET_POINT, 12, 2)
                            },
                            2,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 45), Items.DIAMOND, new VampirismTrades.Price(1, 1), 2, 5),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 20), ModItems.CROSSBOW_ARROW_NORMAL.get(), new VampirismTrades.Price(5, 15)),
                                    new VillagerTrades.ItemsForEmeralds(ModItems.SOUL_ORB_VAMPIRE.get(), 10, 10, 4),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 40), ModItems.HUNTER_COAT_CHEST_NORMAL.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 35), ModItems.HUNTER_COAT_LEGS_NORMAL.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 15), ModItems.HUNTER_COAT_FEET_NORMAL.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 30), ModItems.HUNTER_COAT_HEAD_NORMAL.get(), new VampirismTrades.Price(1, 1), 6, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 40), ModItems.ARMOR_OF_SWIFTNESS_CHEST_ENHANCED.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 35), ModItems.ARMOR_OF_SWIFTNESS_LEGS_ENHANCED.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 15), ModItems.ARMOR_OF_SWIFTNESS_FEET_ENHANCED.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 30), ModItems.ARMOR_OF_SWIFTNESS_HEAD_ENHANCED.get(), new VampirismTrades.Price(1, 1), 6, 1)
                            },
                            3,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(40, 64), ModItems.VAMPIRE_BOOK.get(), new VampirismTrades.Price(1, 1), 10, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 40), ModItems.HUNTER_COAT_CHEST_ENHANCED.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 35), ModItems.HUNTER_COAT_LEGS_ENHANCED.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(10, 15), ModItems.HUNTER_COAT_FEET_ENHANCED.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 30), ModItems.HUNTER_COAT_HEAD_ENHANCED.get(), new VampirismTrades.Price(1, 1), 6, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 45), ModItems.ARMOR_OF_SWIFTNESS_CHEST_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 45), ModItems.ARMOR_OF_SWIFTNESS_LEGS_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(15, 30), ModItems.ARMOR_OF_SWIFTNESS_FEET_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 30), ModItems.ARMOR_OF_SWIFTNESS_HEAD_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 6, 1)
                            },
                            4,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 32), Items.DIAMOND, new VampirismTrades.Price(1, 2)),
                            },
                            5,
                            new VillagerTrades.ItemListing[]{
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(30, 55), ModItems.HUNTER_COAT_CHEST_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 8, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(25, 55), ModItems.HUNTER_COAT_LEGS_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 7, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 35), ModItems.HUNTER_COAT_FEET_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 5, 1),
                                    new VampirismTrades.ItemsForSouls(new VampirismTrades.Price(20, 35), ModItems.HUNTER_COAT_HEAD_ULTIMATE.get(), new VampirismTrades.Price(1, 1), 6, 1)
                            }
                    ))
            );
            map.put(
                    PRIEST.get(), new Int2ObjectOpenHashMap<>(ImmutableMap.of(
                            1,
                            new VillagerTrades.ItemListing[]{
                                    new VillagerTrades.EmeraldForItems(ModItems.PURE_SALT.get(), 25, 2, 4),
                                    new VillagerTrades.EmeraldForItems(ModBlocks.GARLIC.get(), 30, 6, 2),
                            },
                            2,
                            new VillagerTrades.ItemListing[]{
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HOLY_WATER_BOTTLE_NORMAL.get(), 3, 5, 4),
                                    new VillagerTrades.EmeraldForItems(ModItems.SOUL_ORB_VAMPIRE.get(), 10, 10, 4),
                                    new VillagerTrades.EmeraldForItems(ModItems.VAMPIRE_BLOOD_BOTTLE.get(), 9, 4, 5),
                                    new VillagerTrades.ItemsForEmeralds(ModItems.CRUCIFIX_NORMAL.get(), 1, 1, 1)
                            },
                            3,
                            new VillagerTrades.ItemListing[]{
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HOLY_WATER_BOTTLE_ENHANCED.get(), 2, 5, 4),
                            },
                            4,
                            new VillagerTrades.ItemListing[]{
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HOLY_WATER_BOTTLE_ULTIMATE.get(), 1, 4, 4),
                            },
                            5,
                            new VillagerTrades.ItemListing[]{
                                    new VillagerTrades.ItemsForEmeralds(ModItems.HOLY_WATER_BOTTLE_ENHANCED.get(), 3, 4, 4),
                            }
                    ))
            );
        });
    }

    public static Int2ObjectMap<VillagerTrades.ItemListing[]> getWanderingTraderTrades() {
        return new Int2ObjectOpenHashMap<>(ImmutableMap.of(
                1,
                new VillagerTrades.ItemListing[]{
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.VAMPIRE_ORCHID.asItem(), 4, 1, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.CURSED_ROOTS.asItem(), 2, 1, 4, 1),
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.GARLIC.asItem(), 1, 1, 12, 1),
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.DARK_SPRUCE_SAPLING.asItem(), 8, 1, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.CURSED_SPRUCE_SAPLING.asItem(), 8, 1, 6, 1),
                        new VillagerTrades.ItemsForEmeralds(ModBlocks.CURSED_EARTH.asItem(), 4, 4, 4, 1),
                }
        ));
    }
}
