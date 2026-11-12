package de.teamlapen.vampirism.common.world.entity.hunter;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.control.JumpControl;

public class HunterJumpControl extends JumpControl {

    public HunterJumpControl(Mob mob) {
        super(mob);
    }

    public void cancel() {
        this.jump = false;
    }
}
