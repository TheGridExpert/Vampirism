package de.teamlapen.vampirism.common.world.entity.ai.navigation;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class HunterNodeEvaluator extends WalkNodeEvaluator {

    private static final float FORD_MALUS = 1.0F;
    private static final float CAVE_MALUS = 4.0F;
    private static final int MIN_CAVE_DEPTH = 4;
    private static final float DROP_MALUS = 2.0F;
    private static final float CLIMB_MALUS = 0.5F;

    @Override
    public @NotNull PathType getPathTypeOfMob(@NotNull PathfindingContext context, int x, int y, int z, @NotNull Mob mob) {
        PathType pathType = super.getPathTypeOfMob(context, x, y, z, mob);

        BlockState blockState = context.level().getBlockState(new BlockPos(x, y, z));
        if (blockState.getBlock() instanceof FenceGateBlock) {
            boolean isOpen = blockState.getValue(FenceGateBlock.OPEN);
            if (isOpen) {
                return PathType.WALKABLE_DOOR;
            } else {
                return this.canOpenDoors() ? PathType.WALKABLE_DOOR : PathType.DOOR_WOOD_CLOSED;
            }
        }

        if (pathType == PathType.WATER && mob instanceof Hunter && Hunter.isShallowWater(mob.level(), new BlockPos(x, y + 1, z))) {
            return PathType.WALKABLE;
        }

        return pathType;
    }

    @Override
    public @NotNull PathType getPathType(@NotNull PathfindingContext context, int x, int y, int z) {
        PathType pathType = super.getPathType(context, x, y, z);

        BlockState blockState = context.level().getBlockState(new BlockPos(x, y, z));
        if (blockState.getBlock() instanceof FenceGateBlock) {
            boolean isOpen = blockState.getValue(FenceGateBlock.OPEN);
            return isOpen ? PathType.WALKABLE_DOOR : PathType.DOOR_WOOD_CLOSED;
        }

        if (pathType == PathType.WATER && this.mob instanceof Hunter && Hunter.isShallowWater(this.mob.level(), new BlockPos(x, y + 1, z))) {
            return PathType.WALKABLE;
        }

        return pathType;
    }

    @Override
    protected @Nullable Node findAcceptedNode(int x, int y, int z, int jumpSize, double nodeHeight, Direction travelDirection, PathType blockPathTypeCurrent) {
        Node node = super.findAcceptedNode(x, y, z, jumpSize, nodeHeight, travelDirection, blockPathTypeCurrent);
        if (node == null || node.costMalus < 0.0F || node.y < y - 1) {
            if (isLadder(x, y, z)) return createClimbNode(x, y, z);
            if (isLadder(x, y - 1, z)) return createClimbNode(x, y - 1, z);
        }
        if (node == null) return null;

        if (node.type == PathType.WALKABLE && this.currentContext.level().getFluidState(node.asBlockPos()).is(FluidTags.WATER)) {
            node.costMalus = Math.max(node.costMalus, FORD_MALUS);
        }
        int drop = y - node.y - 1;
        if (drop > 0) {
            node.costMalus = Math.max(node.costMalus, drop * DROP_MALUS);
        }
        if (isUnderground(node.asBlockPos())) {
            node.costMalus = Math.max(node.costMalus, CAVE_MALUS);
        }
        return node;
    }

    @Override
    public Node getStart() {
        BlockPos feet = this.mob.blockPosition();
        if (this.mob.onClimbable() && isLadder(feet.getX(), feet.getY(), feet.getZ())) return this.getStartNode(feet);
        return super.getStart();
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node node) {
        int count = super.getNeighbors(neighbors, node);
        if (!isLadder(node.x, node.y, node.z)) return count;

        if (count < neighbors.length && isLadder(node.x, node.y + 1, node.z) && hasBodySpace(node.x, node.y + 1, node.z)) {
            count = addIfValid(neighbors, count, createClimbNode(node.x, node.y + 1, node.z), node);
        }
        if (count < neighbors.length && (isLadder(node.x, node.y - 1, node.z) || isStandable(node.x, node.y - 1, node.z))) {
            count = addIfValid(neighbors, count, createClimbNode(node.x, node.y - 1, node.z), node);
        }
        return count;
    }

    private int addIfValid(Node[] neighbors, int count, Node candidate, Node current) {
        if (isNeighborValid(candidate, current)) {
            neighbors[count++] = candidate;
        }
        return count;
    }

    private Node createClimbNode(int x, int y, int z) {
        Node node = this.getNode(x, y, z);
        node.type = PathType.WALKABLE;
        node.costMalus = Math.max(node.costMalus, CLIMB_MALUS);
        return node;
    }

    private boolean isLadder(int x, int y, int z) {
        return this.currentContext.level().getBlockState(new BlockPos(x, y, z)).getBlock() instanceof LadderBlock;
    }

    private boolean hasBodySpace(int x, int y, int z) {
        BlockPos feet = new BlockPos(x, y, z);
        return this.currentContext.level().getBlockState(feet).getCollisionShape(this.currentContext.level(), feet).isEmpty() || isLadder(x, y, z);
    }

    private boolean isStandable(int x, int y, int z) {
        BlockPos below = new BlockPos(x, y - 1, z);
        return hasBodySpace(x, y, z) && hasBodySpace(x, y + 1, z) && this.currentContext.level().getBlockState(below).isFaceSturdy(this.currentContext.level(), below, Direction.UP);
    }

    private boolean isUnderground(BlockPos pos) {
        Level level = this.mob.level();
        if (!level.dimensionType().hasSkyLight() || level.getBrightness(LightLayer.SKY, pos) > 0) return false;
        return pos.getY() < level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ()) - MIN_CAVE_DEPTH;
    }
}
