package de.teamlapen.vampirism.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.blockentity.BatCageBlockEntity;
import de.teamlapen.vampirism.blocks.BatCageBlock;
import net.minecraft.client.model.BatModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.state.BatRenderState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class BatCageBESR extends VampirismBESR<BatCageBlockEntity> {

    private final BatModel model;

    public BatCageBESR(BlockEntityRendererProvider.Context context) {
        model = new BatModel(context.bakeLayer(ModelLayers.BAT));
    }

    @Override
    public void render(BatCageBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        BlockState blockState = pBlockEntity.getBlockState();
        if (blockState.getValue(BatCageBlock.CONTAINS_BAT)) {
            renderBat(pPoseStack, pBuffer, pPackedLight, pPackedOverlay, blockState.getValue(BatCageBlock.FACING), pBlockEntity.getLevel(), pPartialTick);
        }
    }

    private void renderBat(PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, Direction direction, Level level, float pPartialTick) {
        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 1F, 0.5F);
//        pPoseStack.mulPose(Axis.YN.rotationDegrees(90 * direction.get2DDataValue()));
        pPoseStack.scale(0.65F, 0.65F, 0.65F);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(180));
        BatRenderState batRenderState = new BatRenderState();
        batRenderState.isResting = true;
        batRenderState.restAnimationState.animateWhen(true, 0);
        this.model.setupAnim(batRenderState);
        this.model.renderToBuffer(pPoseStack, pBuffer.getBuffer(this.model.renderType(VResourceLocation.mc("textures/entity/bat.png"))), pPackedLight, pPackedOverlay, -1);
        pPoseStack.popPose();
    }
}
