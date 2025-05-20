package de.teamlapen.vampirism.blockentity;

import de.teamlapen.vampirism.core.ModTiles;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TombstoneBlockEntity extends SignBlockEntity {

    public TombstoneBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModTiles.TOMBSTONE.get(), pos, blockState);
    }

    @Override
    public int getTextLineHeight() {
        return 12;
    }

    @Override
    public int getMaxTextLineWidth() {
        return 60;
    }
}
