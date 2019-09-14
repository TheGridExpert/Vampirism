package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.blocks.*;
import de.teamlapen.vampirism.util.REFERENCE;
import net.minecraft.block.Block;
import net.minecraft.block.FlowerPotBlock;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;

import javax.annotation.Nonnull;

import static de.teamlapen.lib.lib.util.UtilLib.getNull;

/**
 * Handles all block registrations and reference.
 */
@ObjectHolder(REFERENCE.MODID)
public class ModBlocks {
    public static final CastleBricksBlock castle_block_purple_brick = getNull();
    public static final CastleBricksBlock castle_block_dark_brick = getNull();
    public static final CastleBricksBlock castle_block_dark_brick_bloody = getNull();
    public static final CastleBricksBlock castle_block_dark_stone = getNull();
    public static final CastleBricksBlock castle_block_normal_brick = getNull();
    public static final CursedEarthBlock cursed_earth = getNull();
    public static final VampirismFlowerBlock vampirism_flower_vampire_orchid = new VampirismFlowerBlock(VampirismFlowerBlock.TYPE.ORCHID);
    public static final FlowerPotBlock potted_vampire_orchid = getNull();
    public static final TentBlock tent = getNull();
    public static final TentMainBlock tent_main = getNull();
    public static final CoffinBlock block_coffin = getNull();
    public static final AltarInfusionBlock altar_infusion = getNull();
    public static final AltarPillarBlock altar_pillar = getNull();
    public static final AltarTipBlock altar_tip = getNull();
    public static final HunterTableBlock hunter_table = getNull();
    public static final MedChairBlock med_chair = getNull();
    public static final GarlicBlock garlic = getNull();
    public static final ChurchAltarBlock church_altar = getNull();
    public static final BloodContainerBlock blood_container = getNull();
    public static final AltarInspirationBlock altar_inspiration = getNull();
    public static final FirePlaceBlock fire_place = getNull();
    public static final WeaponTableBlock weapon_table = getNull();
    public static final BloodPotionTableBlock blood_potion_table = getNull();
    public static final SunscreenBeaconBlock sunscreen_beacon = getNull();
    public static final AlchemicalFireBlock alchemical_fire = getNull();
    public static final AlchemicalCauldronBlock alchemical_cauldron = getNull();
    public static final GarlicBeaconBlock garlic_beacon_normal = getNull();
    public static final GarlicBeaconBlock garlic_beacon_weak = getNull();
    public static final GarlicBeaconBlock garlic_beacon_improved = getNull();

    public static final StairsBlock castle_stairs_dark_brick = getNull();
    public static final StairsBlock castle_stairs_dark_stone = getNull();
    public static final StairsBlock castle_stairs_purple_brick = getNull();
    public static final CastleSlabBlock castle_slab_dark_stone = getNull();
    public static final CastleSlabBlock castle_slab_dark_brick = getNull();
    public static final CastleSlabBlock castle_slab_purple_brick = getNull();
    public static final PedestalBlock blood_pedestal = getNull();
    public static final GrinderBlock blood_grinder = getNull();
    public static final SieveBlock blood_sieve = getNull();
    public static final TotemTopBlock totem_top = getNull();
    public static final VampirismBlock totem_base = getNull();


    private static @Nonnull
    BlockItem itemBlock(@Nonnull Block block, @Nonnull Item.Properties props) {
        assert block != null;
        BlockItem item = new BlockItem(block, props);
        item.setRegistryName(block.getRegistryName());
        return item;
    }

