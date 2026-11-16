package de.teamlapen.vampirism.common.world.entity.hunter.brain;

import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;

public class HunterComrades {

    public static boolean isComrade(Hunter hunter, LivingEntity entity) {
        if (!entity.isAlive()) return false;
        if (entity instanceof Hunter) return true;
        if (!(entity instanceof Player player) || player.isSpectator() || !Helper.isHunter(player)) return false;
        return !isOpposed(hunter, player);
    }

    public static boolean isFoe(Hunter hunter, LivingEntity entity) {
        if (!entity.isAlive() || entity instanceof Hunter) return false;
        if (isOpposed(hunter, entity)) return true;
        return !isComrade(hunter, entity) && HunterAi.isEnemy(entity, hunter);
    }

    private static boolean isOpposed(Hunter hunter, LivingEntity entity) {
        if (isTargeting(hunter, entity)) return true;
        HunterSquad squad = hunter.getSquad();
        if (squad == null) return false;
        for (Hunter member : squad.members()) {
            if (isTargeting(member, entity)) return true;
        }
        return false;
    }

    private static boolean isTargeting(Hunter hunter, LivingEntity entity) {
        Brain<Hunter> brain = hunter.getBrain();
        return brain.getMemory(MemoryModuleType.ATTACK_TARGET).filter(target -> target == entity).isPresent()
                || brain.getMemory(MemoryModuleType.ANGRY_AT).filter(id -> id.equals(entity.getUUID())).isPresent();
    }
}
