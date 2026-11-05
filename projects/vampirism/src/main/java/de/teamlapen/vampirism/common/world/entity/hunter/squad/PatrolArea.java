package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record PatrolArea(BlockPos center, int radius) {

    private static final double MIN_DISTANCE = 2.0D;

    public BlockPos sample(RandomSource random) {
        double angle = random.nextDouble() * Math.PI * 2.0D;
        double distance = MIN_DISTANCE + Math.sqrt(random.nextDouble()) * Math.max(0.0D, this.radius - MIN_DISTANCE);
        return new BlockPos(this.center.getX() + Mth.floor(Math.cos(angle) * distance), this.center.getY(), this.center.getZ() + Mth.floor(Math.sin(angle) * distance));
    }
}
