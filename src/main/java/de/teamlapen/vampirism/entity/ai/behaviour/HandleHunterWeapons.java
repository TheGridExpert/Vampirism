package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class HandleHunterWeapons {

    private static final int SHEATH_DELAY_TICKS = 100;

    public static class Unsheathe extends Behavior<Hunter> {

        public Unsheathe() {
            super(ImmutableMap.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryStatus.VALUE_PRESENT,
                    ModMemoryModuleTypes.WEAPONS_UNSHEATHED.get(),
                    MemoryStatus.VALUE_ABSENT
            ));
        }

        @Override
        protected void start(ServerLevel level, Hunter hunter, long gameTime) {
            hunter.unsheatheWeapons();
            hunter.getBrain().setMemory(ModMemoryModuleTypes.WEAPONS_UNSHEATHED.get(), Unit.INSTANCE);
            hunter.getBrain().eraseMemory(ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get());
        }
    }

    public static class Sheathe extends Behavior<Hunter> {

        public Sheathe() {
            super(ImmutableMap.of(
                    MemoryModuleType.ATTACK_TARGET,
                    MemoryStatus.VALUE_ABSENT,
                    ModMemoryModuleTypes.WEAPONS_UNSHEATHED.get(),
                    MemoryStatus.VALUE_PRESENT
            ));
        }

        @Override
        protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
            Brain<Hunter> brain = hunter.getBrain();

            if (!brain.hasMemoryValue(ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get())) {
                brain.setMemory(ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get(), level.getGameTime() + SHEATH_DELAY_TICKS);
                return false;
            }

            long expiryTime = brain.getMemory(ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get()).orElse(0L);
            return level.getGameTime() >= expiryTime;
        }

        @Override
        protected void start(ServerLevel level, Hunter hunter, long gameTime) {
            hunter.sheatheWeapons();
            Brain<Hunter> brain = hunter.getBrain();
            brain.eraseMemory(ModMemoryModuleTypes.WEAPONS_UNSHEATHED.get());
            brain.eraseMemory(ModMemoryModuleTypes.WEAPON_SHEATH_COOLDOWN.get());
        }
    }
}
