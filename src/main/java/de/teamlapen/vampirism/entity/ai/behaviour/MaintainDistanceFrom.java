package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class MaintainDistanceFrom {

    public static OneShot<PathfinderMob> entity(MemoryModuleType<? extends Entity> targetMemory, float speedModifier, double minDistance, double maxDistance) {
        return create(targetMemory, speedModifier, minDistance, maxDistance, Entity::position);
    }

    public static OneShot<PathfinderMob> pos(MemoryModuleType<BlockPos> targetMemory, float speedModifier, double minDistance, double maxDistance) {
        return create(targetMemory, speedModifier, minDistance, maxDistance, Vec3::atBottomCenterOf);
    }

    private static <T> OneShot<PathfinderMob> create(MemoryModuleType<T> targetMemory, float speedModifier, double minDistance, double maxDistance, Function<T, Vec3> toPosition) {
        return BehaviorBuilder.create(
                instance -> instance.group(
                                instance.registered(MemoryModuleType.WALK_TARGET),
                                instance.present(targetMemory)
                        ).apply(instance, (walkTargetAcc, targetAcc) -> (ServerLevel level, PathfinderMob mob, long gameTime) -> {
                            Optional<WalkTarget> optional = instance.tryGet(walkTargetAcc);
                            if (optional.isPresent()) return false;

                            Vec3 mobPos = mob.position();
                            Vec3 targetPos = toPosition.apply(instance.get(targetAcc));

                            double dist = mobPos.distanceTo(targetPos);

                            Vec3 desiredPos = null;

                            if (dist > minDistance && dist < maxDistance) return false;

                            if (dist < minDistance) {
                                desiredPos = LandRandomPos.getPosAway(mob, 8, 5, targetPos);
                            } else if (dist > maxDistance) {
                                desiredPos = LandRandomPos.getPosTowards(mob, 8, 5, targetPos);
                            }

                            if (desiredPos != null) {
                                walkTargetAcc.set(new WalkTarget(desiredPos, speedModifier, 0));
                                return true;
                            }

                            return false;
                        })
        );
    }
}
