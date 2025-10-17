package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.*;

public class FindCureTarget {

    public static OneShot<Hunter> create(double cureRadius, double maxHostileRadius, float speed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.registered(ModMemoryModuleTypes.CURE_TARGET.get()),
                instance.registered(ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get()),
                instance.registered(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get()),
                instance.present(ModMemoryModuleTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get())
        ).apply(instance, (walkTargetAcc, lookTargetAcc, cureTargetAcc, nearestAlliesAcc, nearestHostilesAcc, nearestInfectedEntitiesAcc) -> ((level, hunter, gameTime) -> {
            if (hunter.isFighting() || hunter.isRetreating()) return false;

            boolean hostilesNearby = instance.tryGet(nearestHostilesAcc)
                    .orElse(List.of())
                    .stream()
                    .anyMatch(a -> a.isAlive() && a.distanceTo(hunter) < maxHostileRadius);

            if (hostilesNearby) return false;

            Optional<LivingEntity> existingCureTargetOpt = instance.tryGet(cureTargetAcc);
            if (existingCureTargetOpt.isPresent() && existingCureTargetOpt.get().isAlive()) {
                hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(existingCureTargetOpt.get(), speed, 1));

                return true;
            }

            List<LivingEntity> allyCureTargets = instance.tryGet(nearestAlliesAcc)
                    .orElse(List.of())
                    .stream()
                    .filter(e -> e.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURE_TARGET.get()))
                    .map(e -> e.getBrain().getMemory(ModMemoryModuleTypes.CURE_TARGET.get()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            Optional<LivingEntity> nearestTargetOpt = instance.get(nearestInfectedEntitiesAcc).stream()
                    .filter(e -> e.isAlive() && !allyCureTargets.contains(e) && e.distanceTo(hunter) <= cureRadius)
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(hunter)));

            if (nearestTargetOpt.isPresent()) {
                LivingEntity cureTarget = nearestTargetOpt.get();

                List<LivingEntity> allHunters = instance.tryGet(nearestAlliesAcc)
                        .orElse(List.of())
                        .stream()
                        .filter(e -> e.isAlive() && e instanceof Hunter allyHunter && !allyHunter.isFighting() && !allyHunter.isRetreating())
                        .filter(e -> e.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get())
                                .orElse(List.of())
                                .stream()
                                .noneMatch(h -> h.isAlive() && h.distanceTo(e) < maxHostileRadius))
                        .toList();

                allHunters = new ArrayList<>(allHunters);
                allHunters.add(hunter);

                LivingEntity closestHunter = allHunters.stream().min(Comparator.comparingDouble(h -> h.distanceToSqr(cureTarget))).get();

                closestHunter.getBrain().setMemory(ModMemoryModuleTypes.CURE_TARGET.get(), cureTarget);
                closestHunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cureTarget, speed, 1));

                if (closestHunter != hunter) {
                    hunter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(closestHunter, true));
                }

                return true;
            }

            Optional<LivingEntity> allyWithTargetOpt = instance.tryGet(nearestAlliesAcc)
                    .orElse(List.of())
                    .stream()
                    .filter(e -> e.isAlive() && e.getBrain().hasMemoryValue(ModMemoryModuleTypes.CURE_TARGET.get()))
                    .min(Comparator.comparingDouble(e -> e.distanceToSqr(hunter)));

            if (allyWithTargetOpt.isPresent()) {
                hunter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(allyWithTargetOpt.get(), true));

                return true;
            }

            return false;
        })));
    }
}
