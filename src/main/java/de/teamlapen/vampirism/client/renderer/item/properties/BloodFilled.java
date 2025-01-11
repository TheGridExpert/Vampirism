package de.teamlapen.vampirism.client.renderer.item.properties;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.items.component.BottleBlood;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record BloodFilled() implements RangeSelectItemModelProperty {

    public static final ResourceLocation ID = VResourceLocation.mod("bloodfilled");
    public static final MapCodec<BloodFilled> CODEC = MapCodec.unit(BloodFilled::new);

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int light) {
        return stack.getOrDefault(ModDataComponents.BOTTLE_BLOOD, BottleBlood.EMPTY).blood() / 9f;
    }

    @Override
    public @NotNull MapCodec<? extends RangeSelectItemModelProperty> type() {
        return CODEC;
    }
}
