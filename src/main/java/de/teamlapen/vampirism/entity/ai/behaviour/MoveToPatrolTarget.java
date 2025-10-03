package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;

public class MoveToPatrolTarget extends MoveToTargetSink {

    private final int minCooldown;
    private final int maxCooldown;

    public MoveToPatrolTarget(int minCooldown, int maxCooldown) {
        this(250, 400, minCooldown, maxCooldown);
    }

    public MoveToPatrolTarget(int minDuration, int maxDuration, int minCooldown, int maxCooldown) {
        super(minDuration, maxDuration);
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        super.stop(level, entity, gameTime);

        int cooldown = level.getRandom().nextInt(this.minCooldown, this.maxCooldown + 1);
        entity.getBrain().setMemoryWithExpiry(ModMemoryModuleTypes.PATROL_COOLDOWN.get(), Unit.INSTANCE, cooldown);
    }
}
