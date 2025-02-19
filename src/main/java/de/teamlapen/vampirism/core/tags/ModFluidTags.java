package de.teamlapen.vampirism.core.tags;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

public class ModFluidTags {
    public static final TagKey<Fluid> BLOOD = tag("blood");
    public static final TagKey<Fluid> IMPURE_BLOOD = tag("impure_blood");

    private static @NotNull TagKey<Fluid> tag(@NotNull String name) {
        return FluidTags.create(VResourceLocation.mod(name));
    }
}
