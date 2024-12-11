package de.teamlapen.vampirism.mixin.client.accessor;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(HumanoidArmorLayer.class)
public interface HumanoidArmorLayerAccessor<S extends HumanoidRenderState, M extends HumanoidModel<S>, A extends HumanoidModel<S>> {

    @Invoker("usesInnerModel")
    boolean invoke_usesInnerModel(EquipmentSlot slot);

    @Invoker("renderArmorPiece")
    void invoke_renderArmorPiece(PoseStack pPoseStack, MultiBufferSource pBuffer, ItemStack stack, EquipmentSlot pSlot, int pPackedLight, A pModel);

}
