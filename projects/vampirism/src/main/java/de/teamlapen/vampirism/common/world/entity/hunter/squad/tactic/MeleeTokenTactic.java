package de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadContext;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadOrder;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.SquadTactic;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.TargetAllocator;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MeleeTokenTactic implements SquadTactic {

    private static final int TOKENS_WITH_SUPPORT = 2;
    private static final int TOKENS_WITHOUT_SUPPORT = 3;
    private static final float LARGE_TARGET_WIDTH = 1.2F;
    private static final double LEASH_RADIUS = 20.0D;
    private static final double GUARD_RADIUS = 6.0D;
    private static final double FLANK_START_DISTANCE = 3.0D;
    private static final double FLANK_OFFSET = 1.8D;

    private static final double KEEP_TARGET_BONUS = 4.0D;
    private static final double SUPPORT_BONUS = 6.0D;
    private static final double RETALIATE_BONUS = 3.0D;
    private static final double GUARD_BONUS = 4.0D;

    @Override
    public void apply(SquadContext context) {
        if (!context.hasThreats()) return;

        List<Hunter> fighters = context.freeMembers(Hunter::isMeleeClass);
        if (fighters.isEmpty()) return;

        List<Hunter> ranged = context.rangedMembers();
        List<Vec3> protect = ranged.isEmpty() ? List.of(context.squadCenter()) : ranged.stream().map(Hunter::position).toList();
        int baseTokens = ranged.isEmpty() ? TOKENS_WITHOUT_SUPPORT : TOKENS_WITH_SUPPORT;

        List<LivingEntity> targets = context.threats().stream()
                .filter(HunterThreats::canMelee)
                .filter(threat -> threat.position().distanceTo(context.squadCenter()) <= LEASH_RADIUS || context.getSpotterDistance(threat) <= LEASH_RADIUS || calculateDistanceToNearest(threat, protect) <= GUARD_RADIUS)
                .toList();
        if (targets.isEmpty()) return;

        Map<LivingEntity, List<Hunter>> plan = TargetAllocator.allocateTargets(fighters, targets,
                TargetAllocator.createProportionalCapacity(targets, fighters.size(), target -> baseTokens + (target.getBbWidth() > LARGE_TARGET_WIDTH ? 1 : 0)),
                (fighter, target) -> calculateAssignmentCost(context, fighter, target, protect),
                HunterThreats::isWorthEngaging);

        plan.forEach((target, assigned) -> {
            List<Hunter> ordered = assigned.stream().sorted(Comparator.comparingDouble(fighter -> fighter.distanceToSqr(target))).toList();
            for (int i = 0; i < ordered.size(); i++) {
                Hunter fighter = ordered.get(i);
                SquadOrder order = context.claimOrder(fighter).target(target);
                if (i > 0 && fighter.distanceTo(target) > FLANK_START_DISTANCE) {
                    order.position(findFlankPosition(context, target, ordered.getFirst(), i, fighter));
                }
            }
        });
    }

    private static double calculateAssignmentCost(SquadContext context, Hunter fighter, LivingEntity target, List<Vec3> protect) {
        double cost = fighter.distanceTo(target);
        if (fighter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null) == target) cost -= KEEP_TARGET_BONUS;

        Hunter victim = context.getVictimOf(target);
        if (victim != null) cost -= SUPPORT_BONUS;
        if (victim == fighter) cost -= RETALIATE_BONUS;
        if (calculateDistanceToNearest(target, protect) <= GUARD_RADIUS) cost -= GUARD_BONUS;
        return cost;
    }

    private static Vec3 findFlankPosition(SquadContext context, LivingEntity target, Hunter first, int index, Hunter fighter) {
        Vec3 approach = target.position().subtract(first.position());
        Vec3 direction = new Vec3(approach.x, 0, approach.z);
        direction = direction.lengthSqr() < 1.0E-4 ? context.front() : direction.normalize();

        Vec3 flank = switch (index) {
            case 1 -> direction;
            case 2 -> new Vec3(-direction.z, 0, direction.x);
            default -> new Vec3(direction.z, 0, -direction.x);
        };
        return context.findSafeGround(target.position().add(flank.scale(FLANK_OFFSET)), fighter.getY());
    }

    private static double calculateDistanceToNearest(LivingEntity threat, List<Vec3> positions) {
        double best = Double.MAX_VALUE;
        for (Vec3 pos : positions) {
            best = Math.min(best, threat.position().distanceTo(pos));
        }
        return best;
    }
}
