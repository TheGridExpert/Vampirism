package de.teamlapen.vampirism.data.provider.model;

import com.mojang.datafixers.util.Pair;
import de.teamlapen.lib.lib.data.VBlockModelGenerators;
import de.teamlapen.vampirism.blocks.*;
import de.teamlapen.vampirism.blocks.candle.CandleHolderBlock;
import de.teamlapen.vampirism.client.renderer.item.BatCageSpecialRenderer;
import de.teamlapen.vampirism.client.renderer.item.BloodContainerSpecialRenderer;
import de.teamlapen.vampirism.client.renderer.item.MotherTrophyItemRenderer;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.data.ModBlockFamilies;
import de.teamlapen.vampirism.util.ColorListsUtil;
import net.minecraft.client.color.item.GrassColorSource;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.AbstractCandleBlock;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import static de.teamlapen.vampirism.api.util.VResourceLocation.mod;
import static de.teamlapen.vampirism.api.util.VResourceLocation.mc;
import static de.teamlapen.vampirism.api.util.VResourceLocation.modString;
import static net.minecraft.client.data.models.model.ModelLocationUtils.*;

public class ModBlockModelGenerators extends VBlockModelGenerators {

    public ModBlockModelGenerators(net.minecraft.client.data.models.BlockModelGenerators generators) {
        super(generators.blockStateOutput, generators.itemModelOutput, generators.modelOutput);
    }

    public ModBlockModelGenerators(Consumer<BlockStateGenerator> blockStateOutput, ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        super(blockStateOutput, itemModelOutput, modelOutput);
    }

