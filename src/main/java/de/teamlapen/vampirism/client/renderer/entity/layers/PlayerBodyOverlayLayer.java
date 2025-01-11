package de.teamlapen.vampirism.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.client.model.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entity.DualBipedRenderer;
import de.teamlapen.vampirism.client.renderer.entity.state.MinionRenderState;
import de.teamlapen.vampirism.client.renderer.entity.state.VisibilityPlayerRenderState;
import de.teamlapen.vampirism.entity.minion.MinionEntity;
import de.teamlapen.vampirism.util.IPlayerOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ColorRGBA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * This layer is supposed to render the minion with its texture or the player's body texture instead.
 * <br>
 * In this implementation the minion must use a {@link de.teamlapen.vampirism.client.renderer.entity.layers.PlayerBodyOverlayLayer.VisibilityPlayerModel} model which allows this layer to render the model depending on the relevant parts without changing the visibility of the {@link ModelPart}s
 *
 */
public class PlayerBodyOverlayLayer<S extends MinionRenderState, M extends PlayerBodyOverlayLayer.VisibilityPlayerModel<S>> extends RenderLayer<S, M> {

    public PlayerBodyOverlayLayer(@NotNull RenderLayerParent<S, M> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(@NotNull PoseStack stack, @NotNull MultiBufferSource bufferSource, int packedLight, S state, float p_117353_, float p_117354_) {
        ResourceLocation texture = state.skin.texture();
        RenderType type = getParentModel().getRenderType(getParentModel(), texture, state);

        if (state.renderLordSkin) {
            if (type != null) {
                getParentModel().setVisibility(VisibilityPlayerModel.Visibility.HEAD);
                getParentModel().renderToBuffer(stack, bufferSource.getBuffer(type), packedLight, OverlayTexture.NO_OVERLAY, -1);
            }

            texture = state.skin.texture();
            RenderType bodyType = getParentModel().getRenderType(this.getParentModel(), texture, state);
            if (bodyType != null) {
                getParentModel().setVisibility(VisibilityPlayerModel.Visibility.BODY);
                getParentModel().renderToBuffer(stack, bufferSource.getBuffer(bodyType), packedLight, OverlayTexture.NO_OVERLAY, -1);
            }
        } else if (type != null) {
            getParentModel().setVisibility(VisibilityPlayerModel.Visibility.ALL);
            getParentModel().renderToBuffer(stack, bufferSource.getBuffer(type), packedLight, OverlayTexture.NO_OVERLAY, -1);
        }
        getParentModel().setVisibility(VisibilityPlayerModel.Visibility.NONE);
    }

    /**
     * Default {@link PlayerModel} implementation that allows to hide the head and body parts without changing the {@link ModelPart#visible} property.
     */
    public static class VisibilityPlayerModel<T extends VisibilityPlayerRenderState> extends ClothedModel<T> {

        protected final List<ModelPart> bodyWithCloth;
        protected final List<ModelPart> headWithCloth;
        protected final List<ModelPart> all;

        public VisibilityPlayerModel(ModelPart pRoot, boolean pSlim) {
            super(pRoot, pSlim);
            this.bodyWithCloth = List.of(this.body, this.leftArm, this.rightArm, this.leftLeg, this.rightLeg, this.leftSleeve, this.rightSleeve, this.leftPants, this.rightPants, this.jacket);
            this.headWithCloth = List.of(this.head, this.hat);
            this.all = Stream.concat(this.bodyWithCloth.stream(), this.headWithCloth.stream()).toList();
        }

        public void setVisibility(@NotNull Visibility type) {
            this.bodyWithCloth.forEach(s -> s.visible = type.body);
            this.headWithCloth.forEach(s -> s.visible = type.head);
        }

        @Nullable
        public RenderType getRenderType(ClothedModel<T> model, ResourceLocation location, T state) {
            boolean pBodyVisible = !state.isInvisible;
            boolean translucent = !pBodyVisible && !state.isInvisibleToPlayer;
            boolean flag2 = state.appearsGlowing;
            if (translucent) {
                return RenderType.itemEntityTranslucentCull(location);
            } else if (pBodyVisible) {
                return model.renderType(location);
            } else {
                return flag2 ? RenderType.outline(location) : null;
            }
        }

        public enum Visibility {
            HEAD(true, false),
            BODY(false, true),
            NONE(false, false),
            ALL(true, true);

            private final boolean head;
            private final boolean body;

            Visibility(boolean head, boolean body) {
                this.head = head;
                this.body = body;
            }
        }
    }
}
