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
import net.minecraft.world.phys.Vec3;

public record FlyingBloodParticleOption(Vec3 destination, int arrivalInTicks, boolean straight) implements ParticleOptions {

    public static final MapCodec<FlyingBloodParticleOption> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Vec3.CODEC.fieldOf("destination").forGetter(FlyingBloodParticleOption::destination),
            Codec.INT.fieldOf("arrivalInTicks").forGetter(FlyingBloodParticleOption::arrivalInTicks),
            Codec.BOOL.fieldOf("straight").forGetter(FlyingBloodParticleOption::straight)
    ).apply(instance, FlyingBloodParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlyingBloodParticleOption> STREAM_CODEC = StreamCodec.composite(
            Vec3.STREAM_CODEC,
            FlyingBloodParticleOption::destination,
            ByteBufCodecs.INT,
            FlyingBloodParticleOption::arrivalInTicks,
            ByteBufCodecs.BOOL,
            FlyingBloodParticleOption::straight,
            FlyingBloodParticleOption::new
    );

    @Override
    public ParticleType<?> getType() {
        return ModParticles.FLYING_BLOOD.get();
    }
}
