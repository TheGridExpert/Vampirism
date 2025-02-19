package de.teamlapen.vampirism.client.renderer.entity;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.BasicHunterModel;
import de.teamlapen.vampirism.client.renderer.entity.layers.CloakLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.BasicHunterRenderState;
import de.teamlapen.vampirism.entity.hunter.BasicHunterEntity;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


/**
 * There are differently looking level 0 hunters.
 * Hunter as of level 1 look all the same, but have different weapons
 */
public class BasicHunterRenderer extends DualBipedRenderer<BasicHunterEntity, BasicHunterRenderState, BasicHunterModel<BasicHunterRenderState>> {

    private static final ResourceLocation textureCloak = VResourceLocation.mod("textures/entity/hunter_cloak.png");

    private final @NotNull PlayerSkin[] textures;

    public BasicHunterRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new BasicHunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER), false), new BasicHunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER_SLIM), true), 0.5F);
        this.addLayer(new ArmorLayer<HumanoidModel<BasicHunterRenderState>>(this, new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getEquipmentRenderer()));
        this.addLayer(new CloakLayer<>(this, textureCloak, entity -> entity.entityLevel > 0));
        this.textures = gatherTextures("textures/entity/hunter", true);
    }


    @Override
    protected PlayerSkin determineTextureAndModel(@NotNull BasicHunterRenderState entity) {
        return entity.skin;
    }

    @Override
    public @NotNull BasicHunterRenderState createRenderState() {
        return new BasicHunterRenderState();
    }

    @Override
    public void extractRenderState(BasicHunterEntity entity, BasicHunterRenderState state, float p_363123_) {
        super.extractRenderState(entity, state, p_363123_);
        state.skin = textures[entity.getEntityTextureType() % textures.length];
        state.entityLevel = entity.getEntityLevel();

    }
}
