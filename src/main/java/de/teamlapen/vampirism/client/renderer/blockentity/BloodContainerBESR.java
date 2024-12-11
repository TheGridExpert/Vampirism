package de.teamlapen.vampirism.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.teamlapen.lib.lib.client.VertexUtils;
import de.teamlapen.vampirism.blockentity.BloodContainerBlockEntity;
import de.teamlapen.vampirism.client.core.ModMaterials;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class BloodContainerBESR implements BlockEntityRenderer<BloodContainerBlockEntity> {

    public BloodContainerBESR(BlockEntityRendererProvider.Context pContext) {

    }

    @Override
    public void render(BloodContainerBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ModelData modelData = blockEntity.getModelData();
        var level = modelData.get(BloodContainerBlockEntity.FLUID_LEVEL_PROP);
        if (level != null && level > 0) {
            float filled = Math.clamp(blockEntity.getFluid().getAmount() / (float) BloodContainerBlockEntity.CAPACITY,0f,1f);
            Boolean b = modelData.get(BloodContainerBlockEntity.FLUID_IMPURE);
            var material = b != null && b ? ModMaterials.IMPURE_BLOOD_MATERIAL : ModMaterials.BLOOD_MATERIAL;
            poseStack.pushPose();
            poseStack.translate(0.5, 1/16f, 0.5);
            poseStack.scale(10f/16f,14f/16f,10f/16f);
            VertexConsumer consumer = material.buffer(bufferSource, RenderType::entityCutout);
            VertexUtils.addCube(consumer, poseStack, 1, filled, packedLight, -1);
            poseStack.popPose();
        }
    }
}
