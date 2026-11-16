package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Comparator;
import java.util.List;

public class WatchFight {

    public static OneShot<Hunter> create(double closeThreatDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.registered(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()),
                instance.registered(MemoryModuleType.LOOK_TARGET)
        ).apply(instance, (targetAcc, hostilesAcc, lookAcc) -> (_, hunter, _) -> {
            if (hunter.isHoldingPotion() || hunter.isUsingItem()) return false;

            LivingEntity focus = instance.tryGet(hostilesAcc).orElse(List.of()).stream()
                    .filter(hostile -> hostile.isAlive() && hunter.distanceTo(hostile) < closeThreatDistance)
                    .min(Comparator.comparingDouble(hunter::distanceToSqr))
                    .orElse(instance.get(targetAcc));
            lookAcc.set(new EntityTracker(focus, true));
            return true;
        }));
    }
}
