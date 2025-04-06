package de.teamlapen.vampirism.command.test;

import com.google.common.collect.Lists;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.teamlapen.lib.lib.util.BasicCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.food.FoodData;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class HungerBarCommand extends BasicCommand {
    public static ArgumentBuilder<CommandSourceStack, ?> register() {
        return Commands.literal("hungerBar")
                .requires(context -> context.hasPermission(PERMISSION_LEVEL_CHEAT))
                .then(Commands.literal("fill")
                        .executes(context -> setBloodBar(20, Lists.newArrayList(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(context -> setBloodBar(Integer.MAX_VALUE, EntityArgument.getPlayers(context, "player")))))
                .then(Commands.literal("empty")
                        .executes(context -> setBloodBar(0, Lists.newArrayList(context.getSource().getPlayerOrException())))
                        .then(Commands.argument("player", EntityArgument.players())
                                .executes(context -> setBloodBar(0, EntityArgument.getPlayers(context, "player")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                .executes(context -> setBloodBar(IntegerArgumentType.getInteger(context, "amount"), Lists.newArrayList(context.getSource().getPlayerOrException())))
                                .then(Commands.argument("player", EntityArgument.players())
                                        .executes(context -> setBloodBar(IntegerArgumentType.getInteger(context, "amount"), EntityArgument.getPlayers(context, "player"))))));
    }

    private static int setBloodBar(int amount, @NotNull Collection<ServerPlayer> players) {
        players.forEach(player -> {
            FoodData foodData = player.getFoodData();
            foodData.setFoodLevel(amount);
            foodData.setSaturation(amount);
        });
        return 0;
    }
}
