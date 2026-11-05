package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import java.util.List;

public interface SquadStrategy {

    List<SquadTactic> tactics();

    default void tick(SquadContext context) {
        for (SquadTactic tactic : tactics()) {
            tactic.apply(context);
        }
    }
}
