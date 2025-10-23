package de.teamlapen.vampirism.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.blockentity.AltarInfusionBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import org.joml.Matrix4f;

import java.util.List;

/**
 * Renders the beams for the altar of infusion
 */
public class AltarInfusionRenderer implements BlockEntityRenderer<AltarInfusionBlockEntity> {

    private final ResourceLocation INFUSION_BEAM_LOCATION = VResourceLocation.mod("textures/entity/infusion_beam.png");
    private final ResourceLocation BEACON_BEAM_LOCATION = VResourceLocation.mc("textures/entity/beacon_beam.png");
    
    public AltarInfusionRenderer(BlockEntityRendererProvider.Context context) {
    }
    
    @Override
    public void render(AltarInfusionBlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        AltarInfusionBlockEntity.Phase phase = blockEntity.getCurrentPhase();
        if (phase != AltarInfusionBlockEntity.Phase.BEAM1 && phase != AltarInfusionBlockEntity.Phase.BEAM2) {
            return; // Render the beam only when the ritual is running
        }

        BlockPos blockPos = blockEntity.getBlockPos();
        float centerX = blockPos.getX() + 0.5f;
        float centerY = blockPos.getY() + 3.0f;
        float centerZ = blockPos.getZ() + 0.5f;

        poseStack.pushPose();
        poseStack.translate(0.5, 3.0, 0.5);

        float animationOffset = -(blockEntity.getRunTime() + partialTick);

        List<BlockPos> tips = blockEntity.getTips();
        if (!tips.isEmpty()) {
            for (BlockPos tip : tips) {
                float dx = tip.getX() + 0.5f - centerX;
                float dy = tip.getY() + 0.5f - centerY;
                float dz = tip.getZ() + 0.5f - centerZ;
                renderBeam(poseStack, bufferSource, animationOffset, dx, dy, dz, packedLight, true);
            }
        }

        if (phase == AltarInfusionBlockEntity.Phase.BEAM2) {
            blockEntity.getPlayer().ifPresent(player -> {
                float dx = (float) player.getX() - centerX;
                float dy = (float) player.getY() + 1.2f - centerY;
                float dz = (float) player.getZ() - centerZ;
                renderBeam(poseStack, bufferSource, animationOffset, dx, dy, dz, packedLight, false);
            });
        }

        poseStack.popPose();
    }

    /**
     * Renders a beam in the world, similar to the dragon healing beam
     */
    private void renderBeam(PoseStack poseStack, MultiBufferSource bufferSource, float tickOffset, float dx, float dy, float dz, int packedLight, boolean beacon) {
        float distFlat = Mth.sqrt(dx * dx + dz * dz);
        float dist = Mth.sqrt(dx * dx + dy * dy + dz * dz);

        poseStack.pushPose();

        poseStack.mulPose(Axis.YP.rotation((float) (-Math.atan2(dz, dx)) - (float) Math.PI / 2F));
        poseStack.mulPose(Axis.XP.rotation((float) (-Math.atan2(distFlat, dy)) - (float) Math.PI / 2F));

        VertexConsumer vertex = bufferSource.getBuffer(RenderType.entitySmoothCutout(beacon ? BEACON_BEAM_LOCATION : INFUSION_BEAM_LOCATION));

        PoseStack.Pose lastPose = poseStack.last();
        Matrix4f matrix = lastPose.pose();

        float texStart = tickOffset * 0.05f;
        float texEnd = dist / 32.0F + texStart;

        float prevX = 0.0F;
        float prevY = 0.2F;
        float prevU = 0.0F;

        for (int i = 1; i <= 8; i++) {
            float angle = (float) (i * Math.PI * 2F / 8.0F);
            float x = Mth.sin(angle) * 0.2F;
            float y = Mth.cos(angle) * 0.2F;
            float u = i / 8.0F;

            vertex.addVertex(matrix, prevX, prevY, 0.0F)
                    .setColor(75, 0, 0, 255)
                    .setUv(prevU, texStart)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(lastPose, 0.0F, -1.0F, 0.0F);

            vertex.addVertex(matrix, prevX * 0.5f, prevY * 0.5f, dist)
                    .setColor(255, 0, 0, 255)
                    .setUv(prevU, texEnd)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(lastPose, 0.0F, -1.0F, 0.0F);

            vertex.addVertex(matrix, x * 0.5f, y * 0.5f, dist)
                    .setColor(255, 0, 0, 255)
                    .setUv(u, texEnd)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(lastPose, 0.0F, -1.0F, 0.0F);

            vertex.addVertex(matrix, x, y, 0.0F)
                    .setColor(75, 0, 0, 255)
                    .setUv(u, texStart)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(packedLight)
                    .setNormal(lastPose, 0.0F, -1.0F, 0.0F);

            prevX = x;
            prevY = y;
            prevU = u;
        }

        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(AltarInfusionBlockEntity blockEntity) {
        return AABB.INFINITE;
    }
}
