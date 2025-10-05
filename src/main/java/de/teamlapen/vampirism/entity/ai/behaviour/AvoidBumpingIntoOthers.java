package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import de.teamlapen.vampirism.entity.hunter.Hunter;

import java.util.List;

/**
 * Makes hunters step to the side a bit when bumped into another hunter while walking. Not a perfect solution, but makes it a bit better.
 * The one with lower UUID hash gives the way, while the other one steps aside and looks at him, just so that both don't step to the side.
 */
public class AvoidBumpingIntoOthers {

    public static BehaviorControl<Hunter> create(double speed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get()),
                instance.registered(MemoryModuleType.WALK_TARGET)
        ).apply(instance, (visibleHuntersAccessor, walkTargetAccessor) -> (ServerLevel level, Hunter hunter, long gameTime) -> {
            List<LivingEntity> visibleHunters = instance.get(visibleHuntersAccessor);

            Hunter closest = null;
            double closestDistSqr = Double.MAX_VALUE;

            for (LivingEntity entity : visibleHunters) {
                if (entity == hunter || !(entity instanceof Hunter other) || !other.isAlive()) continue;

                double distSqr = hunter.distanceToSqr(other);
                // If withing 0.6 blocks, then probably bumping
                if (distSqr < 0.36D && distSqr < closestDistSqr) {
                    closest = other;
                    closestDistSqr = distSqr;
                }
            }

            if (closest == null) return false;

            boolean thisHunterMoves = hunter.getUUID().hashCode() > closest.getUUID().hashCode();

            Vec3 diff = hunter.position().subtract(closest.position());
            if (diff.lengthSqr() < 0.025) {
                diff = new Vec3(level.random.nextDouble() - 0.5, 0, level.random.nextDouble() - 0.5);
            }
            Vec3 away = diff.normalize();

            if (thisHunterMoves) {
                Vec3 strafeDir = new Vec3(-away.z, 0, away.x).scale(0.4 + level.random.nextDouble() * 0.4);
                Vec3 targetPos = hunter.position().add(strafeDir);

                hunter.getMoveControl().setWantedPosition(targetPos.x, targetPos.y, targetPos.z, speed);
                hunter.getLookControl().setLookAt(closest, 30.0F, 30.0F);
            } else {
                hunter.getNavigation().stop();
                hunter.getLookControl().setLookAt(closest, 30.0F, 30.0F);
            }

            return true;
        }));
    }
}

