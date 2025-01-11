package de.teamlapen.vampirism.client.renderer;

import de.teamlapen.vampirism.client.renderer.entity.layers.ConvertedVampireEntityLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.ConvertedOverlayRenderState;
import de.teamlapen.vampirism.entity.converted.VampirismEntityRegistry;
import de.teamlapen.vampirism.mixin.client.accessor.EntityRenderDispatcherAccessor;
import de.teamlapen.vampirism.mixin.client.accessor.LivingEntityRendererAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class VampirismClientEntityRegistry extends VampirismEntityRegistry {

    private static final Logger LOGGER = LogManager.getLogger();

    public VampirismClientEntityRegistry() {
    }

    public <I extends LivingEntity, S extends LivingEntityRenderState & ConvertedOverlayRenderState, U extends EntityModel<S>> void syncOverlays() {
        for (EntityType<?> type : getConvertibleOverlay().keySet()) {
            LivingEntityRenderer<I, S, U> render = (LivingEntityRenderer<I, S, U>) ((EntityRenderDispatcherAccessor) Minecraft.getInstance().getEntityRenderDispatcher()).renderers().get(type);
            if (render == null) {
                LOGGER.error("Did not find renderer for {}", type);
                continue;
            }
            if (((LivingEntityRendererAccessor) render).getLayers().stream().noneMatch(s -> s instanceof ConvertedVampireEntityLayer<?, ?>)) {
                render.addLayer(new ConvertedVampireEntityLayer<>(render, true));
            }
        }
    }
}
