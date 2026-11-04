package de.teamlapen.vampirism.client.renderer.entities;

import de.teamlapen.vampirism.client.models.entities.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entities.state.AvatarLikeRenderState;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;

import java.util.HashMap;
import java.util.Map;

public class HunterRenderer extends DualBipedRenderer<Hunter, AvatarLikeRenderState, ClothedModel<AvatarLikeRenderState>> {

    private final Map<Identifier, PlayerSkin> skins = new HashMap<>();

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new ClothedModel<>(context.bakeLayer(ModelLayers.PLAYER), false), new ClothedModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
        this.addLayer(new ArmorLayer<HumanoidModel<AvatarLikeRenderState>>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_SLIM_ARMOR, context.getModelSet(), x -> new ClothedModel<>(x, true)),
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), x -> new ClothedModel<>(x, false)),
                context.getEquipmentRenderer()));
    }

    @Override
    public AvatarLikeRenderState createRenderState() {
        return new AvatarLikeRenderState();
    }

    @Override
    public void extractRenderState(Hunter hunter, AvatarLikeRenderState state, float partialTick) {
        super.extractRenderState(hunter, state, partialTick);
        state.skin = this.skins.computeIfAbsent(hunter.getVariant().value().texture(), HunterRenderer::createSkin);
    }

    private static PlayerSkin createSkin(Identifier texture) {
        PlayerModelType model = texture.getPath().endsWith("_slim.png") ? PlayerModelType.SLIM : PlayerModelType.WIDE;
        return new PlayerSkin(new ClientAsset.ResourceTexture(texture, texture), null, null, model, false);
    }
}
