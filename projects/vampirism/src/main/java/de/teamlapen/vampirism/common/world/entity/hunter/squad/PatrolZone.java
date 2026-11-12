package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

public class PatrolZone {

    private static final int MAX_STEP = 1;
    private static final int MAX_VERTICAL_TOLERANCE = 1;
    private static final long EXPIRY = 6000L;

    private final long scannedAt;
    private final Long2IntMap surfaces;
    private final LongSet ladders;

    private PatrolZone(long scannedAt, Long2IntMap surfaces, LongSet ladders) {
        this.scannedAt = scannedAt;
        this.surfaces = surfaces;
        this.ladders = ladders;
    }

    public static PatrolZone scan(ServerLevel level, PatrolArea area) {
        BlockPos center = area.center();
        int radiusSqr = area.radius() * area.radius();
        Long2IntMap surfaces = new Long2IntOpenHashMap();
        surfaces.defaultReturnValue(Integer.MIN_VALUE);
        LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
        LongSet ladders = new LongOpenHashSet();

        if (level.hasChunkAt(center)) {
            long start = key(center.getX(), center.getZ());
            surfaces.put(start, getSurface(level, center.getX(), center.getZ()));
            queue.enqueue(start);
        }

        while (!queue.isEmpty()) {
            long current = queue.dequeueLong();
            int x = getX(current);
            int z = getZ(current);
            int surface = surfaces.get(current);

            for (Direction direction : Direction.Plane.HORIZONTAL) {
                int nx = x + direction.getStepX();
                int nz = z + direction.getStepZ();
                long next = key(nx, nz);
                if (surfaces.containsKey(next)) continue;

                int dx = nx - center.getX();
                int dz = nz - center.getZ();
                if (dx * dx + dz * dz > radiusSqr || !level.hasChunkAt(new BlockPos(nx, center.getY(), nz))) continue;

                int nextSurface = getSurface(level, nx, nz);
                boolean connected = Math.abs(nextSurface - surface) <= MAX_STEP
                        || isLadderLink(level, nx, nz, nextSurface, surface)
                        || isLadderLink(level, x, z, surface, nextSurface);
                if (!connected || !isWalkableColumn(level, nx, nextSurface, nz)) continue;

                if (isLadder(level, nx, nextSurface - 1, nz)) ladders.add(next);
                surfaces.put(next, nextSurface);
                queue.enqueue(next);
            }
        }
        return new PatrolZone(level.getGameTime(), surfaces, ladders);
    }

    public boolean isExpired(long gameTime) {
        return gameTime - this.scannedAt > EXPIRY;
    }

    public int getSurface(int x, int z) {
        return this.surfaces.get(key(x, z));
    }

    public boolean hasColumn(int x, int z) {
        long key = key(x, z);
        return this.surfaces.containsKey(key) && !this.ladders.contains(key);
    }

    public boolean contains(BlockPos pos) {
        long key = key(pos.getX(), pos.getZ());
        int surface = this.surfaces.get(key);
        if (surface == Integer.MIN_VALUE) return false;
        if (this.ladders.contains(key)) return pos.getY() <= surface;
        return Math.abs(pos.getY() - surface) <= MAX_VERTICAL_TOLERANCE;
    }

    public void forEachStandableColumn(ColumnVisitor visitor) {
        for (Long2IntMap.Entry entry : this.surfaces.long2IntEntrySet()) {
            long key = entry.getLongKey();
            if (!this.ladders.contains(key)) visitor.visit(getX(key), entry.getIntValue(), getZ(key));
        }
    }

    public int size() {
        return this.surfaces.size();
    }

    private static boolean isLadderLink(ServerLevel level, int ladderX, int ladderZ, int ladderSurface, int otherSurface) {
        int y = ladderSurface - 1;
        if (!isLadder(level, ladderX, y, ladderZ)) return false;

        while (isLadder(level, ladderX, y - 1, ladderZ)) {
            y--;
        }
        return Math.abs(otherSurface - y) <= MAX_STEP;
    }

    private static boolean isLadder(ServerLevel level, int x, int y, int z) {
        return level.getBlockState(new BlockPos(x, y, z)).getBlock() instanceof LadderBlock;
    }

    private static boolean isWalkableColumn(ServerLevel level, int x, int surface, int z) {
        BlockPos ground = new BlockPos(x, surface - 1, z);
        BlockState state = level.getBlockState(ground);
        return state.getFluidState().isEmpty() && !state.is(BlockTags.FENCES) && !state.is(BlockTags.WALLS);
    }

    private static int getSurface(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    public interface ColumnVisitor {
        void visit(int x, int surface, int z);
    }

    private static long key(int x, int z) {
        return ((long) x << 32) | (z & 0xFFFFFFFFL);
    }

    private static int getX(long key) {
        return (int) (key >> 32);
    }

    private static int getZ(long key) {
        return (int) key;
    }
}
