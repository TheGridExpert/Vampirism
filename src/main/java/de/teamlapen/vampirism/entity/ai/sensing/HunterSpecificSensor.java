package de.teamlapen.vampirism.entity.ai.sensing;

import com.google.common.collect.Lists;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import de.teamlapen.vampirism.entity.hunter.HunterAi;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.entity.ai.sensing.Sensor;

import java.util.List;
import java.util.Set;

public class HunterSpecificSensor extends Sensor<LivingEntity> {

    @Override
    public Set<MemoryModuleType<?>> requires() {
        return Set.of(
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES,
                ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get(),
                ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get()
        );
    }

    @Override
    protected void doTick(ServerLevel level, LivingEntity entity) {
        if (!(entity instanceof Hunter hunter)) return;

        Brain<?> brain = hunter.getBrain();

        List<LivingEntity> hunters = Lists.newArrayList();
        List<LivingEntity> hostiles = Lists.newArrayList();

        NearestVisibleLivingEntities nearestVisibleEntities = brain.getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());

        for (LivingEntity target : nearestVisibleEntities.findAll(target -> true)) {
            if (target instanceof Hunter) {
                hunters.add(target);
                continue;
            }

            if (HunterAi.isEnemy(target, hunter)) hostiles.add(target);
        }

        brain.setMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_HUNTERS.get(), hunters);
        brain.setMemory(ModMemoryModuleTypes.NEAREST_VISIBLE_HOSTILES.get(), hostiles);

        for (LivingEntity ally : hunters) {
            if (ally instanceof Hunter allyHunter && allyHunter.isAlive()) {
                Brain<Hunter> allyBrain = allyHunter.getBrain();

                allyBrain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(target -> {
                    if (!brain.hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) {
                        if (HunterAi.isEnemy(target, hunter) && hunter.distanceToSqr(target) < 400.0D && level.random.nextFloat() < 0.8F) {
                            HunterAi.setAngerTarget(level, hunter, target);
                        }
                    }
                });
            }
        }
    }
}
