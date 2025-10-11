package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;

public class MaintainDistanceFrom {

    private static final double MIN_MOVE_DISTANCE = 4.0;
    private static final int MOVE_COOLDOWN_TICKS = 60;

    public static <T> OneShot<PathfinderMob> create(float speedModifier, double minDistance, double maxDistance) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.registered(MemoryModuleType.WALK_TARGET),
                instance.present(MemoryModuleType.ATTACK_TARGET),
                instance.absent(ModMemoryModuleTypes.REPOSITIONING_COOLDOWN.get())
        ).apply(instance, (walkTargetAcc, targetAcc, repositioningCooldownAcc) -> (ServerLevel level, PathfinderMob shooter, long gameTime) -> {
            LivingEntity target = instance.get(targetAcc);

            Vec3 shooterPos = shooter.position();
            Vec3 targetPos = target.position();
            double dist = shooterPos.distanceTo(targetPos);

            boolean lineOfSightClear = isLineOfSightClear(level, shooter, shooter.getEyePosition(), target.getEyePosition());

            double targetSpeed = target.getDeltaMovement().horizontalDistance();
            double shooterSpeed = shooter.getDeltaMovement().horizontalDistance() + shooter.getAttributeBaseValue(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);

            boolean targetFaster = targetSpeed > shooterSpeed * 1.1;

            Vec3 desiredPos = null;

            if (targetFaster) {
                desiredPos = runTowardAlliesOrOpposite(level, shooter, target, minDistance, maxDistance);
            } else if (dist < minDistance) {
                desiredPos = findCandidateOpposite(level, shooter, target, (int) Math.ceil(minDistance), 10, 16);
            } else if (dist > maxDistance) {
                desiredPos = findCandidateToward(level, shooter, targetPos, 2, (int) Math.ceil(maxDistance), 10);
            } else if (!lineOfSightClear) {
                desiredPos = findSmallReposition(level, shooter, target, dist, 12);
            }

            if (desiredPos != null && desiredPos.distanceToSqr(shooterPos) >= MIN_MOVE_DISTANCE * MIN_MOVE_DISTANCE) {
                walkTargetAcc.set(new WalkTarget(new BlockPosTracker(BlockPos.containing(desiredPos)), speedModifier, 0));
                shooter.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.REPOSITIONING_COOLDOWN.get(), Unit.INSTANCE, MOVE_COOLDOWN_TICKS);
                return true;
            }

            return false;
        }));
    }

    private static @Nullable Vec3 runTowardAlliesOrOpposite(ServerLevel level, PathfinderMob shooter, LivingEntity target, double minDistance, double maxDistance) {
        return shooter.getBrain().getMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get())
                .flatMap(list -> list.stream()
                        .filter(ally -> ally.isAlive() && ally.distanceToSqr(shooter) < 400)
                        .min(Comparator.comparingDouble(ally -> ally.distanceToSqr(shooter)))
                )
                .map(ally -> {
                    Vec3 allyPos = ally.position();
                    Vec3 dir = allyPos.subtract(shooter.position()).normalize();
                    return findGroundNear(level, allyPos.add(dir.scale(2.0)));
                })
                .orElseGet(() -> findCandidateOpposite(level, shooter, target, (int) minDistance, (int) maxDistance, 12));
    }

    private static Vec3 findGroundNear(ServerLevel level, Vec3 near) {
        BlockPos pos = BlockPos.containing(near);
        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());

        return Vec3.atBottomCenterOf(new BlockPos(pos.getX(), surfaceY, pos.getZ()));
    }

    private static @Nullable Vec3 findCandidateOpposite(ServerLevel level, PathfinderMob shooter, LivingEntity target, int minRadius, int maxRadius, int tries) {
        RandomSource rand = shooter.getRandom();
        Vec3 predictedTarget = predictTargetPosition(target, 3.0);

        double dirX = shooter.getX() - predictedTarget.x;
        double dirZ = shooter.getZ() - predictedTarget.z;
        double baseAngle = Math.atan2(dirZ, dirX);

        for (int i = 0; i < tries; i++) {
            double angle = baseAngle + Mth.nextDouble(rand, -Math.toRadians(15), Math.toRadians(15));
            double dist = maxRadius - rand.nextDouble() * (maxRadius - minRadius) * 0.3;
            Vec3 pos = candidateFromAngle(level, shooter, predictedTarget, angle, dist);
            if (pos != null) return pos;
        }

        return null;
    }

    private static @Nullable Vec3 findCandidateToward(ServerLevel level, PathfinderMob shooter, Vec3 targetPos, int minRadius, int maxRadius, int tries) {
        RandomSource rand = shooter.getRandom();
        double dirX = targetPos.x - shooter.getX();
        double dirZ = targetPos.z - shooter.getZ();
        double baseAngle = Math.atan2(dirZ, dirX);

        for (int i = 0; i < tries; i++) {
            double angle = baseAngle + Mth.nextDouble(rand, -Math.toRadians(30), Math.toRadians(30));
            double dist = minRadius + rand.nextDouble() * (maxRadius - minRadius);
            Vec3 pos = candidateFromAngle(level, shooter, targetPos, angle, dist);
            if (pos != null) return pos;
        }
        return null;
    }

    private static @Nullable Vec3 findSmallReposition(ServerLevel level, PathfinderMob shooter, LivingEntity target, double currentDist, int tries) {
        RandomSource rand = shooter.getRandom();
        Vec3 predictedTarget = predictTargetPosition(target, 2.0);
        double baseAngle = Math.atan2(shooter.getZ() - predictedTarget.z, shooter.getX() - predictedTarget.x);

        for (int i = 0; i < tries; i++) {
            double angle = baseAngle + Mth.nextDouble(rand, -Math.PI / 3, Math.PI / 3);
            double jitter = (rand.nextDouble() * 2.0 - 1.0) * Math.min(2.0, currentDist * 0.3);
            double dist = Math.max(1.5, currentDist + jitter);
            Vec3 pos = candidateFromAngle(level, shooter, predictedTarget, angle, dist);
            if (pos != null) return pos;
        }
        return null;
    }

    private static @Nullable Vec3 candidateFromAngle(ServerLevel level, PathfinderMob shooter, Vec3 center, double angle, double distance) {
        int tx = Mth.floor(center.x + Math.cos(angle) * distance);
        int tz = Mth.floor(center.z + Math.sin(angle) * distance);

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
        int shooterY = shooter.blockPosition().getY();
        int ty = (Math.abs(surfaceY - shooterY) < 12) ? surfaceY : shooterY;

        Vec3 candidate = Vec3.atBottomCenterOf(new BlockPos(tx, ty, tz));
        Vec3 eye = candidate.add(0.0, shooter.getEyeHeight(), 0.0);
        Vec3 targetEye = center.add(0.0, 1.5, 0.0);

        return isLineOfSightClear(level, shooter, eye, targetEye) ? candidate : null;
    }

    private static Vec3 predictTargetPosition(LivingEntity target, double scale) {
        return target.position().add(target.getDeltaMovement().scale(scale));
    }

    public static boolean isLineOfSightClear(ServerLevel level, LivingEntity shooter, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }
}
