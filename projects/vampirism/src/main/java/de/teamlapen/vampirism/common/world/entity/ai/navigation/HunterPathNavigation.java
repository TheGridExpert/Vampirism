package de.teamlapen.vampirism.common.world.entity.ai.navigation;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterMoveControl;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.util.Set;

public class HunterPathNavigation extends GroundPathNavigation {

    private static final int PROBE_RADIUS_OFFSET = 16;
    private static final double MIN_PROGRESS = 2.0D;
    private static final double LADDER_NODE_TOLERANCE = 0.25D;
    private static final double EDGE_NODE_TOLERANCE = 0.25D;
    private static final double FALL_TOLERANCE = 0.5D;

    private PathFinder probeFinder;
    private @Nullable Path dropPath;

    public HunterPathNavigation(Mob mob, Level level) {
        super(mob, level);
    }

    @Override
    protected @NotNull PathFinder createPathFinder(int pMaxVisitedNodes) {
        this.nodeEvaluator = new HunterNodeEvaluator();
        this.nodeEvaluator.setCanPassDoors(true);
        this.probeFinder = new PathFinder(this.nodeEvaluator, pMaxVisitedNodes);
        return this.probeFinder;
    }

    @Override
    protected boolean canUpdatePath() {
        return super.canUpdatePath() || isOnLadder();
    }

    public boolean isFollowingDropPath() {
        return this.path != null && this.path == this.dropPath;
    }

    @Override
    public void tick() {
        if (!isDone() && hasFallenUnexpectedly()) stop();
        super.tick();
    }

    private boolean hasFallenUnexpectedly() {
        if (this.mob.onGround() || this.mob.onClimbable() || this.mob.isInLiquid() || isFollowingDropPath()) return false;
        return this.mob.fallDistance > this.mob.getMaxFallDistance() + FALL_TOLERANCE;
    }

    @Override
    protected void followThePath() {
        if (this.path == null) return;
        if (isOnLadder()) {
            followLadder();
        } else if (isNearDrop()) {
            followPrecisely();
        } else {
            super.followThePath();
        }
    }

    private void followLadder() {
        BlockPos feet = this.mob.blockPosition();
        while (!this.path.isDone()) {
            Node next = this.path.getNextNode();
            boolean inColumn = next.x == feet.getX() && next.z == feet.getZ();
            if (!inColumn || Math.abs(this.mob.getY() - next.y) >= LADDER_NODE_TOLERANCE) break;
            this.path.advance();
        }
        this.doStuckDetection(this.getTempMobPos());
    }

    private void followPrecisely() {
        Vec3 mobPos = this.getTempMobPos();
        Node next = this.path.getNextNode();
        double dx = Math.abs(this.mob.getX() - (next.x + 0.5D));
        double dz = Math.abs(this.mob.getZ() - (next.z + 0.5D));
        if (dx <= EDGE_NODE_TOLERANCE && dz <= EDGE_NODE_TOLERANCE && Math.abs(this.mob.getY() - next.y) < 1.0D) {
            this.path.advance();
        }
        this.doStuckDetection(mobPos);
    }

    private boolean isNearDrop() {
        if (!(this.mob instanceof Hunter hunter) || isFollowingDropPath()) return false;
        BlockPos feet = this.mob.blockPosition();
        int maxFall = hunter.getMaxFallDistance();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (!HunterMoveControl.hasSupport(this.level, feet.getX() + direction.getStepX(), feet.getY(), feet.getZ() + direction.getStepZ(), maxFall)) return true;
        }
        return false;
    }

    private boolean isOnLadder() {
        BlockPos feet = this.mob.blockPosition();
        return this.level.getBlockState(feet).getBlock() instanceof LadderBlock || this.level.getBlockState(feet.below()).getBlock() instanceof LadderBlock;
    }

    @Override
    protected @Nullable Path createPath(Set<BlockPos> targets, int radiusOffset, boolean above, int reachRange, float maxPathLength) {
        Path path = super.createPath(targets, radiusOffset, above, reachRange, maxPathLength);
        if (!(this.mob instanceof Hunter hunter) || !hunter.isAvoidingDrops() || !isStranded(hunter, path, targets)) return path;

        hunter.setAllowingDrops(true);
        try {
            Path fallback = super.createPath(targets, radiusOffset, above, reachRange, maxPathLength);
            if (fallback == null) return path;
            this.dropPath = fallback;
            return fallback;
        } finally {
            hunter.setAllowingDrops(false);
        }
    }

    private boolean isStranded(Hunter hunter, @Nullable Path path, Set<BlockPos> targets) {
        PatrolZone zone = hunter.getPatrolZone();
        if (zone != null && zone.size() > 1) return !zone.contains(hunter.blockPosition()) && !makesProgress(path, targets);
        return !makesProgress(path, targets);
    }

    private boolean makesProgress(@Nullable Path path, Set<BlockPos> targets) {
        if (path == null) return false;
        if (path.canReach()) return true;

        Node end = path.getEndNode();
        if (end == null) return false;
        BlockPos start = this.mob.blockPosition();
        return targets.stream().anyMatch(target -> Math.sqrt(end.asBlockPos().distSqr(target)) + MIN_PROGRESS <= Math.sqrt(start.distSqr(target)));
    }

    public boolean canReach(Entity target, int reachRange) {
        if (this.mob.getY() < this.level.getMinY()) return false;
        if (!this.canUpdatePath()) return true;

        float maxPathLength = (float) this.mob.getAttributeValue(Attributes.FOLLOW_RANGE);
        BlockPos from = this.mob.blockPosition().above();
        int radius = (int) (maxPathLength + PROBE_RADIUS_OFFSET);
        PathNavigationRegion region = new PathNavigationRegion(this.level, from.offset(-radius, -radius, -radius), from.offset(radius, radius, radius));
        Path path = this.probeFinder.findPath(region, this.mob, Set.of(target.blockPosition()), maxPathLength, reachRange, 1.0F);
        return path != null && path.canReach();
    }
}
