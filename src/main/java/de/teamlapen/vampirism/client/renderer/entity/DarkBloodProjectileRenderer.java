package de.teamlapen.vampirism.client.renderer.entity;

import de.teamlapen.vampirism.entity.DarkBloodProjectileEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class DarkBloodProjectileRenderer extends EntityRenderer<DarkBloodProjectileEntity, EntityRenderState> {

    public DarkBloodProjectileRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context);

    }

    @Override
    public @NotNull EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

}
