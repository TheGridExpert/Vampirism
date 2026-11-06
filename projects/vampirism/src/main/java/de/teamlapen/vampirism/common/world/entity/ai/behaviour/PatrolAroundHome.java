package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public class PatrolAroundHome {

    private static final int MAX_TRIES = 8;

    public static OneShot<Hunter> create(int radius, float speed, float relaxedLeaderSpeed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.HOME),
                instance.absent(MemoryModuleType.WALK_TARGET),
                instance.absent(ModMemoryTypes.PATROL_COOLDOWN.get()),
                instance.absent(ModMemoryTypes.CURE_TARGET.get()),
                instance.absent(ModMemoryTypes.SQUAD_POSITION.get()),
                instance.registered(ModMemoryTypes.SQUAD_PATROL_AREA.get())
        ).apply(instance, (homeAcc, _, _, _, _, areaAcc) -> ((level, hunter, _) -> {
            BlockPos homePos = instance.get(homeAcc).pos();
            PatrolArea area = instance.tryGet(areaAcc).orElseGet(() -> new PatrolArea(homePos, hunter.getPatrolRadius(radius)));
            float walkSpeed = isRelaxedLeader(hunter, level.getGameTime()) ? relaxedLeaderSpeed : speed;
            for (int i = 0; i < MAX_TRIES; i++) {
                BlockPos target = findDryTarget(level, hunter, area);
                if (target != null) {
                    hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(target), walkSpeed, 1));
                    return true;
                }
            }

            return false;
        })));
    }

    private static boolean isRelaxedLeader(Hunter hunter, long gameTime) {
        HunterSquad squad = hunter.getSquad();
        return squad != null && squad.leader() == hunter && squad.members().size() > 1 && !squad.isOnGuard(gameTime);
    }

    private static @Nullable BlockPos findDryTarget(ServerLevel level, Hunter hunter, PatrolArea area) {
        BlockPos sample = area.pickRandomPosition(hunter.getRandom());
        int targetX = sample.getX();
        int targetZ = sample.getZ();

        int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        if (level.getFluidState(new BlockPos(targetX, surfaceY - 1, targetZ)).is(FluidTags.WATER)) return null;

        int hunterY = hunter.blockPosition().getY();
        int targetY = (Math.abs(surfaceY - hunterY) < 12) ? surfaceY : hunterY;

        return new BlockPos(targetX, targetY, targetZ);
    }
}
