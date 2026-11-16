package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

public class SupportTactic implements SquadTactic {

    private static final double BEHIND_DISTANCE = 2.5D;
    private static final double LINE_SPACING = 2.5D;
    private static final double SAFE_DISTANCE = 4.5D;
    private static final double ENGAGE_DISTANCE = 8.0D;
    private static final double BACK_OFF_DISTANCE = 3.5D;
    private static final double KEEP_POSITION_DISTANCE = 2.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        List<Hunter> alchemists = context.freeMembers(Hunter::isAlchemistClass).stream()
                .sorted(Comparator.comparingDouble(hunter -> hunter.position().dot(context.side())))
                .toList();
        if (alchemists.isEmpty()) return;

        Vec3 lineCenter = context.frontlineCenter().subtract(context.front().scale(BEHIND_DISTANCE));
        for (int i = 0; i < alchemists.size(); i++) {
            Hunter alchemist = alchemists.get(i);
            Vec3 slot = lineCenter.add(context.side().scale((i - (alchemists.size() - 1) / 2.0D) * LINE_SPACING));
            context.claimOrder(alchemist).position(findPosition(context, alchemist, slot));
        }
    }

    private static Vec3 findPosition(SquadContext context, Hunter alchemist, Vec3 slot) {
        LivingEntity closeThreat = context.findNearestThreat(alchemist.position(), threat -> threat.distanceTo(alchemist) < SAFE_DISTANCE);
        if (closeThreat != null) {
            Vec3 away = horizontalDirection(closeThreat.position(), alchemist.position(), context.front().reverse());
            double backOff = Math.max(BACK_OFF_DISTANCE, SAFE_DISTANCE - closeThreat.distanceTo(alchemist) + 1.5D);
            return context.findSafeGround(alchemist.position().add(away.scale(backOff)), alchemist.getY());
        }

        Vec3 engaged = pullIntoReach(context, slot);
        Vec3 previous = alchemist.getBrain().getMemory(ModMemoryTypes.SQUAD_POSITION.get()).orElse(null);
        if (previous != null && previous.distanceTo(engaged) < KEEP_POSITION_DISTANCE && isClearOfThreats(context, previous)) return previous;

        Vec3 position = context.findSafeGround(engaged, alchemist.getY());
        return isClearOfThreats(context, position) ? position : context.findSafeGround(slot, alchemist.getY());
    }

    private static Vec3 pullIntoReach(SquadContext context, Vec3 slot) {
        LivingEntity nearest = context.findNearestThreat(slot, _ -> true);
        if (nearest == null) return slot;

        double distance = nearest.position().distanceTo(slot);
        if (distance <= ENGAGE_DISTANCE) return slot;
        Vec3 towards = horizontalDirection(slot, nearest.position(), context.front());
        return slot.add(towards.scale(distance - ENGAGE_DISTANCE));
    }

    private static Vec3 horizontalDirection(Vec3 from, Vec3 to, Vec3 fallback) {
        Vec3 delta = new Vec3(to.x - from.x, 0, to.z - from.z);
        return delta.lengthSqr() < 1.0E-4 ? fallback : delta.normalize();
    }

    private static boolean isClearOfThreats(SquadContext context, Vec3 position) {
        return context.findNearestThreat(position, threat -> threat.position().distanceTo(position) < SAFE_DISTANCE) == null;
    }
}
