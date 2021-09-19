package de.teamlapen.vampirism.player.actions;

import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.entity.player.actions.IActionManager;
import de.teamlapen.vampirism.core.ModRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.List;
import java.util.stream.Collectors;


public class ActionManager implements IActionManager {

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IFactionPlayer<T>> List<IAction<T>> getActionsForFaction(IPlayableFaction<T> faction) {
        return ModRegistries.ACTIONS.getValues().stream().filter(action -> action.getFaction() == faction).map(action -> (IAction<T>)action).collect(Collectors.toList());
    }

    @Override
    public IForgeRegistry<IAction<?>> getRegistry() {
        return ModRegistries.ACTIONS;
    }
}
