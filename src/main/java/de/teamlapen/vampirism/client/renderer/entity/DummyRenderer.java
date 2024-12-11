package de.teamlapen.vampirism.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;


public class DummyRenderer<T extends Entity> extends EntityRenderer<T, EntityRenderState> {

    public DummyRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context);
    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    public boolean shouldRender(@NotNull T livingEntityIn, @NotNull Frustum camera, double camX, double camY, double camZ) {
        return false;
    }
}
