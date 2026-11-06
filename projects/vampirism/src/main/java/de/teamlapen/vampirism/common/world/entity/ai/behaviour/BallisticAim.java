package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.world.entity.QuarrelEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class BallisticAim {

    private static final double ARROW_GRAVITY = 0.05D;
    private static final double AIR_INERTIA = 0.99D;
    private static final int MAX_FLIGHT_TICKS = 100;
    private static final double MIN_PITCH = Math.toRadians(-80.0D);
    private static final double MAX_PITCH = Math.toRadians(85.0D);
    private static final double PITCH_STEP = Math.toRadians(2.5D);
    private static final int REFINE_STEPS = 12;
    private static final int LEAD_ITERATIONS = 3;
    private static final double TARGET_HEIGHT_FRACTION = 0.5D;
    private static final double CLEAR_MARGIN = 0.75D;

    public static Vec3 calculateShotDirection(Level level, Entity projectile, Vec3 origin, LivingEntity target, double speed, float gravityFactor) {
        Vec3 aimPoint = target.position().add(0, target.getBbHeight() * TARGET_HEIGHT_FRACTION, 0);
        Vec3 velocity = target.getDeltaMovement();
        Vec3 lead = new Vec3(velocity.x, target.onGround() ? 0 : velocity.y, velocity.z);

        Shot best = null;
        for (boolean lob : new boolean[]{false, true}) {
            Shot shot = solveLeadingShot(origin, aimPoint, lead, speed, gravityFactor, lob);
            if (shot == null) continue;

            shot = shot.withClearPath(isTrajectoryClear(level, projectile, origin, shot, speed, gravityFactor));
            if (best == null || shot.isBetterThan(best)) best = shot;
        }

        if (best != null) return best.direction();

        return findClosestReachDirection(origin, aimPoint, speed, gravityFactor);
    }

    private static @Nullable Shot solveLeadingShot(Vec3 origin, Vec3 aimPoint, Vec3 lead, double speed, float gravityFactor, boolean lob) {
        Vec3 point = aimPoint;
        Shot shot = null;
        for (int i = 0; i < LEAD_ITERATIONS; i++) {
            Shot next = findShotTowards(origin, point, speed, gravityFactor, lob);
            if (next == null) return shot;

            shot = next;
            point = aimPoint.add(lead.scale(shot.ticks()));
        }

        return shot;
    }

    private static @Nullable Shot findShotTowards(Vec3 origin, Vec3 point, double speed, float gravityFactor, boolean lob) {
        Vec3 delta = point.subtract(origin);
        double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double height = delta.y;

        double previousPitch = MIN_PITCH;
        double previousError = calculateVerticalMiss(distance, height, previousPitch, speed, gravityFactor);
        for (double pitch = MIN_PITCH + PITCH_STEP; pitch <= MAX_PITCH; pitch += PITCH_STEP) {
            double error = calculateVerticalMiss(distance, height, pitch, speed, gravityFactor);
            boolean crossing = lob ? previousError > 0 && error <= 0 : previousError < 0 && error >= 0;
            if (!Double.isNaN(previousError) && !Double.isNaN(error) && crossing) {
                double root = refineLaunchPitch(distance, height, previousPitch, pitch, speed, gravityFactor, lob);
                return new Shot(createLaunchDirection(delta, root), calculateFlightTicks(distance, root, speed, gravityFactor), false);
            }

            previousPitch = pitch;
            previousError = error;
        }

        return null;
    }

    private static double refineLaunchPitch(double distance, double height, double low, double high, double speed, float gravityFactor, boolean lob) {
        for (int i = 0; i < REFINE_STEPS; i++) {
            double mid = (low + high) * 0.5D;
            double error = calculateVerticalMiss(distance, height, mid, speed, gravityFactor);
            boolean belowRoot = lob ? error > 0 : error < 0;
            if (belowRoot) {
                low = mid;
            } else {
                high = mid;
            }
        }

        return (low + high) * 0.5D;
    }

    private static Vec3 findClosestReachDirection(Vec3 origin, Vec3 aimPoint, double speed, float gravityFactor) {
        Vec3 delta = aimPoint.subtract(origin);
        double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        double bestPitch = 0;
        double bestError = Double.POSITIVE_INFINITY;
        for (double pitch = MIN_PITCH; pitch <= MAX_PITCH; pitch += PITCH_STEP) {
            double error = Math.abs(calculateVerticalMiss(distance, delta.y, pitch, speed, gravityFactor));
            if (error < bestError) {
                bestError = error;
                bestPitch = pitch;
            }
        }

        return createLaunchDirection(delta, bestPitch);
    }

    private static double calculateVerticalMiss(double distance, double height, double pitch, double speed, float gravityFactor) {
        double horizontal = 0;
        double vertical = 0;
        double horizontalSpeed = speed * Math.cos(pitch);
        double verticalSpeed = speed * Math.sin(pitch);
        for (int tick = 0; tick < MAX_FLIGHT_TICKS; tick++) {
            double nextHorizontal = horizontal + horizontalSpeed;
            double nextVertical = vertical + verticalSpeed;
            if (nextHorizontal >= distance) {
                double progress = horizontalSpeed > 0 ? (distance - horizontal) / horizontalSpeed : 0;
                return vertical + (nextVertical - vertical) * progress - height;
            }
            horizontal = nextHorizontal;
            vertical = nextVertical;
            horizontalSpeed *= AIR_INERTIA;
            verticalSpeed = verticalSpeed * AIR_INERTIA - calculateGravity(gravityFactor, horizontalSpeed);
        }

        return Double.NaN;
    }

    private static double calculateFlightTicks(double distance, double pitch, double speed, float gravityFactor) {
        double horizontal = 0;
        double horizontalSpeed = speed * Math.cos(pitch);
        for (int tick = 0; tick < MAX_FLIGHT_TICKS; tick++) {
            if (horizontal + horizontalSpeed >= distance) {
                return tick + (horizontalSpeed > 0 ? (distance - horizontal) / horizontalSpeed : 0);
            }
            horizontal += horizontalSpeed;
            horizontalSpeed *= AIR_INERTIA;
        }

        return MAX_FLIGHT_TICKS;
    }

    private static boolean isTrajectoryClear(Level level, Entity projectile, Vec3 origin, Shot shot, double speed, float gravityFactor) {
        Vec3 position = origin;
        Vec3 velocity = shot.direction().scale(speed);
        int ticks = (int) Math.ceil(shot.ticks());
        for (int tick = 0; tick < ticks; tick++) {
            Vec3 next = position.add(velocity);
            boolean lastStep = tick == ticks - 1;
            HitResult hit = level.clip(new ClipContext(position, next, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, projectile));
            if (hit.getType() != HitResult.Type.MISS) {
                return lastStep && hit.getLocation().distanceTo(next) < CLEAR_MARGIN;
            }
            position = next;
            velocity = velocity.scale(AIR_INERTIA);
            velocity = velocity.subtract(0, calculateGravity(gravityFactor, velocity.horizontalDistance()), 0);
        }

        return true;
    }

    private static double calculateGravity(float gravityFactor, double horizontalSpeed) {
        return ARROW_GRAVITY * QuarrelEntity.effectiveGravityFactor(gravityFactor, horizontalSpeed);
    }

    private static Vec3 createLaunchDirection(Vec3 delta, double pitch) {
        Vec3 horizontal = new Vec3(delta.x, 0, delta.z);
        horizontal = horizontal.lengthSqr() < 1.0E-8 ? new Vec3(1, 0, 0) : horizontal.normalize();
        return horizontal.scale(Math.cos(pitch)).add(0, Math.sin(pitch), 0);
    }

    private record Shot(Vec3 direction, double ticks, boolean clear) {

        Shot withClearPath(boolean clear) {
            return new Shot(this.direction, this.ticks, clear);
        }

        boolean isBetterThan(Shot other) {
            if (this.clear != other.clear) return this.clear;
            return this.ticks < other.ticks;
        }
    }
}
