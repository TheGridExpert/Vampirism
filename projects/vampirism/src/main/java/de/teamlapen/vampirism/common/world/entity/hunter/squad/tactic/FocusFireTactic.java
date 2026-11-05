package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.TargetAllocator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class FocusFireTactic implements SquadTactic {

    private static final int MAX_SHOOTERS_PER_TARGET = 3;
    private static final double MIN_RANGE = 8.0D;
    private static final double MAX_RANGE = 16.0D;
    private static final double RANGE_SLACK = 2.0D;
    private static final double LINE_BUFFER = 3.0D;
    private static final double LINE_SPACING = 3.0D;
    private static final double PEEL_DISTANCE = 5.0D;
    private static final double BACK_OFF_DISTANCE = 6.0D;

    private static final double HIDDEN_PENALTY = 8.0D;
    private static final double KEEP_TARGET_BONUS = 4.0D;
    private static final double SUPPORT_BONUS = 6.0D;
    private static final double PRIORITY_BONUS = 4.0D;
    private static final double WOUNDED_BONUS = 4.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        List<Hunter> shooters = context.free(Hunter::isRangedClass);
        if (shooters.isEmpty()) return;

        List<LivingEntity> targets = context.threats().stream().filter(HunterThreats::canShoot).toList();
        if (targets.isEmpty()) return;

        Map<LivingEntity, List<Hunter>> plan = TargetAllocator.allocate(shooters, targets,
                TargetAllocator.proportionalCapacity(targets, shooters.size(), _ -> MAX_SHOOTERS_PER_TARGET),
                (shooter, target) -> cost(context, shooter, target),
                HunterThreats::isWorthEngaging);

        plan.forEach((target, assigned) -> {
            Vec3 approach = horizontalDirection(context.rangedCenter(), target.position(), context.front());
            Vec3 side = new Vec3(-approach.z, 0, approach.x);
            List<Hunter> ordered = assigned.stream().sorted(Comparator.comparingDouble(shooter -> shooter.position().subtract(target.position()).dot(side))).toList();
            for (int i = 0; i < ordered.size(); i++) {
                Hunter shooter = ordered.get(i);
                context.claim(shooter).target(target).position(firingPosition(context, shooter, target, approach, side, i, ordered.size()));
            }
        });
    }

    private static double cost(SquadContext context, Hunter shooter, LivingEntity target) {
        LivingEntity current = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        double distance = shooter.distanceTo(target);
        if (HunterThreats.isExplosive(target) && target != current && distance < HunterThreats.minShootDistance(target)) return Double.POSITIVE_INFINITY;

        double cost = distance;
        if (!shooter.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()).map(visible -> visible.contains(target)).orElse(false)) cost += HIDDEN_PENALTY;
        if (target == current) cost -= KEEP_TARGET_BONUS;
        if (context.victimOf(target) != null) cost -= SUPPORT_BONUS;
        if (HunterThreats.isPriority(target)) cost -= PRIORITY_BONUS;
        cost -= (1.0D - target.getHealth() / target.getMaxHealth()) * WOUNDED_BONUS;
        return cost;
    }

    private static Vec3 firingPosition(SquadContext context, Hunter shooter, LivingEntity target, Vec3 approach, Vec3 side, int index, int count) {
        LivingEntity closeThreat = context.nearestThreat(shooter.position(), HunterThreats::canMelee);
        if (closeThreat != null && closeThreat.distanceTo(shooter) < PEEL_DISTANCE) {
            Vec3 away = horizontalDirection(closeThreat.position(), shooter.position(), approach.reverse());
            return context.ground(shooter.position().add(away.scale(BACK_OFF_DISTANCE)), shooter.getY());
        }

        double minRange = Math.max(MIN_RANGE, HunterThreats.minShootDistance(target));
        Vec3 previous = shooter.getBrain().getMemory(ModMemoryTypes.SQUAD_POSITION.get()).orElse(null);
        if (previous != null && inRange(previous.distanceTo(target.position()), minRange) && context.hasClearShot(shooter, previous, target)) {
            return previous;
        }

        if (inRange(shooter.distanceTo(target), minRange) && shooter.hasLineOfSight(target)) {
            return shooter.position();
        }

        Vec3 lineCenter = target.position().subtract(approach.scale(minRange + LINE_BUFFER));
        Vec3 slot = lineCenter.add(side.scale((index - (count - 1) / 2.0D) * LINE_SPACING));

        Vec3 first = null;
        for (double shift : new double[]{0.0D, 2.0D, -2.0D}) {
            Vec3 candidate = context.ground(slot.add(side.scale(shift)), shooter.getY());
            if (context.hasClearShot(shooter, candidate, target)) return candidate;
            if (first == null) first = candidate;
        }
        return first;
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 delta = new Vec3(to.x - from.x, 0, to.z - from.z);
        return delta.lengthSqr() < 1.0E-4 ? fallback : delta.normalize();
    }

    private static boolean inRange(double distance, double minRange) {
        return distance >= minRange - RANGE_SLACK && distance <= MAX_RANGE + RANGE_SLACK;
    }
}
