package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RetreatFromEnemies {

    public static OneShot<PathfinderMob> create(MemoryModuleType<List<LivingEntity>> enemiesMemory, float speedModifier, int desiredDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.present(enemiesMemory)
        ).apply(instance, (walkTargetAccessor, enemiesAccessor) ->
                (level, mob, gameTime) -> {
                    List<LivingEntity> enemies = instance.get(enemiesAccessor);
                    if (enemies.isEmpty()) {
                        return false;
                    }

                    Vec3 center = averagePosition(enemies);
                    Vec3 self = mob.position();

                    if (!self.closerThan(center, desiredDistance)) {
                        return false;
                    }

                    for (int i = 0; i < 10; i++) {
                        Vec3 retreat = LandRandomPos.getPosAway(mob, 16, 7, center);
                        if (retreat != null) {
                            walkTargetAccessor.set(new WalkTarget(retreat, speedModifier, 0));
                            return true;
                        }
                    }

                    return false;
                }
        ));
    }

    private static Vec3 averagePosition(List<LivingEntity> entities) {
        if (entities.isEmpty()) return Vec3.ZERO;
        double x = 0, y = 0, z = 0;
        for (LivingEntity e : entities) {
            x += e.getX();
            y += e.getY();
            z += e.getZ();
        }
        return new Vec3(x / entities.size(), y / entities.size(), z / entities.size());
    }
}
