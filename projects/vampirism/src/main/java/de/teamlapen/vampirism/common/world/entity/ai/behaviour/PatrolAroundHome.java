package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.behavior.BlockPosTracker;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.WalkTarget;
import net.minecraft.world.level.levelgen.Heightmap;

public class PatrolAroundHome {

    public static OneShot<Hunter> create(int radius, float speed) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(MemoryModuleType.HOME),
                instance.absent(MemoryModuleType.WALK_TARGET),
                instance.absent(ModMemoryTypes.PATROL_COOLDOWN.get()),
                instance.absent(ModMemoryTypes.CURE_TARGET.get()),
                instance.absent(ModMemoryTypes.SQUAD_POSITION.get())
        ).apply(instance, (homeAcc, walkTargetAcc, patrolCooldownAcc, cureTargetAcc, squadPositionAcc) -> ((level, hunter, gameTime) -> {
            BlockPos homePos = instance.get(homeAcc).pos();
            RandomSource random = hunter.getRandom();

            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 2.0 + random.nextDouble() * (radius - 2.0);

            int targetX = homePos.getX() + Mth.floor(Math.cos(angle) * dist);
            int targetZ = homePos.getZ() + Mth.floor(Math.sin(angle) * dist);

            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, targetX, targetZ);
            int hunterY = hunter.blockPosition().getY();
            int targetY = (Math.abs(surfaceY - hunterY) < 12) ? surfaceY : hunterY;

            BlockPos target = new BlockPos(targetX, targetY, targetZ);
            hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(target), speed, 1));

            return true;
        })));
    }
}
