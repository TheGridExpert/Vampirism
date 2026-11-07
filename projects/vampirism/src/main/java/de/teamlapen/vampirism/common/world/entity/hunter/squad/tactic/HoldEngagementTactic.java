package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class HoldEngagementTactic implements SquadTactic {

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        for (Hunter hunter : context.freeMembers(_ -> true)) {
            LivingEntity target = hunter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null);
            if (target != null && context.threats().contains(target) && canKeepFighting(hunter, target)) {
                context.claimOrder(hunter).target(target);
            }
        }
    }

    private static boolean canKeepFighting(Hunter hunter, LivingEntity target) {
        if (!HunterThreats.canEngage(hunter, target)) return false;
        if (HunterThreats.isExplosive(target) && hunter.distanceTo(target) < HunterThreats.getMinShootDistance(target)) return false;
        return HunterThreats.isWorthEngaging(hunter, target);
    }
}
