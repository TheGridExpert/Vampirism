package de.teamlapen.vampirism.common.world.structures.huntercamp;

import de.teamlapen.vampirism.common.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

public class HunterCampClearing extends StructurePiece implements PieceBeardifierModifier {

    private static final int HEIGHT_BELOW = 8;
    private static final int HEIGHT_ABOVE = 24;
    private static final double FULLY_CLEAR_FRACTION = 0.5;
    private static final double NOISE_SCALE = 1.0 / 6.0;
    private static final SimplexNoise NOISE = new SimplexNoise(new XoroshiroRandomSource(0x6875_6e74_6572L));

    public HunterCampClearing(BlockPos center, int radius) {
        super(ModStructures.HUNTER_CAMP_CLEARING.get(), 0, new BoundingBox(center.getX() - radius, center.getY() - HEIGHT_BELOW, center.getZ() - radius, center.getX() + radius, center.getY() + HEIGHT_ABOVE, center.getZ() + radius));
    }

    public HunterCampClearing(StructurePieceSerializationContext context, CompoundTag tag) {
        super(ModStructures.HUNTER_CAMP_CLEARING.get(), tag);
    }

    public boolean blocksTree(BlockPos pos) {
        BlockPos center = getCenter();
        int radius = getRadius();
        double distance = Math.sqrt(Mth.square(pos.getX() - center.getX()) + Mth.square(pos.getZ() - center.getZ())) / radius;
        if (distance >= 1.0) return false;
        if (distance <= FULLY_CLEAR_FRACTION) return true;

        double edge = (distance - FULLY_CLEAR_FRACTION) / (1.0 - FULLY_CLEAR_FRACTION);
        double noise = (NOISE.getValue(pos.getX() * NOISE_SCALE, pos.getZ() * NOISE_SCALE) + 1.0) / 2.0;
        return noise > edge;
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structureManager, ChunkGenerator generator, RandomSource random, BoundingBox chunkBB, ChunkPos chunkPos, BlockPos referencePos) {
        BlockPos center = getCenter();
        if (!chunkBB.isInside(center.getX(), chunkBB.minY(), center.getZ())) return;

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ());
        level.setBlock(center.atY(y), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private BlockPos getCenter() {
        BoundingBox box = getBoundingBox();
        return new BlockPos(box.minX() + getRadius(), box.minY() + HEIGHT_BELOW, box.minZ() + getRadius());
    }

    private int getRadius() {
        return getBoundingBox().getXSpan() / 2;
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
    }

    @Override
    public BoundingBox getBeardifierBox() {
        return getBoundingBox();
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return TerrainAdjustment.NONE;
    }

    @Override
    public int getGroundLevelDelta() {
        return 0;
    }
}
