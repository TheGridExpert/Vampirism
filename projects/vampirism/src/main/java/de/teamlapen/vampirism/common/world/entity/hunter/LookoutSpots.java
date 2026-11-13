package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.vampirism.common.world.blocks.TentBlock;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LookoutSpots {

    public static final int MIN_SPOT_SPACING = 5;

    private static final int SAMPLE_STEP = 2;
    private static final int MIN_HEIGHT_GAIN = 2;
    private static final double MIN_PROMINENCE = 2.0D;
    private static final double PROMINENCE_WEIGHT = 1.5D;
    private static final int PROMINENCE_DISTANCE = 4;
    private static final double DISTANCE_PENALTY = 0.15D;
    private static final int SECTOR_COUNT = 4;
    private static final int MAX_SPOTS = 10;
    private static final int MAX_LADDER_SPOTS = 4;
    private static final int MIN_LADDER_HEIGHT = 3;
    private static final int MAX_PLATFORM_CELLS = 64;
    private static final int MAX_PLATFORM_RADIUS = 8;
    private static final long RESCAN_INTERVAL = 6000L;

    private final PatrolArea area;
    private final long scannedAt;
    private final List<BlockPos> spots;

    private LookoutSpots(PatrolArea area, long scannedAt, List<BlockPos> spots) {
        this.area = area;
        this.scannedAt = scannedAt;
        this.spots = spots;
    }

    public static LookoutSpots scan(ServerLevel level, PatrolArea area, @Nullable PatrolZone zone) {
        return new LookoutSpots(area, level.getGameTime(), findSpots(level, area, zone));
    }

    public boolean isOutdated(PatrolArea currentArea, long gameTime) {
        if (currentArea.radius() != this.area.radius() || gameTime - this.scannedAt > RESCAN_INTERVAL) return true;
        double maxShift = this.area.radius() / 2.0D;
        return currentArea.center().distToCenterSqr(this.area.center().getX() + 0.5D, currentArea.center().getY() + 0.5D, this.area.center().getZ() + 0.5D) > maxShift * maxShift;
    }

    public List<BlockPos> spots() {
        return this.spots;
    }

    public void reject(BlockPos spot) {
        this.spots.remove(spot);
    }

    private static List<BlockPos> findSpots(ServerLevel level, PatrolArea area, @Nullable PatrolZone zone) {
        BlockPos center = area.center();
        int radius = area.radius();
        List<List<ScoredSpot>> sectors = new ArrayList<>();
        for (int i = 0; i < SECTOR_COUNT; i++) {
            sectors.add(new ArrayList<>());
        }

        for (int dx = -radius; dx <= radius; dx += SAMPLE_STEP) {
            for (int dz = -radius; dz <= radius; dz += SAMPLE_STEP) {
                double distance = Math.sqrt(dx * dx + dz * dz);
                if (distance > radius) continue;

                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) continue;
                if (zone != null && !zone.hasColumn(x, z)) continue;

                BlockPos spot = new BlockPos(x, zone != null ? zone.getSurface(x, z) : getSurface(level, x, z), z);
                int heightGain = spot.getY() - center.getY();
                double prominence = spot.getY() - getAverageSurroundingSurface(level, x, z);
                if ((heightGain < MIN_HEIGHT_GAIN && prominence < MIN_PROMINENCE) || !canStandAt(level, spot)) continue;

                double score = heightGain + prominence * PROMINENCE_WEIGHT - distance * DISTANCE_PENALTY;
                sectors.get(getSector(dx, dz)).add(new ScoredSpot(spot, score));
            }
        }

        sectors.forEach(sector -> sector.sort(Comparator.comparingDouble(ScoredSpot::score).reversed()));

        List<BlockPos> spots = new ArrayList<>(findLadderSpots(level, zone));
        int[] cursors = new int[SECTOR_COUNT];
        boolean added = true;
        while (added && spots.size() < MAX_SPOTS) {
            added = false;
            List<ScoredSpot> round = new ArrayList<>();
            for (int i = 0; i < SECTOR_COUNT; i++) {
                ScoredSpot next = takeNextSpaced(sectors.get(i), cursors, i, spots);
                if (next != null) round.add(next);
            }
            round.sort(Comparator.comparingDouble(ScoredSpot::score).reversed());
            for (ScoredSpot spot : round) {
                if (spots.size() >= MAX_SPOTS || spots.stream().anyMatch(other -> other.closerThan(spot.pos(), MIN_SPOT_SPACING))) continue;
                spots.add(spot.pos());
                added = true;
            }
        }
        return spots;
    }

    private static List<BlockPos> findLadderSpots(ServerLevel level, @Nullable PatrolZone zone) {
        if (zone == null) return List.of();

        Set<BlockPos> visitedLadders = new HashSet<>();
        List<ScoredSpot> found = new ArrayList<>();
        zone.forEachStandableColumn((x, surface, z) -> {
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                for (int y = surface - 1; y <= surface + 1; y++) {
                    BlockPos ladder = new BlockPos(x + direction.getStepX(), y, z + direction.getStepZ());
                    if (!isLadder(level, ladder) || isLadder(level, ladder.below())) continue;
                    if (!visitedLadders.add(ladder)) continue;

                    BlockPos top = ladder;
                    while (isLadder(level, top.above())) {
                        top = top.above();
                    }
                    if (top.getY() - ladder.getY() + 1 < MIN_LADDER_HEIGHT) continue;

                    BlockPos exit = findLadderExit(level, top);
                    if (exit != null) found.add(new ScoredSpot(findPlatformCenter(level, exit), exit.getY() - surface));
                }
            }
        });

        found.sort(Comparator.comparingDouble(ScoredSpot::score).reversed());
        List<BlockPos> spots = new ArrayList<>();
        for (ScoredSpot spot : found) {
            if (spots.size() >= MAX_LADDER_SPOTS) break;
            if (spots.stream().noneMatch(other -> other.closerThan(spot.pos(), MIN_SPOT_SPACING))) spots.add(spot.pos());
        }
        return spots;
    }

    private static @Nullable BlockPos findLadderExit(ServerLevel level, BlockPos top) {
        Direction wall = level.getBlockState(top).getValue(LadderBlock.FACING).getOpposite();
        BlockPos onWall = top.relative(wall).above();
        if (canStandAt(level, onWall)) return onWall;

        BlockPos aboveLadder = top.above();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos beside = aboveLadder.relative(direction);
            if (canStandAt(level, beside)) return beside;
        }
        return null;
    }

    private static BlockPos findPlatformCenter(ServerLevel level, BlockPos exit) {
        List<BlockPos> cells = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        visited.add(exit);
        queue.add(exit);

        while (!queue.isEmpty() && cells.size() < MAX_PLATFORM_CELLS) {
            BlockPos cell = queue.poll();
            cells.add(cell);
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                BlockPos next = cell.relative(direction);
                if (next.distManhattan(exit) > MAX_PLATFORM_RADIUS || !visited.add(next) || !canStandAt(level, next)) continue;
                queue.add(next);
            }
        }

        double centerX = cells.stream().mapToDouble(BlockPos::getX).average().orElse(exit.getX());
        double centerZ = cells.stream().mapToDouble(BlockPos::getZ).average().orElse(exit.getZ());
        return cells.stream()
                .min(Comparator.comparingDouble(cell -> Math.pow(cell.getX() - centerX, 2) + Math.pow(cell.getZ() - centerZ, 2)))
                .orElse(exit);
    }

    private static boolean isLadder(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof LadderBlock;
    }

    private static @Nullable ScoredSpot takeNextSpaced(List<ScoredSpot> sector, int[] cursors, int index, List<BlockPos> taken) {
        while (cursors[index] < sector.size()) {
            ScoredSpot candidate = sector.get(cursors[index]++);
            if (taken.stream().noneMatch(other -> other.closerThan(candidate.pos(), MIN_SPOT_SPACING))) return candidate;
        }
        return null;
    }

    private static int getSector(int dx, int dz) {
        double angle = Math.atan2(dz, dx) + Math.PI;
        return Math.min(SECTOR_COUNT - 1, (int) (angle / (Math.PI * 2.0D) * SECTOR_COUNT));
    }

    private static int getSurface(ServerLevel level, int x, int z) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    private static double getAverageSurroundingSurface(ServerLevel level, int x, int z) {
        double total = 0;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            total += getSurface(level, x + direction.getStepX() * PROMINENCE_DISTANCE, z + direction.getStepZ() * PROMINENCE_DISTANCE);
        }
        return total / 4.0D;
    }

    public static boolean canStandAt(ServerLevel level, BlockPos spot) {
        BlockPos below = spot.below();
        BlockState ground = level.getBlockState(below);
        if (ground.getBlock() instanceof TentBlock || ground.is(BlockTags.CAMPFIRES) || !ground.getFluidState().isEmpty()) return false;
        if (ground.is(BlockTags.FENCES) || ground.is(BlockTags.WALLS) || ground.getBlock() instanceof LadderBlock) return false;
        if (ground.getCollisionShape(level, below).isEmpty()) return false;
        return level.getBlockState(spot).getCollisionShape(level, spot).isEmpty() && level.getBlockState(spot.above()).getCollisionShape(level, spot.above()).isEmpty();
    }

    private record ScoredSpot(BlockPos pos, double score) {
    }
}
