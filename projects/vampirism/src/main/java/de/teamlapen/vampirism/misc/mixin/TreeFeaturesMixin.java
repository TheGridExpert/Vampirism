package de.teamlapen.vampirism.misc.mixin;

import de.teamlapen.vampirism.common.world.structures.huntercamp.HunterCampStructure;
import net.minecraft.world.level.levelgen.feature.BambooFeature;
import net.minecraft.world.level.levelgen.feature.FallenTreeFeature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({TreeFeature.class, FallenTreeFeature.class, BambooFeature.class})
public abstract class TreeFeaturesMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void skipTreeFreeAreas(FeaturePlaceContext<?> context, CallbackInfoReturnable<Boolean> cir) {
        if (HunterCampStructure.isTreeFree(context.level(), context.origin())) {
            cir.setReturnValue(false);
        }
    }
}
