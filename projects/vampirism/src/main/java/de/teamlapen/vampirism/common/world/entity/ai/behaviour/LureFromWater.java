package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterThreats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

public class LureFromWater {

    private static final int VERTICAL_RANGE = 7;
    private static final double EXTRA_RANGE = 4.0D;

    public static OneShot<Hunter> create(float speedModifier, double lureDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(MemoryModuleType.WALK_TARGET),
                instance.absent(ModMemoryTypes.SQUAD_POSITION.get()),
                instance.registered(MemoryModuleType.LOOK_TARGET)
        ).apply(instance, (targetAcc, walkTargetAcc, _, lookTargetAcc) -> (_, hunter, _) -> {
            LivingEntity target = instance.get(targetAcc);
            if (!HunterThreats.shouldLure(target)) return false;

            lookTargetAcc.set(new EntityTracker(target, true));
            double distance = hunter.distanceTo(target);
            if (distance >= lureDistance && !hunter.isInWater()) return false;

            double minDistance = Math.max(2.0D, lureDistance - distance);
            Vec3 destination = LandRandomPos.getPosAway(hunter, minDistance, minDistance + EXTRA_RANGE, VERTICAL_RANGE, target.position());
            if (destination == null) return false;

            walkTargetAcc.set(new WalkTarget(destination, speedModifier, 0));
            return true;
        }));
    }
}
