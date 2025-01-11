package de.teamlapen.vampirism.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.convertible.IConvertedCreature;
import de.teamlapen.vampirism.client.renderer.entity.ConvertedCreatureRenderer;
import de.teamlapen.vampirism.client.renderer.entity.state.ConvertedOverlayRenderState;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;

/**
 * Render the vampire overlay for converted creatures
 */
public class ConvertedVampireEntityLayer<Z extends LivingEntityRenderState & ConvertedOverlayRenderState, U extends EntityModel<Z>> extends RenderLayer<Z, U> {

    public final boolean checkIfRender;

    /**
     * @param checkIfRender If it should check if {@link ConvertedCreatureRenderer#renderOverlay} is true
     */
    public ConvertedVampireEntityLayer(@NotNull RenderLayerParent<Z, U> entityRendererIn, boolean checkIfRender) {
        super(entityRendererIn);
        this.checkIfRender = checkIfRender;
    }

    @Override
    public void render(PoseStack stack, MultiBufferSource bufferSource, int packedLight, Z state, float p_117353_, float p_117354_) {
        if (!state.isInvisible) {
            ResourceLocation texture = state.convertedOverlay();
            if (texture == null) {
                texture = state.overlay();
            }
            if (texture != null) {
                renderColoredCutoutModel(this.getParentModel(), texture, stack, bufferSource, packedLight, state, -1);
            }
        }
    }
}
