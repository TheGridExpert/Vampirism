package de.teamlapen.vampirism.util;

import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.ISkillTree;
import de.teamlapen.vampirism.api.entity.minion.IMinionTask;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.ISkillPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.entity.player.actions.ILastingAction;
import de.teamlapen.vampirism.api.entity.player.refinement.IRefinement;
import de.teamlapen.vampirism.api.entity.player.refinement.IRefinementSet;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.items.oil.IOil;
import de.teamlapen.vampirism.core.ModRegistries;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;

public class RegUtil {

    public static ResourceLocation id(@NotNull IAction<?> action) {
        return ModRegistries.ACTIONS.getKey(action);
    }

    public static ResourceLocation id(@NotNull ISkill<?> skill) {
        return ModRegistries.SKILLS.getKey(skill);
    }

    public static ResourceLocation id(@NotNull IMinionTask<?, ?> minionTask) {
        return ModRegistries.MINION_TASKS.getKey(minionTask);
    }

    public static ResourceLocation id(@NotNull IRefinement refinement) {
        return ModRegistries.REFINEMENTS.getKey(refinement);
    }

    public static ResourceLocation id(@NotNull IRefinementSet refinementSet) {
        return ModRegistries.REFINEMENT_SETS.getKey(refinementSet);
    }

    public static ResourceLocation id(@NotNull Item item) {
        return BuiltInRegistries.ITEM.getKey(item);
    }

    public static ResourceLocation id(@NotNull Block block) {
        return BuiltInRegistries.BLOCK.getKey(block);
    }

    public static ResourceLocation id(@NotNull Fluid block) {
        return BuiltInRegistries.FLUID.getKey(block);
    }

    public static ResourceLocation id(@NotNull EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getKey(type);
    }

    public static ResourceLocation id(Level level, @NotNull Biome type) {
        return level.registryAccess().lookupOrThrow(Registries.BIOME).getKey(type);
    }

    public static ResourceLocation id(@NotNull VillagerProfession profession) {
        return BuiltInRegistries.VILLAGER_PROFESSION.getKey(profession);
    }

    public static ResourceLocation id(@NotNull IOil oil) {
        return ModRegistries.OILS.getKey(oil);
    }

    public static ResourceLocation id(@NotNull Level level, ISkillTree tree) {
        return level.registryAccess().lookupOrThrow(VampirismRegistries.Keys.SKILL_TREE).getKey(tree);
    }

    public static Optional<ResourceKey<IAction<?>>> key(@NotNull IAction<?> action) {
        return ModRegistries.ACTIONS.getResourceKey(action);
    }

    public static boolean has(@NotNull IAction<?> action) {
        return ModRegistries.ACTIONS.containsValue(action);
    }

    public static boolean has(@NotNull ISkill<?> skill) {
        return ModRegistries.SKILLS.containsValue(skill);
    }

    public static boolean has(@NotNull IMinionTask<?, ?> minionTask) {
        return ModRegistries.MINION_TASKS.containsValue(minionTask);
    }

    public static boolean has(@NotNull IRefinement refinement) {
        return ModRegistries.REFINEMENTS.containsValue(refinement);
    }

    public static boolean has(@NotNull IRefinementSet refinementSet) {
        return ModRegistries.REFINEMENT_SETS.containsValue(refinementSet);
    }

    public static boolean has(@NotNull Item item) {
        return BuiltInRegistries.ITEM.containsValue(item);
    }

    public static boolean has(@NotNull Block block) {
        return BuiltInRegistries.BLOCK.containsValue(block);
    }

    public static boolean has(@NotNull ServerLevel level, @NotNull Biome biome) {
        return level.registryAccess().lookupOrThrow(Registries.BIOME).containsValue(biome);
    }


    public static Item getItem(@NotNull ResourceLocation id) {
        return BuiltInRegistries.ITEM.getValue(id);
    }

    public static IAction<?> getAction(@NotNull ResourceLocation id) {
        return ModRegistries.ACTIONS.getValue(id);
    }

    public static ISkill<?> getSkill(@NotNull ResourceLocation id) {
        return ModRegistries.SKILLS.getValue(id);
    }

    public static Biome getBiome(ServerLevel level, @NotNull ResourceLocation id) {
        return level.registryAccess().lookupOrThrow(Registries.BIOME).getValue(id);
    }

    public static IMinionTask<?, ?> getMinionTask(@NotNull ResourceLocation id) {
        return ModRegistries.MINION_TASKS.getValue(id);
    }

    public static IRefinement getRefinement(@NotNull ResourceLocation id) {
        return ModRegistries.REFINEMENTS.getValue(id);
    }

    public static IRefinementSet getRefinementSet(@NotNull ResourceLocation id) {
        return ModRegistries.REFINEMENT_SETS.getValue(id);
    }

    public static IOil getOil(@NotNull ResourceLocation id) {
        return ModRegistries.OILS.getValue(id);
    }

    public static <T, Z extends Registry<T>> Holder<T> getHolder(Level level, ResourceKey<Z> registry, T type) {
        return level.registryAccess().lookupOrThrow(registry).wrapAsHolder(type);
    }

    public static Holder<DamageType> getHolder(Level level, DamageType type) {
        return getHolder(level, Registries.DAMAGE_TYPE, type);
    }

    public static <T> boolean has(@NotNull Registry<T> registry, @NotNull ResourceLocation id) {
        return registry.containsKey(id);
    }

    public static <T> @NotNull Collection<T> values(@NotNull Registry<T> registry) {
        return registry.stream().toList();
    }

    public static Holder<ISkillTree> getSkillTree(Level level, String asString) {
        return level.registryAccess().lookupOrThrow(VampirismRegistries.Keys.SKILL_TREE).getOrThrow(ResourceKey.create(VampirismRegistries.Keys.SKILL_TREE, ResourceLocation.parse(asString)));
    }

    @SuppressWarnings("unchecked")
    public static <T extends IFactionPlayer<T> & ISkillPlayer<T>> Holder<IAction<T>> holder(IAction<T> action) {
        return (Holder<IAction<T>>) (Object) ModRegistries.ACTIONS.wrapAsHolder(action);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IFactionPlayer<T> & ISkillPlayer<T>> Holder<ILastingAction<T>> holder(ILastingAction<T> action) {
        return (Holder<ILastingAction<T>>) (Object) ModRegistries.ACTIONS.wrapAsHolder(action);
    }

    @SuppressWarnings("unchecked")
    public static <T extends IFaction<?>> Holder<T> holder(T faction) {
        return (Holder<T>) ModRegistries.FACTIONS.wrapAsHolder(faction);
    }
}
