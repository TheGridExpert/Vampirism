package de.teamlapen.vampirism.common.world.entity.ai.navigation;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

public class HunterNodeEvaluator extends WalkNodeEvaluator {

    private static final float FORD_MALUS = 1.0F;

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
        if (node != null && node.type == PathType.WALKABLE && this.currentContext.level().getFluidState(node.asBlockPos()).is(FluidTags.WATER)) {
            node.costMalus = Math.max(node.costMalus, FORD_MALUS);
        }
        return node;
    }
}
