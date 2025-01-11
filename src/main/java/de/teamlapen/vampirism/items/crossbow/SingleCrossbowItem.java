package de.teamlapen.vampirism.items.crossbow;

import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.items.IVampirismCrossbowArrow;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SingleCrossbowItem extends HunterCrossbowItem {

    public SingleCrossbowItem(Item.Properties properties, float arrowVelocity, int chargeTime, ToolMaterial itemTier, Holder<ISkill<?>> requiredSkill) {
        super(properties, arrowVelocity, chargeTime, itemTier, requiredSkill);
    }

    @Override
    public @NotNull Predicate<ItemStack> getAllSupportedProjectiles() {
        return (stack -> stack.getItem() instanceof IVampirismCrossbowArrow<?>);
    }

    @Nullable
    @Override
    public Holder<ISkill<?>> requiredSkill(@Nonnull ItemStack stack) {
        return null;
    }

    @Override
    public float getInaccuracy(ItemStack stack, boolean doubleCrossbow) {
        return doubleCrossbow ? 2f : 0.4f;
    }
}