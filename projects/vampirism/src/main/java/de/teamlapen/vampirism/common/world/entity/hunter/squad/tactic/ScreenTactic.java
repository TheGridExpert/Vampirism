package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class ScreenTactic implements SquadTactic {

    private static final double LINE_DISTANCE = 4.0D;
    private static final double LINE_SPACING = 2.0D;
    private static final double INTERCEPT_RADIUS = 6.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        Vec3 anchor = context.rangedCenter();
        List<Hunter> guards = context.freeMembers(Hunter::isMeleeClass).stream()
                .sorted(Comparator.comparingDouble(hunter -> hunter.position().subtract(anchor).dot(context.side())))
                .toList();
        if (guards.isEmpty()) return;

        Vec3 lineCenter = anchor.add(context.front().scale(LINE_DISTANCE));
        for (int i = 0; i < guards.size(); i++) {
            Hunter guard = guards.get(i);
            Vec3 slot = context.findSafeGround(lineCenter.add(context.side().scale((i - (guards.size() - 1) / 2.0D) * LINE_SPACING)), guard.getY());
            LivingEntity intercept = context.findNearestThreat(slot, threat -> HunterThreats.canMelee(threat) && threat.position().distanceTo(slot) < INTERCEPT_RADIUS);
            context.claimOrder(guard).position(slot).target(intercept);
        }
    }
}
