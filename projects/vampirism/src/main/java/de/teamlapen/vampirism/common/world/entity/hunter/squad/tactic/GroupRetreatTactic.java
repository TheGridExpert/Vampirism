package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.core.GlobalPos;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class GroupRetreatTactic implements SquadTactic {

    private static final double START_RATIO = 0.5D;
    private static final double STOP_RATIO = 0.8D;
    private static final double FALLBACK_DISTANCE = 20.0D;
    private static final double MAX_HOME_DISTANCE = 64.0D;

    @Override
    public void apply(SquadContext context) {
        HunterSquad squad = context.squad();
        if (!context.hasThreats()) {
            squad.setRetreating(false);
            return;
        }

        double own = context.members().stream().mapToDouble(HunterThreats::calculatePower).sum();
        double enemy = context.threats().stream().mapToDouble(HunterThreats::calculatePower).sum();

        boolean retreating = squad.isRetreating() ? own < enemy * STOP_RATIO : own < enemy * START_RATIO;
        squad.setRetreating(retreating);
        if (!retreating) return;

        Vec3 fallback = findFallbackPosition(context);
        List<Hunter> free = context.freeMembers(_ -> true);
        for (int i = 0; i < free.size(); i++) {
            Hunter hunter = free.get(i);
            Vec3 offset = context.side().scale((i - (free.size() - 1) / 2.0D) * 2.0D);
            context.claimOrder(hunter).groupRetreat().position(context.findSafeGround(fallback.add(offset), hunter.getY()));
        }
    }

    private static Vec3 findFallbackPosition(SquadContext context) {
        Vec3 threatCenter = context.threatCenter();
        Vec3 squadCenter = context.squadCenter();
        Hunter leader = context.squad().leader();

        if (leader != null) {
            Optional<GlobalPos> home = leader.getBrain().getMemory(MemoryModuleType.HOME);
            if (home.isPresent() && home.get().dimension() == context.level().dimension()) {
                Vec3 homePos = Vec3.atBottomCenterOf(home.get().pos());
                boolean awayFromThreats = homePos.distanceTo(threatCenter) > squadCenter.distanceTo(threatCenter) + 4.0D;
                if (awayFromThreats && homePos.distanceTo(squadCenter) < MAX_HOME_DISTANCE) {
                    return homePos;
                }
            }
        }

        return squadCenter.subtract(context.front().scale(FALLBACK_DISTANCE));
    }
}
