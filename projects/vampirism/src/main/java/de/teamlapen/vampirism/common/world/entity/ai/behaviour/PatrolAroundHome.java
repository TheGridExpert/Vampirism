package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import org.jspecify.annotations.Nullable;

public class PatrolAroundHome {

    private static final int MAX_TRIES = 16;

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
            PatrolZone zone = hunter.getSquad() != null ? hunter.getSquad().getPatrolZone(level, area) : null;
            for (int i = 0; i < MAX_TRIES; i++) {
                BlockPos target = findDryTarget(level, hunter, area, zone);
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

    private static @Nullable BlockPos findDryTarget(ServerLevel level, Hunter hunter, PatrolArea area, @Nullable PatrolZone zone) {
        BlockPos sample = area.pickRandomPosition(hunter.getRandom());
        int targetX = sample.getX();
        int targetZ = sample.getZ();
        if (zone != null && !zone.hasColumn(targetX, targetZ)) return null;

        int surface = zone != null ? zone.getSurface(targetX, targetZ) : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
        BlockPos target = new BlockPos(targetX, surface, targetZ);
        if (level.getFluidState(target.below()).is(FluidTags.WATER)) return null;
        if (level.getBrightness(LightLayer.SKY, target) == 0) return null;

        return target;
    }
}
