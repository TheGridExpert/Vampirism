package de.teamlapen.vampirism.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import de.teamlapen.vampirism.blockentity.TombstoneBlockEntity;
import de.teamlapen.vampirism.blocks.TombstoneBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TombstoneBESR implements BlockEntityRenderer<TombstoneBlockEntity> {

    private static final Vec3 TEXT_OFFSET = new Vec3(0.0, 0.33333334F, 0.046666667F);

    private final Font font;

    public TombstoneBESR(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(@NotNull TombstoneBlockEntity blockEntity, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        TombstoneBlock tombstoneBlock = (TombstoneBlock) state.getBlock();

        translateSign(poseStack, -tombstoneBlock.getYRotationDegrees(state));
        this.renderSignText(tombstoneBlock, blockEntity.getFrontText(), poseStack, bufferSource, packedLight, blockEntity.getTextLineHeight(), blockEntity.getMaxTextLineWidth());
    }

    private static void translateSign(PoseStack poseStack, float yRot) {
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
    }

    private void renderSignText(TombstoneBlock block, SignText text, PoseStack poseStack, MultiBufferSource bufferSource, int lineHeight, int maxLineWidth, int packedLight) {
        poseStack.pushPose();

        this.translateSignText(poseStack, this.getTextOffset());

        int textColor = block.getTextColor();
        int highlightColor = block.getHighlightColor();

        int lineHeightOffset = 4 * lineHeight / 2;
        FormattedCharSequence[] linesToRender = text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), line -> {
            List<FormattedCharSequence> list = this.font.split(line, maxLineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.getFirst();
        });

        for (int i = 0; i < 4; i++) {
            FormattedCharSequence line = linesToRender[i];
            float x = (float) (-this.font.width(line) / 2);

            this.font.drawInBatch8xOutline(line, x, (float) (i * lineHeight - lineHeightOffset), textColor, i, poseStack.last().pose(), bufferSource, packedLight);
            this.font.drawInBatch(line, x, (float) (i * lineHeight - lineHeightOffset), highlightColor, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
        }

        poseStack.popPose();
    }

    private void translateSignText(PoseStack poseStack, Vec3 offset) {
        float scale = 0.015625F * this.getSignTextRenderScale();
        poseStack.translate(offset);
        poseStack.scale(scale, -scale, scale);
    }

    public float getSignTextRenderScale() {
        return 0.6666667F;
    }

    protected Vec3 getTextOffset() {
        return TEXT_OFFSET;
    }

    public static int getDarkColor(SignText text) {
        int color = text.getColor().getTextColor();

        int red = (int) ((double) ARGB.red(color) * 0.4);
        int green = (int) ((double) ARGB.green(color) * 0.4);
        int blue = (int) ((double) ARGB.blue(color) * 0.4);

        return ARGB.color(0, red, green, blue);
    }

    /*
    private void renderSignText(BlockPos pos, SignText text, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int lineHeight, int maxLineWidth, boolean isFront) {
        poseStack.pushPose();
        this.translateSignText(poseStack, isFront, this.getTextOffset());
        int i = getDarkColor(text);
        int j = 4 * lineHeight / 2;
        FormattedCharSequence[] aformattedcharsequence = text.getRenderMessages(Minecraft.getInstance().isTextFilteringEnabled(), p_389418_ -> {
            List<FormattedCharSequence> list = this.font.split(p_389418_, maxLineWidth);
            return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
        });
        int k;
        boolean flag;
        int l;
        if (text.hasGlowingText()) {
            k = text.getColor().getTextColor();
            flag = isOutlineVisible(pos, k);
            l = 15728880;
        } else {
            k = i;
            flag = false;
            l = packedLight;
        }

        for (int i1 = 0; i1 < 4; i1++) {
            FormattedCharSequence formattedcharsequence = aformattedcharsequence[i1];
            float f = (float)(-this.font.width(formattedcharsequence) / 2);
            if (flag) {
                this.font.drawInBatch8xOutline(formattedcharsequence, f, (float)(i1 * lineHeight - j), k, i, poseStack.last().pose(), bufferSource, l);
            } else {
                this.font.drawInBatch(formattedcharsequence, f, (float)(i1 * lineHeight - j), k, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.POLYGON_OFFSET, 0, l);
            }
        }

        poseStack.popPose();
    }
     */
}
