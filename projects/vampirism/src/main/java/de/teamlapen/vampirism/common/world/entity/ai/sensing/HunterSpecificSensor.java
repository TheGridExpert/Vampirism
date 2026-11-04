package de.teamlapen.vampirism.common.world.entity.ai.sensing;

import de.teamlapen.vampirism.common.core.ModEffects;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class HunterSpecificSensor extends Sensor<LivingEntity> {

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

        for (LivingEntity target : nearestVisibleEntities.findAll(target -> true)) {
            if (target instanceof Hunter) {
                hunters.add(target);
                continue;
            }

            if (HunterAi.isEnemy(target, hunter)) {
                hostiles.add(target);
            } else if (target.hasEffect(ModEffects.SANGUINARE)) {
                infectedEntities.add(target);
            }
        }

        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_HUNTERS.get(), hunters);
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), hostiles);
        brain.setMemory(ModMemoryTypes.NEAREST_VISIBLE_INFECTED_ENTITIES.get(), infectedEntities);

        for (LivingEntity ally : hunters) {
            if (ally instanceof Hunter allyHunter && allyHunter.isAlive()) {
                allyHunter.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    if (!brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                        if (HunterAi.isEnemy(target, hunter) && hunter.distanceToSqr(target) < 400.0D && level.getRandom().nextFloat() < 0.8F) {
                            HunterAi.setAngerTarget(level, hunter, target);
                        }
                    }
                });
            }
        }
    }
}
