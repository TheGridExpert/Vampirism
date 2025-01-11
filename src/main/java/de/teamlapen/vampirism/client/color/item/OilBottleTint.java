package de.teamlapen.vampirism.client.color.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.items.component.OilContent;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class OilBottleTint implements ItemTintSource {

    public static final OilBottleTint INSTANCE = new OilBottleTint();
    public static final MapCodec<OilBottleTint> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public int calculate(@NotNull ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        return OilContent.getOil(stack).value().getColor() | 0xFF000000;
    }

    @Override
    public @NotNull MapCodec<? extends ItemTintSource> type() {
        return CODEC;
    }
}
