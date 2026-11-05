package de.teamlapen.vampirism.common.world.entity.ai.navigation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HunterPathNavigation extends GroundPathNavigation {

    private static final int PROBE_RADIUS_OFFSET = 16;

    private PathFinder probeFinder;

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
