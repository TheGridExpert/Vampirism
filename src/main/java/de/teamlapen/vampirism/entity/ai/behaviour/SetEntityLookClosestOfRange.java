package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class SetEntityLookClosestOfRange {

    public static OneShot<LivingEntity> create(MemoryModuleType<List<LivingEntity>> lookListMemory, float maxDist) {
        return BehaviorBuilder.create(instance -> instance.group(
                    instance.absent(MemoryModuleType.LOOK_TARGET),
                    instance.present(lookListMemory)
                ).apply(instance, (lookTargetAccessor, nearestAccessor) -> (level, entity, gameTime) -> {
                    Optional<LivingEntity> closest = instance.get(nearestAccessor).stream()
                            .filter(target -> target.isAlive() && !entity.hasPassenger(target))
                            .filter(target -> target.distanceToSqr(entity) <= (double) maxDist * maxDist)
                            .min(Comparator.comparingDouble(entity::distanceToSqr));

                    if (closest.isEmpty()) return false;

                    lookTargetAccessor.set(new EntityTracker(closest.get(), true));

                    return true;
                }));
    }
}
