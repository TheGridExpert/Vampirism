package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.client.renderer.blockentity.BatCageRenderer;
import de.teamlapen.vampirism.common.world.entity.IllusoryBatEntity;
import net.minecraft.client.model.ambient.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;

public class IllusoryBatRenderer extends MobRenderer<IllusoryBatEntity, IllusoryBatRenderer.IllusoryBatRenderState, BatModel> {

    public IllusoryBatRenderer(EntityRendererProvider.Context context) {
        super(context, new BatModel(context.bakeLayer(ModelLayers.BAT)), 0.0f);
    }

    @Override
    public IllusoryBatRenderState createRenderState() {
        return new IllusoryBatRenderState();
    }

    @Override
    public void extractRenderState(IllusoryBatEntity bat, IllusoryBatRenderState state, float partialTick) {
        super.extractRenderState(bat, state, partialTick);
        state.isResting = false;
        state.flyAnimationState.copyFrom(bat.flyAnimationState);
        state.bodyRot = Mth.rotLerp(partialTick, bat.yRotO, bat.getYRot());
        state.yRot = 0f;
        state.transparency = bat.getCurrentTransparency(partialTick);
    }

    @Override
    public Identifier getTextureLocation(IllusoryBatRenderState state) {
        return BatCageRenderer.BAT_LOCATION;
    }

    @Override
    protected RenderType getRenderType(IllusoryBatRenderState state, boolean bodyVisible, boolean translucent, boolean glowing) {
        return RenderTypes.entityTranslucent(getTextureLocation(state));
    }

    @Override
    protected int getModelTint(IllusoryBatRenderState state) {
        return ARGB.white(state.transparency);
    }

    @Override
    protected void setupRotations(IllusoryBatRenderState state, PoseStack poseStack, float bodyRot, float scale) {
        super.setupRotations(state, poseStack, bodyRot, scale);
        poseStack.mulPose(Axis.XP.rotationDegrees(-state.xRot));
    }

    public static class IllusoryBatRenderState extends BatRenderState {
        public float transparency = 1.0f;
    }
}