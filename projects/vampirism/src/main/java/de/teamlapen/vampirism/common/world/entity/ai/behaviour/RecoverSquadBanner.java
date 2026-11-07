package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.item.ItemEntity;

public class RecoverSquadBanner {

    public static OneShot<Hunter> create(float speed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET)
        ).apply(instance, walkTargetAcc -> (level, hunter, _) -> {
            HunterSquad squad = hunter.getSquad();
            if (squad == null || squad.leader() != hunter || hunter.hasBanner()) return false;

            ItemEntity banner = squad.findDroppedBanner(level);
            if (banner == null) return false;

            if (hunter.isWithinPickupReach(banner)) {
                hunter.pickUpBanner(banner);
                walkTargetAcc.erase();
            } else {
                squad.tickBannerPursuit();
                walkTargetAcc.set(new WalkTarget(new EntityTracker(banner, false), speed, 0));
            }
            return true;
        }));
    }
}
