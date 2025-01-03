package de.teamlapen.vampirism.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.renderer.entity.layers.TaskMasterTypeLayer;
import de.teamlapen.vampirism.client.renderer.entity.layers.VampireEntityLayer;
import de.teamlapen.vampirism.entity.vampire.VampireTaskMasterEntity;
import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.util.TaskStatusRendererUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Render the advanced vampire with overlays
 */
public class VampireTaskMasterRenderer extends MobRenderer<VampireTaskMasterEntity, VillagerModel<VampireTaskMasterEntity>> {
    private final static ResourceLocation texture = VResourceLocation.mc("textures/entity/villager/villager.png");
    private final static ResourceLocation vampireOverlay = VResourceLocation.mod("textures/entity/vanilla/villager_overlay.png");
    private final static ResourceLocation overlay = VResourceLocation.mod("textures/entity/vampire_task_master_overlay.png");

    public VampireTaskMasterRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new VillagerModel<>(context.bakeLayer(ModEntitiesRender.TASK_MASTER)), 0.5F);
//        this.addLayer(new HeldItemLayer<>(this));
        this.addLayer(new VampireEntityLayer<>(this, vampireOverlay));
        this.addLayer(new TaskMasterTypeLayer<>(this, overlay));
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull VampireTaskMasterEntity entity) {
        return texture;
    }

    @Override
    protected void renderNameTag(@NotNull VampireTaskMasterEntity entityIn, @NotNull Component displayNameIn, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn, float partialTicks) {
        double dist = this.entityRenderDispatcher.distanceToSqr(entityIn);
        if (dist <= 128) {
            super.renderNameTag(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn, packedLightIn);
        }
    }

    @Override
    public void render(@NotNull VampireTaskMasterEntity entity, float entityYaw, float partialTicks, @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer, int packedLight) {
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

        //TaskStatusRendererUtil.renderStatusCloud(this, entity, poseStack, false);
    }

    @Override
    protected boolean shouldShowName(@NotNull VampireTaskMasterEntity pEntity) {
        return Helper.isVampire(Minecraft.getInstance().player) && super.shouldShowName(pEntity);
    }
}
