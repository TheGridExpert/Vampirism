package de.teamlapen.vampirism.api;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.common.asm.enumextension.EnumProxy;

import java.util.function.Supplier;

public class VEnums {
    /**
     * Hunter creatures are of this creature type. But when they are counted for spawning they belong to {@link net.minecraft.world.entity.MobCategory#MONSTER}
     */
    public static final EnumProxy<MobCategory> HUNTER_CATEGORY = new EnumProxy<>(MobCategory.class, "vampirism:hunter", 15, false, false, 128);
    /**
     * Vampire creatures are of this creature type. But when they are counted for spawning they belong to {@link net.minecraft.world.entity.MobCategory#MONSTER}
     */
    public static final EnumProxy<MobCategory> VAMPIRE_CATEGORY = new EnumProxy<>(MobCategory.class, "vampirism:vampire", 30, false, false, 128);
}
