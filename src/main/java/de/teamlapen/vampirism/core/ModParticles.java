package de.teamlapen.vampirism.core;

import com.mojang.serialization.MapCodec;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.particle.FlyingBloodEntityParticleOption;
import de.teamlapen.vampirism.particle.FlyingBloodParticleOption;
import de.teamlapen.vampirism.particle.OldFlyingBloodParticleOption;
import de.teamlapen.vampirism.particle.GenericParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<OldFlyingBloodParticleOption>> OLD_FLYING_BLOOD = registerParticle("old_flying_blood", false, type -> OldFlyingBloodParticleOption.CODEC, type -> OldFlyingBloodParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<FlyingBloodParticleOption>> FLYING_BLOOD = registerParticle("flying_blood", false, type -> FlyingBloodParticleOption.CODEC, type -> FlyingBloodParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<FlyingBloodEntityParticleOption>> FLYING_BLOOD_ENTITY = registerParticle("flying_blood_entity", false, type -> FlyingBloodEntityParticleOption.CODEC, type -> FlyingBloodEntityParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, ParticleType<GenericParticleOption>> GENERIC = registerParticle("generic", false, type -> GenericParticleOption.CODEC, type -> GenericParticleOption.STREAM_CODEC);
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SANGUINARE = registerParticle("sanguinare", false);

    static void register(IEventBus bus) {
        PARTICLE_TYPES.register(bus);
    }

    /**
     * Note: overrideLimiter determines whether the particle should override particle quantity settings. Set to false if it is not too important to render all the particles, the game may reduce the amount if needed. If it's something vital like, for example, Warden's vibrations, set to true, then they will render at all costs.
     */
    private static DeferredHolder<ParticleType<?>, SimpleParticleType> registerParticle(String name, boolean overrideLimiter) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(overrideLimiter));
    }

    private static <T extends ParticleOptions> DeferredHolder<ParticleType<?>, ParticleType<T>> registerParticle(String name, boolean overrideLimitter, final Function<ParticleType<T>, MapCodec<T>> codecGetter, final Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> streamCodecGetter) {
        return PARTICLE_TYPES.register(name, () -> new ParticleType<T>(overrideLimitter) {
            @Override
            public MapCodec<T> codec() {
                return codecGetter.apply(this);
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                return streamCodecGetter.apply(this);
            }
        });
    }

    public static void spawnParticlesClient(Level worldIn, @NotNull ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, int count, double maxDist, @NotNull RandomSource rand) {
        assert !(worldIn instanceof ServerLevel) : "Calling spawnParticlesClient on ServerWorld is pointless";
        for (int i = 0; i < count; i++) {
            worldIn.addParticle(particle, x + maxDist * (2 * rand.nextDouble() - 1), y + (2 * rand.nextDouble() - 1) * maxDist, z + (2 * rand.nextDouble() - 1) * maxDist, xSpeed, ySpeed, zSpeed);
        }
    }

    public static void spawnParticlesClient(Level worldIn, @NotNull ParticleOptions particle, double x, double y, double z, int count, double maxDist, @NotNull RandomSource rand) {
        spawnParticlesClient(worldIn, particle, x, y, z, 0, 0, 0, count, maxDist, rand);
    }

    public static void spawnParticleClient(@NotNull Level worldIn, @NotNull ParticleOptions particle, double x, double y, double z) {
        spawnParticleClient(worldIn, particle, x, y, z, 0, 0, 0);
    }

    public static void spawnParticleClient(@NotNull Level worldIn, @NotNull ParticleOptions particle, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
        assert !(worldIn instanceof ServerLevel) : "Calling spawnParticleClient on ServerWorld is pointless";
        worldIn.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
    }

    /**
     * Sends particle packages to client if the given world is a server world
     *
     * @param worldIn       World, should be instanceof ServerWorld
     * @param particleCount How many to spawn
     * @param xOffset       Used for random offset
     * @param yOffset       Used for random offset
     * @param zOffset       Used for random offset
     * @param speed         Direction is randomized but multiplied by x/y/zOffset
     * @return Number of players this has been sent to.
     */
    public static int spawnParticlesServer(Level worldIn, @NotNull ParticleOptions particle, double posX, double posY, double posZ, int particleCount, double xOffset, double yOffset, double zOffset, double speed) {
        assert worldIn instanceof ServerLevel : "Calling spawnParticlesServer on client side is pointless";
        if (worldIn instanceof ServerLevel) {
            return ((ServerLevel) worldIn).sendParticles(particle, posX, posY, posZ, particleCount, xOffset, yOffset, zOffset, speed);
        }
        return 0;
    }
}