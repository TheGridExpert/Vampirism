package de.teamlapen.vampirism.data.provider;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModParticles;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ParticleDescriptionProvider;

public class ModParticleDescriptionProvider extends ParticleDescriptionProvider {

    public ModParticleDescriptionProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void addDescriptions() {
        this.spriteSet(ModParticles.FLYING_BLOOD.get(), VResourceLocation.mod("shred"), 4, false);
        this.sprite(ModParticles.SANGUINARE.get(), VResourceLocation.mod("sanguinare"));
    }
}
