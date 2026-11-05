package de.teamlapen.vampirism.common.world.entity.hunter;

import net.minecraft.core.BlockPos;

public record HunterPost(BlockPos center, int radius) {

    public boolean contains(BlockPos pos, double margin) {
        double reach = this.radius + margin;
        return this.center.distToCenterSqr(pos.getX() + 0.5, this.center.getY() + 0.5, pos.getZ() + 0.5) <= reach * reach;
    }
}
