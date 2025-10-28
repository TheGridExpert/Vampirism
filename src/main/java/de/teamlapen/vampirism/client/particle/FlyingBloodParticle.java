package de.teamlapen.vampirism.client.particle;

import de.teamlapen.vampirism.particle.FlyingBloodParticleOption;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class FlyingBloodParticle extends TextureSheetParticle {

    private final Vec3 destination;

    public FlyingBloodParticle(ClientLevel level, double x, double y, double z, Vec3 destination, SpriteSet sprites, int arrivalInTicks, boolean straight) {
        super(level, x, y, z);
        this.destination = destination;
        this.lifetime = arrivalInTicks;
        this.hasPhysics = false;
        this.quadSize = 0.135f;

        double deltaX = destination.x - this.x;
        double deltaY = destination.y - this.y;
        double deltaZ = destination.z - this.z;

        RandomSource random = this.level.random;
        if (straight) {
            this.xd = deltaX / arrivalInTicks;
            this.yd = deltaY / arrivalInTicks;
            this.zd = deltaZ / arrivalInTicks;
        } else {
            this.xd = (random.nextDouble() / 10 - 0.05) + deltaX / arrivalInTicks;
            this.yd = (random.nextDouble() / 10 - 0.01) + deltaY / arrivalInTicks;
            this.zd = (random.nextDouble() / 10 - 0.05) + deltaZ / arrivalInTicks;
        }

        this.setSprite(sprites.get(random));
        this.setColor(148 / 255f, 4 / 255f, 36 / 255f);
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;

        if (this.age++ >= this.lifetime) {
            this.remove();
            return;
        }

        int ticksLeft = this.lifetime - this.age;

        double deltaX = this.destination.x - this.x;
        double deltaY = this.destination.y - this.y;
        double deltaZ = this.destination.z - this.z;

        if (ticksLeft < this.lifetime / 1.2) {
            this.xd = deltaX / ticksLeft;
            this.yd = deltaY / ticksLeft;
            this.zd = deltaZ / ticksLeft;
        }
        this.move(this.xd, this.yd, this.zd);
    }

    public static class Provider implements ParticleProvider<FlyingBloodParticleOption> {

        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Nullable
        @Override
        public Particle createParticle(FlyingBloodParticleOption type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new FlyingBloodParticle(level, x, y, z, type.destination(), this.sprites, type.arrivalInTicks(), type.straight());
        }
    }
}
