package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.Optional;

public class PatrolAroundHome extends Behavior<Hunter> {

    public static final float MAX_DISTANCE_TO_PLAYER_TO_STOP = 2.5F;

    private final int radius;
    private final float speed;

    public PatrolAroundHome(int radius, float speed) {
        super(ImmutableMap.of(
                MemoryModuleType.HOME,
                MemoryStatus.VALUE_PRESENT,
                ModMemoryModuleTypes.PATROL_COOLDOWN.get(),
                MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.WALK_TARGET,
                MemoryStatus.REGISTERED,
                MemoryModuleType.NEAREST_VISIBLE_PLAYER,
                MemoryStatus.REGISTERED
        ), 20, 40);
        this.radius = radius;
        this.speed = speed;
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        // TODO: Make some cooldown to check if player is interacting with the hunter. If not, just looking, then continue going. If the player just approached, make the hunter guaranteed to look at him once at first
        //Optional<Player> playerOpt = hunter.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_PLAYER);
        //if (playerOpt.isPresent() && playerOpt.get().distanceTo(hunter) <= MAX_DISTANCE_TO_PLAYER_TO_STOP) return;

        hunter.getBrain().getMemory(MemoryModuleType.HOME).ifPresent(globalPos -> {
            BlockPos homePos = globalPos.pos();
            RandomSource rand = hunter.getRandom();

            double angle = rand.nextDouble() * Math.PI * 2.0;
            double dist = 2.0 + rand.nextDouble() * (this.radius - 2.0);

            int tx = homePos.getX() + Mth.floor(Math.cos(angle) * dist);
            int tz = homePos.getZ() + Mth.floor(Math.sin(angle) * dist);

            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
            int hunterY = hunter.blockPosition().getY();
            int ty = (Math.abs(surfaceY - hunterY) < 12) ? surfaceY : hunterY;

            BlockPos target = new BlockPos(tx, ty, tz);

            hunter.getBrain().setMemory(
                    MemoryModuleType.WALK_TARGET,
                    new WalkTarget(new BlockPosTracker(target), this.speed, 1)
            );
        });
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        return !hunter.getBrain().hasMemoryValue(MemoryModuleType.WALK_TARGET) && !hunter.getBrain().hasMemoryValue(ModMemoryModuleTypes.PATROL_COOLDOWN.get());
    }
}
