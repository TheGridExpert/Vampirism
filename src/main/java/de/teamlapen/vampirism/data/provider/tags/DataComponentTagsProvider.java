package de.teamlapen.vampirism.data.provider.tags;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.core.tags.ModDataComponentTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class DataComponentTagsProvider extends TagsProvider<DataComponentType<?>> {

    protected DataComponentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @SuppressWarnings("removal") @Nullable net.neoforged.neoforge.common.data.ExistingFileHelper existingFileHelper) {
        super(output, Registries.DATA_COMPONENT_TYPE, lookupProvider, REFERENCE.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.@NotNull Provider provider) {
        //noinspection unchecked
        this.tag(ModDataComponentTags.FACTION_FOOD).addTags(ModDataComponentTags.HUNTER_FOOD, ModDataComponentTags.VAMPIRE_FOOD, ModDataComponentTags.BASE_FOOD);
        this.tag(ModDataComponentTags.VAMPIRE_FOOD).add(ModDataComponents.VAMPIRE_FOOD.getKey());
        this.tag(ModDataComponentTags.HUNTER_FOOD);
        this.tag(ModDataComponentTags.BASE_FOOD).add(BuiltInRegistries.DATA_COMPONENT_TYPE.getResourceKey(DataComponents.FOOD).orElseThrow());
    }
}
