package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class RetreatFromEnemies {

    private static final double DANGER_RADIUS = 8.0D;
    private static final double ALLY_RADIUS = 20.0D;
    private static final int FLEE_RANGE = 8;
    private static final int VERTICAL_RANGE = 7;
    private static final double AVOID_POINT_OFFSET = 4.0D;

    public static OneShot<Hunter> create(float speedModifier, double minFleeDistance, double safeDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.NEAREST_LIVING_ENTITIES),
                instance.absent(MemoryModuleType.WALK_TARGET),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get())
        ).apply(instance, (nearbyAcc, walkTargetAcc, alliesAcc) -> (_, hunter, _) -> {
            List<LivingEntity> threats = instance.get(nearbyAcc).stream()
                    .filter(entity -> hunter.distanceTo(entity) < safeDistance && isDangerTo(hunter, entity))
                    .toList();
            if (threats.isEmpty()) return false;

            Vec3 fleeDirection = Vec3.ZERO;
            for (LivingEntity threat : threats) {
                Vec3 away = hunter.position().subtract(threat.position());
                Vec3 horizontal = new Vec3(away.x, 0, away.z);
                double distance = Math.max(1.0D, horizontal.length());
                fleeDirection = fleeDirection.add(horizontal.scale(1.0D / (distance * distance)));
            }
            if (fleeDirection.lengthSqr() < 1.0E-6) {
                fleeDirection = new Vec3(hunter.getRandom().nextDouble() - 0.5D, 0, hunter.getRandom().nextDouble() - 0.5D);
            }
            fleeDirection = fleeDirection.normalize();

            Vec3 allyPos = instance.tryGet(alliesAcc).orElse(List.of()).stream()
                    .filter(ally -> ally.isAlive() && ally.distanceTo(hunter) < ALLY_RADIUS)
                    .min(Comparator.comparingDouble(ally -> ally.distanceToSqr(hunter)))
                    .map(LivingEntity::position)
                    .orElse(null);
            if (allyPos != null) {
                Vec3 toAlly = allyPos.subtract(hunter.position());
                Vec3 horizontal = new Vec3(toAlly.x, 0, toAlly.z);
                if (horizontal.lengthSqr() > 1.0E-4 && horizontal.normalize().dot(fleeDirection) > 0) {
                    fleeDirection = fleeDirection.add(horizontal.normalize()).normalize();
                }
            }

            Vec3 avoidPoint = hunter.position().subtract(fleeDirection.scale(AVOID_POINT_OFFSET));
            Vec3 destination = LandRandomPos.getPosAway(hunter, minFleeDistance, minFleeDistance + FLEE_RANGE, VERTICAL_RANGE, avoidPoint);
            if (destination == null) return false;

            walkTargetAcc.set(new WalkTarget(destination, speedModifier, 0));
            return true;
        }));
    }

    private static boolean isDangerTo(Hunter hunter, LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof Hunter) return false;
        if (!(entity instanceof Monster || Helper.isVampire(entity))) return false;

        boolean targetsHunter = entity instanceof Mob mob && mob.getTarget() == hunter;
        if (targetsHunter) return true;
        if (HunterThreats.requiresProvocation(entity) && !HunterThreats.isProvoked(entity)) return false;
        return hunter.distanceTo(entity) < DANGER_RADIUS;
    }
}
