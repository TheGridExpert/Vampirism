package de.teamlapen.vampirism.mixin.accessor;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumMap;
import java.util.UUID;
import java.util.function.Supplier;

@Mixin(ArmorItem.class)
public interface ArmorItemAccessor {

    @Accessor("defaultModifiers")
    Supplier<ItemAttributeModifiers> getDefaultModifiers();

    @Mutable
    @Final
    @Accessor("defaultModifiers")
    void setDefaultModifiers(Supplier<ItemAttributeModifiers> modifiers);
}
