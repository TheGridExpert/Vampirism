package de.teamlapen.vampirism.client.renderer.entity;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.HunterModel;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class HunterRenderer extends HumanoidMobRenderer<Hunter, PlayerRenderState, HunterModel<PlayerRenderState>> {

    private static final ResourceLocation TEXTURE_LOCATION = VResourceLocation.mod("textures/entity/hunter/hunter5_slim.png");

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER), false), new HunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER_SLIM), true), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull PlayerRenderState renderState) {
        return TEXTURE_LOCATION;
    }

    @Override
    public @NotNull PlayerRenderState createRenderState() {
        return new PlayerRenderState();
    }
}
