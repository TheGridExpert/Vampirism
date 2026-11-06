package de.teamlapen.vampirism.common.world.entity.hunter;

import net.minecraft.world.entity.ai.control.MoveControl;

public class HunterMoveControl extends MoveControl {

    private final Hunter hunter;

    public HunterMoveControl(Hunter hunter) {
        super(hunter);
        this.hunter = hunter;
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double gait) {
        super.setWantedPosition(x, y, z, this.hunter.calculateSpeedModifier(gait));
    }
}
