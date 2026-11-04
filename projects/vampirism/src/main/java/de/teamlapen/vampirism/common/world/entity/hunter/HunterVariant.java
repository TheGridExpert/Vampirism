package de.teamlapen.vampirism.common.world.entity.hunter;

import com.mojang.serialization.Codec;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.world.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.common.core.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;

public record HunterVariant(Identifier texture) implements IHunterVariant {

    public static final Codec<Holder<IHunterVariant>> CODEC = ModRegistries.HUNTER_VARIANT.holderByNameCodec();
    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<IHunterVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(VampirismRegistries.Keys.HUNTER_VARIANT);

    public static Holder<IHunterVariant> getRandomVariant(Holder<IHunterVariant> defaultVariant, RandomSource random) {
        return ModRegistries.HUNTER_VARIANT.getRandom(random).<Holder<IHunterVariant>>map(holder -> holder).orElse(defaultVariant);
    }
}
