package de.teamlapen.vampirism.client.renderer.entity.state;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public interface ConvertedOverlayRenderState {

    @Nullable
    ResourceLocation overlay();

    void setOverlay(@Nullable ResourceLocation overlay);

    @Nullable
    ResourceLocation convertedOverlay();

    void setConvertedOverlay(@Nullable ResourceLocation overlay);
}
