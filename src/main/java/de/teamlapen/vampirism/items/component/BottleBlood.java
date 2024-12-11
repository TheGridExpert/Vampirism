package de.teamlapen.vampirism.items.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.components.IBottleBlood;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record BottleBlood(int blood) implements IBottleBlood {

    public static final BottleBlood EMPTY = new BottleBlood(0);
    public static final Codec<BottleBlood> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("blood").forGetter(BottleBlood::blood)
    ).apply(instance, BottleBlood::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BottleBlood> STREAM_CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, BottleBlood::blood, BottleBlood::new);

    public BottleBlood {
        if (blood < 0 || blood > 9) {
            throw new IllegalArgumentException("Blood amount must be between 0 and 9");
        }
    }
//
//    @Override
//    public void onConsume(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull Consumable consumable) {
//        if (entity instanceof IVampire vampire) {
//            vampire.
//        }
//    }
}
