package de.teamlapen.vampirism.client.core;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.Material;

public class ModMaterials {
    @SuppressWarnings("deprecation")
    public static final Material BLOOD_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, VResourceLocation.mod("block/blood_still"));
    @SuppressWarnings("deprecation")
    public static final Material IMPURE_BLOOD_MATERIAL = new Material(TextureAtlas.LOCATION_BLOCKS, VResourceLocation.mod("block/impure_blood_still"));
}
