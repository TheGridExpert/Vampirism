package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RetreatFromEnemies {

    private static final double MIN_MOVE_DISTANCE = 4.0;

    public static OneShot<PathfinderMob> create(MemoryModuleType<List<LivingEntity>> enemiesMemory, float speedModifier, double retreatDistance, double safeDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(enemiesMemory),
                instance.absent(MemoryModuleType.WALK_TARGET)
        ).apply(instance, (enemiesAccessor, walkTargetAccessor) -> (ServerLevel level, PathfinderMob mob, long gameTime) -> {
            List<LivingEntity> aliveEnemies = instance.get(enemiesAccessor).stream().filter(LivingEntity::isAlive).collect(Collectors.toList());

            if (aliveEnemies.isEmpty()) return false;

            Vec3 enemiesCenter = averagePosition(aliveEnemies);
            Vec3 mobPos = mob.position();

            if (mobPos.distanceTo(enemiesCenter) >= safeDistance) return false;

            Vec3 fleeDir = mobPos.subtract(enemiesCenter).normalize();

            Vec3 alliesCenter = mob.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get())
                    .flatMap(list -> list.stream()
                            .filter(a -> a.isAlive() && a.distanceToSqr(mob) < 400)
                            .min(Comparator.comparingDouble(a -> a.distanceToSqr(mob)))
                    )
                    .map(Entity::position)
                    .orElse(null);

            if (alliesCenter != null) {
                Vec3 toAlly = alliesCenter.subtract(mobPos).normalize();
                fleeDir = fleeDir.add(toAlly).normalize();
            }

            Vec3 targetPos = mobPos.add(fleeDir.scale(retreatDistance));

            if (mobPos.distanceToSqr(targetPos) >= MIN_MOVE_DISTANCE * MIN_MOVE_DISTANCE) {
                walkTargetAccessor.set(new WalkTarget(targetPos, speedModifier, 0));
                return true;
            }

            return false;
        }));
    }

    private static Vec3 averagePosition(List<LivingEntity> entities) {
        if (entities.isEmpty()) return Vec3.ZERO;
        double x = 0, y = 0, z = 0;
        for (LivingEntity e : entities) {
            x += e.getX();
            y += e.getY();
            z += e.getZ();
        }
        int count = entities.size();
        return new Vec3(x / count, y / count, z / count);
    }
}
