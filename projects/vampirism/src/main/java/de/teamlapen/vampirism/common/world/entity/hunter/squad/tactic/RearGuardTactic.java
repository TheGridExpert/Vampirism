package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class RearGuardTactic implements SquadTactic {

    private static final float WOUNDED_HEALTH = 0.4F;
    private static final float HEALTHY_HEALTH = 0.5F;
    private static final double REAR_DISTANCE = 18.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        boolean hasHealthyMelee = !context.free(hunter -> hunter.isMeleeClass() && healthFraction(hunter) >= HEALTHY_HEALTH).isEmpty();
        List<Hunter> rear = context.free(hunter -> hunter.getBrain().hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())
                || (hasHealthyMelee && hunter.isMeleeClass() && healthFraction(hunter) < WOUNDED_HEALTH));
        if (rear.isEmpty()) return;

        Vec3 rearCenter = context.threatCenter().subtract(context.front().scale(REAR_DISTANCE));
        for (int i = 0; i < rear.size(); i++) {
            Hunter hunter = rear.get(i);
            Vec3 offset = context.side().scale((i - (rear.size() - 1) / 2.0D) * 2.0D);
            context.claim(hunter).retreat().position(context.ground(rearCenter.add(offset), hunter.getY()));
        }
    }

    private static float healthFraction(Hunter hunter) {
        return hunter.getHealth() / hunter.getMaxHealth();
    }
}
