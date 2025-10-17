package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class CureTargetIfClose {

    public static OneShot<Hunter> create(double cureRange, int stareTicks) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryModuleTypes.CURE_TARGET.get())
        ).apply(instance, (cureTargetAcc) -> ((level, hunter, gameTime) -> {
            LivingEntity cureTarget = instance.get(cureTargetAcc);

            if (cureTarget.distanceTo(hunter) > cureRange) return false;

            hunter.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET);
            hunter.getNavigation().stop();

            if (cureTarget.hasEffect(ModEffects.SANGUINARE)) {
                cureTarget.removeEffect(ModEffects.SANGUINARE);

                if (stareTicks > 0) {
                    hunter.getBrain().setMemoryWithExpiry(MemoryModuleType.LOOK_TARGET, new EntityTracker(cureTarget, true), stareTicks);
                    hunter.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.PATROL_COOLDOWN.get(), Unit.INSTANCE, (long) (stareTicks * 1.5));
                }
            }

            hunter.getBrain().eraseMemory(ModMemoryModuleTypes.CURE_TARGET.get());

            return true;
        })));
    }
}
