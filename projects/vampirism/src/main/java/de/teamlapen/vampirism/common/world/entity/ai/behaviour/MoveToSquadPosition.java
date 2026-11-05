package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class MoveToSquadPosition {

    private static final double REPATH_DISTANCE = 2.0D;
    private static final double FAR_DISTANCE = 10.0D;

    public static OneShot<Hunter> create(float speed, double tolerance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryTypes.SQUAD_POSITION.get()),
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.absent(ModMemoryTypes.CURE_TARGET.get())
        ).apply(instance, (positionAcc, walkTargetAcc, cureTargetAcc) -> (level, hunter, gameTime) -> {
            Vec3 position = instance.get(positionAcc);
            double distance = hunter.position().distanceTo(position);
            if (distance <= tolerance) return false;

            Optional<WalkTarget> current = instance.tryGet(walkTargetAcc);
            if (current.isPresent() && current.get().getTarget().currentPosition().distanceTo(position) < REPATH_DISTANCE) return false;

            float actualSpeed = distance > FAR_DISTANCE ? Math.max(speed, 0.7F) : speed;
            walkTargetAcc.set(new WalkTarget(position, actualSpeed, 1));
            return true;
        }));
    }
}
