package de.teamlapen.vampirism.client.renderer.entity;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entity.state.BasicHunterRenderState;
import de.teamlapen.vampirism.client.renderer.entity.state.BasicVampireRenderState;
import de.teamlapen.vampirism.entity.vampire.BasicVampireEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class BasicVampireRenderer extends HumanoidMobRenderer<BasicVampireEntity, BasicVampireRenderState, HumanoidModel<BasicVampireRenderState>> {

    private final ResourceLocation @NotNull [] textures;

    public BasicVampireRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new ClothedModel<>(context.bakeLayer(ModEntitiesRender.GENERIC_BIPED), false), 0.5F);
        textures = Minecraft.getInstance().getResourceManager().listResources("textures/entity/vampire", s -> s.getPath().endsWith(".png")).keySet().stream().filter(r -> REFERENCE.MODID.equals(r.getNamespace())).toArray(ResourceLocation[]::new);
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull BasicVampireRenderState entity) {
        return entity.texture;
    }

    public ResourceLocation getVampireTexture(int entityId) {
        return textures[entityId % textures.length];
    }

    @Override
    public @NotNull BasicVampireRenderState createRenderState() {
        return new BasicVampireRenderState();
    }

    @Override
    public void extractRenderState(BasicVampireEntity entity, BasicVampireRenderState state, float p_363123_) {
        super.extractRenderState(entity, state, p_363123_);
        state.texture = getVampireTexture(entity.getEntityTextureType());
    }
}
