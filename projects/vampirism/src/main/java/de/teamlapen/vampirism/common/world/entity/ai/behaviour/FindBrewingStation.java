package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.BrewingStation;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.BrewingStations;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

public class FindBrewingStation {

    private static final int SEARCH_COOLDOWN = 200;

    public static OneShot<Hunter> create() {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.absent(ModMemoryTypes.BREWING_STATION.get()),
                instance.absent(ModMemoryTypes.BREWING_SEARCH_COOLDOWN.get()),
                instance.absent(ModMemoryTypes.PASTIME.get()),
                instance.absent(MemoryModuleType.ATTACK_TARGET),
                instance.absent(ModMemoryTypes.SQUAD_POSITION.get())
        ).apply(instance, (stationAcc, cooldownAcc, _, _, _) -> (level, hunter, _) -> {
            if (!hunter.isAlchemistClass() || !hunter.needsPotions() || hunter.getPost() == null) return false;
            if (!BrewingStations.isRelaxed(level, hunter)) return false;

            cooldownAcc.setWithExpiry(Unit.INSTANCE, SEARCH_COOLDOWN);
            BrewingStation station = BrewingStations.find(level, hunter);
            if (station == null) return false;

            stationAcc.set(station);
            return true;
        }));
    }
}
