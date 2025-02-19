package de.teamlapen.vampirism.items;


import de.teamlapen.vampirism.api.items.IEntityCrossbowArrow;
import de.teamlapen.vampirism.api.items.IVampirismCrossbowArrow;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import de.teamlapen.vampirism.entity.CrossbowArrowEntity;
import de.teamlapen.vampirism.items.component.FactionRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CrossbowArrowItem extends ArrowItem implements IVampirismCrossbowArrow<CrossbowArrowEntity> {

    private final ICrossbowArrowBehavior behavior;


    public CrossbowArrowItem(ICrossbowArrowBehavior behavior, Properties properties) {
        super(FactionRestriction.apply(ModFactionTags.IS_HUNTER, properties));
        this.behavior = behavior;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> components, @NotNull TooltipFlag tooltipFlag) {
        this.behavior.appendHoverText(stack, context, components, tooltipFlag);
    }

    @NotNull
    @Override
    public AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity entity, ItemStack weapon) {
        return createArrow(level, stack, entity, entity.position().add(0, entity.getEyeHeight(), 0), weapon);
    }

    @NotNull
    public AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack stack, @NotNull Position position, @Nullable ItemStack weapon) {
        return createArrow(level, stack, null, position, weapon);
    }

    @NotNull
    public AbstractArrow createArrow(@NotNull Level level, @NotNull ItemStack stack, @Nullable LivingEntity shooter, Position position, @Nullable ItemStack weapon) {
        CrossbowArrowEntity arrowEntity = new CrossbowArrowEntity(level, position.x(), position.y(), position.z(), stack, weapon);
        arrowEntity.setBaseDamage(this.behavior.baseDamage(level, stack, shooter) * VampirismConfig.BALANCE.crossbowDamageMult.get());
        this.behavior.modifyArrow(level, stack, shooter, arrowEntity);
        if (shooter instanceof Player || shooter == null) {
            arrowEntity.pickup = this.behavior.pickupBehavior();
        } else {
            arrowEntity.pickup = AbstractArrow.Pickup.DISALLOWED;
        }
        return arrowEntity;
    }

    public ICrossbowArrowBehavior getBehavior() {
        return this.behavior;
    }

    public int tintIndex() {
        return this.behavior.color();
    }

    @Override
    public boolean isCanBeInfinite() {
        return this.behavior.canBeInfinite();
    }

    @Override
    public void onHitBlock(ItemStack arrow, @NotNull BlockPos blockPos, IEntityCrossbowArrow arrowEntity, @Nullable Entity shootingEntity) {
        this.behavior.onHitBlock(arrow, blockPos, (AbstractArrow) arrowEntity, shootingEntity, Direction.UP);
    }

    @Override
    public void onHitBlock(ItemStack arrow, @NotNull BlockPos blockPos, IEntityCrossbowArrow arrowEntity, @Nullable Entity shootingEntity, @NotNull Direction direction) {
        this.behavior.onHitBlock(arrow, blockPos, (AbstractArrow) arrowEntity, shootingEntity, direction);
    }

    @Override
    public void onHitEntity(ItemStack arrow, LivingEntity shooter, IEntityCrossbowArrow arrowEntity, Entity shootingEntity) {
        this.behavior.onHitEntity(arrow, shooter, (AbstractArrow) arrowEntity, shootingEntity);
    }
}
