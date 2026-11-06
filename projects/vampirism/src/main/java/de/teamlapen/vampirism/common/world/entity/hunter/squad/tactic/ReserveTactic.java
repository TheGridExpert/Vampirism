package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ReserveTactic implements SquadTactic {

    private static final double RESERVE_DISTANCE = 14.0D;
    private static final double LINE_SPACING = 3.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        List<Hunter> reserve = context.freeMembers(_ -> true);
        if (reserve.isEmpty()) return;

        Vec3 center = context.threatCenter().subtract(context.front().scale(RESERVE_DISTANCE));
        for (int i = 0; i < reserve.size(); i++) {
            Hunter hunter = reserve.get(i);
            Vec3 offset = context.side().scale((i - (reserve.size() - 1) / 2.0D) * LINE_SPACING);
            context.claimOrder(hunter).position(context.findSafeGround(center.add(offset), hunter.getY()));
        }
    }
}
