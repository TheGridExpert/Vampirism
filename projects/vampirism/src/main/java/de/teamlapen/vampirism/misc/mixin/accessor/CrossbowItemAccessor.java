package de.teamlapen.vampirism.misc.mixin.accessor;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CrossbowItem.class)
public interface CrossbowItemAccessor {

    @Invoker("getProjectileShotVector")
    static Vector3f invokeGetProjectileShotVector(LivingEntity body, Vec3 originalVector, float angle) {
        throw new IllegalStateException("Mixin failed to apply");
    }

    @Invoker("getShotPitch")
    static float invokeGetShotPitch(RandomSource random, int index) {
        throw new IllegalStateException("Mixin failed to apply");
    }
}
