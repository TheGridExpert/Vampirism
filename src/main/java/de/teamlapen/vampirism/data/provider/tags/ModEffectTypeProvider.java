package de.teamlapen.vampirism.data.provider.tags;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.tags.ModEffectTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModEffectTypeProvider extends TagsProvider<MobEffect> {

    public ModEffectTypeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> provider, @SuppressWarnings("removal") @Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.MOB_EFFECT, provider, REFERENCE.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider pProvider) {
        this.tag(ModEffectTags.HUNTER_POTION_RESISTANCE).add(MobEffects.BLINDNESS.unwrapKey().orElseThrow(), MobEffects.CONFUSION.unwrapKey().orElseThrow(), MobEffects.HUNGER.unwrapKey().orElseThrow(), MobEffects.POISON.unwrapKey().orElseThrow(), ModEffects.FREEZE.getKey());
    }
}
