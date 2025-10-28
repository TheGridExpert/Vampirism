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
import org.jetbrains.annotations.NotNull;

public record FlyingBloodEntityParticleOption(int entity, boolean direct) implements ParticleOptions {

    public static final MapCodec<FlyingBloodEntityParticleOption> CODEC = RecordCodecBuilder.mapCodec((p_239803_0_) -> p_239803_0_
            .group(
                    Codec.INT.fieldOf("entity").forGetter(FlyingBloodEntityParticleOption::entity),
                    Codec.BOOL.fieldOf("direct").forGetter(FlyingBloodEntityParticleOption::direct))
            .apply(p_239803_0_, FlyingBloodEntityParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlyingBloodEntityParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, FlyingBloodEntityParticleOption::entity,
            ByteBufCodecs.BOOL, FlyingBloodEntityParticleOption::direct,
            FlyingBloodEntityParticleOption::new);

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return ModParticles.FLYING_BLOOD_ENTITY.get();
    }

}
