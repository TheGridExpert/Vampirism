package de.teamlapen.vampirism.client.model;

import de.teamlapen.vampirism.client.renderer.entity.state.BasicHunterRenderState;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.jetbrains.annotations.NotNull;

/**
 * Model for Basic Vampire Hunter
 */
public class BasicHunterModel<T extends PlayerRenderState> extends BipedCloakedModel<T> {

    public static @NotNull LayerDefinition createBodyLayer() {
        return LayerDefinition.create(BipedCloakedModel.createMesh(false), 64, 64);
    }

    public static @NotNull LayerDefinition createSlimBodyLayer() {
        return LayerDefinition.create(BipedCloakedModel.createMesh(true), 64, 64);
    }

    public BasicHunterModel(ModelPart part, boolean smallArms) {
        super(part, smallArms);
    }

}
