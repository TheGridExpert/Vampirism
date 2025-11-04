package de.teamlapen.vampirism.sit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public interface ISittableBlock {

    /**
     * Gets position where player dismounts from this sittable block.
     * Defaulted to the block top.
     */
    Vec3 getStandUpLocation(Level level, BlockPos pos, Entity entity, Direction facing);

    /**
     * Gets the rotation the sit entity will face when the player sits on the block.
     */
    float getSitRotation(BlockState state);
}
