package de.teamlapen.vampirism.client.renderer.blockentity;

import com.google.common.collect.Streams;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.teamlapen.lib.lib.client.VertexUtils;
import de.teamlapen.vampirism.core.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GarlicAuraRenderer implements BlockEntityRenderer<BlockEntity> {

    public static final RenderType BLOCK_AURA = RenderType.create(
            "block_aura",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .createCompositeState(false)
    );

    public GarlicAuraRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BlockEntity blockEntity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        if (!(Minecraft.getInstance().getCameraEntity() instanceof LivingEntity entity)) return;

        boolean holdingFinder = hasFinder(entity);

        if (!holdingFinder) return;

        float distance = (float) blockEntity.getBlockPos().distSqr(entity.blockPosition());
        float maxDistance = 1024;
        if (distance > maxDistance) return;

        Level level = blockEntity.getLevel();
        if (level == null) return;

        BlockState state = blockEntity.getBlockState();
        VoxelShape shape = state.getShape(level, blockEntity.getBlockPos());

        float closeDistance = 49;
        float alpha = distance <= closeDistance ? 1.0f - ((closeDistance - distance) / closeDistance) : 1.0f;

        VertexConsumer vertex = bufferSource.getBuffer(BLOCK_AURA);

        poseStack.pushPose();
        poseStack.translate(0, 0, 0);

        VertexUtils.renderShape(poseStack, vertex, shape, -1, -1, 0xe0b74f, alpha);

        poseStack.popPose();
    }

    public static boolean hasFinder(LivingEntity entity) {
        if (entity instanceof Player player) {
            Item item = ModItems.GARLIC_FINDER.get();

            if (Streams.stream(player.getHandSlots()).anyMatch(stack -> stack.is(item))) {
                return true;
            }

            for (int i = 0; i < 9; i++) {
                if (player.getInventory().getItem(i).is(item)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean shouldRenderOffScreen(BlockEntity blockEntity) {
        return true;
    }
}
