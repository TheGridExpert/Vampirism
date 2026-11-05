package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class FormationTactic implements SquadTactic {

    private static final double ROW_SPACING = 2.5D;
    private static final double SIDE_OFFSET = 1.5D;

    @Override
    public void apply(SquadContext context) {
        if (context.hasThreats() || context.isRelaxed()) return;

        Hunter leader = context.squad().leader();
        if (leader == null) return;

        List<Hunter> melee = context.free(hunter -> hunter != leader && hunter.isMeleeClass());
        List<Hunter> ranged = context.free(hunter -> hunter != leader && hunter.isRangedClass());

        List<Hunter> column = new ArrayList<>(melee.subList(0, (melee.size() + 1) / 2));
        column.addAll(ranged);
        column.addAll(melee.subList((melee.size() + 1) / 2, melee.size()));

        Vec3 heading = context.front();
        Vec3 side = context.side();
        for (int i = 0; i < column.size(); i++) {
            Hunter hunter = column.get(i);
            int row = i / 2 + 1;
            double lateral = i % 2 == 0 ? -SIDE_OFFSET : SIDE_OFFSET;
            Vec3 slot = leader.position().subtract(heading.scale(row * ROW_SPACING)).add(side.scale(lateral));
            context.claim(hunter).position(context.ground(slot, leader.getY()));
        }
    }
}
