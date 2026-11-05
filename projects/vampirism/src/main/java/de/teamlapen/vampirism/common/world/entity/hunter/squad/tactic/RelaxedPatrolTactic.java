package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterPost;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadOrder;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;

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
            patrolPost(context, post);
        } else {
            followLeader(context, leader);
        }
    }

    private static void patrolPost(SquadContext context, HunterPost post) {
        List<Hunter> patrol = context.free(_ -> true).stream().sorted(Comparator.comparing(Entity::getUUID)).toList();
        double sweep = PatrolArea.FULL_CIRCLE / patrol.size();
        for (int i = 0; i < patrol.size(); i++) {
            context.claim(patrol.get(i)).patrolArea(new PatrolArea(post.center(), post.radius(), i * sweep, sweep));
        }
    }

    private static void followLeader(SquadContext context, Hunter leader) {
        PatrolArea area = PatrolArea.circle(leader.blockPosition(), LEADER_AREA_RADIUS);
        for (Hunter hunter : context.free(member -> member != leader)) {
            SquadOrder order = context.claim(hunter).patrolArea(area);
            if (hunter.distanceTo(leader) > LEASH_DISTANCE) {
                Vec3 toHunter = hunter.position().subtract(leader.position());
                Vec3 direction = new Vec3(toHunter.x, 0, toHunter.z).normalize();
                order.position(context.ground(leader.position().add(direction.scale(CATCH_UP_DISTANCE)), leader.getY()));
            }
        }
    }
}
