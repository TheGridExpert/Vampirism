package de.teamlapen.vampirism.client.renderer.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.client.renderer.blockentity.BatCageRenderer;
import de.teamlapen.vampirism.core.ModDataComponents;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class BatCageSpecialRenderer implements SpecialModelRenderer<CompoundTag> {

    private final BatCageRenderer renderer;

    public BatCageSpecialRenderer(BatCageRenderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public void render(@Nullable CompoundTag entityTag, ItemDisplayContext displayContext, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, int packedOverlay, boolean hasFoilType) {
        if (entityTag != null) {
            renderer.renderBat(poseStack, bufferSource, packedLight, packedOverlay, Direction.NORTH);
        }
    }

    @Nullable
    @Override
    public CompoundTag extractArgument(ItemStack stack) {
        return stack.get(ModDataComponents.HELD_ENTITY);
    }

    public record Unbaked() implements SpecialModelRenderer.Unbaked {

        public static final MapCodec<BatCageSpecialRenderer.Unbaked> MAP_CODEC = MapCodec.unit(BatCageSpecialRenderer.Unbaked::new);

        @Override
        public SpecialModelRenderer<?> bake(EntityModelSet modelSet) {
            return new BatCageSpecialRenderer(new BatCageRenderer(modelSet));
        }

        @Override
        public MapCodec<? extends SpecialModelRenderer.Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
