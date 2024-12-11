package de.teamlapen.vampirism.client.renderer.entity.state;

import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;

public class AdvancedVampireRenderState extends PlayerRenderState implements OverlayRenderState {
    public ResourceLocation bodyTexture;
    public int fangType;
    public int eyeType;
    public ResourceLocation overlay;

    @Override
    public ResourceLocation overlay() {
        return this.overlay;
    }
}
