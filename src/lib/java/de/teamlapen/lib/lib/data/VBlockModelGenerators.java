package de.teamlapen.lib.lib.data;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelOutput;
import net.minecraft.client.data.models.blockstates.*;
import net.minecraft.client.data.models.model.*;
import net.minecraft.data.BlockFamily;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class VBlockModelGenerators extends BlockModelGenerators {

    private static final ResourceLocation CUTOUT = ResourceLocation.withDefaultNamespace("cutout");

    public VBlockModelGenerators(Consumer<BlockStateGenerator> blockStateGenerator, ItemModelOutput itemModelOutput, BiConsumer<ResourceLocation, ModelInstance> modelOutput) {
        super(blockStateGenerator, itemModelOutput, modelOutput);
    }

    protected void createFamilies(Collection<BlockFamily> families) {
        for (BlockFamily family : families) {
            if (family.shouldGenerateModel()) {
                this.family(family.getBaseBlock()).generateFor(family);
                createDefaultBlockItem(family.getBaseBlock());
                Stream.of(BlockFamily.Variant.PRESSURE_PLATE, BlockFamily.Variant.FENCE_GATE, BlockFamily.Variant.CRACKED, BlockFamily.Variant.CHISELED).forEach(variant -> {
                    Block block = family.get(variant);
                    //noinspection ConstantValue
                    if (block != null) {
                        createDefaultBlockItem(block, ModelLocationUtils.getModelLocation(block));
                    }
                });
            }
        }
    }

    protected void createTrivialBlockWithItem(Block block) {
        createTrivialCube(block);
        createDefaultBlockItem(block);
    }

    protected void createNonTemplateBlockWithItem(Block block) {
        if (block.getStateDefinition().getProperty(BlockStateProperties.HORIZONTAL_FACING.getName()) != null) {
            createNonTemplateHorizontalBlock(block);
        } else {
            createNonTemplateModelBlock(block);
        }
        createDefaultBlockItem(block);
    }

    private final Set<Item> createdItemModels = new HashSet<>();

    protected void createDefaultBlockItem(Block block, ResourceLocation model) {
        var item = block.asItem();
        if (item != Items.AIR && !createdItemModels.contains(item)) {
            this.createdItemModels.add(item);
            this.itemModelOutput.accept(block.asItem(), ItemModelUtils.plainModel(model));
        }
    }

    protected void createDefaultBlockItem(Block block) {
        createDefaultBlockItem(block, ModelLocationUtils.getModelLocation(block));
    }

    protected void createDefaultBlockItem(Block block, Block model) {
        createDefaultBlockItem(block, ModelLocationUtils.getModelLocation(model));
    }

    /**
     * The normal methods in the base class don't add the cutout render type, but it must be here so that the texture is rendered in a correct way.
     */
    @Override
    public void createCrossBlock(@NotNull Block block, PlantType type, @NotNull TextureMapping textureMapping) {
        ResourceLocation resourcelocation = type.getCross().extend().renderType(CUTOUT).build().create(block, textureMapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(block, resourcelocation));
    }

    @Override
    public void createPlant(@NotNull Block block, @NotNull Block pottedBlock, @NotNull PlantType plantType) {
        this.createCrossBlock(block, plantType);
        TextureMapping texturemapping = plantType.getPlantTextureMapping(block);
        ResourceLocation resourcelocation = plantType.getCrossPot().extend().renderType(CUTOUT).build().create(pottedBlock, texturemapping, this.modelOutput);
        this.blockStateOutput.accept(createSimpleBlock(pottedBlock, resourcelocation));
    }
}
