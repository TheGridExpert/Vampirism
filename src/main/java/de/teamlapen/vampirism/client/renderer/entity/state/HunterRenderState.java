package de.teamlapen.vampirism.client.renderer.entity.state;

import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.resources.ResourceLocation;

public class HunterRenderState extends PlayerRenderState {

    private static final ResourceLocation DEFAULT_TEXTURE = Hunter.DEFAULT_VARIANT.value().texture();

    public ResourceLocation texture = DEFAULT_TEXTURE;
}
