package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterThreats;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class RetreatFromEnemies {

    private static final double DANGER_RADIUS = 8.0D;
    private static final double AWARENESS_RADIUS = 24.0D;
    private static final double ALLY_RADIUS = 6.0D;
    private static final double ALLY_BONUS = 3.0D;
    private static final double ROUTE_CLEARANCE = 6.0D;
    private static final double ROUTE_PENALTY = 3.0D;
    private static final int FLEE_RANGE = 8;
    private static final int VERTICAL_RANGE = 7;
    private static final int DIRECTIONS = 16;
    private static final int REPLAN_COOLDOWN = 10;

    public static OneShot<Hunter> create(float speedModifier, double minFleeDistance, double safeDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES),
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()),
                instance.registered(ModMemoryTypes.REPOSITIONING_COOLDOWN.get())
        ).apply(instance, (nearbyAcc, walkTargetAcc, hostilesAcc, alliesAcc, cooldownAcc) -> (_, hunter, _) -> {
            List<LivingEntity> nearby = instance.get(nearbyAcc);
            if (!hasPursuers(hunter, nearby, safeDistance)) return false;

            List<LivingEntity> hostiles = collectHostiles(hunter, nearby, instance.tryGet(hostilesAcc).orElse(List.of()));
            WalkTarget current = instance.tryGet(walkTargetAcc).orElse(null);
            if (current != null && (instance.tryGet(cooldownAcc).isPresent() || isRouteSafe(hunter.position(), current.getTarget().currentPosition(), hostiles))) return false;

            List<LivingEntity> allies = instance.tryGet(alliesAcc).orElse(List.of());
            Vec3 destination = findSafestDestination(hunter, hostiles, allies, minFleeDistance);
            if (destination == null) return false;

            walkTargetAcc.set(new WalkTarget(destination, speedModifier, 0));
            cooldownAcc.setWithExpiry(Unit.INSTANCE, REPLAN_COOLDOWN);
            return true;
        }));
    }

    public static boolean isPursued(Hunter hunter, double safeDistance) {
        return hasPursuers(hunter, hunter.getBrain().getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of()), safeDistance);
    }

    private static boolean hasPursuers(Hunter hunter, List<LivingEntity> nearby, double safeDistance) {
        for (LivingEntity entity : nearby) {
            if (hunter.distanceTo(entity) < safeDistance && isPursuer(hunter, entity)) return true;
        }
        return false;
    }

    private static boolean isPursuer(Hunter hunter, LivingEntity entity) {
        if (!isHostile(entity)) return false;
        if (entity instanceof Mob mob && mob.getTarget() == hunter) return true;
        return hunter.distanceTo(entity) < DANGER_RADIUS;
    }

    private static boolean isHostile(LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof Hunter) return false;
        if (!(entity instanceof Monster || Helper.isVampire(entity))) return false;
        return !HunterThreats.requiresProvocation(entity) || HunterThreats.isProvoked(entity);
    }

    private static List<LivingEntity> collectHostiles(Hunter hunter, List<LivingEntity> nearby, List<LivingEntity> visible) {
        Set<LivingEntity> hostiles = new LinkedHashSet<>();
        for (List<LivingEntity> source : List.of(nearby, visible)) {
            for (LivingEntity entity : source) {
                if (hunter.distanceTo(entity) < AWARENESS_RADIUS && isHostile(entity)) hostiles.add(entity);
            }
        }
        return new ArrayList<>(hostiles);
    }

    private static @Nullable Vec3 findSafestDestination(Hunter hunter, List<LivingEntity> hostiles, List<LivingEntity> allies, double minFleeDistance) {
        int range = Mth.ceil(minFleeDistance) + FLEE_RANGE;
        double angleOffset = hunter.getRandom().nextDouble() * Math.PI * 2.0D / DIRECTIONS;
        Vec3 best = null;
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int i = 0; i < DIRECTIONS; i++) {
            double angle = angleOffset + Math.PI * 2.0D * i / DIRECTIONS;
            Vec3 towards = hunter.position().add(Math.cos(angle) * range, 0, Math.sin(angle) * range);
            Vec3 candidate = LandRandomPos.getPosTowards(hunter, range, VERTICAL_RANGE, towards);
            if (candidate == null || candidate.distanceTo(hunter.position()) < minFleeDistance) continue;

            double score = scoreDestination(hunter.position(), candidate, hostiles, allies);
            if (score > bestScore) {
                bestScore = score;
                best = candidate;
            }
        }
        return best;
    }

    private static double scoreDestination(Vec3 from, Vec3 destination, List<LivingEntity> hostiles, List<LivingEntity> allies) {
        double clearance = AWARENESS_RADIUS;
        double routePenalty = 0.0D;
        for (LivingEntity hostile : hostiles) {
            Vec3 position = hostile.position();
            clearance = Math.min(clearance, position.distanceTo(destination));
            routePenalty += calculateRouteExposure(from, destination, position);
        }

        double score = clearance - routePenalty * ROUTE_PENALTY;
        for (LivingEntity ally : allies) {
            if (ally.isAlive() && ally.position().distanceTo(destination) < ALLY_RADIUS) {
                score += ALLY_BONUS;
                break;
            }
        }
        return score;
    }

    private static boolean isRouteSafe(Vec3 from, Vec3 destination, List<LivingEntity> hostiles) {
        for (LivingEntity hostile : hostiles) {
            if (calculateRouteExposure(from, destination, hostile.position()) > 0.5D) return false;
        }
        return true;
    }

    private static double calculateRouteExposure(Vec3 from, Vec3 destination, Vec3 hostile) {
        double currentDistance = horizontalDistance(from, hostile);
        double closestOnRoute = distanceToSegment(hostile, from, destination);
        return Math.max(0.0D, Math.min(currentDistance, ROUTE_CLEARANCE) - closestOnRoute);
    }

    private static double distanceToSegment(Vec3 point, Vec3 start, Vec3 end) {
        double dx = end.x - start.x;
        double dz = end.z - start.z;
        double lengthSqr = dx * dx + dz * dz;
        double t = lengthSqr < 1.0E-6 ? 0.0D : Mth.clamp(((point.x - start.x) * dx + (point.z - start.z) * dz) / lengthSqr, 0.0D, 1.0D);
        return horizontalDistance(point, new Vec3(start.x + dx * t, point.y, start.z + dz * t));
    }

    private static double horizontalDistance(Vec3 first, Vec3 second) {
        double dx = first.x - second.x;
        double dz = first.z - second.z;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
