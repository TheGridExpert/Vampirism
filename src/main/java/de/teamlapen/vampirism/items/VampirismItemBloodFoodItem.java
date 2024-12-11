package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import de.teamlapen.vampirism.items.consume.BloodFoodProperties;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class VampirismItemBloodFoodItem extends Item {

    public VampirismItemBloodFoodItem(Properties properties, BloodFoodProperties vampireFood) {
        super(properties.component(ModDataComponents.VAMPIRE_FOOD.get(), vampireFood));
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving) {
        super.finishUsingItem(stack, worldIn, entityLiving);
        if (!Helper.isVampire(entityLiving)) {
            entityLiving.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * 20));
        }
        return stack;
    }
}
