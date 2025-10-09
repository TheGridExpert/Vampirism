package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.CrossbowItem;

public class SimpleCrossbowAttack<E extends Mob & CrossbowAttackMob, T extends LivingEntity> extends CrossbowAttack<E, T> {

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, E shooter) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive() && shooter.isHolding(item -> item.getItem() instanceof CrossbowItem);
    }

    @Override
    protected boolean canStillUse(ServerLevel level, E shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        return target != null && target.isAlive();
    }

    @Override
    protected void tick(ServerLevel level, E shooter, long gameTime) {
        LivingEntity target = shooter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
        if (target == null || !target.isAlive()) return;

        shooter.getLookControl().setLookAt(target, 45.0F, 45.0F);

        if (shooter.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET)) return;

        super.tick(level, shooter, gameTime);
    }
}
