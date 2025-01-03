package de.teamlapen.vampirism.blocks;

import de.teamlapen.vampirism.core.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.DeadBushBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class CursedRootsBlock extends DeadBushBlock {

    public CursedRootsBlock(BlockBehaviour.@NotNull Properties properties) {
        super(properties);
    }

    @Override
    protected boolean mayPlaceOn(@NotNull BlockState blockState, @NotNull BlockGetter blockReader, @NotNull BlockPos pos) {
        return blockState.is(ModTags.Blocks.CURSED_EARTH);
    }
}
