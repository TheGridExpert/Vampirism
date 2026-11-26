package de.teamlapen.vampirism.common.world.structures.huntercamp;

import de.teamlapen.vampirism.common.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.XoroshiroRandomSource;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.synth.SimplexNoise;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HunterCampClearing extends StructurePiece implements PieceBeardifierModifier {

    private static final int HEIGHT_BELOW = 8;
    private static final int HEIGHT_ABOVE = 24;
    private static final double FULLY_CLEAR_FRACTION = 0.5;
    private static final double NOISE_SCALE = 1.0 / 6.0;
    private static final int PATCH_RADIUS = 12;
    private static final int PATCH_DEPTH = 4;
    private static final int SURFACE_RANGE = 4;
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
        patchCarvedGround(level, generator, chunkBB, center);
        if (!chunkBB.isInside(center.getX(), chunkBB.minY(), center.getZ())) return;

        int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, center.getX(), center.getZ());
        level.setBlock(center.atY(y), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private void patchCarvedGround(WorldGenLevel level, ChunkGenerator generator, BoundingBox chunkBB, BlockPos center) {
        int radius = Math.min(PATCH_RADIUS, getRadius());
        int top = center.getY() + SURFACE_RANGE + 1;
        int bottom = center.getY() - SURFACE_RANGE - PATCH_DEPTH;
        List<BlockPos> damaged = new ArrayList<>();
        Map<BlockState, Integer> surfaces = new HashMap<>();
        Map<BlockState, Integer> fillers = new HashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int x = Math.max(chunkBB.minX(), center.getX() - radius); x <= Math.min(chunkBB.maxX(), center.getX() + radius); x++) {
            for (int z = Math.max(chunkBB.minZ(), center.getZ() - radius); z <= Math.min(chunkBB.maxZ(), center.getZ() + radius); z++) {
                if (Mth.square(x - center.getX()) + Mth.square(z - center.getZ()) > radius * radius) continue;

                int ground = Integer.MIN_VALUE;
                boolean hollow = false;
                for (int y = top; y >= bottom; y--) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (ground == Integer.MIN_VALUE) {
                        if (isGround(state)) ground = y;
                    } else if (state.isAir()) {
                        hollow = true;
                        break;
                    }
                }

                if (hollow || ground < center.getY() - SURFACE_RANGE) {
                    damaged.add(new BlockPos(x, 0, z));
                    continue;
                }

                BlockState surface = level.getBlockState(pos.set(x, ground, z));
                BlockState filler = level.getBlockState(pos.move(Direction.DOWN));
                if (isMaterial(surface)) surfaces.merge(surface, 1, Integer::sum);
                if (isMaterial(filler)) fillers.merge(filler, 1, Integer::sum);
            }
        }

        if (damaged.isEmpty()) return;

        BlockState filler = mostCommon(fillers, Blocks.DIRT.defaultBlockState());
        BlockState surface = mostCommon(surfaces, filler);
        RandomState randomState = level.getLevel().getChunkSource().randomState();
        for (BlockPos column : damaged) {
            int base = generator.getBaseHeight(column.getX(), column.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
            for (int y = base - PATCH_DEPTH; y < base; y++) {
                if (level.getBlockState(pos.set(column.getX(), y, column.getZ())).isAir()) {
                    level.setBlock(pos, y == base - 1 ? surface : filler, Block.UPDATE_CLIENTS);
                }
            }
        }
    }

    private static boolean isGround(BlockState state) {
        return state.is(BlockTags.OVERWORLD_CARVER_REPLACEABLES);
    }

    private static boolean isMaterial(BlockState state) {
        return isGround(state) && !(state.getBlock() instanceof FallingBlock) && state.isCollisionShapeFullBlock(EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
    }

    private static BlockState mostCommon(Map<BlockState, Integer> counts, BlockState fallback) {
        return counts.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey).orElse(fallback);
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
