package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.world.entity.hunter.squad.tactic.*;

import java.util.List;

public class GeneralStrategy implements SquadStrategy {

    public static final GeneralStrategy INSTANCE = new GeneralStrategy();

    private final List<SquadTactic> tactics = List.of(
            new GroupRetreatTactic(),
            new RearGuardTactic(),
            new FocusFireTactic(),
            new MeleeTokenTactic(),
            new ScreenTactic(),
            new ReserveTactic(),
            new FormationTactic()
    );

    private GeneralStrategy() {
    }

    @Override
    public List<SquadTactic> tactics() {
        return this.tactics;
    }
}
