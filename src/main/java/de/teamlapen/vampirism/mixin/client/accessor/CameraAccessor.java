package de.teamlapen.vampirism.mixin.client.accessor;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {

    @Invoker("move")
    void invoke_move(float pDistanceOffset, float pVerticalOffset, float pHorizontalOffset);
}
