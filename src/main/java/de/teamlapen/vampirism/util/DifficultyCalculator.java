package de.teamlapen.vampirism.util;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.difficulty.Difficulty;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Calculates a (local) difficulity based on the player faction levels
 */
public class DifficultyCalculator {

    /**
     * Can be null if no players are found
     *
     * @return a difficulty level based on the given player's faction levels
     */
    private static
    @Nullable
    Difficulty calculateDifficulty(@Nullable List<? extends Player> playerList) {
        if (playerList == null || playerList.isEmpty()) return null;
        int min = Integer.MAX_VALUE;
        int max = 0;
        int sum = 0;
        for (Player p : playerList) {
            if (!p.isAlive()) continue;
            FactionPlayerHandler handler = FactionPlayerHandler.get(p);
            int pLevel = handler.getCurrentLevel();
            if (pLevel == 0) {
                min = 0;
                continue;
            }
            int level = (int) (pLevel / (float) handler.getFaction().value().getHighestReachableLevel());
            if (level < min) {
                min = level;
            }
            if (level > max) {
                max = level;
            }
            sum += max;
        }
        return new Difficulty(min, max, Math.round(sum / (float) playerList.size()));
    }

    /**
     * Can be null if no players are found
     *
     * @return A difficulty level based on the world's player's faction levels
     */
    public static @Nullable Difficulty getWorldDifficulty(@NotNull Level w) {
        return calculateDifficulty(w.players());
    }

    /**
     * Can be null if no players are found
     *
     * @return A difficulty level based on the faction level of all players in the specified area
     */
    public static @Nullable Difficulty getLocalDifficulty(@NotNull Level w, @NotNull BlockPos center, int radius) {

        List<Player> list = w.getEntitiesOfClass(Player.class, UtilLib.createBB(center, radius, true));
        return calculateDifficulty(list);
    }

    /**
     * Can be null if no players are found
     * ONLY CALL THIS SERVER SIDE
     *
     * @return A difficulty level based on the server's player's faction levels
     */
    public static @Nullable Difficulty getGlobalDifficulty() {
        if (EffectiveSide.get() == LogicalSide.CLIENT) {
            throw new IllegalStateException("You can only use this method on server side");
        }
        return calculateDifficulty(ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers());
    }

    /**
     * Tries to find a difficulty for the given position, by checking local area, world and then sever. If none returns a difficulty a zero difficulty is returned.
     * ONLY CALLED SERVER SIDE
     */
    public static
    @NotNull
    Difficulty findDifficultyForPos(@NotNull Level world, @NotNull BlockPos pos, int radius) {
        Difficulty d = getLocalDifficulty(world, pos, radius);
        if (d == null) {
            d = getWorldDifficulty(world);
            if (d == null) {
                d = getGlobalDifficulty();
                if (d == null) {
                    d = new Difficulty(0, 0, 0);
                }
            }
        }
        return d;
    }
}
