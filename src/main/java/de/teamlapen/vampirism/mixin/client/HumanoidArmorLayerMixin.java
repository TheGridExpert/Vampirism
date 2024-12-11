package de.teamlapen.vampirism.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.util.HumanoidArmorLayerData;
import de.teamlapen.vampirism.util.MixinHooks;
import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidArmorLayer.class)
public class HumanoidArmorLayerMixin<S extends HumanoidRenderState> {

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "HEAD"))
    private void catchRenderState(PoseStack p_117096_, MultiBufferSource p_117097_, int p_117098_, S p_363290_, float p_117100_, float p_117101_, CallbackInfo ci) {
        HumanoidArmorLayerData.setRenderState(p_363290_);
    }

    @Inject(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/renderer/entity/state/HumanoidRenderState;FF)V", at = @At(value = "RETURN"))
    private void clearRenderState(PoseStack p_117096_, MultiBufferSource p_117097_, int p_117098_, S p_363290_, float p_117100_, float p_117101_, CallbackInfo ci) {
        HumanoidArmorLayerData.reset();
    }

}
