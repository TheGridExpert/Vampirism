package de.teamlapen.vampirism.common.world.entity.ai.sensing;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.sensing.Sensor;
import net.minecraft.world.entity.monster.Creeper;

import java.util.List;
import java.util.Set;

public class HunterDangerSensor extends Sensor<LivingEntity> {

    private static final double AVOID_RADIUS = 12.0D;
    private static final double SWELLING_CREEPER_RADIUS = 7.0D;
    private static final double CHARGED_CREEPER_RADIUS = 10.0D;
    private static final double IDLE_CREEPER_RADIUS = 3.5D;

    public HunterDangerSensor() {
        super(5);
    }

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(MemoryModuleType.NEAREST_LIVING_ENTITIES, ModMemoryTypes.DANGER.get());
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        Brain<?> brain = entity.getBrain();
        List<LivingEntity> nearby = brain.getMemory(MemoryModuleType.NEAREST_LIVING_ENTITIES).orElse(List.of());

        for (LivingEntity other : nearby) {
            if (other.isAlive() && isDangerous(entity, other)) {
                brain.setMemory(ModMemoryTypes.DANGER.get(), other);
                return;
            }
        }

        brain.eraseMemory(ModMemoryTypes.DANGER.get());
    }

    private static boolean isDangerous(LivingEntity hunter, LivingEntity other) {
        double distance = hunter.distanceTo(other);
        if (HunterThreats.isAvoided(other)) return distance < AVOID_RADIUS;

        if (other instanceof Creeper creeper) {
            boolean swelling = creeper.getSwellDir() > 0 || creeper.isIgnited();
            double radius = swelling ? (creeper.isPowered() ? CHARGED_CREEPER_RADIUS : SWELLING_CREEPER_RADIUS) : IDLE_CREEPER_RADIUS;
            return distance < radius;
        }

        return false;
    }
}
