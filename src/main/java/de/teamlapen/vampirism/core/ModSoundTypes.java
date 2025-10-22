package de.teamlapen.vampirism.core;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.util.DeferredSoundType;

public class ModSoundTypes {

    public static final SoundType BAT_CAGE = new DeferredSoundType(
            1.0F,
            1.15F,
            () -> SoundEvents.CHAIN_BREAK,
            () -> SoundEvents.CHAIN_STEP,
            () -> SoundEvents.WOOD_PLACE,
            () -> SoundEvents.CHAIN_HIT,
            () -> SoundEvents.CHAIN_FALL
    );
}
