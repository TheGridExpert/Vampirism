package de.teamlapen.vampirism.common.world.entity.hunter.squad;

@FunctionalInterface
public interface SquadTactic {

    void apply(SquadContext context);
}
