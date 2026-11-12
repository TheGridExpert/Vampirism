package de.teamlapen.vampirism.client.renderer.entities.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.client.models.entities.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entities.HunterRenderer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;

public class HunterItemInHandLayer extends ItemInHandLayer<HunterRenderer.HunterRenderState, ClothedModel<HunterRenderer.HunterRenderState>> {

    private static final float HEAD_MIN_X_ROT = (float) (-Math.PI / 6);
    private static final float HEAD_MAX_X_ROT = (float) (Math.PI / 2);
    private static final float EYE_OFFSET_X = 2.5F / 16.0F;
    private static final float EYE_OFFSET_Y = -0.0625F;

    public HunterItemInHandLayer(RenderLayerParent<HunterRenderer.HunterRenderState, ClothedModel<HunterRenderer.HunterRenderState>> renderer) {
        super(renderer);
    }

    @Override
    protected void submitArmWithItem(HunterRenderer.HunterRenderState state, ItemStackRenderState item, ItemStack itemStack, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        if (item.isEmpty()) return;

        InteractionHand hand = arm == state.mainArm ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (state.isUsingItem && state.useItemHand == hand && state.attackTime < 1.0E-5F && !state.heldOnHead.isEmpty()) {
            submitItemHeldToEye(state, arm, poseStack, submitNodeCollector, lightCoords);
        } else {
            super.submitArmWithItem(state, item, itemStack, arm, poseStack, submitNodeCollector, lightCoords);
        }
    }

    private void submitItemHeldToEye(HunterRenderer.HunterRenderState state, HumanoidArm arm, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int lightCoords) {
        poseStack.pushPose();
        this.getParentModel().root().translateAndRotate(poseStack);
        ModelPart head = this.getParentModel().getHead();
        float previousXRot = head.xRot;
        head.xRot = Mth.clamp(head.xRot, HEAD_MIN_X_ROT, HEAD_MAX_X_ROT);
        head.translateAndRotate(poseStack);
        head.xRot = previousXRot;
        CustomHeadLayer.translateToHead(poseStack, CustomHeadLayer.Transforms.DEFAULT);
        poseStack.translate(arm == HumanoidArm.LEFT ? -EYE_OFFSET_X : EYE_OFFSET_X, EYE_OFFSET_Y, 0.0F);
        state.heldOnHead.submit(poseStack, submitNodeCollector, lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
        poseStack.popPose();
    }
}
