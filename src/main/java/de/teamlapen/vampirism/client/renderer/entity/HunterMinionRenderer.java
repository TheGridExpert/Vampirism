package de.teamlapen.vampirism.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.client.core.ModEntitiesRender;
import de.teamlapen.vampirism.client.model.HunterMinionModel;
import de.teamlapen.vampirism.client.renderer.entity.layers.PlayerBodyOverlayLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.HunterMinionRenderState;
import de.teamlapen.vampirism.entity.minion.HunterMinionEntity;
import net.minecraft.client.model.HumanoidArmorModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

/**
 * There are differently looking level 0 hunters.
 * Hunter as of level 1 look all the same, but have different weapons
 */
public class HunterMinionRenderer extends DualBipedRenderer<HunterMinionEntity, HunterMinionRenderState, PlayerBodyOverlayLayer.VisibilityPlayerModel<HunterMinionRenderState>> {
    private final PlayerSkin @NotNull [] textures;
    private final PlayerSkin @NotNull [] minionSpecificTextures;


    public HunterMinionRenderer(EntityRendererProvider.@NotNull Context context) {
        super(context, new PlayerBodyOverlayLayer.VisibilityPlayerModel<>(context.bakeLayer(ModEntitiesRender.GENERIC_BIPED), false), new PlayerBodyOverlayLayer.VisibilityPlayerModel<>(context.bakeLayer(ModEntitiesRender.GENERIC_BIPED_SLIM), true), 0.5F);
        this.textures = gatherTextures("textures/entity/hunter", true);
        this.minionSpecificTextures = gatherTextures("textures/entity/minion/hunter", false);
        this.addLayer(new PlayerBodyOverlayLayer<>(this));
        this.addLayer(new ArmorLayer<HumanoidModel<HunterMinionRenderState>>(this, new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM_OUTER_ARMOR)), new HumanoidArmorModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getEquipmentRenderer()));
    }

    public int getHunterTextureCount() {
        return this.textures.length;
    }

    public int getMinionSpecificTextureCount() {
        return this.minionSpecificTextures.length;
    }

    @Override
    protected PlayerSkin determineTextureAndModel(@NotNull HunterMinionRenderState entity) {
        return entity.skin;
    }

    @Override
    protected void renderNameTag(@NotNull HunterMinionRenderState state, @NotNull Component name, PoseStack stack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        stack.popPose();
        stack.translate(0, 0.4f, 0);
        super.renderNameTag(state, name, stack, bufferSource, packedLight);
        stack.pushPose();
    }

    @Override
    public @NotNull HunterMinionRenderState createRenderState() {
        return new HunterMinionRenderState();
    }

    @Override
    public void extractRenderState(@NotNull HunterMinionEntity entity, @NotNull HunterMinionRenderState state, float p_363123_) {
        super.extractRenderState(entity, state, p_363123_);
        state.skin = (entity.hasMinionSpecificSkin() && this.minionSpecificTextures.length > 0) ? minionSpecificTextures[entity.getHunterType() % minionSpecificTextures.length] : textures[entity.getHunterType() % textures.length];
    }

    @Override
    protected HumanoidModel.@NotNull ArmPose getArmPose(HunterMinionEntity entity, @NotNull HumanoidArm arm) {
        var hand = arm == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack itemstack = entity.getItemInHand(hand);
        if (itemstack.isEmpty()) {
            return HumanoidModel.ArmPose.EMPTY;
        } else {
            if (entity.getUsedItemHand() == hand && entity.getUseItemRemainingTicks() > 0) {
                ItemUseAnimation useanim = itemstack.getUseAnimation();
                if (useanim == ItemUseAnimation.CROSSBOW && hand == entity.getUsedItemHand()) {
                    return HumanoidModel.ArmPose.CROSSBOW_CHARGE;
                }
            } else if (!entity.swinging && itemstack.getItem() instanceof CrossbowItem && CrossbowItem.isCharged(itemstack)) {
                return HumanoidModel.ArmPose.CROSSBOW_HOLD;
            }

            HumanoidModel.ArmPose forgeArmPose = IClientItemExtensions.of(itemstack).getArmPose(entity, hand, itemstack);
            if (forgeArmPose != null) return forgeArmPose;

            return HumanoidModel.ArmPose.ITEM;
        }
    }
}