package de.teamlapen.vampirism.mixin.accessor;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.CrossbowAttack;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowAttack.class)
public interface CrossbowAttackAccessor {

    @Invoker("crossbowAttack")
    <E extends Mob & CrossbowAttackMob, T extends LivingEntity> void invokeCrossbowAttack(E shooter, T target);
}
