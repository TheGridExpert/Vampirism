package de.teamlapen.vampirism.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.core.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record GenericParticleOption(ResourceLocation texture, int maxAge, int color, float speed) implements ParticleOptions {

    public static final MapCodec<GenericParticleOption> CODEC = RecordCodecBuilder.mapCodec((p_239803_0_) -> p_239803_0_
            .group(
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(GenericParticleOption::texture),
                    Codec.INT.fieldOf("maxAge").forGetter(GenericParticleOption::maxAge),
                    Codec.INT.fieldOf("color").forGetter(GenericParticleOption::color),
                    Codec.FLOAT.fieldOf("speed").forGetter(GenericParticleOption::speed))
            .apply(p_239803_0_, GenericParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, GenericParticleOption> STREAM_CODEC = StreamCodec.composite(
            ResourceLocation.STREAM_CODEC, GenericParticleOption::texture,
            ByteBufCodecs.VAR_INT, GenericParticleOption::maxAge,
            ByteBufCodecs.VAR_INT, GenericParticleOption::color,
            ByteBufCodecs.FLOAT, GenericParticleOption::speed,
            GenericParticleOption::new);


    public GenericParticleOption(ResourceLocation texture, int maxAge, int color) {
        this(texture, maxAge, color, 1.0F);
    }


    @NotNull
    @Override
    public ParticleType<?> getType() {
        return ModParticles.GENERIC.get();
    }

}
