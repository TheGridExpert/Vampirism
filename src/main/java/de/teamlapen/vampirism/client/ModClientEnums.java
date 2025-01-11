package de.teamlapen.vampirism.client;

import de.teamlapen.vampirism.client.extensions.ItemExtensions;
import net.minecraft.client.model.HumanoidModel;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.Supplier;

public class ModClientEnums {

    public static final EnumProxy<HumanoidModel.ArmPose> DOUBLE_CROSSBOW_CHARGE = new EnumProxy<>(HumanoidModel.ArmPose.class, true, ItemExtensions.DOUBLE_CROSSBOW_CHARGE_ARM_POSE_TRANSFORMER);

    public static final EnumProxy<HumanoidModel.ArmPose> DOUBLE_CROSSBOW_HOLD = new EnumProxy<>(HumanoidModel.ArmPose.class, true, ItemExtensions.DOUBLE_CROSSBOW_HOLD_ARM_POSE_TRANSFORMER);

}
