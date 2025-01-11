package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.blocks.BloodContainerBlock;
import de.teamlapen.vampirism.core.ModFluids;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import de.teamlapen.vampirism.fluids.BloodHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;

public class FeedingAdapterItem extends Item {

    public FeedingAdapterItem(Item.Properties properties) {
        super(properties.stacksTo(1));
    }


    @NotNull
    @Override
    public ItemUseAnimation getUseAnimation(@NotNull ItemStack stack) {
        return ItemUseAnimation.DRINK;
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity p_344979_) {
        return 15;
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity player, @NotNull ItemStack stack, int count) {
        if (!(player instanceof Player) || !player.isAlive()) {
            player.releaseUsingItem();
            return;
        }
        ItemStack bloodContainer = BloodHelper.getBloodContainerInInventory(((Player) player).getInventory(), true, false);
        FluidStack fluidStack = BloodContainerBlock.getFluidFromItemStack(bloodContainer);
        int blood = fluidStack.isEmpty() || fluidStack.getFluid() != ModFluids.BLOOD.get() ? 0 : fluidStack.getAmount();
        VampirePlayer vampire = VampirePlayer.get((Player) player);
        if (blood == 0 || vampire.getLevel() == 0 || !vampire.getBloodStats().needsBlood()) {
            player.releaseUsingItem();
            return;
        }


        if (blood > 0 && count == 1) {
            int drink = Math.min(blood, 3 * VReference.FOOD_TO_FLUID_BLOOD);
            BloodContainerBlock.writeFluidToItemStack(bloodContainer, new FluidStack(ModFluids.BLOOD.get(), blood - drink));
            vampire.drinkBlood(Math.round(((float) drink) / VReference.FOOD_TO_FLUID_BLOOD), 0.45F, false, new DrinkBloodContext(bloodContainer));

            blood = blood - drink;
            if (blood > 0) {
                player.startUsingItem(player.getUsedItemHand());
            }
        }
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        VampirePlayer vampire = VampirePlayer.get(playerIn);
        if (vampire.getLevel() == 0) return InteractionResult.PASS;


        if (vampire.getBloodStats().needsBlood() && !BloodHelper.getBloodContainerInInventory(playerIn.getInventory(), true, false).isEmpty()) {
            playerIn.startUsingItem(handIn);
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
