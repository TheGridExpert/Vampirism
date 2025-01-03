package de.teamlapen.vampirism.util;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.entity.VampirismEntity;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import org.joml.Matrix4f;

import java.util.Objects;

public class TaskStatusRendererUtil {

    private static final ResourceLocation QUEST_ACCEPTED_TEXTURE = ResourceLocation.fromNamespaceAndPath(REFERENCE.MODID, "textures/entity/status/quest_accepted_status.png");
    private static final ResourceLocation QUEST_COMPLETED_TEXTURE = ResourceLocation.fromNamespaceAndPath(REFERENCE.MODID, "textures/entity/status/quest_completed_status.png");

    public static void renderStatusCloud(MobRenderer<?, ?> renderer, VampirismEntity entity, PoseStack poseStack, boolean hasHat) {
        Status status = getCloudStatus(entity);
        if (status == Status.NONE) return;

        double distance = renderer.entityRenderDispatcher.distanceToSqr(entity);
        double startVanishDistance = 16 * 16;
        double stopVanishDistance = 28 * 28;

        float transparency = 1.0f;
        if (distance > startVanishDistance) {
            transparency = (float) Math.max(0.0, 1.0 - ((Math.sqrt(distance) - 16.0) / 8.0));
        }
        if (distance >= stopVanishDistance) {
            return;
        }

        float textureScaleX = 16;
        float textureScaleY = 16;
        ResourceLocation texture = status == Status.QUEST_ACCEPTED ? QUEST_ACCEPTED_TEXTURE : QUEST_COMPLETED_TEXTURE;

        int y = hasHat ? -16 : -12;

        poseStack.pushPose();
        poseStack.translate(0.0d, entity.getBbHeight() + 0.5d, 0.0d);
        poseStack.mulPose(renderer.entityRenderDispatcher.cameraOrientation());
        poseStack.scale(0.05f, -0.05f, 0.05f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();

        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, texture);

        Lighting.setupLevel();

        BufferBuilder bufferBuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        Matrix4f matrix = poseStack.last().pose();

        bufferBuilder.addVertex(matrix, -textureScaleX / 2, y + textureScaleY, 0).setUv(0, 1).setColor(1.0f, 1.0f, 1.0f, transparency);
        bufferBuilder.addVertex(matrix, textureScaleX / 2, y + textureScaleY, 0).setUv(1, 1).setColor(1.0f, 1.0f, 1.0f, transparency);
        bufferBuilder.addVertex(matrix, textureScaleX / 2, y, 0).setUv(1, 0).setColor(1.0f, 1.0f, 1.0f, transparency);
        bufferBuilder.addVertex(matrix, -textureScaleX / 2, y, 0).setUv(0, 0).setColor(1.0f, 1.0f, 1.0f, transparency);

        BufferUploader.drawWithShader(Objects.requireNonNull(bufferBuilder.build()));

        RenderSystem.disableDepthTest();
        RenderSystem.disableBlend();

        poseStack.popPose();
    }

    // TODO: Fix this, it can't get if there are any completed or accepted tasks
    public static TaskStatusRendererUtil.Status getCloudStatus(VampirismEntity entity) {
        Player player = VampirismMod.proxy.getClientPlayer();

        if (player != null) {
            return FactionPlayerHandler.getCurrentFactionPlayer(player).map(IFactionPlayer::getTaskManager).map(taskManager -> {
                if (taskManager.hasCompletedTasks(entity.getUUID()) || taskManager.hasCompletedTasks(entity.getUUID())) {
                    return TaskStatusRendererUtil.Status.QUEST_COMPLETED;
                } else if (taskManager.hasAcceptedTasks(entity.getUUID())) {
                    return TaskStatusRendererUtil.Status.QUEST_ACCEPTED;
                }
                return TaskStatusRendererUtil.Status.NONE;
            }).orElse(TaskStatusRendererUtil.Status.NONE);
        }

        return TaskStatusRendererUtil.Status.NONE;
    }

    public enum Status {
        NONE,
        QUEST_ACCEPTED,
        QUEST_COMPLETED
    }
}
