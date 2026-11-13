package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.LookoutSpots;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class FishingSpots {

    private static final int CAST_DISTANCE = 3;
    private static final int MAX_BANK_HEIGHT = 2;
    private static final int OPEN_WATER_RADIUS = 2;
    private static final int MIN_OPEN_WATER = 12;
    private static final double DISTANCE_PENALTY = 0.1D;
    private static final double SCORE_JITTER = 3.0D;
    private static final int MIN_ANCHOR_SPACING = 6;
    private static final int MAX_PATH_CHECKS = 4;
    private static final double CLAIM_DISTANCE = 2.0D;
    private static final double GROUP_RADIUS = 6.0D;
    private static final int MIN_NEIGHBOUR_SPACING = 2;
    private static final int ICE_INTERIOR_RADIUS = 3;
    private static final double GROUP_SEPARATION = 8.0D;
    private static final float SMALLER_HOLE_CHANCE = 0.3F;
    private static final float BIGGER_HOLE_CHANCE = 0.4F;

    public static List<FishingSpot> findGroup(ServerLevel level, Hunter scout, int count) {
        PatrolArea area = scout.getPatrolArea();
        if (area == null || count <= 0) return List.of();

        RandomSource random = scout.getRandom();
        List<ScoredSpot> shore = new ArrayList<>();
        List<BlockPos> ice = new ArrayList<>();
        forEachStand(level, area, scout.getPatrolZone(), stand -> {
            evaluateShore(level, area, stand, random, shore);
            if (isFishableIce(level, stand.below())) ice.add(stand.below());
        });

        if (!shore.isEmpty()) return groupOnShore(level, scout, shore, count);
        if (!ice.isEmpty() && isCold(level, area.center())) return groupOnIce(level, scout, area, ice, count);
        return List.of();
    }

    private static void forEachStand(ServerLevel level, PatrolArea area, @Nullable PatrolZone zone, Consumer<BlockPos> visitor) {
        if (zone != null) {
            zone.forEachStandableColumn((x, surface, z) -> visitor.accept(new BlockPos(x, surface, z)));
            return;
        }

        BlockPos center = area.center();
        int radius = area.radius();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx * dx + dz * dz > radius * radius) continue;
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                if (!level.hasChunkAt(new BlockPos(x, center.getY(), z))) continue;
                visitor.accept(new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z));
            }
        }
    }

    private static List<FishingSpot> groupOnShore(ServerLevel level, Hunter scout, List<ScoredSpot> shore, int count) {
        shore.sort(Comparator.comparingDouble(ScoredSpot::score).reversed());

        List<FishingSpot> checked = new ArrayList<>();
        for (ScoredSpot candidate : shore) {
            if (checked.size() >= MAX_PATH_CHECKS) break;
            FishingSpot anchor = candidate.spot();
            if (checked.stream().anyMatch(other -> other.stand().closerThan(anchor.stand(), MIN_ANCHOR_SPACING))) continue;
            if (isClaimed(level, scout, anchor.stand())) continue;

            checked.add(anchor);
            if (!canReach(scout, anchor.stand())) continue;

            List<FishingSpot> group = new ArrayList<>(List.of(anchor));
            shore.stream()
                    .map(ScoredSpot::spot)
                    .filter(spot -> spot.stand().closerThan(anchor.stand(), GROUP_RADIUS))
                    .sorted(Comparator.comparingDouble(spot -> spot.stand().distSqr(anchor.stand())))
                    .forEach(spot -> {
                        if (group.size() >= count) return;
                        if (group.stream().anyMatch(other -> other.stand().closerThan(spot.stand(), MIN_NEIGHBOUR_SPACING) || other.water().equals(spot.water()))) return;
                        if (!isClaimed(level, scout, spot.stand())) group.add(spot);
                    });
            return group;
        }
        return List.of();
    }

    private static List<FishingSpot> groupOnIce(ServerLevel level, Hunter scout, PatrolArea area, List<BlockPos> ice, int count) {
        Set<BlockPos> sheet = new HashSet<>(ice);
        RandomSource random = scout.getRandom();
        List<BlockPos> middles = ice.stream()
                .sorted(Comparator.comparingDouble((BlockPos pos) -> countNearby(sheet, pos) - Math.sqrt(pos.distSqr(area.center())) * DISTANCE_PENALTY).reversed())
                .toList();

        List<BlockPos> checked = new ArrayList<>();
        for (BlockPos middle : middles) {
            if (checked.size() >= MAX_PATH_CHECKS) break;
            if (checked.stream().anyMatch(other -> other.closerThan(middle, MIN_ANCHOR_SPACING))) continue;
            if (isClaimed(level, scout, middle.above(), GROUP_SEPARATION)) continue;

            checked.add(middle);
            if (!canReach(scout, middle.above())) continue;

            List<BlockPos> hole = shapeHole(sheet, middle, random);
            List<BlockPos> rim = new ArrayList<>(hole.stream()
                    .flatMap(pos -> Direction.Plane.HORIZONTAL.stream().map(pos::relative))
                    .filter(pos -> sheet.contains(pos) && !hole.contains(pos) && LookoutSpots.canStandAt(level, pos.above()))
                    .distinct()
                    .toList());
            Util.shuffle(rim, random);

            List<FishingSpot> group = new ArrayList<>();
            for (BlockPos footing : rim) {
                if (group.size() >= count) break;
                BlockPos stand = footing.above();
                if (group.stream().anyMatch(other -> other.stand().closerThan(stand, MIN_NEIGHBOUR_SPACING))) continue;

                BlockPos water = hole.stream().min(Comparator.comparingDouble(pos -> pos.distSqr(footing))).orElseThrow();
                group.add(new FishingSpot(stand, water, hole));
            }
            if (!group.isEmpty()) return group;
        }
        return List.of();
    }

    private static List<BlockPos> shapeHole(Set<BlockPos> sheet, BlockPos middle, RandomSource random) {
        int stepX = random.nextBoolean() ? 1 : -1;
        int stepZ = random.nextBoolean() ? 1 : -1;
        List<BlockPos> hole = new ArrayList<>(List.of(middle, middle.offset(stepX, 0, 0), middle.offset(0, 0, stepZ), middle.offset(stepX, 0, stepZ)));

        float shape = random.nextFloat();
        if (shape < SMALLER_HOLE_CHANCE) {
            hole.remove(1 + random.nextInt(hole.size() - 1));
        } else if (shape < SMALLER_HOLE_CHANCE + BIGGER_HOLE_CHANCE) {
            List<BlockPos> bulges = hole.stream()
                    .flatMap(pos -> Direction.Plane.HORIZONTAL.stream().map(pos::relative))
                    .filter(pos -> !hole.contains(pos))
                    .distinct()
                    .toList();
            hole.add(bulges.get(random.nextInt(bulges.size())));
        }
        hole.removeIf(pos -> !sheet.contains(pos));
        return List.copyOf(hole);
    }

    private static int countNearby(Set<BlockPos> sheet, BlockPos pos) {
        int count = 0;
        for (int dx = -ICE_INTERIOR_RADIUS; dx <= ICE_INTERIOR_RADIUS; dx++) {
            for (int dz = -ICE_INTERIOR_RADIUS; dz <= ICE_INTERIOR_RADIUS; dz++) {
                if (sheet.contains(pos.offset(dx, 0, dz))) count++;
            }
        }
        return count;
    }

    private static void evaluateShore(ServerLevel level, PatrolArea area, BlockPos stand, RandomSource random, List<ScoredSpot> scored) {
        if (!LookoutSpots.canStandAt(level, stand)) return;

        double dx = stand.getX() - area.center().getX();
        double dz = stand.getZ() - area.center().getZ();
        double distance = Math.sqrt(dx * dx + dz * dz);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos water = findCastTarget(level, stand, direction);
            if (water == null) continue;

            int openWater = countOpenWater(level, water);
            if (openWater < MIN_OPEN_WATER) continue;
            scored.add(new ScoredSpot(new FishingSpot(stand, water, List.of()),openWater - distance * DISTANCE_PENALTY + random.nextDouble() * SCORE_JITTER));
        }
    }

    private static @Nullable BlockPos findCastTarget(ServerLevel level, BlockPos stand, Direction direction) {
        BlockPos edge = stand.relative(direction);
        if (!level.getBlockState(edge).getCollisionShape(level, edge).isEmpty()) return null;
        for (int drop = 1; drop <= MAX_BANK_HEIGHT; drop++) {
            BlockPos edgeWater = edge.below(drop);
            if (!isOpenWater(level, edgeWater)) {
                if (!level.getBlockState(edgeWater).getCollisionShape(level, edgeWater).isEmpty()) return null;
                continue;
            }
            BlockPos target = stand.relative(direction, CAST_DISTANCE).below(drop);
            return isOpenWater(level, target) ? target : null;
        }
        return null;
    }

    private static int countOpenWater(ServerLevel level, BlockPos water) {
        int count = 0;
        for (int dx = -OPEN_WATER_RADIUS; dx <= OPEN_WATER_RADIUS; dx++) {
            for (int dz = -OPEN_WATER_RADIUS; dz <= OPEN_WATER_RADIUS; dz++) {
                if (isOpenWater(level, water.offset(dx, 0, dz))) count++;
            }
        }
        return count;
    }

    public static boolean isOpenWater(ServerLevel level, BlockPos pos) {
        return level.getFluidState(pos).isSourceOfType(Fluids.WATER) && level.getBlockState(pos.above()).isAir();
    }

    public static boolean isFishableIce(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).is(Blocks.ICE) && level.getFluidState(pos.below()).is(FluidTags.WATER) && level.getBlockState(pos.above()).isAir();
    }

    private static boolean isCold(ServerLevel level, BlockPos pos) {
        return level.getBiome(pos).value().coldEnoughToSnow(pos, level.getSeaLevel());
    }

    private static boolean canReach(Hunter scout, BlockPos stand) {
        Path path = scout.getNavigation().createPath(stand, 0);
        return path != null && path.canReach();
    }

    private static boolean isClaimed(ServerLevel level, Hunter scout, BlockPos stand) {
        return isClaimed(level, scout, stand, CLAIM_DISTANCE);
    }

    private static boolean isClaimed(ServerLevel level, Hunter scout, BlockPos stand, double distance) {
        AABB box = new AABB(stand).inflate(distance);
        return !level.getEntitiesOfClass(Hunter.class, box, other -> other != scout && other.getBrain().getMemory(ModMemoryTypes.FISHING_SPOT.get())
                .map(claimed -> claimed.stand().closerThan(stand, distance))
                .orElse(false)).isEmpty();
    }

    private record ScoredSpot(FishingSpot spot, double score) {
    }
}
