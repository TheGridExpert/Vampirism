package de.teamlapen.vampirism.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.teamlapen.lib.lib.client.VertexUtils;
import de.teamlapen.vampirism.blockentity.AltarInspirationBlockEntity;
import de.teamlapen.vampirism.client.core.ModMaterials;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import org.jetbrains.annotations.NotNull;

public class AltarInspirationBESR implements BlockEntityRenderer<AltarInspirationBlockEntity> {

    public AltarInspirationBESR(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AltarInspirationBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        float fillPercentage = blockEntity.getFillPercentage();
        if (fillPercentage > 0.0F) {
            poseStack.pushPose();
            poseStack.translate(0.5, 1/16f, 0.5);
            poseStack.scale(8/16f, 11/16f, 8/16f);
            VertexConsumer buffer = ModMaterials.BLOOD_MATERIAL.buffer(bufferSource, RenderType::entityCutout);
            VertexUtils.addCube(buffer, poseStack, 1, fillPercentage, packedLight, -1);
            poseStack.popPose();
        }
    }
}
