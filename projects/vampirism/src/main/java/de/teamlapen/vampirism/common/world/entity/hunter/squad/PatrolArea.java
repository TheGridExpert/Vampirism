package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public record PatrolArea(BlockPos center, int radius, double startAngle, double sweep) {

    public static final double FULL_CIRCLE = Math.PI * 2.0D;
    private static final double MIN_DISTANCE = 2.0D;

    public static PatrolArea circle(BlockPos center, int radius) {
        return new PatrolArea(center, radius, 0.0D, FULL_CIRCLE);
    }

    public BlockPos sample(RandomSource random) {
        double angle = this.startAngle + random.nextDouble() * this.sweep;
        double distance = MIN_DISTANCE + Math.sqrt(random.nextDouble()) * Math.max(0.0D, this.radius - MIN_DISTANCE);
        return new BlockPos(this.center.getX() + Mth.floor(Math.cos(angle) * distance), this.center.getY(), this.center.getZ() + Mth.floor(Math.sin(angle) * distance));
    }
}
