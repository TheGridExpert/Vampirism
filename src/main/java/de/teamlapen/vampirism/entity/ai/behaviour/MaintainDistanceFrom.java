package de.teamlapen.vampirism.entity.ai.behaviour;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class MaintainDistanceFrom {

    public static <T> OneShot<PathfinderMob> create(float speedModifier, double minDistance, double maxDistance) {
        return BehaviorBuilder.create(
                instance -> instance.group(
                                instance.registered(MemoryModuleType.WALK_TARGET),
                                instance.present(MemoryModuleType.ATTACK_TARGET)
                        ).apply(instance, (walkTargetAcc, targetAcc) -> (ServerLevel level, PathfinderMob shooter, long gameTime) -> {
                            if (instance.tryGet(walkTargetAcc).isPresent()) return false;

                            LivingEntity target = instance.get(targetAcc);

                            Vec3 shooterPos = shooter.position();
                            Vec3 targetPos = target.position();
                            double dist = shooterPos.distanceTo(targetPos);

                            Vec3 desiredPos = null;

                            boolean lineOfSightClear = isLineOfSightClear(level, shooter, shooter.getEyePosition(), target.getEyePosition());

                            if (dist > minDistance && dist < maxDistance) {
                                if (!lineOfSightClear) {
                                    desiredPos = LandRandomPos.getPosTowards(shooter, 6, 3, targetPos.add(2, 0, 2));
                                } else {
                                    return false;
                                }
                            }

                            if (dist < minDistance) {
                                desiredPos = LandRandomPos.getPosAway(shooter, 8, 5, targetPos);
                            } else if (dist > maxDistance) {
                                desiredPos = LandRandomPos.getPosTowards(shooter, 8, 5, targetPos);
                            }

                            if (desiredPos != null) {
                                walkTargetAcc.set(new WalkTarget(desiredPos, speedModifier, 0));
                                return true;
                            }

                            return false;
                        })
        );
    }

    public static boolean isLineOfSightClear(ServerLevel level, LivingEntity shooter, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }
}
