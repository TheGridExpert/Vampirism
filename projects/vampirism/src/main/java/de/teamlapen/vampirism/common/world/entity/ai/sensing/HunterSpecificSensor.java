package de.teamlapen.vampirism.common.world.entity.ai.sensing;

import de.teamlapen.vampirism.common.core.ModEffects;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterAi;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class HunterSpecificSensor extends Sensor<LivingEntity> {

    private static final double NOTICE_RANGE_FACTOR = 0.75D;
    private static final double WATER_VISIBILITY_FACTOR = 0.25D;
    private static final double ALLY_TARGET_RANGE = 15.0D;
    private static final double SPYGLASS_RANGE_FACTOR = 2.5D;
    private static final double SPYGLASS_CONE_COS = Math.cos(Math.toRadians(20.0D));

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get(),
                ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(),
                ModMemoryTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get()
        );
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Hunter hunter)) return;

        Brain<?> brain = hunter.getBrain();

        List<LivingEntity> hunters = new ArrayList<>();
        List<LivingEntity> hostiles = new ArrayList<>();
        List<LivingEntity> infectedEntities = new ArrayList<>();

        NearestVisibleLivingEntities nearestVisibleEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        double noticeRange = hunter.getAttributeValue(Attributes.FOLLOW_RANGE) * NOTICE_RANGE_FACTOR;

        for (LivingEntity target : nearestVisibleEntities.findAll(_ -> true)) {
            if (!isNoticeable(level, hunter, target, noticeRange)) continue;

            if (target instanceof Hunter) {
                hunters.add(target);
                continue;
            }

            if (HunterAi.isEnemy(target, hunter)) {
                if (!HunterThreats.isDistraction(hunter, target)) hostiles.add(target);
            } else if (target.hasEffect(ModEffects.SANGUINARE)) {
                infectedEntities.add(target);
            }
        }

        if (hunter.isUsingSpyglass()) {
            addSpottedThroughSpyglass(level, hunter, getSpyglassRange(hunter), hostiles);
        }

        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get(), hunters);
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), hostiles);
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get(), infectedEntities);

        for (LivingEntity ally : hunters) {
            if (ally instanceof Hunter allyHunter && allyHunter.isAlive()) {
                allyHunter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    if (!brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                        if (HunterAi.isEnemy(target, hunter) && hunter.distanceToSqr(target) < ALLY_TARGET_RANGE * ALLY_TARGET_RANGE && level.getRandom().nextFloat() < 0.8F) {
                            HunterAi.setAngerTarget(level, hunter, target);
                        }
                    }
                });
            }
        }
    }

    private static double getSpyglassRange(Hunter hunter) {
        return hunter.getAttributeValue(Attributes.FOLLOW_RANGE) * NOTICE_RANGE_FACTOR * SPYGLASS_RANGE_FACTOR;
    }

    private static void addSpottedThroughSpyglass(ServerLevel level, Hunter hunter, double range, List<LivingEntity> hostiles) {
        Vec3 eye = hunter.getEyePosition();
        Vec3 view = hunter.getViewVector(1.0F);
        level.getEntitiesOfClass(LivingEntity.class, hunter.getBoundingBox().inflate(range), target -> {
            if (target == hunter || !target.isAlive() || target.isInvisible() || hostiles.contains(target)) return false;
            Vec3 toTarget = target.getEyePosition().subtract(eye);
            double distanceSqr = toTarget.lengthSqr();
            if (distanceSqr > range * range || toTarget.normalize().dot(view) < SPYGLASS_CONE_COS) return false;
            return HunterAi.isEnemy(target, hunter) && !HunterThreats.isDistraction(hunter, target) && hunter.hasLineOfSight(target) && !isSeenThroughWater(level, hunter, target);
        }).stream().sorted(Comparator.comparingDouble(hunter::distanceToSqr)).forEach(hostiles::add);
    }

    private static boolean isNoticeable(ServerLevel level, Hunter hunter, LivingEntity target, double noticeRange) {
        double distanceSqr = hunter.distanceToSqr(target);
        if (distanceSqr > noticeRange * noticeRange) return false;

        double waterRange = noticeRange * WATER_VISIBILITY_FACTOR;
        return distanceSqr <= waterRange * waterRange || !isSeenThroughWater(level, hunter, target);
    }

    private static boolean isSeenThroughWater(ServerLevel level, Hunter hunter, LivingEntity target) {
        if (hunter.isUnderWater() || target.isUnderWater()) return true;
        ClipContext context = new ClipContext(hunter.getEyePosition(), target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.WATER, hunter);
        return level.clip(context).getType() != HitResult.Type.MISS;
    }
}
