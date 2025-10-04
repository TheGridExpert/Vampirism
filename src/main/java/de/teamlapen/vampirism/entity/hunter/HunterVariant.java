package de.teamlapen.vampirism.entity.hunter;

import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.core.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.List;

public record HunterVariant(ResourceLocation texture) implements IHunterVariant {

    public static final StreamCodec<RegistryFriendlyByteBuf, Holder<IHunterVariant>> STREAM_CODEC = ByteBufCodecs.holderRegistry(VampirismRegistries.Keys.HUNTER_VARIANT);

    public static List<IHunterVariant> getAllVariants() {
        return ModRegistries.HUNTER_VARIANT.stream().toList();
    }

    public static Holder<IHunterVariant> getRandomVariant(Holder<IHunterVariant> defaultVariant, RandomSource random) {
        List<IHunterVariant> variants = getAllVariants();
        if (variants.isEmpty()) return defaultVariant;
        return ModRegistries.HUNTER_VARIANT.wrapAsHolder(variants.get(random.nextInt(variants.size())));
    }
}
