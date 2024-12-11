package de.teamlapen.vampirism.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

/**
 * Mainly intended to be used by aggressive villagers.
 */
public class PitchforkItem extends VampirismSwordItem {

    public PitchforkItem(Item.Properties properties) {
        super(ToolMaterial.IRON, 6, -3, properties);
    }
}
