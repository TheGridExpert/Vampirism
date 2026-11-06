package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterPost;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadOrder;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.phys.Vec3;

public class RelaxedPatrolTactic implements SquadTactic {

    private static final int LEADER_AREA_RADIUS = 8;
    private static final double LEASH_DISTANCE = 14.0D;
    private static final double CATCH_UP_DISTANCE = 4.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.isRelaxed()) return;

        Hunter leader = context.squad().leader();
        if (leader == null) return;

        HunterPost post = context.squad().post();
        if (post != null) {
            assignPostPatrol(context, post);
        } else {
            assignLeaderPatrol(context, leader);
        }
    }

    private static void assignPostPatrol(SquadContext context, HunterPost post) {
        PatrolArea area = new PatrolArea(post.center(), post.radius());
        for (Hunter hunter : context.freeMembers(_ -> true)) {
            context.claimOrder(hunter).patrolArea(area);
        }
    }

    private static void assignLeaderPatrol(SquadContext context, Hunter leader) {
        PatrolArea area = new PatrolArea(leader.blockPosition(), LEADER_AREA_RADIUS);
        for (Hunter hunter : context.freeMembers(member -> member != leader)) {
            SquadOrder order = context.claimOrder(hunter).patrolArea(area);
            if (hunter.distanceTo(leader) > LEASH_DISTANCE) {
                Vec3 toHunter = hunter.position().subtract(leader.position());
                Vec3 direction = new Vec3(toHunter.x, 0, toHunter.z).normalize();
                order.position(context.findSafeGround(leader.position().add(direction.scale(CATCH_UP_DISTANCE)), leader.getY()));
            }
        }
    }
}
