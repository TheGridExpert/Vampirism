package de.teamlapen.vampirism.mixin.client;

import de.teamlapen.vampirism.client.renderer.entity.state.ConvertedOverlayRenderState;
import de.teamlapen.vampirism.client.renderer.entity.state.VampirismRenderState;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ambient.Bat;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements ConvertedOverlayRenderState, VampirismRenderState {

    @Unique
    public ResourceLocation convertedOverlay;
    @Unique
    public ResourceLocation overlay;
    @Unique
    private int blood;
    @Unique
    private boolean poisonousBlood;
    @Unique
    private boolean isHunter;
    @Unique
    private boolean sleepingInCoffin;
    @Unique
    private float attackTime;
    @Unique
    private HumanoidArm attackArm = HumanoidArm.RIGHT;
    @Unique
    private Bat bat;

    @Override
    public @Nullable ResourceLocation overlay() {
        return this.overlay;
    }

    @Override
    public void setOverlay(@Nullable ResourceLocation overlay) {
        this.overlay = overlay;
    }

    @Override
    public @Nullable ResourceLocation convertedOverlay() {
        return convertedOverlay;
    }

    @Override
    public void setConvertedOverlay(@Nullable ResourceLocation overlay) {
        convertedOverlay = overlay;
    }


    @Unique
    VampirismPlayerAttributes vampirismAttributes;

    @Override
    public VampirismPlayerAttributes vampirismAttributes() {
        return vampirismAttributes;
    }

    @Override
    public void vampirismAttributes(VampirismPlayerAttributes attributes) {
        this.vampirismAttributes = attributes;
    }



    @Override
    public int vampirismBlood() {
        return this.blood;
    }

    @Override
    public void vampirismBlood(int blood) {
        this.blood = blood;
    }

    @Override
    public boolean vampirismPoisonousBlood() {
        return this.poisonousBlood;
    }

    @Override
    public void vampirismPoisonousBlood(boolean poisonous) {
        this.poisonousBlood = poisonous;
    }

    @Override
    public boolean isHunter() {
        return this.isHunter;
    }

    @Override
    public void setHunter(boolean hunter) {
        this.isHunter = hunter;
    }

    @Override
    public boolean sleepingInCoffin() {
        return this.sleepingInCoffin;
    }

    @Override
    public void sleepingInCoffin(boolean sleepingInCoffin) {
        this.sleepingInCoffin = sleepingInCoffin;
    }

    @Override
    public float vampirismAttackTime() {
        return this.attackTime;
    }

    @Override
    public void setVampirismAttackTime(float attackTime) {
        this.attackTime = attackTime;
    }

    @Override
    public HumanoidArm vampirismAttackArm() {
        return this.attackArm;
    }

    @Override
    public void setVampirismAttackArm(HumanoidArm arm) {
        this.attackArm = arm;
    }

    @Override
    public Bat vampirismBat() {
        return this.bat;
    }

    @Override
    public void setVampirismBat(Bat bat) {
        this.bat = bat;
    }
}
