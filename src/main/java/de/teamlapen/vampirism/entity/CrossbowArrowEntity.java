package de.teamlapen.vampirism.entity;

import de.teamlapen.vampirism.api.items.IEntityCrossbowArrow;
import de.teamlapen.vampirism.api.items.IVampirismCrossbowArrow;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.items.CrossbowArrowItem;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class CrossbowArrowEntity extends AbstractArrow implements IEntityCrossbowArrow {

    @NotNull
    private ItemStack arrowStack = new ItemStack(ModItems.CROSSBOW_ARROW_NORMAL.get());
    private boolean ignoreHurtTimer = false;

    public CrossbowArrowEntity(@NotNull EntityType<? extends CrossbowArrowEntity> type, @NotNull Level world) {
        super(type, world);
    }

    public CrossbowArrowEntity(Level level, LivingEntity entity, ItemStack stack, ItemStack weapon) {
        super(ModEntities.CROSSBOW_ARROW.get(), entity, level, stack, weapon);
        this.arrowStack = stack.copy();
        this.arrowStack.setCount(1);
    }


    /**
     * @param arrow ItemStack of the represented arrow. Is copied.
     */
    public CrossbowArrowEntity(@NotNull Level worldIn, double x, double y, double z, @NotNull ItemStack arrow, ItemStack weapon) {
        super(ModEntities.CROSSBOW_ARROW.get(), x, y, z, worldIn, arrow, weapon);
        this.setPos(x, y, z);
        this.arrowStack = arrow.copy();
        arrowStack.setCount(1);
    }

    @Nullable
    public IVampirismCrossbowArrow.ICrossbowArrowBehavior getArrowType() {
        return getPickupItem().getItem() instanceof CrossbowArrowItem ? ((CrossbowArrowItem) getPickupItem().getItem()).getBehavior() : null;
    }


    public @NotNull RandomSource getRNG() {
        return this.random;
    }

    /**
     * Allows the arrow to ignore the hurt timer of the hit entity
     */
    public void setIgnoreHurtTimer() {
        this.ignoreHurtTimer = true;
    }

    @Override
    protected void doPostHurtEffects(@NotNull LivingEntity living) {
        super.doPostHurtEffects(living);
        Item item = arrowStack.getItem();
        if (item instanceof IVampirismCrossbowArrow) {
            if (ignoreHurtTimer && living.invulnerableTime > 0) {
                living.invulnerableTime = 0;
            }
            ((IVampirismCrossbowArrow<?>) item).onHitEntity(arrowStack, living, this, getOwner());
        }
    }

    @NotNull
    @Override
    protected ItemStack getPickupItem() {
        return arrowStack;
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return ModItems.CROSSBOW_ARROW_NORMAL.toStack();
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult blockRayTraceResult) { //onHitBlock
        Item item = arrowStack.getItem();
        if (item instanceof IVampirismCrossbowArrow) {
            ((IVampirismCrossbowArrow<?>) item).onHitBlock(arrowStack, (blockRayTraceResult).getBlockPos(), this, getOwner(), blockRayTraceResult.getDirection());
        }
        super.onHitBlock(blockRayTraceResult);
    }

    @Override
    public void shoot(double pX, double pY, double pZ, float pVelocity, float pInaccuracy) {
        super.shoot(pX, pY, pZ, pVelocity, pInaccuracy);
    }
}