    @Override
    public void run() {
        createFamilies(ModBlockFamilies.getFamilies());

        createGarlicDiffusers();
        createCandleHolders();
        createVampireSoulLantern();
        createBloodGrinder();
        createBloodSieve();
        createCursedBark();
        createHunterTable();
        createCoffin();
        createAlchemyTable();
        createWeaponTable();
        createNonTemplateBlocks();
        createTrivialBlocks();
        createMotherTrophy();
        createAltarPillars();
        createTent();
        createMedChair();
        createTotem();
        createAlchemicalCauldron();
        createCursedGrassBlock();
        createAlchemicalFire();
        createPlants();
        createWood();
        createCursedEarthPath();
        createInfuser();

        createTintedLeaves(ModBlocks.DARK_SPRUCE_LEAVES.get(), TexturedModel.LEAVES, -1);

        ResourceLocation sunscreenModel = ModModelTemplates.BEACON_MODEL.create(ModBlocks.SUNSCREEN_BEACON.get(), new TextureMapping().put(ModTextureSlots.BEACON, mod("block/cursed_earth")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.SUNSCREEN_BEACON.get(), sunscreenModel));
        createDefaultBlockItem(ModBlocks.SUNSCREEN_BEACON.get(), sunscreenModel);

        ResourceLocation vampireBeaconModel = ModModelTemplates.BEACON_MODEL.create(ModBlocks.VAMPIRE_BEACON.get(), new TextureMapping().put(ModTextureSlots.BEACON, mod("block/vampire_beacon")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.VAMPIRE_BEACON.get(), vampireBeaconModel));
        createDefaultBlockItem(ModBlocks.VAMPIRE_BEACON.get(), vampireBeaconModel);

        ResourceLocation infestedDarkStoneModel = ModModelTemplates.CUBE_ALL.create(ModBlocks.INFESTED_DARK_STONE.get(), new TextureMapping().put(TextureSlot.ALL, mod("block/dark_stone")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.INFESTED_DARK_STONE.get(), infestedDarkStoneModel));
        createDefaultBlockItem(ModBlocks.INFESTED_DARK_STONE.get(), mod("block/infested_dark_stone"));

        createNonTemplateModelBlock(ModBlocks.BLOOD_CONTAINER.get());
        ResourceLocation bloodContainerModel = ModelLocationUtils.getModelLocation(ModBlocks.BLOOD_CONTAINER.get());
        this.itemModelOutput.accept(ModBlocks.BLOOD_CONTAINER.asItem(), ItemModelUtils.composite(ItemModelUtils.plainModel(bloodContainerModel), ItemModelUtils.specialModel(bloodContainerModel, new BloodContainerSpecialRenderer.Unbaked())));

        ResourceLocation altarInspirationModel = mod("block/altar_inspiration/altar_inspiration");
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.ALTAR_INSPIRATION.get(), altarInspirationModel));
        createDefaultBlockItem(ModBlocks.ALTAR_INSPIRATION.get(), altarInspirationModel);

        createNonTemplateModelBlock(ModBlocks.ALTAR_INFUSION.get());
        ResourceLocation altarInfusionInventory = mod("block/altar_infusion_inventory");
        createDefaultBlockItem(ModBlocks.ALTAR_INFUSION.get(), altarInfusionInventory);

        createNonTemplateModelBlock(ModBlocks.BLOOD.get());

        createNonTemplateModelBlock(ModBlocks.BAT_CAGE.get());
        ResourceLocation batCageModel = ModelLocationUtils.getModelLocation(ModBlocks.BAT_CAGE.get());
        this.itemModelOutput.accept(ModBlocks.BAT_CAGE.asItem(), ItemModelUtils.composite(ItemModelUtils.plainModel(batCageModel), ItemModelUtils.specialModel(batCageModel, new BatCageSpecialRenderer.Unbaked())));
    }

    protected void createCursedEarthPath() {
        ResourceLocation pathModel = ModModelTemplates.DIRT_PATH.create(ModBlocks.CURSED_EARTH_PATH.get(), new TextureMapping().put(TextureSlot.PARTICLE, mod("block/cursed_earth")).put(TextureSlot.BOTTOM, mod("block/cursed_earth")).put(TextureSlot.SIDE, mod("block/cursed_earth_path_side")).put(TextureSlot.TOP, mod("block/cursed_earth_path_top")), this.modelOutput);
        this.blockStateOutput.accept(createRotatedVariant(ModBlocks.CURSED_EARTH_PATH.get(), pathModel));
        createDefaultBlockItem(ModBlocks.CURSED_EARTH_PATH.get(), pathModel);
    }

    protected void createPlants() {
        createCropBlock(ModBlocks.GARLIC.get(), BlockStateProperties.AGE_7, 0, 0, 1, 1, 2, 2, 2, 3);
        createPlantWithDefaultItem(ModBlocks.VAMPIRE_ORCHID.get(), ModBlocks.POTTED_VAMPIRE_ORCHID.get(), PlantType.NOT_TINTED);
        createPlantWithDefaultItem(ModBlocks.CURSED_SPRUCE_SAPLING.get(), ModBlocks.POTTED_CURSED_SPRUCE_SAPLING.get(), PlantType.NOT_TINTED);
        createPlantWithDefaultItem(ModBlocks.DARK_SPRUCE_SAPLING.get(), ModBlocks.POTTED_DARK_SPRUCE_SAPLING.get(),PlantType.NOT_TINTED);
        createPlantWithDefaultItem(ModBlocks.CURSED_ROOTS.get(), ModBlocks.POTTED_CURSED_ROOTS.get(), PlantType.NOT_TINTED);
        createCrossBlock(ModBlocks.CURSED_HANGING_ROOTS.get(), PlantType.NOT_TINTED);
        this.itemModelOutput.accept(ModBlocks.CURSED_HANGING_ROOTS.asItem(), ItemModelUtils.plainModel(createFlatItemModelWithBlockTexture(ModBlocks.CURSED_HANGING_ROOTS.asItem(), ModBlocks.CURSED_HANGING_ROOTS.get())));
    }

    protected void createWood() {
        this.woodProvider(ModBlocks.DARK_SPRUCE_LOG.get()).logWithHorizontal(ModBlocks.DARK_SPRUCE_LOG.get()).wood(ModBlocks.DARK_SPRUCE_WOOD.get());
        this.woodProvider(ModBlocks.STRIPPED_DARK_SPRUCE_LOG.get()).logWithHorizontal(ModBlocks.STRIPPED_DARK_SPRUCE_LOG.get()).wood(ModBlocks.STRIPPED_DARK_SPRUCE_WOOD.get());
        this.woodProvider(ModBlocks.CURSED_SPRUCE_LOG.get()).logWithHorizontal(ModBlocks.CURSED_SPRUCE_LOG.get()).wood(ModBlocks.CURSED_SPRUCE_WOOD.get());
        this.woodProvider(ModBlocks.STRIPPED_CURSED_SPRUCE_LOG.get()).logWithHorizontal(ModBlocks.STRIPPED_CURSED_SPRUCE_LOG.get()).wood(ModBlocks.STRIPPED_CURSED_SPRUCE_WOOD.get());
        createDefaultBlockItem(ModBlocks.DARK_SPRUCE_LOG.get());
        createDefaultBlockItem(ModBlocks.DARK_SPRUCE_WOOD.get());
        createDefaultBlockItem(ModBlocks.STRIPPED_DARK_SPRUCE_LOG.get());
        createDefaultBlockItem(ModBlocks.STRIPPED_DARK_SPRUCE_WOOD.get());
        createDefaultBlockItem(ModBlocks.CURSED_SPRUCE_LOG.get());
        createDefaultBlockItem(ModBlocks.CURSED_SPRUCE_WOOD.get());
        createDefaultBlockItem(ModBlocks.STRIPPED_CURSED_SPRUCE_LOG.get());
        createDefaultBlockItem(ModBlocks.STRIPPED_CURSED_SPRUCE_WOOD.get());

        this.createHangingSign(ModBlocks.DARK_SPRUCE_LOG.get(), ModBlocks.DARK_SPRUCE_HANGING_SIGN.get(), ModBlocks.DARK_SPRUCE_WALL_HANGING_SIGN.get());
        this.createHangingSign(ModBlocks.CURSED_SPRUCE_LOG.get(), ModBlocks.CURSED_SPRUCE_HANGING_SIGN.get(), ModBlocks.CURSED_SPRUCE_WALL_HANGING_SIGN.get());
    }

    protected void createGarlicDiffusers() {
        ResourceLocation defaultModel = mod("block/garlic_diffuser");
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.GARLIC_DIFFUSER_NORMAL.get(), defaultModel));
        createDefaultBlockItem(ModBlocks.GARLIC_DIFFUSER_NORMAL.get(), defaultModel);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.GARLIC_DIFFUSER_WEAK.get(), defaultModel));
        createDefaultBlockItem(ModBlocks.GARLIC_DIFFUSER_WEAK.get(), defaultModel);

        ResourceLocation improvedModel = ModModelTemplates.GARLIC_DIFFUSER.create(ModBlocks.GARLIC_DIFFUSER_IMPROVED.get(), new TextureMapping().put(ModTextureSlots.CORE, mod("block/garlic_diffuser_core_improved")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.GARLIC_DIFFUSER_IMPROVED.get(), improvedModel));
        createDefaultBlockItem(ModBlocks.GARLIC_DIFFUSER_IMPROVED.get(), improvedModel);

        createNonTemplateModelBlock(ModBlocks.GARLIC_DIFFUSER_CORE.get());
        registerSimpleItemModel(ModBlocks.GARLIC_DIFFUSER_CORE.get(), mod("item/garlic_diffuser_core"));
        createFlatItemModel(ModBlocks.GARLIC_DIFFUSER_CORE.asItem());

        ResourceLocation improvedCoreModel = ModModelTemplates.GARLIC_DIFFUSER_CORE.create(ModBlocks.GARLIC_DIFFUSER_CORE_IMPROVED.get(), new TextureMapping().put(ModTextureSlots.CORE, mod("block/garlic_diffuser_core_improved")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.GARLIC_DIFFUSER_CORE_IMPROVED.get(), improvedCoreModel));
        registerSimpleItemModel(ModBlocks.GARLIC_DIFFUSER_CORE_IMPROVED.get(), mod("item/garlic_diffuser_core_improved"));
        createFlatItemModel(ModBlocks.GARLIC_DIFFUSER_CORE_IMPROVED.asItem());
    }

    protected void createAltarPillars() {
        AltarPillarBlock block = ModBlocks.ALTAR_PILLAR.get();
        ResourceLocation baseModel = decorateBlockModelLocation(modString("altar_pillar"));
        PropertyDispatch.C1<AltarPillarBlock.EnumPillarType> dispatch = PropertyDispatch.property(AltarPillarBlock.PILLAR_TYPE);

        dispatch.select(AltarPillarBlock.EnumPillarType.NONE, Variant.variant().with(VariantProperties.MODEL, baseModel));

        for (AltarPillarBlock.EnumPillarType type : AltarPillarBlock.EnumPillarType.values()) {
            if (type == AltarPillarBlock.EnumPillarType.NONE) continue;

            ResourceLocation sideTexture = mod("block/altar_pillar_" + type.getSerializedName());
            ResourceLocation model = ModModelTemplates.ALTAR_PILLAR_FILLED.createWithSuffix(block, "_" + type.getSerializedName(), new TextureMapping().put(TextureSlot.SIDE, sideTexture), this.modelOutput);

            dispatch.select(type, Variant.variant().with(VariantProperties.MODEL, model));
        }

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(dispatch));

        createDefaultBlockItem(block, baseModel);
    }

    protected void createAlchemicalCauldron() {
        var cauldron = mod("block/alchemy_cauldron");
        var normal = mod("block/alchemy_cauldron_liquid");
        var boiling = ModModelTemplates.ALCHEMICAL_CAULDRON.createWithSuffix(ModBlocks.ALCHEMICAL_CAULDRON.get(), "_boiling", new TextureMapping().put(ModTextureSlots.LIQUID, mod("block/blank_liquid_boiling")), this.modelOutput);
        this.blockStateOutput.accept(MultiPartGenerator.multiPart(ModBlocks.ALCHEMICAL_CAULDRON.get())
                .with(Variant.variant().with(VariantProperties.MODEL, cauldron))
                .with(stateCondition(AlchemicalCauldronBlock.LIT, true), Variant.variant().with(VariantProperties.MODEL, mod("block/alchemy_cauldron_fire")))
                .with(stateCondition(AlchemicalCauldronBlock.LIQUID, AlchemicalCauldronBlock.LiquidState.FILLED), Variant.variant().with(VariantProperties.MODEL, normal))
                .with(stateCondition(AlchemicalCauldronBlock.LIQUID, AlchemicalCauldronBlock.LiquidState.BOILING), Variant.variant().with(VariantProperties.MODEL, boiling)));
        createDefaultBlockItem(ModBlocks.ALCHEMICAL_CAULDRON.get(), cauldron);
    }

    protected void createTotem() {
        createNonTemplateModelBlock(ModBlocks.TOTEM_BASE.get());
        createDefaultBlockItem(ModBlocks.TOTEM_BASE.get());

        createNonTemplateBlockWithItem(ModBlocks.TOTEM_TOP.get());

        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE.get(), ModModelTemplates.TOTEM_TOP.create(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE.get(), new TextureMapping().putForced(ModTextureSlots.CORE, mod("block/totem_top_core_vampire")), this.modelOutput)));
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER.get(), ModModelTemplates.TOTEM_TOP.create(ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER.get(), new TextureMapping().putForced(ModTextureSlots.CORE, mod("block/totem_top_core_hunter")), this.modelOutput)));

        ResourceLocation totemCraftedModel = ModModelTemplates.TOTEM_TOP.create(ModBlocks.TOTEM_TOP_CRAFTED.get(), new TextureMapping().putForced(TextureSlot.BOTTOM, mod("block/totem_top_crafted_bottom")).putForced(TextureSlot.SIDE, mod("block/totem_top_crafted_side")).putForced(TextureSlot.PARTICLE, mc("block/obsidian")), this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.TOTEM_TOP_CRAFTED.get(), totemCraftedModel));
        createDefaultBlockItem(ModBlocks.TOTEM_TOP_CRAFTED.get(), totemCraftedModel);

        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE_CRAFTED.get(), ModModelTemplates.TOTEM_TOP_CRAFTED.create(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE_CRAFTED.get(), new TextureMapping().putForced(ModTextureSlots.CORE, mod("block/totem_top_core_vampire")), this.modelOutput)));
        this.blockStateOutput.accept(createSimpleBlock(ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER_CRAFTED.get(), ModModelTemplates.TOTEM_TOP_CRAFTED.create(ModBlocks.TOTEM_TOP_VAMPIRISM_HUNTER_CRAFTED.get(), new TextureMapping().putForced(ModTextureSlots.CORE, mod("block/totem_top_core_hunter")), this.modelOutput)));
    }

    protected void createCandleHolders() {
        createEmptyCandleHolder(ModBlocks.CANDLE_STICK.get());
        createDefaultBlockItem(ModBlocks.CANDLE_STICK.get());
        createEmptyCandleHolder(ModBlocks.WALL_CANDLE_STICK.get());

        for (int i = 1; i < ColorListsUtil.STANDING_AND_WALL_CANDLE_STICKS.size(); i++) {
            Pair<CandleHolderBlock, CandleHolderBlock> pair = ColorListsUtil.STANDING_AND_WALL_CANDLE_STICKS.get(i);
            createCandleStick(pair.getFirst(), pair.getSecond(), pair.getFirst().getCandle().get());
            createDefaultBlockItem(pair.getFirst());
        }

        createEmptyCandleHolder(ModBlocks.CANDELABRA.get());
        createDefaultBlockItem(ModBlocks.CANDELABRA.get());
        createEmptyCandleHolder(ModBlocks.WALL_CANDELABRA.get());

        for (int i = 1; i < ColorListsUtil.STANDING_AND_WALL_CANDELABRAS.size(); i++) {
            Pair<CandleHolderBlock, CandleHolderBlock> pair = ColorListsUtil.STANDING_AND_WALL_CANDELABRAS.get(i);
            createCandelabra(pair.getFirst(), pair.getSecond(), pair.getFirst().getCandle().get());
            createDefaultBlockItem(pair.getFirst());
        }

        createNonTemplateModelBlock(ModBlocks.CHANDELIER.get());
        createDefaultBlockItem(ModBlocks.CHANDELIER.get());

        for (int i = 1; i < ColorListsUtil.HANGING_CHANDELIERS.size(); i++) {
            CandleHolderBlock block = ColorListsUtil.HANGING_CHANDELIERS.get(i);
            createChandelier(block, block.getCandle().get());
            createDefaultBlockItem(block);
        }
    }

    private void createCandleStick(CandleHolderBlock standingBlock, CandleHolderBlock wallBlock, Item candle) {
        createFilledCandleHolder(standingBlock, candle, ModModelTemplates.CANDLE_STICK_FILLED);
        createFilledCandleHolder(wallBlock, candle, ModModelTemplates.WALL_CANDLE_STICK_FILLED);
    }

    private void createCandelabra(CandleHolderBlock standingBlock, CandleHolderBlock wallBlock, Item candle) {
        createFilledCandleHolder(standingBlock, candle, ModModelTemplates.CANDELABRA_FILLED);
        createFilledCandleHolder(wallBlock, candle, ModModelTemplates.WALL_CANDELABRA_FILLED);
    }

    protected void createEmptyCandleHolder(CandleHolderBlock block) {
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(BlockStateProperties.HORIZONTAL_FACING).generate(facing -> Variant.variant().with(VariantProperties.MODEL, getModelLocation(block)).with(VariantProperties.Y_ROT, directionToRotation(facing)))));
    }

    protected void createFilledCandleHolder(CandleHolderBlock block, Item candle, ModelTemplate modelTemplate) {
        ResourceLocation candleTexture = BuiltInRegistries.ITEM.getKey(candle).withPrefix("block/");
        ResourceLocation model = modelTemplate.create(block, new TextureMapping().put(ModTextureSlots.CANDLE, candleTexture), this.modelOutput);
        ResourceLocation litModel = modelTemplate.createWithSuffix(block, "_lit", new TextureMapping().put(ModTextureSlots.CANDLE, candleTexture.withSuffix("_lit")), this.modelOutput);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.properties(AbstractCandleBlock.LIT, BlockStateProperties.HORIZONTAL_FACING).generate((lit, facing) -> Variant.variant().with(VariantProperties.MODEL, lit ? litModel : model).with(VariantProperties.Y_ROT, directionToRotation(facing)))));
    }

    private void createChandelier(CandleHolderBlock block, Item candle) {
        ResourceLocation candleTexture = BuiltInRegistries.ITEM.getKey(candle).withPrefix("block/");
        ResourceLocation model = ModModelTemplates.CHANDELIER_FILLED.create(block, new TextureMapping().put(ModTextureSlots.CANDLE, candleTexture), this.modelOutput);
        ResourceLocation litModel = ModModelTemplates.CHANDELIER_FILLED.createWithSuffix(block, "_lit", new TextureMapping().put(ModTextureSlots.CANDLE, candleTexture.withSuffix("_lit")), this.modelOutput);
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block).with(PropertyDispatch.property(AbstractCandleBlock.LIT).generate(lit -> Variant.variant().with(VariantProperties.MODEL, lit ? litModel : model))));
    }

    public static VariantProperties.Rotation directionToRotation(Direction direction) {
        return switch (direction) {
            case NORTH -> VariantProperties.Rotation.R0;
            case EAST -> VariantProperties.Rotation.R90;
            case SOUTH -> VariantProperties.Rotation.R180;
            case WEST -> VariantProperties.Rotation.R270;
            default -> throw new IllegalStateException("Unexpected value: " + direction);
        };
    }

    public void createVampireSoulLantern() {
        VampireSoulLanternBlock block = ModBlocks.VAMPIRE_SOUL_LANTERN.get();

        ResourceLocation model = getModelLocation(block);
        ResourceLocation hangingModel = getModelLocation(block, "_hanging");

        this.registerSimpleFlatItemModel(block.asItem());

        MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);

        Condition.TerminalCondition standing = stateCondition(VampireSoulLanternBlock.HANGING, false);
        Condition.TerminalCondition hanging = stateCondition(VampireSoulLanternBlock.HANGING, true);

        HORIZONTAL_ROTATION.forEach(pair -> {
            Direction direction = pair.getFirst();
            VariantProperties.Rotation rotation = pair.getSecond();
            Condition.TerminalCondition facing = stateCondition(VampireSoulLanternBlock.HORIZONTAL_FACING, direction);

            multiPartGenerator.with(Condition.and(facing, standing), Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, rotation));
            multiPartGenerator.with(Condition.and(facing, hanging), Variant.variant().with(VariantProperties.MODEL, hangingModel).with(VariantProperties.Y_ROT, rotation));
        });

        this.blockStateOutput.accept(multiPartGenerator);
    }

    protected void createBloodGrinder() {
        BloodGrinderBlock block = ModBlocks.BLOOD_GRINDER.get();

        ResourceLocation fullModel = getModelLocation(block);
        ResourceLocation bottomModelEmpty = getModelLocation(block, "_bottom_empty");
        ResourceLocation bottomModel = getModelLocation(block, "_bottom");
        ResourceLocation baseModel = getModelLocation(block, "_base");
        ResourceLocation wheelModel = getModelLocation(block, "_wheel");
        ResourceLocation grindingWheelModel = getModelLocation(block, "_wheel_grinding");

        this.createDefaultBlockItem(block, fullModel);

        MultiPartGenerator multiPartGenerator = MultiPartGenerator.multiPart(block);

        HORIZONTAL_ROTATION.forEach(pair -> {
            Direction direction = pair.getFirst();
            VariantProperties.Rotation rotation = pair.getSecond();
            Condition.TerminalCondition facing = stateCondition(BloodGrinderBlock.FACING, direction);

            multiPartGenerator.with(Condition.and(stateCondition(BloodGrinderBlock.HAS_FILTER, false), facing), Variant.variant().with(VariantProperties.MODEL, bottomModelEmpty).with(VariantProperties.Y_ROT, rotation));
            multiPartGenerator.with(Condition.and(stateCondition(BloodGrinderBlock.HAS_FILTER, true), facing), Variant.variant().with(VariantProperties.MODEL, bottomModel).with(VariantProperties.Y_ROT, rotation));
        });

        multiPartGenerator.with(Variant.variant().with(VariantProperties.MODEL, baseModel));

        multiPartGenerator.with(stateCondition(BloodGrinderBlock.GRINDING, false), Variant.variant().with(VariantProperties.MODEL, wheelModel));
        multiPartGenerator.with(stateCondition(BloodGrinderBlock.GRINDING, true), Variant.variant().with(VariantProperties.MODEL, grindingWheelModel));

        this.blockStateOutput.accept(multiPartGenerator);
    }

    protected void createBloodSieve() {
        BloodSieveBlock block = ModBlocks.BLOOD_SIEVE.get();

        ResourceLocation model = getModelLocation(block);
        ResourceLocation modelEmpty = getModelLocation(block, "_empty");

        this.createDefaultBlockItem(block, model);

        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block)
                .with(PropertyDispatch.properties(BloodSieveBlock.FACING, BloodSieveBlock.HAS_FILTER)
                        .generate((facing, hasFilter) -> Variant.variant()
                                .with(VariantProperties.MODEL, hasFilter ? model : modelEmpty)
                                .with(VariantProperties.Y_ROT, directionToRotation(facing))
                        )
                )
        );
    }

    protected void createMotherTrophy() {
        createNonTemplateModelBlock(ModBlocks.MOTHER_TROPHY.get());
        ResourceLocation modelLocation = getModelLocation(ModBlocks.MOTHER_TROPHY.get());
        this.itemModelOutput.accept(ModBlocks.MOTHER_TROPHY.asItem(), ItemModelUtils.composite(ItemModelUtils.plainModel(modelLocation), ItemModelUtils.specialModel(modelLocation, new MotherTrophyItemRenderer.Unbaked())));
    }

    protected void createNonTemplateBlocks() {
        Stream.of(
                ModBlocks.ALTAR_CLEANSING,
                ModBlocks.ALTAR_TIP,
                ModBlocks.BLOOD_PEDESTAL,
                ModBlocks.POTION_TABLE,
                ModBlocks.FIRE_PLACE,
                ModBlocks.CROSS,
                ModBlocks.TOMBSTONE_SHORT,
                ModBlocks.TOMBSTONE_MEDIUM,
                ModBlocks.TOMBSTONE_CROSS,
                ModBlocks.GRAVE_CAGE,
                ModBlocks.VAMPIRE_RACK,
                ModBlocks.THRONE,
                ModBlocks.FOG_DIFFUSER
        ).map(DeferredHolder::get).forEach(this::createNonTemplateBlockWithItem);
    }

    protected void createMedChair() {
        this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(ModBlocks.MED_CHAIR.get())
                .with(PropertyDispatch.properties(MedChairBlock.PART, MedChairBlock.FACING)
                        .generate(((enumPart, direction) ->
                                Variant.variant().with(VariantProperties.MODEL, enumPart == MedChairBlock.EnumPart.BOTTOM ? mod("block/medchairbase") : mod("block/medchairhead")).with(VariantProperties.Y_ROT, switch (direction){
                                    case NORTH -> VariantProperties.Rotation.R0;
                                    case EAST -> VariantProperties.Rotation.R90;
                                    case SOUTH -> VariantProperties.Rotation.R180;
                                    case WEST -> VariantProperties.Rotation.R270;
                                    default -> throw new IllegalStateException("Unexpected value: " + direction);
                                }))
                )));
    }

    protected void createTrivialBlocks() {
        Stream.of(
                ModBlocks.CURSED_EARTH,
                ModBlocks.BLOOD_INFUSED_IRON_BLOCK,
                ModBlocks.BLOOD_INFUSED_ENHANCED_IRON_BLOCK,
                ModBlocks.REMAINS,
                ModBlocks.INCAPACITATED_VULNERABLE_REMAINS,
                ModBlocks.VULNERABLE_REMAINS,
                ModBlocks.ACTIVE_VULNERABLE_REMAINS,
                ModBlocks.MOTHER,
                ModBlocks.BLOODY_DARK_STONE_BRICKS
        ).map(DeferredHolder::get).forEach(this::createTrivialBlockWithItem);
    }

    protected void createCursedBark() {
        ResourceLocation side1 = mod("block/cursed_bark_side");
        ResourceLocation side2 = mod("block/cursed_bark_side_2");
        this.blockStateOutput.accept(MultiPartGenerator.multiPart(ModBlocks.DIRECT_CURSED_BARK.get())
                .with(stateCondition(DirectCursedBarkBlock.EAST_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(stateCondition(DirectCursedBarkBlock.NORTH_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R0))
                .with(stateCondition(DirectCursedBarkBlock.WEST_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(stateCondition(DirectCursedBarkBlock.SOUTH_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(stateCondition(DirectCursedBarkBlock.UP_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(stateCondition(DirectCursedBarkBlock.DOWN_TYPE, DirectCursedBarkBlock.Type.VERTICAL), Variant.variant().with(VariantProperties.MODEL, side1).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))

                .with(stateCondition(DirectCursedBarkBlock.EAST_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90))
                .with(stateCondition(DirectCursedBarkBlock.NORTH_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(stateCondition(DirectCursedBarkBlock.WEST_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270))
                .with(stateCondition(DirectCursedBarkBlock.SOUTH_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180))
                .with(stateCondition(DirectCursedBarkBlock.UP_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.X_ROT, VariantProperties.Rotation.R270))
                .with(stateCondition(DirectCursedBarkBlock.DOWN_TYPE, DirectCursedBarkBlock.Type.HORIZONTAL), Variant.variant().with(VariantProperties.MODEL, side2).with(VariantProperties.X_ROT, VariantProperties.Rotation.R90))
        );
        createNonTemplateModelBlock(ModBlocks.DIAGONAL_CURSED_BARK.get());
    }

    protected void createHunterTable() {
        ResourceLocation hunterTable = mod("block/hunter_table/hunter_table");
        ResourceLocation hunterTableHammer = mod("block/hunter_table/hunter_table_hammer");
        ResourceLocation hunterTableGarlic = mod("block/hunter_table/hunter_table_garlic");
        ResourceLocation hunterTableBottle = mod("block/hunter_table/hunter_table_bottle");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(ModBlocks.HUNTER_TABLE.get());
        withHorizontalRotation(generator, null, hunterTable);
        withHorizontalRotation(generator, stateCondition(HunterTableBlock.WEAPON_TABLE, true), hunterTableHammer);
        withHorizontalRotation(generator, stateCondition(HunterTableBlock.ALCHEMICAL_CAULDRON, true), hunterTableGarlic);
        withHorizontalRotation(generator, stateCondition(HunterTableBlock.POTION_TABLE, true), hunterTableBottle);
        this.blockStateOutput.accept(generator);
        createDefaultBlockItem(ModBlocks.HUNTER_TABLE.get(), hunterTable);
    }

    protected void createAlchemicalFire() {
        List<ResourceLocation> list = this.createFloorFireModels(ModBlocks.ALCHEMICAL_FIRE.get());
        List<ResourceLocation> list1 = this.createSideFireModels(ModBlocks.ALCHEMICAL_FIRE.get());
        this.blockStateOutput
                .accept(
                        MultiPartGenerator.multiPart(ModBlocks.ALCHEMICAL_FIRE.get())
                                .with(wrapModels(list, p_387290_ -> p_387290_))
                                .with(wrapModels(list1, p_386602_ -> p_386602_))
                                .with(wrapModels(list1, p_388679_ -> p_388679_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R90)))
                                .with(wrapModels(list1, p_387956_ -> p_387956_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R180)))
                                .with(wrapModels(list1, p_386620_ -> p_386620_.with(VariantProperties.Y_ROT, VariantProperties.Rotation.R270)))
                );
    }

    protected void createCoffin() {
        Stream.of(ModBlocks.COFFIN_WHITE, ModBlocks.COFFIN_ORANGE, ModBlocks.COFFIN_MAGENTA, ModBlocks.COFFIN_LIGHT_BLUE, ModBlocks.COFFIN_YELLOW, ModBlocks.COFFIN_LIME, ModBlocks.COFFIN_PINK, ModBlocks.COFFIN_GRAY, ModBlocks.COFFIN_LIGHT_GRAY, ModBlocks.COFFIN_CYAN, ModBlocks.COFFIN_PURPLE, ModBlocks.COFFIN_BLUE, ModBlocks.COFFIN_BROWN, ModBlocks.COFFIN_GREEN, ModBlocks.COFFIN_RED, ModBlocks.COFFIN_BLACK).map(DeferredHolder::get).forEach(block -> {
            // TODO: Coffin items should actually use the coffin full model, but for some reason, when putting it into itemModelOutput, it just doesn't work
            ModModelTemplates.COFFIN.create(mod("block/coffin_" + block.getColor().getName()), new TextureMapping().put(ModTextureSlots.INNER, mod("block/coffin/coffin_inner_" + block.getColor().getName())), this.modelOutput);

            ResourceLocation bottomModel = ModModelTemplates.COFFIN_BOTTOM.create(mod("block/coffin_bottom_" + block.getColor().getName()), new TextureMapping().put(ModTextureSlots.INNER, mod("block/coffin/coffin_inner_" + block.getColor().getName())), this.modelOutput);
            ResourceLocation emptyModel = mod("block/coffin_empty");

            this.blockStateOutput.accept(MultiVariantGenerator.multiVariant(block, Variant.variant().with(VariantProperties.MODEL, emptyModel)));
            this.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(bottomModel));
        });
    }

    protected void createAlchemyTable() {
        ResourceLocation model = mod("block/alchemy_table/alchemy_table");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(ModBlocks.ALCHEMY_TABLE.get());
        withHorizontalRotation(generator, null, model);
        withHorizontalRotation(generator, stateCondition(AlchemyTableBlock.HAS_BOTTLE_INPUT_0, true), mod("block/alchemy_table/alchemy_table_input_0"));
        withHorizontalRotation(generator, stateCondition(AlchemyTableBlock.HAS_BOTTLE_INPUT_1, true), mod("block/alchemy_table/alchemy_table_input_1"));
        withHorizontalRotation(generator, stateCondition(AlchemyTableBlock.HAS_BOTTLE_OUTPUT_0, true), mod("block/alchemy_table/alchemy_table_output_0"));
        withHorizontalRotation(generator, stateCondition(AlchemyTableBlock.HAS_BOTTLE_OUTPUT_1, true), mod("block/alchemy_table/alchemy_table_output_1"));
        this.blockStateOutput.accept(generator);
        createDefaultBlockItem(ModBlocks.ALCHEMY_TABLE.get(), model);
    }

    protected void createWeaponTable() {
        ResourceLocation model = mod("block/weapon_table/weapon_table");
        MultiPartGenerator generator = MultiPartGenerator.multiPart(ModBlocks.WEAPON_TABLE.get());
        withHorizontalRotation(generator, null, model);
        withHorizontalRotation(generator, stateCondition(WeaponTableBlock.LAVA, 1), mod("block/weapon_table/weapon_table_lava1"));
        withHorizontalRotation(generator, stateCondition(WeaponTableBlock.LAVA, 2), mod("block/weapon_table/weapon_table_lava2"));
        withHorizontalRotation(generator, stateCondition(WeaponTableBlock.LAVA, 3), mod("block/weapon_table/weapon_table_lava3"));
        withHorizontalRotation(generator, stateCondition(WeaponTableBlock.LAVA, 4), mod("block/weapon_table/weapon_table_lava4"));
        withHorizontalRotation(generator, stateCondition(WeaponTableBlock.LAVA, 5), mod("block/weapon_table/weapon_table_lava5"));
        this.blockStateOutput.accept(generator);
        createDefaultBlockItem(ModBlocks.WEAPON_TABLE.get(), model);
    }

    protected void createTent() {
        ResourceLocation tr = ModModelTemplates.TENT.create(mod("block/tent_tr"), new TextureMapping().put(ModTextureSlots.FLOOR, mod("block/tent/floor_tr")), this.modelOutput);
        ResourceLocation tl = ModModelTemplates.TENT.create(mod("block/tent_tl"), new TextureMapping().put(ModTextureSlots.FLOOR, mod("block/tent/floor_tl")), this.modelOutput);
        ResourceLocation bl = ModModelTemplates.TENT.create(mod("block/tent_bl"), new TextureMapping().put(ModTextureSlots.FLOOR, mod("block/tent/floor_bl")), this.modelOutput);
        ResourceLocation br = ModModelTemplates.TENT.create(mod("block/tent_br"), new TextureMapping().put(ModTextureSlots.FLOOR, mod("block/tent/floor_br")), this.modelOutput);

        Stream.of(ModBlocks.TENT, ModBlocks.TENT_MAIN).map(DeferredHolder::get).forEach(block -> {
            MultiPartGenerator generator = MultiPartGenerator.multiPart(block);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 0), br);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 1), bl);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 2), tl);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 3), tr);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 2), mod("block/tentback"), 2);
            withHorizontalRotation(generator, stateCondition(TentBlock.POSITION, 3), mod("block/tentback_flipped"));
            this.blockStateOutput.accept(generator);
        });
    }

    protected void withHorizontalRotation(MultiPartGenerator generator, @Nullable Condition condition, ResourceLocation model) {
        this.withHorizontalRotation(generator, condition, model, 0);
    }

    protected void withHorizontalRotation(MultiPartGenerator generator, @Nullable Condition condition, ResourceLocation model, int rotation) {
        Function<Condition, Condition> and = cond -> condition == null ? cond : Condition.and(cond, condition);
        List<VariantProperties.Rotation> list = Arrays.stream(VariantProperties.Rotation.values()).toList();
        generator
                .with(and.apply(stateCondition(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH)), Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, list.get((list.indexOf(VariantProperties.Rotation.R0) + rotation) % list.size())))
                .with(and.apply(stateCondition(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST)), Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, list.get((list.indexOf(VariantProperties.Rotation.R90) + rotation) % list.size())))
                .with(and.apply(stateCondition(BlockStateProperties.HORIZONTAL_FACING, Direction.SOUTH)), Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, list.get((list.indexOf(VariantProperties.Rotation.R180) + rotation) % list.size())))
                .with(and.apply(stateCondition(BlockStateProperties.HORIZONTAL_FACING, Direction.WEST)), Variant.variant().with(VariantProperties.MODEL, model).with(VariantProperties.Y_ROT, list.get((list.indexOf(VariantProperties.Rotation.R270) + rotation) % list.size())));
    }

    protected void createCursedGrassBlock() {
        ResourceLocation resourceLocation = TextureMapping.getBlockTexture(ModBlocks.CURSED_EARTH.get());
        TextureMapping textureMapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, resourceLocation)
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(ModBlocks.CURSED_GRASS.get(), "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(ModBlocks.CURSED_GRASS.get(), "_side"));
        var model = ModModelTemplates.CUBE_BOTTOM_TOP.create(ModBlocks.CURSED_GRASS.get(), textureMapping ,this.modelOutput);
        TextureMapping snowTextureMapping = new TextureMapping()
                .put(TextureSlot.BOTTOM, resourceLocation)
                .copyForced(TextureSlot.BOTTOM, TextureSlot.PARTICLE)
                .put(TextureSlot.TOP, TextureMapping.getBlockTexture(ModBlocks.CURSED_GRASS.get(), "_top"))
                .put(TextureSlot.SIDE, TextureMapping.getBlockTexture(ModBlocks.CURSED_GRASS.get(), "_side_snowy"));
        Variant variant = Variant.variant().
                with(VariantProperties.MODEL, ModModelTemplates.CUBE_BOTTOM_TOP.createWithSuffix(ModBlocks.CURSED_GRASS.get(), "_snowy", snowTextureMapping, this.modelOutput));
        this.createGrassLikeBlock(ModBlocks.CURSED_GRASS.get(), model, variant);
        this.registerSimpleTintedItemModel(ModBlocks.CURSED_GRASS.get(), getModelLocation(ModBlocks.CURSED_GRASS.get()), new GrassColorSource());
    }

    protected void createInfuser() {
        this.blockStateOutput.accept(MultiPartGenerator.multiPart(ModBlocks.INFUSER.get())
                .with(Variant.variant().with(VariantProperties.MODEL, mod("block/blood_infuser/infuser")))
                .with(stateCondition(BloodInfuserBlock.IS_ACTIVE, true), Variant.variant().with(VariantProperties.MODEL, mod("block/blood_infuser/infuser_blood"))));
        this.createDefaultBlockItem(ModBlocks.INFUSER.get(), mod("block/blood_infuser/infuser"));
    }
}
