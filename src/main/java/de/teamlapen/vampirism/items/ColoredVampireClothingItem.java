package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.core.ModArmorMaterials;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import org.jetbrains.annotations.NotNull;


public class ColoredVampireClothingItem extends VampireClothingItem {

    public ColoredVampireClothingItem(@NotNull ArmorType type, EnumClothingColor color, Item.Properties properties) {
        super(type, color.armorMaterial, properties);
    }

    public enum EnumClothingColor implements StringRepresentable {
        REDBLACK("red_black", ModArmorMaterials.VAMPIRE_CLOAK_RED_BLACK),
        BLACKRED("black_red", ModArmorMaterials.VAMPIRE_CLOAK_BLACK_RED),
        BLACKWHITE("black_white", ModArmorMaterials.VAMPIRE_CLOAK_BLACK_WHITE),
        WHITEBLACK("white_black", ModArmorMaterials.VAMPIRE_CLOAK_WHITE_BLACK),
        BLACKBLUE("black_blue", ModArmorMaterials.VAMPIRE_CLOAK_BLACK_BLUE);


        private final String name;
        private final ArmorMaterial armorMaterial;

        EnumClothingColor(String nameIn, ArmorMaterial armorMaterial) {
            this.name = nameIn;
            this.armorMaterial = armorMaterial;
        }

        public @NotNull String getName() {
            return getSerializedName();
        }

        @NotNull
        @Override
        public String getSerializedName() {
            return this.name;
        }


    }
}
