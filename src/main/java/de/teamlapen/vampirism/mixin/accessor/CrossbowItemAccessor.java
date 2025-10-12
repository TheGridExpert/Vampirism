package de.teamlapen.vampirism.mixin.accessor;

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
    Vector3f invokeGetProjectileShotVector(LivingEntity shooter, Vec3 distance, float angle);

    @Invoker("getShotPitch")
    float invokeGetShotPitch(RandomSource random, int index);
}
