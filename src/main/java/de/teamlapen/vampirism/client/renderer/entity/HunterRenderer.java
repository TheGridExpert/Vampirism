package de.teamlapen.vampirism.client.renderer.entity;

import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.HunterModel;
import de.teamlapen.vampirism.client.renderer.entity.state.HunterRenderState;
import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public class HunterRenderer extends HumanoidMobRenderer<Hunter, HunterRenderState, HunterModel<HunterRenderState>> {

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new HunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER), false), new HunterModel<>(context.bakeLayer(ModEntitiesRender.HUNTER_SLIM), true), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(HunterRenderState renderState) {
        return renderState.texture;
    }

    @Override
    public HunterRenderState createRenderState() {
        return new HunterRenderState();
    }

    @Override
    public void extractRenderState(Hunter hunter, HunterRenderState renderState, float value) {
        super.extractRenderState(hunter, renderState, value);
        renderState.texture = hunter.getVariant().value().texture();
    }
}
