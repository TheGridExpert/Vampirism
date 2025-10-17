package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import de.teamlapen.vampirism.entity.hunter.Hunter;
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
                instance.absent(ModMemoryModuleTypes.PATROL_COOLDOWN.get()),
                instance.absent(ModMemoryModuleTypes.CURE_TARGET.get())
        ).apply(instance, (homeAcc, walkTargetAcc, patrolCooldownAcc, cureTargetAcc) -> ((level, hunter, gameTime) -> {
            BlockPos homePos = instance.get(homeAcc).pos();
            RandomSource random = hunter.getRandom();

            double angle = random.nextDouble() * Math.PI * 2.0;
            double dist = 2.0 + random.nextDouble() * (radius - 2.0);

            int tx = homePos.getX() + Mth.floor(Math.cos(angle) * dist);
            int tz = homePos.getZ() + Mth.floor(Math.sin(angle) * dist);

            int surfaceY = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, tx, tz);
            int hunterY = hunter.blockPosition().getY();
            int ty = (Math.abs(surfaceY - hunterY) < 12) ? surfaceY : hunterY;

            BlockPos target = new BlockPos(tx, ty, tz);

            hunter.getBrain().setMemory(MemoryModuleType.WALK_TARGET, new WalkTarget(new BlockPosTracker(target), speed, 1));

            return true;
        })));
    }
}
