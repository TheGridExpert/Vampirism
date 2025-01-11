package de.teamlapen.vampirism.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.teamlapen.vampirism.client.renderer.entity.GhostRenderer;
import de.teamlapen.vampirism.entity.GhostEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class GhostModel extends EntityModel<GhostRenderer.GhostRenderState> {

    private static final String HEAD = "head";
    private static final String BODY = "body";
    private static final String RIGHT_ARM = "right_arm";
    private static final String LEFT_ARM = "left_arm";

    public static LayerDefinition createMesh() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition body = partdefinition.addOrReplaceChild(BODY, CubeListBuilder.create().texOffs(0, 12).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 17.0F, 0.0F));
        PartDefinition head = body.addOrReplaceChild(HEAD, CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));
        PartDefinition arm_left = body.addOrReplaceChild(LEFT_ARM, CubeListBuilder.create().texOffs(16, 12).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, -3.0F, 0.0F, 0.0F, 0.0F, 0.1745F));
        PartDefinition arm_right = body.addOrReplaceChild(RIGHT_ARM, CubeListBuilder.create().texOffs(24, 12).addBox(0.0F, 0.0F, -1.0F, 2.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart right_arm;
    private final ModelPart left_arm;

    public GhostModel(@NotNull ModelPart part) {
        super(part);
        this.body = part.getChild(BODY);
        this.head = this.body.getChild(HEAD);
        this.right_arm = this.body.getChild(RIGHT_ARM);
        this.left_arm = this.body.getChild(LEFT_ARM);
    }

    @Override
    public void setupAnim(@NotNull GhostRenderer.GhostRenderState renderState) {
        this.body.getAllParts().forEach(ModelPart::resetPose);
        this.head.yRot = renderState.yRot / (180F / (float) Math.PI);
        this.head.xRot = renderState.xRot / (180F / (float) Math.PI);
        if (renderState.aggressive) {
            float f6 = Mth.sin(renderState.walkAnimationSpeed * (float) Math.PI);
            float f7 = Mth.sin((1.0F - (1.0F - renderState.walkAnimationSpeed) * (1.0F - renderState.walkAnimationSpeed)) * (float) Math.PI);
            this.right_arm.yRot = -(0.1F - f6 * 0.6F);
            this.left_arm.yRot = 0.1F - f6 * 0.6F;
            this.right_arm.xRot = -((float) Math.PI / 2F);
            this.left_arm.xRot = -((float) Math.PI / 2F);
            this.right_arm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.left_arm.xRot -= f6 * 1.2F - f7 * 0.4F;
            this.right_arm.zRot += Mth.cos(renderState.walkAnimationPos * 0.09F) * 0.05F + 0.05F;
            this.left_arm.zRot -= Mth.cos(renderState.walkAnimationPos * 0.09F) * 0.05F + 0.05F;
            this.right_arm.xRot += Mth.sin(renderState.walkAnimationPos * 0.067F) * 0.05F;
            this.left_arm.xRot -= Mth.sin(renderState.walkAnimationPos * 0.067F) * 0.05F;
        }

        float f3 = renderState.ageInTicks * 5.0F * ((float) Math.PI / 180F);
        float f4 = Math.min(renderState.walkAnimationSpeed / 0.3F, 1.0F);
        float f5 = 1.0F - f4;

        this.body.y += (float) Math.cos(f3) * 0.25F * f5;
    }

    public void setupAnim2(float pAgeInTicks) {
        this.body.getAllParts().forEach(ModelPart::resetPose);
        float f3 = pAgeInTicks * 5.0F * ((float) Math.PI / 180F);

        this.body.y += (float) Math.cos(f3) * 0.25F;
    }

}
