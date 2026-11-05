package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.MoveToTargetSink;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class MoveToPatrolTarget extends MoveToTargetSink {

    private static final double MIN_COOLDOWN_FACTOR = 2.5D;
    private static final double MAX_COOLDOWN_FACTOR = 3.5D;
    private static final double FULL_PAUSE_DISTANCE = 8.0D;
    private static final double MIN_DISTANCE_SCALE = 0.3D;

    private final int minCooldown;
    private final int maxCooldown;
    private @Nullable Vec3 walkStart;

    public MoveToPatrolTarget(int minCooldown, int maxCooldown) {
        this(250, 400, minCooldown, maxCooldown);
    }

    public MoveToPatrolTarget(int minDuration, int maxDuration, int minCooldown, int maxCooldown) {
        super(minDuration, maxDuration);
        this.minCooldown = minCooldown;
        this.maxCooldown = maxCooldown;
    }

    @Override
    protected void start(ServerLevel level, Mob entity, long gameTime) {
        super.start(level, entity, gameTime);
        this.walkStart = entity.position();
    }

    @Override
    protected void stop(ServerLevel level, Mob entity, long gameTime) {
        super.stop(level, entity, gameTime);

        double walked = this.walkStart != null ? entity.position().distanceTo(this.walkStart) : FULL_PAUSE_DISTANCE;
        this.walkStart = null;

        RandomSource random = level.getRandom();
        double base = random.nextInt(this.minCooldown, this.maxCooldown + 1);
        double factor = Mth.nextDouble(random, MIN_COOLDOWN_FACTOR, MAX_COOLDOWN_FACTOR);
        double distanceScale = Mth.clamp(walked / FULL_PAUSE_DISTANCE, MIN_DISTANCE_SCALE, 1.0D);
        int cooldown = (int) Math.round(base * factor * distanceScale);

        entity.getBrain().setMemoryWithExpiry(ModMemoryTypes.PATROL_COOLDOWN.get(), Unit.INSTANCE, cooldown);
    }
}
