package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CheckIfSafeToStopRetreating extends Behavior<Hunter> {

    private final float safeHealthPercent;

    public CheckIfSafeToStopRetreating(float safeHealthPercent) {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.SHOULD_RETREAT.get(),
                MemoryStatus.VALUE_PRESENT
        ), 20, 40);
        this.safeHealthPercent = safeHealthPercent;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        return hunter.getHealth() > hunter.getMaxHealth() * safeHealthPercent;
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        hunter.getBrain().eraseMemory(ModMemoryModuleTypes.SHOULD_RETREAT.get());
    }
}
