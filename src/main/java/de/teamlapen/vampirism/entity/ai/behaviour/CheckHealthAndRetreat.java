package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

public class CheckHealthAndRetreat extends Behavior<Hunter> {

    private final float retreatHealthPercent;
    private final int maxRetreatDuration;

    public CheckHealthAndRetreat(float retreatHealthPercent, int maxRetreatDuration) {
        super(ImmutableMap.of(
                ModMemoryModuleTypes.SHOULD_RETREAT.get(),
                MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET,
                MemoryStatus.REGISTERED
        ), 20, 40);
        this.retreatHealthPercent = retreatHealthPercent;
        this.maxRetreatDuration = maxRetreatDuration;
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        return hunter.getHealth() < hunter.getMaxHealth() * retreatHealthPercent;
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        hunter.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.SHOULD_RETREAT.get(), Unit.INSTANCE, maxRetreatDuration);
        hunter.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
    }
}

