package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record FishingSpot(BlockPos stand, BlockPos water, List<BlockPos> hole) {

    private static final double SURFACE_OFFSET = 0.9D;

    public Vec3 surface() {
        return new Vec3(this.water.getX() + 0.5D, this.water.getY() + SURFACE_OFFSET, this.water.getZ() + 0.5D);
    }
}
