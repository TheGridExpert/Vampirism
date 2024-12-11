package de.teamlapen.vampirism.client.model;

import de.teamlapen.vampirism.client.renderer.entity.VampireBaronRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public abstract class BaronBaseModel extends EntityModel<VampireBaronRenderer.VampireBaronRenderState> {

    public BaronBaseModel(ModelPart root) {
        super(root);
    }

    public abstract ModelPart getBody();
}
