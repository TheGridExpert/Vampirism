package de.teamlapen.vampirism.world.gen.structure.huntercamp;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.dimension.DimensionDefaults;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class HunterCampStructure extends Structure {

    public static final MapCodec<HunterCampStructure> CODEC = simpleCodec(HunterCampStructure::new);

    public HunterCampStructure(StructureSettings settings) {
        super(settings);
    }

    @Override
    public @NotNull Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos chunkCenter = chunkPos.getWorldPosition().offset(8, 0, 8);
        BlockPos bestPos = findBestFlatSpot(context, chunkCenter);

        if (bestPos == null) {
            return Optional.empty();
        }

        return Optional.of(new GenerationStub(bestPos, builder -> generatePieces(builder, context, bestPos)));
    }

    public static BlockPos findBestFlatSpot(GenerationContext context, BlockPos start) {
        int radius = 8;
        int step = 2;

        if (isInitialPosAcceptable(context, start, radius, step)) {
            return start;
        }

        int offset = 32;
        BlockPos[] candidates = {
                start.north(offset),
                start.south(offset),
                start.east(offset),
                start.west(offset)
        };

        BlockPos best = null;
        int bestDiff = DimensionDefaults.OVERWORLD_LEVEL_HEIGHT;

        for (BlockPos candidate : candidates) {
            if (!isSurface(context, candidate)) continue;

            int diff = getHeightDifference(context, candidate, radius, step);
            if (diff >= 0 && diff < bestDiff) {
                best = candidate;
                bestDiff = diff;
            }
        }

        return best;
    }

    public static boolean isInitialPosAcceptable(GenerationContext context, BlockPos pos, int radius, int step) {
        int diff = getHeightDifference(context, pos, radius, step);
        if (diff < 0 || diff > 13) {
            return false;
        }

        return isSurface(context, pos);
    }

    public static int getHeightDifference(GenerationContext context, BlockPos pos, int radius, int step) {
        int minY = DimensionDefaults.OVERWORLD_LEVEL_HEIGHT;
        int maxY = DimensionDefaults.OVERWORLD_MIN_Y;

        for (int dx = -radius; dx <= radius; dx += step) {
            for (int dz = -radius; dz <= radius; dz += step) {
                int y = context.chunkGenerator().getFirstFreeHeight(pos.getX() + dx, pos.getZ() + dz, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());

                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }

        return maxY - minY;
    }

    public static boolean isSurface(GenerationContext context, BlockPos pos) {
        int offset = 8;
        BlockPos[] candidates = {
                pos.north(offset),
                pos.south(offset),
                pos.east(offset),
                pos.west(offset)
        };

        for (BlockPos candidate : candidates) {
            int surfaceY = context.chunkGenerator().getFirstFreeHeight(candidate.getX(), candidate.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            Holder<Biome> biome = context.biomeSource().getNoiseBiome(QuartPos.fromBlock(candidate.getX()), QuartPos.fromBlock(surfaceY), QuartPos.fromBlock(candidate.getZ()), context.randomState().sampler());

            if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) return false;
        }

        return true;
    }

    private void generatePieces(StructurePiecesBuilder builder, Structure.GenerationContext context, BlockPos pos) {
        Rotation rotation = Rotation.getRandom(context.random());
        HunterCampPieces.addPieces(context.structureTemplateManager(), pos, rotation, builder, context.random());
    }

    @Override
    public @NotNull StructureType<?> type() {
        return ModStructures.HUNTER_CAMP_TYPE.get();
    }
}
