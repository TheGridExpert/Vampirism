package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public class PotionAim {

    private static final double GRAVITY = 0.05D;
    private static final double INERTIA = 0.99D;
    private static final int MAX_FLIGHT_TICKS = 100;
    private static final double MIN_PITCH = Math.toRadians(-60.0D);
    private static final double MAX_PITCH = Math.toRadians(80.0D);
    private static final double PITCH_STEP = Math.toRadians(2.0D);
    private static final int REFINE_STEPS = 14;
    private static final int LEAD_ITERATIONS = 4;
    private static final double TARGET_HEIGHT_FRACTION = 0.4D;

    public static Vec3 calculateThrowDirection(Vec3 origin, LivingEntity target, double speed) {
        Vec3 aimPoint = target.position().add(0, target.getBbHeight() * TARGET_HEIGHT_FRACTION, 0);
        Vec3 velocity = target.position().subtract(target.xo, target.yo, target.zo);
        Vec3 lead = new Vec3(velocity.x, target.onGround() ? 0 : velocity.y, velocity.z);

        Vec3 point = aimPoint;
        Throw best = solve(origin, point, speed);
        for (int i = 0; i < LEAD_ITERATIONS; i++) {
            point = aimPoint.add(lead.scale(best.ticks()));
            best = solve(origin, point, speed);
        }
        return best.direction();
    }

    private static Throw solve(Vec3 origin, Vec3 point, double speed) {
        Vec3 delta = point.subtract(origin);
        double distance = Math.sqrt(delta.x * delta.x + delta.z * delta.z);

        double previousPitch = MIN_PITCH;
        Flight previous = simulate(distance, previousPitch, speed);
        Flight bestReach = previous;
        double bestReachPitch = previousPitch;
        for (double pitch = MIN_PITCH + PITCH_STEP; pitch <= MAX_PITCH; pitch += PITCH_STEP) {
            Flight flight = simulate(distance, pitch, speed);
            if (previous.reached() && flight.reached() && previous.height() < delta.y && flight.height() >= delta.y) {
                double root = refine(distance, delta.y, previousPitch, pitch, speed);
                return new Throw(createDirection(delta, root), simulate(distance, root, speed).ticks());
            }
            if (flight.reached() && (!bestReach.reached() || Math.abs(flight.height() - delta.y) < Math.abs(bestReach.height() - delta.y))) {
                bestReach = flight;
                bestReachPitch = pitch;
            }
            previous = flight;
            previousPitch = pitch;
        }

        double fallbackPitch = bestReach.reached() ? bestReachPitch : Math.toRadians(45.0D);
        return new Throw(createDirection(delta, fallbackPitch), bestReach.ticks());
    }

    private static double refine(double distance, double height, double low, double high, double speed) {
        for (int i = 0; i < REFINE_STEPS; i++) {
            double mid = (low + high) * 0.5D;
            Flight flight = simulate(distance, mid, speed);
            if (flight.reached() && flight.height() >= height) {
                high = mid;
            } else {
                low = mid;
            }
        }
        return (low + high) * 0.5D;
    }

    private static Flight simulate(double distance, double pitch, double speed) {
        double horizontal = 0;
        double vertical = 0;
        double horizontalSpeed = speed * Math.cos(pitch);
        double verticalSpeed = speed * Math.sin(pitch);
        for (int tick = 0; tick < MAX_FLIGHT_TICKS; tick++) {
            verticalSpeed = (verticalSpeed - GRAVITY) * INERTIA;
            horizontalSpeed *= INERTIA;
            double nextHorizontal = horizontal + horizontalSpeed;
            double nextVertical = vertical + verticalSpeed;
            if (nextHorizontal >= distance) {
                double progress = horizontalSpeed > 0 ? (distance - horizontal) / horizontalSpeed : 0;
                return new Flight(true, vertical + (nextVertical - vertical) * progress, tick + progress);
            }
            horizontal = nextHorizontal;
            vertical = nextVertical;
        }
        return new Flight(false, vertical, MAX_FLIGHT_TICKS);
    }

    private static Vec3 createDirection(Vec3 delta, double pitch) {
        Vec3 horizontal = new Vec3(delta.x, 0, delta.z);
        horizontal = horizontal.lengthSqr() < 1.0E-8 ? new Vec3(1, 0, 0) : horizontal.normalize();
        return horizontal.scale(Math.cos(pitch)).add(0, Math.sin(pitch), 0);
    }

    private record Flight(boolean reached, double height, double ticks) {
    }

    private record Throw(Vec3 direction, double ticks) {
    }
}
