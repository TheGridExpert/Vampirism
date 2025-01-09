package de.teamlapen.vampirism.core;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.ModRegistryItems;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.blocks.CursedRootsBlock;
import de.teamlapen.vampirism.blocks.*;
import de.teamlapen.vampirism.blocks.mother.ActiveVulnerableRemainsBlock;
import de.teamlapen.vampirism.blocks.mother.MotherBlock;
import de.teamlapen.vampirism.blocks.mother.RemainsBlock;
import de.teamlapen.vampirism.items.MotherTrophyItem;
import de.teamlapen.vampirism.util.BlockVoxelshapes;
import de.teamlapen.vampirism.world.gen.ModTreeGrower;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

import static de.teamlapen.lib.lib.util.RegisterHelper.flammable;
import static de.teamlapen.lib.lib.util.RegisterHelper.potted;

/**
 * Handles all block registrations and reference.
 */
public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(REFERENCE.MODID);

    public static final DeferredBlock<AlchemicalCauldronBlock> ALCHEMICAL_CAULDRON = registerWithItem("alchemical_cauldron",() -> new AlchemicalCauldronBlock(Block.Properties.of().mapColor(MapColor.METAL).strength(4f).lightLevel(litBlockEmission(13)).noOcclusion()));
    public static final DeferredBlock<AlchemicalFireBlock> ALCHEMICAL_FIRE = BLOCKS.register("alchemical_fire", AlchemicalFireBlock::new);
    public static final DeferredBlock<AltarInfusionBlock> ALTAR_INFUSION = registerWithItem("altar_infusion", () -> new AltarInfusionBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(5).noOcclusion()));
    public static final DeferredBlock<AltarInspirationBlock> ALTAR_INSPIRATION = registerWithItem("altar_inspiration", () -> new AltarInspirationBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2f, 3f).noOcclusion()));
    public static final DeferredBlock<AltarPillarBlock> ALTAR_PILLAR = registerWithItem("altar_pillar", AltarPillarBlock::new);
    public static final DeferredBlock<AltarTipBlock> ALTAR_TIP = registerWithItem("altar_tip", AltarTipBlock::new);
    public static final DeferredBlock<BloodContainerBlock> BLOOD_CONTAINER = registerWithItem("blood_container", () -> new BloodContainerBlock(BlockBehaviour.Properties.of().strength(1f).isViewBlocking(UtilLib::never).noOcclusion()), new Item.Properties().stacksTo(1));
    public static final DeferredBlock<GrinderBlock> BLOOD_GRINDER = registerWithItem("blood_grinder", () -> new GrinderBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<PedestalBlock> BLOOD_PEDESTAL = registerWithItem("blood_pedestal", () -> new PedestalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3f).noOcclusion()));
    public static final DeferredBlock<SieveBlock> BLOOD_SIEVE = registerWithItem("blood_sieve", () -> new SieveBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOD).ignitedByLava().strength(2.5f).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<AltarCleansingBlock> ALTAR_CLEANSING = registerWithItem("altar_cleansing", AltarCleansingBlock::new);
    public static final DeferredBlock<CursedEarthBlock> CURSED_EARTH = registerWithItem("cursed_earth", CursedEarthBlock::new);
    public static final DeferredBlock<FirePlaceBlock> FIRE_PLACE = registerWithItem("fire_place", FirePlaceBlock::new);
    public static final DeferredBlock<GarlicBlock> GARLIC = registerWithItem("garlic", GarlicBlock::new, (b) -> new GarlicBlock.GarlicItem(b, new Item.Properties()));
    public static final DeferredBlock<GarlicDiffuserBlock> GARLIC_DIFFUSER_IMPROVED = registerWithItem("garlic_diffuser_improved", () -> new GarlicDiffuserBlock(GarlicDiffuserBlock.Type.IMPROVED));
    public static final DeferredBlock<GarlicDiffuserBlock> GARLIC_DIFFUSER_NORMAL = registerWithItem("garlic_diffuser_normal", () -> new GarlicDiffuserBlock(GarlicDiffuserBlock.Type.NORMAL));
    public static final DeferredBlock<GarlicDiffuserBlock> GARLIC_DIFFUSER_WEAK = registerWithItem("garlic_diffuser_weak", () -> new GarlicDiffuserBlock(GarlicDiffuserBlock.Type.WEAK));
    public static final DeferredBlock<HunterTableBlock> HUNTER_TABLE = registerWithItem("hunter_table", HunterTableBlock::new);
    public static final DeferredBlock<MedChairBlock> MED_CHAIR = registerWithItem("med_chair", MedChairBlock::new);
    public static final DeferredBlock<SunscreenBeaconBlock> SUNSCREEN_BEACON = registerWithItem("sunscreen_beacon", () -> new SunscreenBeaconBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(-1, 3600000).noOcclusion()), new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<TentBlock> TENT = BLOCKS.register("tent", TentBlock::new);
    public static final DeferredBlock<TentMainBlock> TENT_MAIN = BLOCKS.register("tent_main", TentMainBlock::new);
    public static final DeferredBlock<TotemBaseBlock> TOTEM_BASE = registerWithItem("totem_base", TotemBaseBlock::new);
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP = registerWithItem("totem_top", () -> new TotemTopBlock(false, VResourceLocation.mc("none")));
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP_VAMPIRISM_VAMPIRE = BLOCKS.register("totem_top_vampirism_vampire", () -> new TotemTopBlock(false, VReference.VAMPIRE_FACTION_ID));
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP_VAMPIRISM_HUNTER = BLOCKS.register("totem_top_vampirism_hunter", () -> new TotemTopBlock(false, VReference.HUNTER_FACTION_ID));
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP_CRAFTED = registerWithItem("totem_top_crafted", () -> new TotemTopBlock(true, VResourceLocation.mc("none")));
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP_VAMPIRISM_VAMPIRE_CRAFTED = BLOCKS.register("totem_top_vampirism_vampire_crafted", () -> new TotemTopBlock(true, VReference.VAMPIRE_FACTION_ID));
    public static final DeferredBlock<TotemTopBlock> TOTEM_TOP_VAMPIRISM_HUNTER_CRAFTED = BLOCKS.register("totem_top_vampirism_hunter_crafted", () -> new TotemTopBlock(true, VReference.HUNTER_FACTION_ID));
    public static final DeferredBlock<FlowerBlock> VAMPIRE_ORCHID = registerWithItem("vampire_orchid", () -> new FlowerBlock(MobEffects.BLINDNESS, 7, BlockBehaviour.Properties.ofFullCopy(Blocks.POPPY).mapColor(MapColor.COLOR_MAGENTA)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_VAMPIRE_ORCHID = BLOCKS.register("potted_vampire_orchid", () -> potted(new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, VAMPIRE_ORCHID, Block.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).instabreak()), VAMPIRE_ORCHID.getId()));
    public static final DeferredBlock<WeaponTableBlock> WEAPON_TABLE = registerWithItem("weapon_table", WeaponTableBlock::new);
    public static final DeferredBlock<PotionTableBlock> POTION_TABLE = registerWithItem("potion_table", () -> new PotionTableBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(1f).noOcclusion()));
    public static final DeferredBlock<DarkSpruceLeavesBlock> DARK_SPRUCE_LEAVES = registerWithItem("dark_spruce_leaves", DarkSpruceLeavesBlock::new);
    public static final DeferredBlock<Block> CROSS = registerWithItem("cross", () -> new VampirismSplitBlock(BlockBehaviour.Properties.of().pushReaction(PushReaction.DESTROY).mapColor(MapColor.WOOD).ignitedByLava().strength(2, 3), BlockVoxelshapes.crossBottom, BlockVoxelshapes.crossTop, true));
    public static final DeferredBlock<VampirismHorizontalBlock> TOMBSTONE1 = registerWithItem("tombstone1", () -> new VampirismHorizontalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2, 6), BlockVoxelshapes.tomb1));
    public static final DeferredBlock<VampirismHorizontalBlock> TOMBSTONE2 = registerWithItem("tombstone2", () -> new VampirismHorizontalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2, 6), BlockVoxelshapes.tomb2));
    public static final DeferredBlock<VampirismSplitBlock> TOMBSTONE3 = registerWithItem("tombstone3", () -> new VampirismSplitBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).pushReaction(PushReaction.DESTROY).strength(2, 6), BlockVoxelshapes.tomb3_base, BlockVoxelshapes.tomb3_top, true));
    public static final DeferredBlock<VampirismHorizontalBlock> GRAVE_CAGE = registerWithItem("grave_cage", () -> new VampirismHorizontalBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(6, 8).requiresCorrectToolForDrops().sound(SoundType.METAL), BlockVoxelshapes.grave_cage));
    public static final DeferredBlock<CursedGrass> CURSED_GRASS = registerWithItem("cursed_grass", () -> new CursedGrass(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).randomTicks().strength(0.6F).sound(SoundType.GRASS)));
    public static final DeferredBlock<CursedRootsBlock> CURSED_ROOTS = registerWithItem("cursed_roots", () -> flammable(new CursedRootsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED).isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).ignitedByLava().replaceable().noCollission().instabreak().sound(SoundType.GRASS)),60, 100));
    public static final DeferredBlock<FlowerPotBlock> POTTED_CURSED_ROOTS = BLOCKS.register("potted_cursed_roots", () -> potted(new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, CURSED_ROOTS, Block.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).ignitedByLava().replaceable().instabreak().noOcclusion()), CURSED_ROOTS.getId()));
    public static final DeferredBlock<Block> DARK_SPRUCE_PLANKS = registerWithItem(ModRegistryItems.DARK_SPRUCE_PLANKS.getId().getPath(), () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).ignitedByLava().mapColor(MapColor.COLOR_GRAY).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> CURSED_SPRUCE_PLANKS = registerWithItem(ModRegistryItems.CURSED_SPRUCE_PLANKS.getId().getPath(), () -> new Block(BlockBehaviour.Properties.of().ignitedByLava().mapColor(MapColor.CRIMSON_HYPHAE).strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<LogBlock> STRIPPED_DARK_SPRUCE_LOG = registerWithItem("stripped_dark_spruce_log", () -> new LogBlock(MapColor.COLOR_BLACK, MapColor.COLOR_GRAY));
    public static final DeferredBlock<LogBlock> STRIPPED_CURSED_SPRUCE_LOG = registerWithItem("stripped_cursed_spruce_log", () -> new LogBlock(MapColor.COLOR_BLACK, MapColor.CRIMSON_HYPHAE));
    public static final DeferredBlock<LogBlock> DARK_SPRUCE_LOG = registerWithItem("dark_spruce_log", () -> new StrippableLogBlock(MapColor.COLOR_BLACK, MapColor.COLOR_BLACK, STRIPPED_DARK_SPRUCE_LOG));
    public static final DeferredBlock<CursedSpruceBlock> CURSED_SPRUCE_LOG_CURED = registerWithItem("cursed_spruce_log_cured", () -> new CursedSpruceBlock(STRIPPED_CURSED_SPRUCE_LOG));
    public static final DeferredBlock<LogBlock> CURSED_SPRUCE_LOG = registerWithItem("cursed_spruce_log", () -> new CursedSpruceBlock(STRIPPED_CURSED_SPRUCE_LOG, CURSED_SPRUCE_LOG_CURED));
    public static final DeferredBlock<DarkSpruceSaplingBlock> DARK_SPRUCE_SAPLING = registerWithItem("dark_spruce_sapling", () -> new DarkSpruceSaplingBlock(ModTreeGrower.DARK_SPRUCE, ModTreeGrower.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).isViewBlocking(UtilLib::never).replaceable().pushReaction(PushReaction.DESTROY).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final DeferredBlock<SaplingBlock> CURSED_SPRUCE_SAPLING = registerWithItem("cursed_spruce_sapling", () -> new SaplingBlock(ModTreeGrower.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).isViewBlocking(UtilLib::never).replaceable().pushReaction(PushReaction.DESTROY).noCollission().randomTicks().instabreak().sound(SoundType.GRASS)));
    public static final DeferredBlock<DirectCursedBarkBlock> DIRECT_CURSED_BARK = registerWithItem("direct_cursed_bark", DirectCursedBarkBlock::new);
    public static final DeferredBlock<DiagonalCursedBarkBlock> DIAGONAL_CURSED_BARK = BLOCKS.register("diagonal_cursed_bark", DiagonalCursedBarkBlock::new);
    public static final DeferredBlock<DoorBlock> DARK_SPRUCE_DOOR = registerWithItem("dark_spruce_door", () -> new DoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).ignitedByLava().strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<DoorBlock> CURSED_SPRUCE_DOOR = registerWithItem("cursed_spruce_door", () -> new DoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).ignitedByLava().strength(3.0F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<TrapDoorBlock> DARK_SPRUCE_TRAPDOOR = registerWithItem("dark_spruce_trapdoor", () -> new TrapDoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).ignitedByLava().strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn((p_61031_, p_61032_, p_61033_, p_61034_) -> false)));
    public static final DeferredBlock<TrapDoorBlock> CURSED_SPRUCE_TRAPDOOR = registerWithItem("cursed_spruce_trapdoor", () -> new TrapDoorBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).ignitedByLava().strength(3.0F).sound(SoundType.WOOD).noOcclusion().isValidSpawn((p_61031_, p_61032_, p_61033_, p_61034_) -> false)));
    public static final DeferredBlock<StairBlock> DARK_SPRUCE_STAIRS = registerWithItem("dark_spruce_stairs", () -> new StairBlock(DARK_SPRUCE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(DARK_SPRUCE_PLANKS.get())));
    public static final DeferredBlock<StairBlock> CURSED_SPRUCE_STAIRS = registerWithItem("cursed_spruce_stairs", () -> new StairBlock(CURSED_SPRUCE_PLANKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(CURSED_SPRUCE_PLANKS.get())));
    public static final DeferredBlock<LogBlock> STRIPPED_DARK_SPRUCE_WOOD = registerWithItem("stripped_dark_spruce_wood", () -> new LogBlock(MapColor.COLOR_BLACK, MapColor.COLOR_GRAY));
    public static final DeferredBlock<LogBlock> STRIPPED_CURSED_SPRUCE_WOOD = registerWithItem("stripped_cursed_spruce_wood", () -> new LogBlock(MapColor.COLOR_BLACK, MapColor.CRIMSON_HYPHAE));
    public static final DeferredBlock<LogBlock> DARK_SPRUCE_WOOD = registerWithItem("dark_spruce_wood", () -> new StrippableLogBlock(MapColor.COLOR_BLACK, MapColor.COLOR_BLACK, STRIPPED_DARK_SPRUCE_WOOD));
    public static final DeferredBlock<CursedSpruceBlock> CURSED_SPRUCE_WOOD_CURED = registerWithItem("cursed_spruce_wood_cured", () -> new CursedSpruceBlock(STRIPPED_CURSED_SPRUCE_LOG));
    public static final DeferredBlock<LogBlock> CURSED_SPRUCE_WOOD = registerWithItem("cursed_spruce_wood", () -> new CursedSpruceBlock(STRIPPED_CURSED_SPRUCE_WOOD, CURSED_SPRUCE_WOOD_CURED));
    public static final DeferredBlock<StandingSignBlock> DARK_SPRUCE_SIGN = BLOCKS.register("dark_spruce_sign", () -> new StandingSignBlock(LogBlock.DARK_SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<StandingSignBlock> CURSED_SPRUCE_SIGN = BLOCKS.register("cursed_spruce_sign", () -> new StandingSignBlock(LogBlock.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<WallSignBlock> DARK_SPRUCE_WALL_SIGN = BLOCKS.register("dark_spruce_wall_sign", () -> new WallSignBlock(LogBlock.DARK_SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(DARK_SPRUCE_SIGN)));
    public static final DeferredBlock<WallSignBlock> CURSED_SPRUCE_WALL_SIGN = BLOCKS.register("cursed_spruce_wall_sign", () -> new WallSignBlock(LogBlock.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(CURSED_SPRUCE_SIGN)));
    public static final DeferredBlock<PressurePlateBlock> DARK_SPRUCE_PRESSURE_PLACE = registerWithItem("dark_spruce_pressure_plate", () -> new PressurePlateBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().noCollission().strength(0.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<PressurePlateBlock> CURSED_SPRUCE_PRESSURE_PLACE = registerWithItem("cursed_spruce_pressure_plate", () -> new PressurePlateBlock(BlockSetType.SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().noCollission().strength(0.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<ButtonBlock> DARK_SPRUCE_BUTTON = registerWithItem("dark_spruce_button", () -> new ButtonBlock(BlockSetType.SPRUCE, 30, BlockBehaviour.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).ignitedByLava().replaceable().strength(0.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<ButtonBlock> CURSED_SPRUCE_BUTTON = registerWithItem("cursed_spruce_button", () -> new ButtonBlock(BlockSetType.SPRUCE, 30, BlockBehaviour.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).ignitedByLava().replaceable().strength(0.5F).sound(SoundType.WOOD)));
    public static final DeferredBlock<SlabBlock> DARK_SPRUCE_SLAB = registerWithItem("dark_spruce_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<SlabBlock> CURSED_SPRUCE_SLAB = registerWithItem("cursed_spruce_slab", () -> new SlabBlock(BlockBehaviour.Properties.of().mapColor(MapColor.CRIMSON_HYPHAE).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceGateBlock> DARK_SPRUCE_FENCE_GATE = registerWithItem("dark_spruce_fence_gate", () -> new FenceGateBlock(LogBlock.DARK_SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceGateBlock> CURSED_SPRUCE_FENCE_GATE = registerWithItem("cursed_spruce_fence_gate", () -> new FenceGateBlock(LogBlock.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceBlock> DARK_SPRUCE_FENCE = registerWithItem("dark_spruce_fence", () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<FenceBlock> CURSED_SPRUCE_FENCE = registerWithItem("cursed_spruce_fence", () -> new FenceBlock(BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_PLANKS.get().defaultMapColor()).ignitedByLava().strength(2.0F, 3.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<Block> VAMPIRE_RACK = registerWithItem("vampire_rack", () -> new VampirismHorizontalBlock(BlockBehaviour.Properties.of().ignitedByLava().strength(2, 3), BlockVoxelshapes.vampire_rack));
    public static final DeferredBlock<Block> THRONE = registerWithItem("throne", ThroneBlock::new);
    public static final DeferredBlock<CoffinBlock> COFFIN_WHITE = registerWithItem("coffin_white", () -> new CoffinBlock(DyeColor.WHITE));
    public static final DeferredBlock<CoffinBlock> COFFIN_ORANGE = registerWithItem("coffin_orange", () -> new CoffinBlock(DyeColor.ORANGE));
    public static final DeferredBlock<CoffinBlock> COFFIN_MAGENTA = registerWithItem("coffin_magenta", () -> new CoffinBlock(DyeColor.MAGENTA));
    public static final DeferredBlock<CoffinBlock> COFFIN_LIGHT_BLUE = registerWithItem("coffin_light_blue", () -> new CoffinBlock(DyeColor.LIGHT_BLUE));
    public static final DeferredBlock<CoffinBlock> COFFIN_YELLOW = registerWithItem("coffin_yellow", () -> new CoffinBlock(DyeColor.YELLOW));
    public static final DeferredBlock<CoffinBlock> COFFIN_LIME = registerWithItem("coffin_lime", () -> new CoffinBlock(DyeColor.LIME));
    public static final DeferredBlock<CoffinBlock> COFFIN_PINK = registerWithItem("coffin_pink", () -> new CoffinBlock(DyeColor.PINK));
    public static final DeferredBlock<CoffinBlock> COFFIN_GRAY = registerWithItem("coffin_gray", () -> new CoffinBlock(DyeColor.GRAY));
    public static final DeferredBlock<CoffinBlock> COFFIN_LIGHT_GRAY = registerWithItem("coffin_light_gray", () -> new CoffinBlock(DyeColor.LIGHT_GRAY));
    public static final DeferredBlock<CoffinBlock> COFFIN_CYAN = registerWithItem("coffin_cyan", () -> new CoffinBlock(DyeColor.CYAN));
    public static final DeferredBlock<CoffinBlock> COFFIN_PURPLE = registerWithItem("coffin_purple", () -> new CoffinBlock(DyeColor.PURPLE));
    public static final DeferredBlock<CoffinBlock> COFFIN_BLUE = registerWithItem("coffin_blue", () -> new CoffinBlock(DyeColor.BLUE));
    public static final DeferredBlock<CoffinBlock> COFFIN_BROWN = registerWithItem("coffin_brown", () -> new CoffinBlock(DyeColor.BROWN));
    public static final DeferredBlock<CoffinBlock> COFFIN_GREEN = registerWithItem("coffin_green", () -> new CoffinBlock(DyeColor.GREEN));
    public static final DeferredBlock<CoffinBlock> COFFIN_RED = registerWithItem("coffin_red", () -> new CoffinBlock(DyeColor.RED));
    public static final DeferredBlock<CoffinBlock> COFFIN_BLACK = registerWithItem("coffin_black", () -> new CoffinBlock(DyeColor.BLACK));
    public static final DeferredBlock<AlchemyTableBlock> ALCHEMY_TABLE = registerWithItem("alchemy_table", AlchemyTableBlock::new);
    public static final DeferredBlock<CeilingHangingSignBlock> DARK_SPRUCE_HANGING_SIGN = BLOCKS.register("dark_spruce_hanging_sign", () -> new CeilingHangingSignBlock(LogBlock.DARK_SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<CeilingHangingSignBlock> CURSED_SPRUCE_HANGING_SIGN = BLOCKS.register("cursed_spruce_hanging_sign", () -> new CeilingHangingSignBlock(LogBlock.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<WallHangingSignBlock> DARK_SPRUCE_WALL_HANGING_SIGN = BLOCKS.register("dark_spruce_wall_hanging_sign", () -> new WallHangingSignBlock(LogBlock.DARK_SPRUCE, BlockBehaviour.Properties.of().mapColor(DARK_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(DARK_SPRUCE_HANGING_SIGN)));
    public static final DeferredBlock<WallHangingSignBlock> CURSED_SPRUCE_WALL_HANGING_SIGN = BLOCKS.register("cursed_spruce_wall_hanging_sign", () -> new WallHangingSignBlock(LogBlock.CURSED_SPRUCE, BlockBehaviour.Properties.of().mapColor(CURSED_SPRUCE_LOG.get().defaultMapColor()).ignitedByLava().noCollission().strength(1.0F).sound(SoundType.WOOD).lootFrom(CURSED_SPRUCE_HANGING_SIGN)));
    public static final DeferredBlock<CursedEarthPathBlock> CURSED_EARTH_PATH = registerWithItem("cursed_earth_path", () -> new CursedEarthPathBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIRT).strength(0.65F).sound(SoundType.GRASS).isViewBlocking(UtilLib::always).isSuffocating(UtilLib::always)));
    public static final DeferredBlock<DarkStoneBlock> DARK_STONE = registerWithItem("dark_stone", () -> new DarkStoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2f, 10f).sound(SoundType.STONE)));
    public static final DeferredBlock<DarkStoneStairsBlock> DARK_STONE_STAIRS = registerWithItem("dark_stone_stairs", () -> new DarkStoneStairsBlock(DARK_STONE, BlockBehaviour.Properties.ofFullCopy(DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneSlabBlock> DARK_STONE_SLAB = registerWithItem("dark_stone_slab", () -> new DarkStoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE.get())));
    public static final DeferredBlock<WallBlock> DARK_STONE_WALL = registerWithItem("dark_stone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE.get()).forceSolidOn()));
    public static final DeferredBlock<DarkStoneBlock> DARK_STONE_BRICKS = registerWithItem("dark_stone_bricks", () -> new DarkStoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).requiresCorrectToolForDrops().strength(2f, 10f).sound(SoundType.STONE)));
    public static final DeferredBlock<DarkStoneStairsBlock> DARK_STONE_BRICK_STAIRS = registerWithItem("dark_stone_brick_stairs", () -> new DarkStoneStairsBlock(DARK_STONE_BRICKS, BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get())));
    public static final DeferredBlock<DarkStoneSlabBlock> DARK_STONE_BRICK_SLAB = registerWithItem("dark_stone_brick_slab", () -> new DarkStoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get())));
    public static final DeferredBlock<WallBlock> DARK_STONE_BRICK_WALL = registerWithItem("dark_stone_brick_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get()).forceSolidOn()));
    public static final DeferredBlock<Block> CRACKED_DARK_STONE_BRICKS = registerWithItem("cracked_dark_stone_bricks", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get())));
    public static final DeferredBlock<DarkStoneBlock> COBBLED_DARK_STONE = registerWithItem("cobbled_dark_stone", () -> new DarkStoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DEEPSLATE).strength(2.5f, 10f).requiresCorrectToolForDrops().sound(SoundType.STONE)));
    public static final DeferredBlock<DarkStoneStairsBlock> COBBLED_DARK_STONE_STAIRS = registerWithItem("cobbled_dark_stone_stairs", () -> new DarkStoneStairsBlock(ModBlocks.COBBLED_DARK_STONE, BlockBehaviour.Properties.ofFullCopy(COBBLED_DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneSlabBlock> COBBLED_DARK_STONE_SLAB = registerWithItem("cobbled_dark_stone_slab", () -> new DarkStoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_DARK_STONE.get())));
    public static final DeferredBlock<WallBlock> COBBLED_DARK_STONE_WALL = registerWithItem("cobbled_dark_stone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_DARK_STONE.get()).forceSolidOn()));
    public static final DeferredBlock<DarkStoneBlock> POLISHED_DARK_STONE = registerWithItem("polished_dark_stone", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneStairsBlock> POLISHED_DARK_STONE_STAIRS = registerWithItem("polished_dark_stone_stairs", () -> new DarkStoneStairsBlock(POLISHED_DARK_STONE, BlockBehaviour.Properties.ofFullCopy(POLISHED_DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneSlabBlock> POLISHED_DARK_STONE_SLAB = registerWithItem("polished_dark_stone_slab", () -> new DarkStoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_DARK_STONE.get())));
    public static final DeferredBlock<WallBlock> POLISHED_DARK_STONE_WALL = registerWithItem("polished_dark_stone_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(POLISHED_DARK_STONE.get()).forceSolidOn()));
    public static final DeferredBlock<DarkStoneBlock> DARK_STONE_TILES = registerWithItem("dark_stone_tiles", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(COBBLED_DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneBlock> CRACKED_DARK_STONE_TILES = registerWithItem("cracked_dark_stone_tiles", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_TILES.get())));
    public static final DeferredBlock<DarkStoneStairsBlock> DARK_STONE_TILES_STAIRS = registerWithItem("dark_stone_tiles_stairs", () -> new DarkStoneStairsBlock(ModBlocks.DARK_STONE_TILES, BlockBehaviour.Properties.ofFullCopy(DARK_STONE_TILES.get())));
    public static final DeferredBlock<DarkStoneSlabBlock> DARK_STONE_TILES_SLAB = registerWithItem("dark_stone_tiles_slab", () -> new DarkStoneSlabBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_TILES.get())));
    public static final DeferredBlock<WallBlock> DARK_STONE_TILES_WALL = registerWithItem("dark_stone_tiles_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_TILES.get()).forceSolidOn()));
    public static final DeferredBlock<DarkStoneBlock> CHISELED_DARK_STONE_BRICKS = registerWithItem("chiseled_dark_stone_bricks", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get())));
    public static final DeferredBlock<DarkStoneBlock> INFESTED_DARK_STONE = registerWithItem("infested_dark_stone", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE.get())));
    public static final DeferredBlock<DarkStoneBlock> BLOODY_DARK_STONE_BRICKS = registerWithItem("bloody_dark_stone_bricks", () -> new DarkStoneBlock(BlockBehaviour.Properties.ofFullCopy(DARK_STONE_BRICKS.get())));
    public static final DeferredBlock<Block> BAT_CAGE = registerWithItem("bat_cage", () -> new BatCageBlock(BlockBehaviour.Properties.of().strength(5.0F, 6.0F).sound(SoundType.METAL).noOcclusion()));
    public static final DeferredBlock<RemainsBlock> REMAINS = BLOCKS.register("remains", () -> new RemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(-1, 3600000.0F).sound(SoundType.ROOTED_DIRT).randomTicks().noLootTable(), false, false));
    public static final DeferredBlock<RemainsBlock> VULNERABLE_REMAINS = BLOCKS.register("vulnerable_remains", () -> new RemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(-1, 3600000.0F).sound(SoundType.ROOTED_DIRT).randomTicks().noLootTable(), true, true));
    public static final DeferredBlock<RemainsBlock> INCAPACITATED_VULNERABLE_REMAINS = BLOCKS.register("incapacitated_vulnerable_remains", () -> new RemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(-1.0F, 3600000.0F).sound(SoundType.ROOTED_DIRT).randomTicks().noLootTable(), false, true));
    public static final DeferredBlock<ActiveVulnerableRemainsBlock> ACTIVE_VULNERABLE_REMAINS = BLOCKS.register("active_vulnerable_remains", () -> new ActiveVulnerableRemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).strength(-1, 3600000.0F).randomTicks().sound(SoundType.ROOTED_DIRT).noLootTable()));
    public static final DeferredBlock<HangingRootsBlock> CURSED_HANGING_ROOTS = registerWithItem("cursed_hanging_roots", () -> {
        var block = new HangingRootsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.TERRACOTTA_BROWN).noCollission().instabreak().sound(SoundType.GRASS).offsetType(BlockBehaviour.OffsetType.XZ).ignitedByLava().pushReaction(PushReaction.DESTROY));
        ((FireBlock) Blocks.FIRE).setFlammable(block, 30, 60);
        return block;
    });
    public static final DeferredBlock<MotherBlock> MOTHER = BLOCKS.register("mother", MotherBlock::new);
    public static final DeferredBlock<Block> MOTHER_TROPHY = registerWithItem("mother_trophy", () -> new MotherTrophyBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GRAY).strength(3, 9).lightLevel(s -> 1).noOcclusion()), block -> new MotherTrophyItem(block, new Item.Properties().rarity(Rarity.EPIC).stacksTo(1)));
    public static final DeferredBlock<FogDiffuserBlock> FOG_DIFFUSER = registerWithItem("fog_diffuser", () -> new FogDiffuserBlock(BlockBehaviour.Properties.of().noOcclusion().mapColor(MapColor.STONE).strength(40.0F, 1200.0F).sound(SoundType.STONE)));
    public static final DeferredBlock<FlowerPotBlock> POTTED_DARK_SPRUCE_SAPLING = BLOCKS.register("potted_dark_spruce_sapling", () -> potted(new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, DARK_SPRUCE_SAPLING, Block.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).instabreak()), DARK_SPRUCE_SAPLING.getId()));
    public static final DeferredBlock<FlowerPotBlock> POTTED_CURSED_SPRUCE_SAPLING = BLOCKS.register("potted_cursed_spruce_sapling", () -> potted(new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, CURSED_SPRUCE_SAPLING, Block.Properties.of().noCollission().isViewBlocking(UtilLib::never).pushReaction(PushReaction.DESTROY).instabreak()), CURSED_SPRUCE_SAPLING.getId()));
    public static final DeferredBlock<Block> BLOOD_INFUSED_IRON_BLOCK = registerWithItem("blood_infused_iron_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(6.0F, 7.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<Block> BLOOD_INFUSED_ENHANCED_IRON_BLOCK = registerWithItem("blood_infused_enhanced_iron_block", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).requiresCorrectToolForDrops().strength(6.5F, 8.0F).sound(SoundType.METAL)));
    public static final DeferredBlock<VampireBeaconBlock> VAMPIRE_BEACON = registerWithItem("vampire_beacon", () -> new VampireBeaconBlock(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).instrument(NoteBlockInstrument.HAT).strength(3.0F).lightLevel((p_50828_) -> 15).noOcclusion().isRedstoneConductor(UtilLib::never)), new Item.Properties().rarity(Rarity.RARE));
    public static final DeferredBlock<Block> PURPLE_STONE_BRICKS = registerWithItem("purple_stone_bricks", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(2f, 10f).sound(SoundType.STONE)));
    public static final DeferredBlock<StairBlock> PURPLE_STONE_BRICK_STAIRS = registerWithItem("purple_stone_brick_stairs", () -> new StairBlock(PURPLE_STONE_BRICKS.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_BRICKS.get())));
    public static final DeferredBlock<SlabBlock> PURPLE_STONE_BRICK_SLAB = registerWithItem("purple_stone_brick_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_BRICKS.get())));
    public static final DeferredBlock<WallBlock> PURPLE_STONE_BRICK_WALL = registerWithItem("purple_stone_brick_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_BRICKS.get()).forceSolidOn()));
    public static final DeferredBlock<Block> PURPLE_STONE_TILES = registerWithItem("purple_stone_tiles", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).requiresCorrectToolForDrops().strength(2f, 10f).sound(SoundType.STONE)));
    public static final DeferredBlock<StairBlock> PURPLE_STONE_TILES_STAIRS = registerWithItem("purple_stone_tiles_stairs", () -> new StairBlock(PURPLE_STONE_TILES.get().defaultBlockState(), BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_TILES.get())));
    public static final DeferredBlock<SlabBlock> PURPLE_STONE_TILES_SLAB = registerWithItem("purple_stone_tiles_slab", () -> new SlabBlock(BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_TILES.get())));
    public static final DeferredBlock<WallBlock> PURPLE_STONE_TILES_WALL = registerWithItem("purple_stone_tiles_wall", () -> new WallBlock(BlockBehaviour.Properties.ofFullCopy(PURPLE_STONE_TILES.get()).forceSolidOn()));

    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK = BLOCKS.register("candle_stick", () -> new StandingCandleStickBlock(null, () -> null, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(2f, 3f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK = BLOCKS.register("wall_candle_stick", () -> new WallCandleStickBlock(null, () -> null, BlockBehaviour.Properties.ofFullCopy(CANDLE_STICK.get()).lootFrom(CANDLE_STICK)));

    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_NORMAL = BLOCKS.register("candle_stick_normal", () -> candleStickBlock(Items.CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_NORMAL = BLOCKS.register("wall_candle_stick_normal", () -> wallCandleStickBlock(Items.CANDLE, CANDLE_STICK_NORMAL));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_WHITE = BLOCKS.register("candle_stick_white", () -> candleStickBlock(Items.WHITE_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_WHITE = BLOCKS.register("wall_candle_stick_white", () -> wallCandleStickBlock(Items.WHITE_CANDLE, CANDLE_STICK_WHITE));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_ORANGE = BLOCKS.register("candle_stick_orange", () -> candleStickBlock(Items.ORANGE_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_ORANGE = BLOCKS.register("wall_candle_stick_orange", () -> wallCandleStickBlock(Items.ORANGE_CANDLE, CANDLE_STICK_ORANGE));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_MAGENTA = BLOCKS.register("candle_stick_magenta", () -> candleStickBlock(Items.MAGENTA_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_MAGENTA = BLOCKS.register("wall_candle_stick_magenta", () -> wallCandleStickBlock(Items.MAGENTA_CANDLE, CANDLE_STICK_MAGENTA));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_LIGHT_BLUE = BLOCKS.register("candle_stick_light_blue", () -> candleStickBlock(Items.LIGHT_BLUE_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_LIGHT_BLUE = BLOCKS.register("wall_candle_stick_light_blue", () -> wallCandleStickBlock(Items.LIGHT_BLUE_CANDLE, CANDLE_STICK_LIGHT_BLUE));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_YELLOW = BLOCKS.register("candle_stick_yellow", () -> candleStickBlock(Items.YELLOW_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_YELLOW = BLOCKS.register("wall_candle_stick_yellow", () -> wallCandleStickBlock(Items.YELLOW_CANDLE, CANDLE_STICK_YELLOW));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_LIME = BLOCKS.register("candle_stick_lime", () -> candleStickBlock(Items.LIME_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_LIME = BLOCKS.register("wall_candle_stick_lime", () -> wallCandleStickBlock(Items.LIME_CANDLE, CANDLE_STICK_LIME));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_PINK = BLOCKS.register("candle_stick_pink", () -> candleStickBlock(Items.PINK_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_PINK = BLOCKS.register("wall_candle_stick_pink", () -> wallCandleStickBlock(Items.PINK_CANDLE, CANDLE_STICK_PINK));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_GRAY = BLOCKS.register("candle_stick_gray", () -> candleStickBlock(Items.GRAY_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_GRAY = BLOCKS.register("wall_candle_stick_gray", () -> wallCandleStickBlock(Items.GRAY_CANDLE, CANDLE_STICK_GRAY));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_LIGHT_GRAY = BLOCKS.register("candle_stick_light_gray", () -> candleStickBlock(Items.LIGHT_GRAY_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_LIGHT_GRAY = BLOCKS.register("wall_candle_stick_light_gray", () -> wallCandleStickBlock(Items.LIGHT_GRAY_CANDLE, CANDLE_STICK_LIGHT_GRAY));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_CYAN = BLOCKS.register("candle_stick_cyan", () -> candleStickBlock(Items.CYAN_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_CYAN = BLOCKS.register("wall_candle_stick_cyan", () -> wallCandleStickBlock(Items.CYAN_CANDLE, CANDLE_STICK_CYAN));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_PURPLE = BLOCKS.register("candle_stick_purple", () -> candleStickBlock(Items.PURPLE_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_PURPLE = BLOCKS.register("wall_candle_stick_purple", () -> wallCandleStickBlock(Items.PURPLE_CANDLE, CANDLE_STICK_PURPLE));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_BLUE = BLOCKS.register("candle_stick_blue", () -> candleStickBlock(Items.BLUE_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_BLUE = BLOCKS.register("wall_candle_stick_blue", () -> wallCandleStickBlock(Items.BLUE_CANDLE, CANDLE_STICK_BLUE));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_BROWN = BLOCKS.register("candle_stick_brown", () -> candleStickBlock(Items.BROWN_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_BROWN = BLOCKS.register("wall_candle_stick_brown", () -> wallCandleStickBlock(Items.BROWN_CANDLE, CANDLE_STICK_BROWN));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_GREEN = BLOCKS.register("candle_stick_green", () -> candleStickBlock(Items.GREEN_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_GREEN = BLOCKS.register("wall_candle_stick_green", () -> wallCandleStickBlock(Items.GREEN_CANDLE, CANDLE_STICK_GREEN));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_RED = BLOCKS.register("candle_stick_red", () -> candleStickBlock(Items.RED_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_RED = BLOCKS.register("wall_candle_stick_red", () -> wallCandleStickBlock(Items.RED_CANDLE, CANDLE_STICK_RED));
    public static final DeferredBlock<StandingCandleStickBlock> CANDLE_STICK_BLACK = BLOCKS.register("candle_stick_black", () -> candleStickBlock(Items.BLACK_CANDLE));
    public static final DeferredBlock<WallCandleStickBlock> WALL_CANDLE_STICK_BLACK = BLOCKS.register("wall_candle_stick_black", () -> wallCandleStickBlock(Items.BLACK_CANDLE, CANDLE_STICK_BLACK));

    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA = BLOCKS.register("candelabra", () -> new StandingCandelabraBlock(null, () -> null, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(4f, 5f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA = BLOCKS.register("wall_candelabra", () -> new WallCandelabraBlock(null, () -> null, BlockBehaviour.Properties.ofFullCopy(CANDELABRA.get()).lootFrom(CANDELABRA)));

    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_NORMAL = BLOCKS.register("candelabra_normal", () -> candelabraBlock(Items.CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_NORMAL = BLOCKS.register("wall_candelabra_normal", () -> wallCandelabraBlock(Items.CANDLE, CANDELABRA_NORMAL));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_WHITE = BLOCKS.register("candelabra_white", () -> candelabraBlock(Items.WHITE_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_WHITE = BLOCKS.register("wall_candelabra_white", () -> wallCandelabraBlock(Items.WHITE_CANDLE, CANDELABRA_WHITE));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_ORANGE = BLOCKS.register("candelabra_orange", () -> candelabraBlock(Items.ORANGE_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_ORANGE = BLOCKS.register("wall_candelabra_orange", () -> wallCandelabraBlock(Items.ORANGE_CANDLE, CANDELABRA_ORANGE));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_MAGENTA = BLOCKS.register("candelabra_magenta", () -> candelabraBlock(Items.MAGENTA_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_MAGENTA = BLOCKS.register("wall_candelabra_magenta", () -> wallCandelabraBlock(Items.MAGENTA_CANDLE, CANDELABRA_MAGENTA));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_LIGHT_BLUE = BLOCKS.register("candelabra_light_blue", () -> candelabraBlock(Items.LIGHT_BLUE_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_LIGHT_BLUE = BLOCKS.register("wall_candelabra_light_blue", () -> wallCandelabraBlock(Items.LIGHT_BLUE_CANDLE, CANDELABRA_LIGHT_BLUE));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_YELLOW = BLOCKS.register("candelabra_yellow", () -> candelabraBlock(Items.YELLOW_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_YELLOW = BLOCKS.register("wall_candelabra_yellow", () -> wallCandelabraBlock(Items.YELLOW_CANDLE, CANDELABRA_YELLOW));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_LIME = BLOCKS.register("candelabra_lime", () -> candelabraBlock(Items.LIME_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_LIME = BLOCKS.register("wall_candelabra_lime", () -> wallCandelabraBlock(Items.LIME_CANDLE, CANDELABRA_LIME));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_PINK = BLOCKS.register("candelabra_pink", () -> candelabraBlock(Items.PINK_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_PINK = BLOCKS.register("wall_candelabra_pink", () -> wallCandelabraBlock(Items.PINK_CANDLE, CANDELABRA_PINK));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_GRAY = BLOCKS.register("candelabra_gray", () -> candelabraBlock(Items.GRAY_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_GRAY = BLOCKS.register("wall_candelabra_gray", () -> wallCandelabraBlock(Items.GRAY_CANDLE, CANDELABRA_GRAY));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_LIGHT_GRAY = BLOCKS.register("candelabra_light_gray", () -> candelabraBlock(Items.LIGHT_GRAY_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_LIGHT_GRAY = BLOCKS.register("wall_candelabra_light_gray", () -> wallCandelabraBlock(Items.LIGHT_GRAY_CANDLE, CANDELABRA_LIGHT_GRAY));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_CYAN = BLOCKS.register("candelabra_cyan", () -> candelabraBlock(Items.CYAN_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_CYAN = BLOCKS.register("wall_candelabra_cyan", () -> wallCandelabraBlock(Items.CYAN_CANDLE, CANDELABRA_CYAN));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_PURPLE = BLOCKS.register("candelabra_purple", () -> candelabraBlock(Items.PURPLE_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_PURPLE = BLOCKS.register("wall_candelabra_purple", () -> wallCandelabraBlock(Items.PURPLE_CANDLE, CANDELABRA_PURPLE));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_BLUE = BLOCKS.register("candelabra_blue", () -> candelabraBlock(Items.BLUE_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_BLUE = BLOCKS.register("wall_candelabra_blue", () -> wallCandelabraBlock(Items.BLUE_CANDLE, CANDELABRA_BLUE));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_BROWN = BLOCKS.register("candelabra_brown", () -> candelabraBlock(Items.BROWN_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_BROWN = BLOCKS.register("wall_candelabra_brown", () -> wallCandelabraBlock(Items.BROWN_CANDLE, CANDELABRA_BROWN));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_GREEN = BLOCKS.register("candelabra_green", () -> candelabraBlock(Items.GREEN_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_GREEN = BLOCKS.register("wall_candelabra_green", () -> wallCandelabraBlock(Items.GREEN_CANDLE, CANDELABRA_GREEN));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_RED = BLOCKS.register("candelabra_red", () -> candelabraBlock(Items.RED_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_RED = BLOCKS.register("wall_candelabra_red", () -> wallCandelabraBlock(Items.RED_CANDLE, CANDELABRA_RED));
    public static final DeferredBlock<StandingCandelabraBlock> CANDELABRA_BLACK = BLOCKS.register("candelabra_black", () -> candelabraBlock(Items.BLACK_CANDLE));
    public static final DeferredBlock<WallCandelabraBlock> WALL_CANDELABRA_BLACK = BLOCKS.register("wall_candelabra_black", () -> wallCandelabraBlock(Items.BLACK_CANDLE, CANDELABRA_BLACK));

    public static final DeferredBlock<ChandelierBlock> CHANDELIER = BLOCKS.register("chandelier", () -> new ChandelierBlock(null, () -> null, BlockBehaviour.Properties.of().mapColor(MapColor.METAL).noOcclusion().strength(4f, 5f).sound(SoundType.METAL).pushReaction(PushReaction.DESTROY)));

    public static final DeferredBlock<ChandelierBlock> CHANDELIER_NORMAL = BLOCKS.register("chandelier_normal", () -> chandelierBlock(Items.CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_WHITE = BLOCKS.register("chandelier_white", () -> chandelierBlock(Items.WHITE_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_ORANGE = BLOCKS.register("chandelier_orange", () -> chandelierBlock(Items.ORANGE_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_MAGENTA = BLOCKS.register("chandelier_magenta", () -> chandelierBlock(Items.MAGENTA_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_LIGHT_BLUE = BLOCKS.register("chandelier_light_blue", () -> chandelierBlock(Items.LIGHT_BLUE_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_YELLOW = BLOCKS.register("chandelier_yellow", () -> chandelierBlock(Items.YELLOW_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_LIME = BLOCKS.register("chandelier_lime", () -> chandelierBlock(Items.LIME_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_PINK = BLOCKS.register("chandelier_pink", () -> chandelierBlock(Items.PINK_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_GRAY = BLOCKS.register("chandelier_gray", () -> chandelierBlock(Items.GRAY_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_LIGHT_GRAY = BLOCKS.register("chandelier_light_gray", () -> chandelierBlock(Items.LIGHT_GRAY_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_CYAN = BLOCKS.register("chandelier_cyan", () -> chandelierBlock(Items.CYAN_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_PURPLE = BLOCKS.register("chandelier_purple", () -> chandelierBlock(Items.PURPLE_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_BLUE = BLOCKS.register("chandelier_blue", () -> chandelierBlock(Items.BLUE_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_BROWN = BLOCKS.register("chandelier_brown", () -> chandelierBlock(Items.BROWN_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_GREEN = BLOCKS.register("chandelier_green", () -> chandelierBlock(Items.GREEN_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_RED = BLOCKS.register("chandelier_red", () -> chandelierBlock(Items.RED_CANDLE));
    public static final DeferredBlock<ChandelierBlock> CHANDELIER_BLACK = BLOCKS.register("chandelier_black", () -> chandelierBlock(Items.BLACK_CANDLE));

    /**
     * TUTORIAL:
     * - Register blocks here.
     * - To register itemblock, use {@link ModBlocks#registerWithItem}
     * - Register blockstate in {@link de.teamlapen.vampirism.data.provider.BlockStateProvider#registerStatesAndModels()} (pass existent model if desired)
     * - Maybe set render layer in the json model or blockstate generator.
     * - Register itemrender in {@link de.teamlapen.vampirism.data.provider.ItemModelGenerator#registerModels()}
     * - Register loot table in {@link de.teamlapen.vampirism.data.provider.LootTablesProvider.ModBlockLootTables#addTables()}
     * - Add lang keys
     * - Consider adding tool type in {@link de.teamlapen.vampirism.data.provider.TagProvider.ModBlockTagsProvider}
     * - Run genData (twice?)
     */
    @SuppressWarnings("JavadocReference")
    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> supplier, Item.@NotNull Properties properties) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        ModItems.register(name, () -> new BlockItem(block.get(), properties));
        return block;
    }

    private static <T extends Block, R extends Item> DeferredBlock<T> registerWithItem(String name, Supplier<T> supplier, @NotNull Function<T, R> itemMaker) {
        DeferredBlock<T> block = BLOCKS.register(name, supplier);
        ModItems.register(name, () -> itemMaker.apply(block.get()));
        return block;
    }

    private static <T extends Block> DeferredBlock<T> registerWithItem(String name, Supplier<T> supplier) {
        return registerWithItem(name, supplier, new Item.Properties());
    }

    public static @NotNull Set<Block> getAllBlocks() {
        return BLOCKS.getEntries().stream().map(DeferredHolder::get).collect(Collectors.toUnmodifiableSet());
    }

    private static StandingCandleStickBlock candleStickBlock(Item candle) {
        StandingCandleStickBlock block = new StandingCandleStickBlock(CANDLE_STICK, () -> candle, BlockBehaviour.Properties.ofFullCopy(CANDLE_STICK.get()));
        CANDLE_STICK.get().addCandle(BuiltInRegistries.ITEM.getKey(candle), () -> block);
        return block;
    }

    private static WallCandleStickBlock wallCandleStickBlock(Item candle, Supplier<StandingCandleStickBlock> standingCandleStickBlock) {
        WallCandleStickBlock block = new WallCandleStickBlock(WALL_CANDLE_STICK, () -> candle, BlockBehaviour.Properties.ofFullCopy(WALL_CANDLE_STICK.get()).lootFrom(standingCandleStickBlock));
        WALL_CANDLE_STICK.get().addCandle(BuiltInRegistries.ITEM.getKey(candle), () -> block);
        return block;
    }

    private static StandingCandelabraBlock candelabraBlock(Item candle) {
        StandingCandelabraBlock block = new StandingCandelabraBlock(CANDELABRA.get(), candle, BlockBehaviour.Properties.ofFullCopy(CANDELABRA.get()));
        CANDELABRA.get().addCandle(BuiltInRegistries.ITEM.getKey(candle), () -> block);
        return block;
    }

    private static WallCandelabraBlock wallCandelabraBlock(Item candle, Supplier<StandingCandelabraBlock> standingCandelabraBlock) {
        WallCandelabraBlock block = new WallCandelabraBlock(WALL_CANDELABRA.get(), candle, BlockBehaviour.Properties.ofFullCopy(WALL_CANDELABRA.get()).lootFrom(standingCandelabraBlock));
        WALL_CANDELABRA.get().addCandle(BuiltInRegistries.ITEM.getKey(candle), () -> block);
        return block;
    }

    private static ChandelierBlock chandelierBlock(Item candle) {
        ChandelierBlock block = new ChandelierBlock(CHANDELIER.get(), candle, BlockBehaviour.Properties.ofFullCopy(CHANDELIER.get()));
        CHANDELIER.get().addCandle(BuiltInRegistries.ITEM.getKey(candle), () -> block);
        return block;
    }

    private static ToIntFunction<BlockState> litBlockEmission(int lightValue) {
        return state -> state.getValue(BlockStateProperties.LIT) ? lightValue : 0;
    }

    static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}
