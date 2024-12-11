package de.teamlapen.vampirism.client.model;

import de.teamlapen.vampirism.client.renderer.entity.layers.PlayerBodyOverlayLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.HunterMinionRenderState;
import net.minecraft.client.model.geom.ModelPart;

public class HunterMinionModel<T extends HunterMinionRenderState> extends PlayerBodyOverlayLayer.VisibilityPlayerModel<T> {

    public HunterMinionModel(ModelPart p_170821_, boolean p_170822_) {
        super(p_170821_, p_170822_);
    }
}
