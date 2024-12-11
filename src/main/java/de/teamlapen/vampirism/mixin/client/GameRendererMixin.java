package de.teamlapen.vampirism.mixin.client;

import com.mojang.blaze3d.resource.CrossFrameResourcePool;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.entity.player.IVampirismPlayer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
//
//    @Shadow @Final private Minecraft minecraft;
//
//    @Shadow @Final private CrossFrameResourcePool resourcePool;
//
//    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/pipeline/RenderTarget;bindWrite(Z)V", shift = At.Shift.BEFORE))
//    public void s(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
//        if (((IVampirismPlayer) Minecraft.getInstance().player).getVampAtts().getVampSpecial().blood_vision) {
//            var chain =this.minecraft.getShaderManager().getPostChain(VResourceLocation.mc("blur"), LevelTargetBundle.MAIN_TARGETS);
//            chain.setUniform("Radius", 0.6f);
//            chain.process(this.minecraft.getMainRenderTarget(), this.resourcePool);
//
//            VampirismModClient.getINSTANCE().getRenderHandler().endBloodVisionBatch();
//        }
//    }
}
