package de.teamlapen.vampirism.data.provider.tags;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFactionProvider extends TagsProvider<IFaction<?>> {

    public ModFactionProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @SuppressWarnings("removal") @Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, VampirismRegistries.Keys.FACTION, lookupProvider, REFERENCE.MODID, existingFileHelper);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void addTags(HolderLookup.@NotNull Provider holderProvider) {
        this.tag(ModFactionTags.HOSTILE_TOWARDS_NEUTRAL).add(ModFactions.VAMPIRE.getRawKey());
        this.tag(ModFactionTags.IS_HUNTER).add(ModFactions.HUNTER.getRawKey());
        this.tag(ModFactionTags.IS_VAMPIRE).add(ModFactions.VAMPIRE.getRawKey());
        this.tag(ModFactionTags.IS_NEUTRAL).add(ModFactions.NEUTRAL.getRawKey());
        this.tag(ModFactionTags.HAS_LORD_SKILLS).add(ModFactions.VAMPIRE.getRawKey(), ModFactions.HUNTER.getRawKey());
        this.tag(ModFactionTags.ALL_FACTIONS).addTag(ModFactionTags.IS_HUNTER).addTag(ModFactionTags.IS_VAMPIRE);
        this.tag(ModFactionTags.USE_GARLIC_BREAD).addTags(ModFactionTags.IS_HUNTER, ModFactionTags.IS_NEUTRAL);
    }
}
