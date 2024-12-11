package de.teamlapen.vampirism.client.renderer.entity.state;

import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ambient.Bat;

public interface VampirismRenderState {

    VampirismPlayerAttributes vampirismAttributes();

    void vampirismAttributes(VampirismPlayerAttributes attributes);

    int vampirismBlood();

    void vampirismBlood(int blood);

    boolean vampirismPoisonousBlood();

    void vampirismPoisonousBlood(boolean poisonous);

    boolean isHunter();

    void setHunter(boolean hunter);

    boolean sleepingInCoffin();

    void sleepingInCoffin(boolean sleepingInCoffin);

    float vampirismAttackTime();

    void setVampirismAttackTime(float attackTime);

    HumanoidArm vampirismAttackArm();

    void setVampirismAttackArm(HumanoidArm arm);

    Bat vampirismBat();

    void setVampirismBat(Bat bat);

}