    static void registerItemBlocks(IForgeRegistry<Item> registry) {
        registry.register(itemBlock(castle_block_dark_brick, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_block_dark_brick_bloody, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_block_dark_stone, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_block_normal_brick, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_block_purple_brick, new Item.Properties().group(VampirismMod.creativeTab)));

        registry.register(itemBlock(blood_container, new Item.Properties().maxStackSize(1).group(VampirismMod.creativeTab)));
        registry.register(itemBlock(cursed_earth, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(vampirism_flower_vampire_orchid, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(altar_infusion, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(altar_pillar, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(altar_tip, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(hunter_table, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(church_altar, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(altar_inspiration, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(fire_place, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(weapon_table, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(blood_potion_table, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(sunscreen_beacon, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(alchemical_cauldron, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(garlic_beacon_normal, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(garlic_beacon_improved, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(garlic_beacon_weak, new Item.Properties().group(VampirismMod.creativeTab)));

        registry.register(itemBlock(castle_stairs_dark_brick, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_stairs_dark_stone, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_stairs_purple_brick, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(blood_pedestal, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(blood_grinder, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(blood_sieve, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(totem_base, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(totem_top, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_slab_dark_brick, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_slab_dark_stone, new Item.Properties().group(VampirismMod.creativeTab)));
        registry.register(itemBlock(castle_slab_purple_brick, new Item.Properties().group(VampirismMod.creativeTab)));
    }


    static void registerBlocks(IForgeRegistry<Block> registry) {
        CastleBricksBlock castleBlock_dark_brick = new CastleBricksBlock(CastleBricksBlock.EnumVariant.DARK_BRICK);
        registry.register(castleBlock_dark_brick);
        CastleBricksBlock castleBlock_dark_stone = new CastleBricksBlock(CastleBricksBlock.EnumVariant.DARK_STONE);
        registry.register(castleBlock_dark_stone);
        CastleBricksBlock castleBlock_purple_brick = new CastleBricksBlock(CastleBricksBlock.EnumVariant.PURPLE_BRICK);
        registry.register(castleBlock_purple_brick);
        registry.register(new CastleBricksBlock(CastleBricksBlock.EnumVariant.DARK_BRICK_BLOODY));
        registry.register(new CastleBricksBlock(CastleBricksBlock.EnumVariant.NORMAL_BRICK));
        registry.register(vampirism_flower_vampire_orchid);
        registry.register(new CursedEarthBlock());
        registry.register(new TentBlock());
        registry.register(new TentMainBlock());
        registry.register(new CoffinBlock());
        registry.register(new AltarInfusionBlock());
        registry.register(new AltarPillarBlock());
        registry.register(new AltarTipBlock());
        registry.register(new HunterTableBlock());
        registry.register(new MedChairBlock());
        registry.register(new GarlicBlock());
        registry.register(new ChurchAltarBlock());
        registry.register(new BloodContainerBlock());
        registry.register(new AltarInspirationBlock());
        registry.register(new FirePlaceBlock());
        registry.register(new WeaponTableBlock());
        registry.register(new BloodPotionTableBlock());
        registry.register(new SunscreenBeaconBlock());
        registry.register(new AlchemicalFireBlock());
        registry.register(new AlchemicalCauldronBlock());
        registry.register(new GarlicBeaconBlock(GarlicBeaconBlock.Type.NORMAL));
        registry.register(new GarlicBeaconBlock(GarlicBeaconBlock.Type.WEAK));
        registry.register(new GarlicBeaconBlock(GarlicBeaconBlock.Type.IMPROVED));
        registry.register(new FlowerPotBlock(vampirism_flower_vampire_orchid, Block.Properties.create(Material.MISCELLANEOUS).zeroHardnessAndResistance()).setRegistryName(REFERENCE.MODID, "potted_vampire_orchid"));


        registry.register(new CastleStairsBlock(castleBlock_dark_brick.getDefaultState(), "dark_brick"));
        registry.register(new CastleStairsBlock(castleBlock_dark_stone.getDefaultState(), "dark_stone"));
        registry.register(new CastleStairsBlock(castleBlock_purple_brick.getDefaultState(), "purple_brick"));
        registry.register(new CastleSlabBlock(CastleSlabBlock.EnumVariant.DARK_BRICK));
        registry.register(new CastleSlabBlock(CastleSlabBlock.EnumVariant.PURPLE_BRICK));
        registry.register(new CastleSlabBlock(CastleSlabBlock.EnumVariant.DARK_STONE));

        registry.register(new PedestalBlock());
        registry.register(new GrinderBlock());
        registry.register(new SieveBlock());
        registry.register(new TotemTopBlock());
        registry.register(new TotemBaseBlock());
    }
}
