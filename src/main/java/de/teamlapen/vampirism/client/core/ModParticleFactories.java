package de.teamlapen.vampirism.client.core;

import de.teamlapen.vampirism.client.particle.FlyingBloodEntityParticle;
import de.teamlapen.vampirism.client.particle.FlyingBloodParticle;
import de.teamlapen.vampirism.client.particle.OldFlyingBloodParticle;
import de.teamlapen.vampirism.client.particle.GenericParticle;
import de.teamlapen.vampirism.core.ModParticles;
import net.minecraft.client.particle.SpellParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@OnlyIn(Dist.CLIENT)
public class ModParticleFactories {

    static void registerFactories(RegisterParticleProvidersEvent event) {
        event.registerSpecial(ModParticles.OLD_FLYING_BLOOD.get(), new OldFlyingBloodParticle.Factory());
        event.registerSpriteSet(ModParticles.FLYING_BLOOD.get(), FlyingBloodParticle.Provider::new);
        event.registerSpecial(ModParticles.FLYING_BLOOD_ENTITY.get(), new FlyingBloodEntityParticle.Factory());
        event.registerSpecial(ModParticles.GENERIC.get(), new GenericParticle.Factory());
        event.registerSpriteSet(ModParticles.SANGUINARE.get(), SpellParticle.Provider::new);
    }
}
