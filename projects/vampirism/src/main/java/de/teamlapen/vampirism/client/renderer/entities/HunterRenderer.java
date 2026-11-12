package de.teamlapen.vampirism.client.renderer.entities;

import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.client.models.entities.ClothedModel;
import de.teamlapen.vampirism.client.renderer.entities.layers.HunterItemInHandLayer;
import de.teamlapen.vampirism.client.renderer.entities.state.AvatarLikeRenderState;
import de.teamlapen.vampirism.common.core.ModAttachments;
import de.teamlapen.vampirism.common.world.entity.hunter.AdvancedHunter;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.PlayerSkin;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import org.jspecify.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class HunterRenderer extends DualBipedRenderer<Hunter, HunterRenderer.HunterRenderState, ClothedModel<HunterRenderer.HunterRenderState>> {

    private static final float TITLE_SCALE = 0.9F;
    private static final int TITLE_OFFSET = -11;
    private static final int TITLE_COLOR = 0xD4D4D4;
    private static final Vec3 TITLE_ATTACHMENT = new Vec3(0.0D, -0.5D, 0.0D);

    private final Map<Identifier, PlayerSkin> skins = new HashMap<>();
    private final Map<String, PlayerSkin> supporterSkins = VampirismModClient.services().playerSkinHelper().getSkins();

    public HunterRenderer(EntityRendererProvider.Context context) {
        super(context, new ClothedModel<>(context.bakeLayer(ModelLayers.PLAYER), false), new ClothedModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true), 0.5F);
        this.addLayer(new ArmorLayer<HumanoidModel<HunterRenderState>>(this,
                ArmorModelSet.bake(ModelLayers.PLAYER_SLIM_ARMOR, context.getModelSet(), x -> new ClothedModel<>(x, true)),
                ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), x -> new ClothedModel<>(x, false)),
                context.getEquipmentRenderer()));
        this.layers.removeIf(layer -> layer instanceof ItemInHandLayer);
        this.addLayer(new HunterItemInHandLayer(this));
    }

    @Override
    public HunterRenderState createRenderState() {
        return new HunterRenderState();
    }

    @Override
    protected @Nullable Component getNameTag(Hunter hunter) {
        if (hunter instanceof AdvancedHunter advanced && !advanced.hasCustomName()) return advanced.getPersonalName();
        return super.getNameTag(hunter);
    }

    @Override
    public void extractRenderState(Hunter hunter, HunterRenderState state, float partialTick) {
        super.extractRenderState(hunter, state, partialTick);
        state.skin = findSkin(hunter);
        extractBanner(hunter, state);
        extractHeldOnHead(hunter, state);
        Component title = hunter instanceof AdvancedHunter advanced && !advanced.hasCustomName() ? advanced.getTitle() : null;
        state.title = title == null ? null : title.copy().withColor(TITLE_COLOR);
    }

    @Override
    protected HumanoidModel.ArmPose getArmPose(Hunter hunter, HumanoidArm arm) {
        InteractionHand hand = arm == hunter.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        if (hunter.isUsingItem() && hunter.getUsedItemHand() == hand && hunter.getItemInHand(hand).canPerformAction(ItemAbilities.SPYGLASS_SCOPE)) {
            return HumanoidModel.ArmPose.SPYGLASS;
        }
        return super.getArmPose(hunter, arm);
    }

    private void extractHeldOnHead(Hunter hunter, HunterRenderState state) {
        state.heldOnHead.clear();
        if (!state.isUsingItem) return;

        ItemStack useItem = hunter.getItemInHand(state.useItemHand);
        if (useItem.canPerformAction(ItemAbilities.SPYGLASS_SCOPE)) {
            this.itemModelResolver.updateForLiving(state.heldOnHead, useItem, ItemDisplayContext.HEAD, hunter);
        }
    }

    private void extractBanner(Hunter hunter, HunterRenderState state) {
        ItemStack banner = hunter.getBanner();
        if (banner.isEmpty()) return;
        state.wornHeadType = null;
        this.itemModelResolver.updateForLiving(state.headItem, banner, ItemDisplayContext.HEAD, hunter);
    }

    private PlayerSkin findSkin(Hunter hunter) {
        if (hunter instanceof AdvancedHunter) {
            PlayerSkin supporterSkin = this.supporterSkins.get(hunter.getData(ModAttachments.SUPPORTER).player());
            if (supporterSkin != null) return supporterSkin;
        }
        return this.skins.computeIfAbsent(hunter.getVariant().value().texture(), HunterRenderer::createSkin);
    }

    @Override
    protected void submitNameDisplay(HunterRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
        super.submitNameDisplay(state, poseStack, submitNodeCollector, camera);
        if (state.title == null || state.nameTag == null || state.nameTagAttachment == null) return;

        poseStack.pushPose();
        poseStack.translate(state.nameTagAttachment.x, state.nameTagAttachment.y + 0.5D, state.nameTagAttachment.z);
        poseStack.scale(TITLE_SCALE, TITLE_SCALE, TITLE_SCALE);
        submitNodeCollector.submitNameTag(poseStack, TITLE_ATTACHMENT, TITLE_OFFSET, state.title, !state.isDiscrete, state.lightCoords, state.distanceToCameraSq, camera);
        poseStack.popPose();
    }

    private static PlayerSkin createSkin(Identifier texture) {
        PlayerModelType model = texture.getPath().endsWith("_slim.png") ? PlayerModelType.SLIM : PlayerModelType.WIDE;
        return new PlayerSkin(new ClientAsset.ResourceTexture(texture, texture), null, null, model, false);
    }

    public static class HunterRenderState extends AvatarLikeRenderState {
        public @Nullable Component title;
        public final ItemStackRenderState heldOnHead = new ItemStackRenderState();
    }
}
