package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModEffects;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;

import java.util.*;

public class FindCureTarget {

    private static final int CURE_TIMEOUT = 600;

    public static OneShot<Hunter> create(double cureRadius, double maxHostileRadius, float speed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.registered(MemoryModuleType.LOOK_TARGET),
                instance.registered(ModMemoryTypes.CURE_TARGET.get()),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get()),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get())
        ).apply(instance, (_, _, cureTargetAcc, nearestAlliesAcc, nearestHostilesAcc, nearestInfectedEntitiesAcc) -> ((_, hunter, _) -> {
            Optional<LivingEntity> existingCureTargetOpt = instance.tryGet(cureTargetAcc);
            if (existingCureTargetOpt.isPresent() && !isValidCureTarget(hunter, existingCureTargetOpt.get(), cureRadius * 2)) {
                cureTargetAcc.erase();
                existingCureTargetOpt = Optional.empty();
            }

            if (hunter.isFighting() || hunter.isRetreating()) return false;

            boolean hostilesNearby = instance.tryGet(nearestHostilesAcc)
                    .orElse(List.of())
                    .stream()
                    .anyMatch(hostile -> hostile.isAlive() && hostile.distanceTo(hunter) < maxHostileRadius);
            if (hostilesNearby) return false;

            if (existingCureTargetOpt.isPresent()) {
                hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(existingCureTargetOpt.get(), speed, 1));
                return true;
            }

            List<LivingEntity> allyCureTargets = instance.tryGet(nearestAlliesAcc)
                    .orElse(List.of())
                    .stream()
                    .filter(ally -> ally.getBrain().hasMemoryValue(ModMemoryTypes.CURE_TARGET.get()))
                    .map(ally -> ally.getBrain().getMemory(ModMemoryTypes.CURE_TARGET.get()).orElse(null))
                    .filter(Objects::nonNull)
                    .toList();

            Optional<LivingEntity> nearestTargetOpt = instance.tryGet(nearestInfectedEntitiesAcc).orElse(List.of()).stream()
                    .filter(infected -> infected.isAlive() && !allyCureTargets.contains(infected) && infected.distanceTo(hunter) <= cureRadius)
                    .min(Comparator.comparingDouble(infected -> infected.distanceToSqr(hunter)));

            if (nearestTargetOpt.isPresent()) {
                LivingEntity cureTarget = nearestTargetOpt.get();

                List<LivingEntity> allHunters = new ArrayList<>(instance.tryGet(nearestAlliesAcc)
                        .orElse(List.of())
                        .stream()
                        .filter(ally -> ally.isAlive() && ally instanceof Hunter allyHunter && !allyHunter.isFighting() && !allyHunter.isRetreating())
                        .filter(ally -> ally.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get())
                                .orElse(List.of())
                                .stream()
                                .noneMatch(hostile -> hostile.isAlive() && hostile.distanceTo(ally) < maxHostileRadius))
                        .toList());
                allHunters.add(hunter);

                LivingEntity closestHunter = allHunters.stream().min(Comparator.comparingDouble(candidate -> candidate.distanceToSqr(cureTarget))).get();

                closestHunter.getBrain().setMemoryWithExpiry(ModMemoryTypes.CURE_TARGET.get(), cureTarget, CURE_TIMEOUT);
                closestHunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(cureTarget, speed, 1));

                if (closestHunter != hunter) {
                    hunter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(closestHunter, true));
                }

                return true;
            }

            Optional<LivingEntity> allyWithTargetOpt = instance.tryGet(nearestAlliesAcc)
                    .orElse(List.of())
                    .stream()
                    .filter(ally -> ally.isAlive() && ally.getBrain().hasMemoryValue(ModMemoryTypes.CURE_TARGET.get()))
                    .min(Comparator.comparingDouble(ally -> ally.distanceToSqr(hunter)));

            if (allyWithTargetOpt.isPresent()) {
                hunter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(allyWithTargetOpt.get(), true));
                return true;
            }

            return false;
        })));
    }

    private static boolean isValidCureTarget(Hunter hunter, LivingEntity target, double maxDistance) {
        return target.isAlive() && target.level() == hunter.level() && target.hasEffect(ModEffects.SANGUINARE) && target.distanceTo(hunter) <= maxDistance;
    }
}
