package de.teamlapen.vampirism.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entity.state.HunterMinionRenderState;
import de.teamlapen.vampirism.client.renderer.entity.state.HunterTrainerRenderState;
import de.teamlapen.vampirism.entity.VampirismEntity;
import de.teamlapen.vampirism.entity.hunter.HunterTrainerEntity;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;

public class HunterTrainerRenderer extends HumanoidMobRenderer<VampirismEntity, HunterTrainerRenderState, ClothedModel<HunterTrainerRenderState>> {

    private final ResourceLocation texture = VResourceLocation.mod("textures/entity/hunter_trainer.png");

    public HunterTrainerRenderer(EntityRendererProvider.@NotNull Context context, boolean renderEquipment) {
        super(context, new ClothedModel<>(context.bakeLayer(ModEntitiesRender.GENERIC_BIPED), false), 0.5F);
        if (renderEquipment) {
            this.addLayer(new HumanoidArmorLayer<>(this, new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getEquipmentRenderer()));
        }
    }


    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull HunterTrainerRenderState entity) {
        return texture;
    }

    @Override
    protected void renderNameTag(HunterTrainerRenderState state, @NotNull Component name, @NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        if (state.distanceToCameraSq <= 128) {
            super.renderNameTag(state, name, stack, bufferSource, packedLight);
        }
    }

    @Override
    public @NotNull HunterTrainerRenderState createRenderState() {
        return new HunterTrainerRenderState();
    }
}
