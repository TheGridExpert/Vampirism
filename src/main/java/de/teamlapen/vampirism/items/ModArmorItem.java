package de.teamlapen.vampirism.items;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;

public class ModArmorItem extends Item {

    public ModArmorItem(ArmorMaterial armorMaterial, ArmorType armorType, Properties properties) {
        super(addAttributes(armorMaterial.humanoidProperties(properties, armorType), ItemAttributeModifiers.EMPTY));
    }

    public ModArmorItem(ArmorMaterial armorMaterial, ArmorType armorType, Properties properties, ItemAttributeModifiers customModifiers) {
        super(addAttributes(armorMaterial.humanoidProperties(properties, armorType), customModifiers));
    }

    private static Item.Properties addAttributes(Item.Properties properties, ItemAttributeModifiers customModifiers) {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        copyTo((ItemAttributeModifiers) properties.components.map.get(DataComponents.ATTRIBUTE_MODIFIERS), builder);
        copyTo(customModifiers, builder);
        properties.attributes(builder.build());
        return properties;
    }

    private static void copyTo(ItemAttributeModifiers from, ItemAttributeModifiers.Builder to) {
        from.modifiers().forEach(entry -> to.add(entry.attribute(), entry.modifier(), entry.slot()));
    }
}
