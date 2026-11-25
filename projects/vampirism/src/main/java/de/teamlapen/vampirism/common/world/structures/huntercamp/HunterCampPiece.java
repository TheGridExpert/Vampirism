package de.teamlapen.vampirism.common.world.structures.huntercamp;

import de.teamlapen.vampirism.common.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

import java.util.HashMap;
import java.util.Map;

public class HunterCampPiece extends PoolElementStructurePiece implements PieceBeardifierModifier {

    public static final String TAG_FOUNDATION = "foundation";

    private static final int MAX_FOUNDATION_DEPTH = 16;

    private final boolean foundation;

    public HunterCampPiece(StructureTemplateManager structureTemplateManager, StructurePoolElement element, BlockPos position, Rotation rotation, BoundingBox boundingBox, boolean foundation) {
        super(structureTemplateManager, element, position, 0, rotation, boundingBox, LiquidSettings.IGNORE_WATERLOGGING);
        this.foundation = foundation;
    }

    public HunterCampPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(context, tag);
        this.foundation = tag.getBooleanOr(TAG_FOUNDATION, true);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.putBoolean(TAG_FOUNDATION, this.foundation);
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        super.postProcess(level, structureManager, generator, random, chunkBB, chunkPos, referencePos);
        if (this.foundation) {
            extendFoundations(level, chunkBB);
        }
    }

    private void extendFoundations(WorldGenLevel level, BoundingBox chunkBB) {
        BoundingBox box = getBoundingBox();
        int minX = Math.max(box.minX(), chunkBB.minX());
        int maxX = Math.min(box.maxX(), chunkBB.maxX());
        int minZ = Math.max(box.minZ(), chunkBB.minZ());
        int maxZ = Math.min(box.maxZ(), chunkBB.maxZ());
        int minY = Math.max(box.minY() - MAX_FOUNDATION_DEPTH, level.getMinY());
        Ground ground = Ground.sample(level, minX, maxX, minZ, maxZ, box.minY() - 1);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                pos.set(x, box.minY(), z);
                BlockState bottom = level.getBlockState(pos);
                if (bottom.canBeReplaced()) continue;

                boolean leg = isLeg(bottom);
                pos.move(Direction.DOWN);
                while (pos.getY() >= minY && level.getBlockState(pos).canBeReplaced()) {
                    level.setBlock(pos, leg ? bottom : pos.getY() == box.minY() - 1 ? ground.surface() : ground.filler(), Block.UPDATE_CLIENTS);
                    pos.move(Direction.DOWN);
                }
            }
        }
    }

    private static boolean isLeg(BlockState state) {
        return state.is(BlockTags.LOGS) && state.getOptionalValue(RotatedPillarBlock.AXIS).map(axis -> axis == Direction.Axis.Y).orElse(true);
    }

    private record Ground(BlockState surface, BlockState filler) {

        private static final Ground DEFAULT = new Ground(Blocks.DIRT.defaultBlockState(), Blocks.DIRT.defaultBlockState());

        private static Ground sample(WorldGenLevel level, int minX, int maxX, int minZ, int maxZ, int y) {
            Map<BlockState, Integer> surfaces = new HashMap<>();
            Map<BlockState, Integer> fillers = new HashMap<>();
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    BlockState surface = level.getBlockState(pos.set(x, y, z));
                    BlockState filler = level.getBlockState(pos.move(Direction.DOWN));
                    if (!isGround(surface) || !isGround(filler)) continue;

                    surfaces.merge(surface, 1, Integer::sum);
                    fillers.merge(filler, 1, Integer::sum);
                }
            }

            if (surfaces.isEmpty()) return DEFAULT;
            return new Ground(mostCommon(surfaces), mostCommon(fillers));
        }

        private static boolean isGround(BlockState state) {
            return !state.canBeReplaced() && !(state.getBlock() instanceof FallingBlock) && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
        }

        private static BlockState mostCommon(Map<BlockState, Integer> counts) {
            return counts.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
        }
    }

    @Override
    public BoundingBox getBeardifierBox() {
        return getBoundingBox();
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return this.foundation ? TerrainAdjustment.BEARD_THIN : TerrainAdjustment.NONE;
    }

    @Override
    public StructurePieceType getType() {
        return ModStructures.HUNTER_CAMP_PIECE.get();
    }
}
