package de.teamlapen.vampirism.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModParticles;
import de.teamlapen.vampirism.util.ByteBufferCodecUtil;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

public record OldFlyingBloodParticleOption(int maxAge, boolean direct, double targetX, double targetY, double targetZ, ResourceLocation texture, float scale) implements ParticleOptions {

    public OldFlyingBloodParticleOption(int maxAge, boolean direct, Vector3d target, ResourceLocation texture, float scale) {
        this(maxAge, direct, target.x, target.y, target.z, texture, scale);
    }

    public static final MapCodec<OldFlyingBloodParticleOption> CODEC = RecordCodecBuilder.mapCodec((inst) -> inst
            .group(
                    Codec.INT.fieldOf("maxAge").forGetter(OldFlyingBloodParticleOption::maxAge),
                    Codec.BOOL.fieldOf("direct").forGetter(OldFlyingBloodParticleOption::direct),
                    Codec.DOUBLE.fieldOf("targetX").forGetter(OldFlyingBloodParticleOption::targetX),
                    Codec.DOUBLE.fieldOf("targetY").forGetter(OldFlyingBloodParticleOption::targetY),
                    Codec.DOUBLE.fieldOf("targetZ").forGetter(OldFlyingBloodParticleOption::targetZ),
                    ResourceLocation.CODEC.fieldOf("texture").forGetter(OldFlyingBloodParticleOption::texture),
                    Codec.FLOAT.fieldOf("scale").forGetter(OldFlyingBloodParticleOption::scale)
            ).apply(inst, OldFlyingBloodParticleOption::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, OldFlyingBloodParticleOption> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, OldFlyingBloodParticleOption::maxAge,
            ByteBufCodecs.BOOL, OldFlyingBloodParticleOption::direct,
            ByteBufferCodecUtil.VECTOR3D, p -> new Vector3d(p.targetX, p.targetY, p.targetZ),
            ResourceLocation.STREAM_CODEC, OldFlyingBloodParticleOption::texture,
            ByteBufCodecs.FLOAT, OldFlyingBloodParticleOption::scale,
            OldFlyingBloodParticleOption::new);


    public OldFlyingBloodParticleOption(int maxAgeIn, boolean direct, double targetX, double targetY, double targetZ) {
        this(maxAgeIn, direct, targetX, targetY, targetZ, 1f);
    }

    public OldFlyingBloodParticleOption(int maxAgeIn, boolean direct, double targetX, double targetY, double targetZ, float scale) {
        this(maxAgeIn, direct, targetX, targetY, targetZ, VResourceLocation.mod("shred_3"), scale);
    }

    public OldFlyingBloodParticleOption(int maxAge, boolean direct, double targetX, double targetY, double targetZ, ResourceLocation texture) {
        this(maxAge, direct, targetX, targetY, targetZ, texture, 1f);
    }

    public int getMaxAge() {
        return maxAge;
    }

    @NotNull
    @Override
    public ParticleType<?> getType() {
        return ModParticles.OLD_FLYING_BLOOD.get();
    }

}
