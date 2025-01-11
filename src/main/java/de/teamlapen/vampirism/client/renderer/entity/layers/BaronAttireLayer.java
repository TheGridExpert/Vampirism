package de.teamlapen.vampirism.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.BaronAttireModel;
import de.teamlapen.vampirism.client.model.BaronBaseModel;
import de.teamlapen.vampirism.client.model.BaronessAttireModel;
import de.teamlapen.vampirism.client.renderer.entity.VampireBaronRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;


/**
 * Render attire for baron. Includes Male and female version
 */
public class BaronAttireLayer extends RenderLayer<VampireBaronRenderer.VampireBaronRenderState, BaronBaseModel> {
    private final @NotNull BaronessAttireModel baroness;
    private final @NotNull BaronAttireModel baron;
    private final ResourceLocation textureBaroness = VResourceLocation.mod("textures/entity/baroness_attire.png");
    private final ResourceLocation textureBaron = VResourceLocation.mod("textures/entity/baron_attire.png");
    private final Predicate<VampireBaronRenderer.VampireBaronRenderState> predicateFemale;

    /**
     * @param predicateFemale used to choose between baron and baroness attire
     */
    public BaronAttireLayer(@NotNull RenderLayerParent<VampireBaronRenderer.VampireBaronRenderState, BaronBaseModel> entityRendererIn, EntityRendererProvider.@NotNull Context context, Predicate<VampireBaronRenderer.VampireBaronRenderState> predicateFemale) {
        super(entityRendererIn);
        this.baroness = new BaronessAttireModel(context.bakeLayer(ModEntitiesRender.BARONESS_ATTIRE));
        this.baron = new BaronAttireModel(context.bakeLayer(ModEntitiesRender.BARON_ATTIRE));
        this.predicateFemale = predicateFemale;
    }


    @Override
    public void render(@NotNull PoseStack matrixStackIn, @NotNull MultiBufferSource bufferIn, int packedLightIn, @NotNull VampireBaronRenderer.VampireBaronRenderState entityIn, float f1, float f2) {
        if (!entityIn.isInvisible) {
            boolean female = predicateFemale.test(entityIn);
            EntityModel<VampireBaronRenderer.VampireBaronRenderState> model = female ? baroness : baron;
            coloredCutoutModelCopyLayerRender(model, female ? textureBaroness : textureBaron, matrixStackIn, bufferIn, packedLightIn, entityIn, -1);
        }
    }


}