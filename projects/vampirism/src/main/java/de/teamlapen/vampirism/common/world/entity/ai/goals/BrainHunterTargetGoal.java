package de.teamlapen.vampirism.common.world.entity.ai.goals;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class BrainHunterTargetGoal extends NearestAttackableTargetGoal<Hunter> {

    public BrainHunterTargetGoal(Mob mob) {
        super(mob, Hunter.class, true);
    }

    @Override
    public boolean canUse() {
        return !this.mob.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET) && super.canUse();
    }

    @Override
    public void start() {
        if (this.target != null) {
            this.mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this.target);
        }
    }

    @Override
    public boolean canContinueToUse() {
        return false;
    }
}
