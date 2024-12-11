package de.teamlapen.vampirism.client.renderer.entity.state;

import de.teamlapen.vampirism.client.renderer.entity.DualBipedRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;

public class AdvancedHunterRenderState extends PlayerRenderState implements OverlayRenderState {
    public boolean hasCloak;
    public ResourceLocation overlayTexture;

    @Override
    public ResourceLocation overlay() {
        return this.overlayTexture;
    }
}
