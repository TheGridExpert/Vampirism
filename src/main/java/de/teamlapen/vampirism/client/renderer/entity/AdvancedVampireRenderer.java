package de.teamlapen.vampirism.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entity.layers.AdvancedVampireEyeLayer;
import de.teamlapen.vampirism.client.renderer.entity.layers.AdvancedVampireFangLayer;
import de.teamlapen.vampirism.client.renderer.entity.layers.PlayerFaceOverlayLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.AdvancedVampireRenderState;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.entity.vampire.AdvancedVampireEntity;
import de.teamlapen.vampirism.util.TextureComparator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

/**
 * Render the advanced vampire with overlays
 */
public class AdvancedVampireRenderer extends HumanoidMobRenderer<AdvancedVampireEntity, AdvancedVampireRenderState, HumanoidModel<AdvancedVampireRenderState>> {
    private final ResourceLocation texture = VResourceLocation.mod("textures/entity/advanced_vampire.png");
    private final ResourceLocation @NotNull [] textures;


    public AdvancedVampireRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new ClothedModel<>(context.bakeLayer(ModEntitiesRender.GENERIC_BIPED), false), 0.5F);
        if (VampirismConfig.CLIENT.renderAdvancedMobPlayerFaces.get()) {
            this.addLayer(new PlayerFaceOverlayLayer<>(this));
            this.addLayer(new AdvancedVampireEyeLayer(this));
            this.addLayer(new AdvancedVampireFangLayer(this));

        }
        this.textures = Minecraft.getInstance().getResourceManager().listResources("textures/entity/vampire", s -> s.getPath().endsWith(".png")).keySet().stream().filter(r -> REFERENCE.MODID.equals(r.getNamespace())).sorted(TextureComparator.alphaNumericComparator()).toArray(ResourceLocation[]::new);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull AdvancedVampireRenderState entity) {
        return entity.bodyTexture;
    }

    @Override
    public @NotNull AdvancedVampireRenderState createRenderState() {
        return new AdvancedVampireRenderState();
    }

    @Override
    public void extractRenderState(@NotNull AdvancedVampireEntity entity, @NotNull AdvancedVampireRenderState renderState, float p_363123_) {
        super.extractRenderState(entity, renderState, p_363123_);
        renderState.bodyTexture = this.textures.length == 0 ? this.texture : this.textures[entity.getBodyTexture() % this.textures.length];
        renderState.fangType = entity.getFangType();
        renderState.eyeType = entity.getEyeType();
        renderState.overlay = entity.getPlayerOverlay().map(p -> Minecraft.getInstance().getSkinManager().getInsecureSkin(p)).map(PlayerSkin::texture).orElseGet(DefaultPlayerSkin::getDefaultTexture);
    }

    @Override
    protected void renderNameTag(@NotNull AdvancedVampireRenderState entityIn, @NotNull Component displayNameIn, @NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn) {
        double dist = this.entityRenderDispatcher.distanceToSqr(entityIn.x, entityIn.y, entityIn.z);
        if (dist <= 256) {
            super.renderNameTag(entityIn, displayNameIn, matrixStackIn, bufferIn, packedLightIn);
        }
    }

}
