package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.ItemPropertiesExtension;
import net.minecraft.Util;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

public class VampirismSwordItem extends SwordItem {


    public VampirismSwordItem(@NotNull ToolMaterial material, int attackDamageIn, float attackSpeedIn, @NotNull Properties builder) {
        super(material, attackDamageIn, attackSpeedIn, ItemPropertiesExtension.descriptionWithout(builder, "_normal|_enhanced|_ultimate"));
    }
}
