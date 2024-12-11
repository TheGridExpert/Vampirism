package de.teamlapen.vampirism.entity.player.skills;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.core.ModRegistries;
import de.teamlapen.vampirism.util.FactionCodec;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record SkillTree(@NotNull Holder<? extends IPlayableFaction<?>> faction, @NotNull EntityPredicate unlockPredicate, @NotNull ItemStack display, @NotNull Component name, @NotNull Optional<ResourceLocation> background) implements ISkillTree {

    public static final Codec<ISkillTree> CODEC = Codec.lazyInitialized(() -> RecordCodecBuilder.create(inst ->
            inst.group(
                    FactionCodec.playable().fieldOf("faction").forGetter(ISkillTree::faction),
                    EntityPredicate.CODEC.fieldOf("unlock_predicate").forGetter(ISkillTree::unlockPredicate),
                    ItemStack.CODEC.fieldOf("display").forGetter(ISkillTree::display),
                    ComponentSerialization.CODEC.fieldOf("name").forGetter(ISkillTree::name),
                    ResourceLocation.CODEC.optionalFieldOf("background").forGetter(ISkillTree::background)
            ).apply(inst, SkillTree::new)
    ));

    public SkillTree(@NotNull Holder<? extends IPlayableFaction<?>> faction, @NotNull EntityPredicate unlockPredicate, @NotNull ItemStack display, @NotNull Component name) {
        this(faction, unlockPredicate, display, name, Optional.empty());
    }

}
