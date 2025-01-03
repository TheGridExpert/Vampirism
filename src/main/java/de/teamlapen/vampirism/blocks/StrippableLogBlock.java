package de.teamlapen.vampirism.blocks;

import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StrippableLogBlock extends LogBlock {
    private final Supplier<? extends LogBlock> strippedBlock;

    public StrippableLogBlock(BlockBehaviour.Properties properties, Supplier<? extends LogBlock> strippedLog) {
        super(properties);
        this.strippedBlock = strippedLog;
    }

    public StrippableLogBlock(MapColor color1, MapColor color2, Supplier<? extends LogBlock> strippedLog) {
        super(color1, color2);
        this.strippedBlock = strippedLog;
    }

    @Override
    public @Nullable BlockState getToolModifiedState(BlockState state, UseOnContext context, ItemAbility toolAction, boolean simulate) {
        if (toolAction == ItemAbilities.AXE_STRIP) {
            return getStrippedState(state);
        }
        return super.getToolModifiedState(state, context, toolAction, simulate);
    }

    private BlockState getStrippedState(BlockState state) {
        LogBlock strippedBlock = this.strippedBlock.get();
        return strippedBlock.defaultBlockState().setValue(RotatedPillarBlock.AXIS, state.getValue(RotatedPillarBlock.AXIS));
    }
}
