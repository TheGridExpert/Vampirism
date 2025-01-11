package de.teamlapen.vampirism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * ModelBiped with a cloak
 */
public class BipedCloakedModel<T extends PlayerRenderState> extends ClothedModel<T> {
    private static final String CLOAK = "cloak";
    protected final @NotNull ModelPart bipedCloak;

    public static @NotNull MeshDefinition createMesh(boolean smallArms) {
        MeshDefinition var2 = PlayerModel.createMesh(CubeDeformation.NONE, smallArms);
        PartDefinition var3 = var2.getRoot();
        var3.addOrReplaceChild(CLOAK, CubeListBuilder.create().texOffs(0, 0).addBox(-7, 0, 2f, 14, 20, 1), PartPose.offset(0, 0, 2));
        return var2;
    }

    public BipedCloakedModel(@NotNull ModelPart part, boolean smallArms) {
        super(part, smallArms);
        bipedCloak = part.getChild(CLOAK);
    }

    public void renderCustomCloak(@NotNull PoseStack matrixStackIn, @NotNull VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn) {
        this.bipedCloak.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn);
    }

    @Override
    public void setAllVisible(boolean visible) {
        super.setAllVisible(visible);
        bipedCloak.visible = visible;
    }

    @Override
    public void setupAnim(T state) {
        super.setupAnim(state);
        float f = 1.0F;
        if (state.fallFlyingTimeInTicks > 4) {
            f = state.speedValue;
            f /= 0.2F;
            f *= f * f;
        }

        if (f < 1.0F) {
            f = 1.0F;
        }
        if (state.isCrouching) {
            this.bipedCloak.y = 2.0F;
        } else {
            this.bipedCloak.y = 0.0F;
        }
        this.bipedCloak.xRot = Math.max(Mth.cos(state.walkAnimationPos * 0.6662F) * 1.4F * state.walkAnimationSpeed / f, Mth.cos(state.walkAnimationPos * 0.6662F + (float) Math.PI) * 1.4F * state.walkAnimationSpeed / f);
    }

}
