package de.teamlapen.vampirism.command.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.core.ModRegistries;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class SkillArgument implements ArgumentType<ISkill<?>> {
    public static final DynamicCommandExceptionType SKILL_NOT_FOUND = new DynamicCommandExceptionType((particle) -> {
        return new TranslatableComponent("command.vampirism.argument.skill.notfound", particle);
    });
    private static final Collection<String> EXAMPLES = Arrays.asList("skill", "modid:skill");

    public static SkillArgument skills() {
        return new SkillArgument();
    }

    public static ISkill<?> getSkill(CommandContext<CommandSourceStack> context, String name) {
        return context.getArgument(name, ISkill.class);
    }

    @Override
    public Collection<String> getExamples() {
        return EXAMPLES;
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(ModRegistries.SKILLS.getKeys(), builder);
    }

    @Override
    public ISkill<?> parse(StringReader reader) throws CommandSyntaxException {
        ResourceLocation id = ResourceLocation.read(reader);
        ISkill<?> skill = ModRegistries.SKILLS.getValue(id);
        if (skill == null)
            throw SKILL_NOT_FOUND.create(id);
        return skill;
    }
}
