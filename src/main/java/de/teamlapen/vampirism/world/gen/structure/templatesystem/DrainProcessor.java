package de.teamlapen.vampirism.world.gen.structure.templatesystem;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DrainProcessor extends StructureProcessor {
    public static final MapCodec<DrainProcessor> CODEC = MapCodec.unit(DrainProcessor::new);

    public DrainProcessor() {
    }

    @Nullable
    @Override
    public StructureTemplate.StructureBlockInfo process(@NotNull LevelReader level, @NotNull BlockPos offset, @NotNull BlockPos pos, StructureTemplate.@NotNull StructureBlockInfo blockInfo, StructureTemplate.@NotNull StructureBlockInfo relativeBlockInfo, @NotNull StructurePlaceSettings settings, @Nullable StructureTemplate template) {
        BlockState blockState = relativeBlockInfo.state();
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED)) {
            blockState = blockState.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return new StructureTemplate.StructureBlockInfo(relativeBlockInfo.pos(), blockState, relativeBlockInfo.nbt());
    }

    @Override
    protected @NotNull StructureProcessorType<?> getType() {
        return ModStructures.WATER_DRAIN.get();
    }
}
