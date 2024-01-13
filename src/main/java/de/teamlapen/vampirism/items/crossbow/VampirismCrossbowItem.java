package de.teamlapen.vampirism.items.crossbow;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.items.*;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.core.ModEnchantments;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.items.component.SelectedAmmunition;
import de.teamlapen.vampirism.mixin.accessor.CrossbowItemMixin;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public abstract class VampirismCrossbowItem extends CrossbowItem implements IFactionLevelItem<IHunterPlayer>, IVampirismCrossbow {

    protected final Tier itemTier;
    private final Supplier<ISkill<IHunterPlayer>> requiredSkill;
    protected final float arrowVelocity;
    protected final int chargeTime;

    public VampirismCrossbowItem(Properties properties, float arrowVelocity, int chargeTime, Tier itemTier, @NotNull Supplier<@Nullable ISkill<IHunterPlayer>> requiredSkill) {
        super(properties);
        this.arrowVelocity = arrowVelocity;
        this.chargeTime = chargeTime;
        this.itemTier = itemTier;
        this.requiredSkill = requiredSkill;
    }

    @Override
    public @Nullable ISkill<IHunterPlayer> getRequiredSkill(@NotNull ItemStack stack) {
        return this.requiredSkill.get();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltips, flag);
        this.addAmmunitionTypeHoverText(stack, context, tooltips, flag);
        this.addFactionToolTips(stack, context, tooltips, flag,  VampirismMod.proxy.getClientPlayer());
    }

    protected void addAmmunitionTypeHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltips, @NotNull TooltipFlag flag) {
        getAmmunition(stack).ifPresent(ammunition -> {
            tooltips.add(Component.translatable("text.vampirism.crossbow.ammo_type").append(" ").append(ammunition.getName(stack)).withStyle(ChatFormatting.GRAY));
        });
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return (super.canApplyAtEnchantingTable(stack, enchantment) && enchantment != Enchantments.MULTISHOT) || getCompatibleEnchantments().contains(enchantment);
    }

    protected Collection<Enchantment> getCompatibleEnchantments() {
        return Arrays.asList(Enchantments.INFINITY, Enchantments.PUNCH, Enchantments.POWER); // plus normal crossbow enchantments
    }

    @Override
    public boolean isValidRepairItem(@NotNull ItemStack crossbow, ItemStack repairItem) {
        return repairItem.is(Tags.Items.STRINGS) || super.isValidRepairItem(crossbow, repairItem);
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return this.itemTier.getEnchantmentValue();
    }

    @Override
    public int getMinLevel(@NotNull ItemStack stack) {
        return 0;
    }

    @Override
    public @Nullable IFaction<?> getExclusiveFaction(@NotNull ItemStack stack) {
        return  VReference.HUNTER_FACTION;
    }

    @NotNull
    @Override
    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return getAllSupportedProjectiles();
    }

    @Override
    public int getUseDuration(@NotNull ItemStack crossbow) {
        return getChargeDurationMod(crossbow) + 3;
    }

    @Override
    public void performShooting(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbow, float speed, float angle, @Nullable LivingEntity p_331602_) {
        if (!level.isClientSide()) {
            if (shooter instanceof Player player && net.neoforged.neoforge.event.EventHooks.onArrowLoose(crossbow, shooter.level(), player, 1, true) < 0) return;
            ChargedProjectiles chargedprojectiles = crossbow.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY);
            if (!chargedprojectiles.isEmpty()) {
                List<ItemStack> availableProjectiles = new ArrayList<>(chargedprojectiles.getItems());
                List<ItemStack> arrows = getShootingProjectiles(crossbow, availableProjectiles);
                this.shoot(level, shooter, hand, crossbow, arrows, speed, angle, shooter instanceof Player, p_331602_);
                onShoot(shooter, crossbow);
                crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(availableProjectiles));
            }
        }
    }

    @Override
    protected void shoot(Level level, LivingEntity shooter, InteractionHand hand, ItemStack crossbowStack, List<ItemStack> projectiles, float speed, float inaccuracy, boolean isPlayer, @Nullable LivingEntity p_331167_) {
        for (int i = 0; i < projectiles.size(); i++) {
            ItemStack itemstack = projectiles.get(i);
            if (!itemstack.isEmpty()) {
                crossbowStack.hurtAndBreak(this.getDurabilityUse(itemstack), shooter, LivingEntity.getSlotForHand(hand));
                Projectile projectile = this.createProjectile(level, shooter, crossbowStack, itemstack, isPlayer);
                this.shootProjectile(shooter, projectile, i, speed, inaccuracy, 0, p_331167_);
                level.addFreshEntity(projectile);
            }
        }
    }

    protected List<ItemStack> getShootingProjectiles(ItemStack crossbow, List<ItemStack> availableProjectiles) {
        List<ItemStack> shootingProjectiles = List.copyOf(availableProjectiles);
        availableProjectiles.clear();
        return shootingProjectiles;
    }

    protected void onShoot(LivingEntity shooter, ItemStack crossbow) {
        if (shooter instanceof ServerPlayer serverplayer) {
            CriteriaTriggers.SHOT_CROSSBOW.trigger(serverplayer, crossbow);
            serverplayer.awardStat(Stats.ITEM_USED.get(crossbow.getItem()));
        }
    }

    @Override
    public AbstractArrow customArrow(AbstractArrow arrow, ItemStack stack) {
        if (ignoreHurtTimer(stack) && arrow instanceof IEntityCrossbowArrow) {
            ((IEntityCrossbowArrow)arrow).setIgnoreHurtTimer();
        }
        return arrow;
    }

    protected boolean ignoreHurtTimer(ItemStack crossbow) {
        return false;
    }

    @Override
    public void releaseUsing(@NotNull ItemStack p_77615_1_, @NotNull Level p_77615_2_, @NotNull LivingEntity p_77615_3_, int p_77615_4_) {
        int i = this.getUseDuration(p_77615_1_) - p_77615_4_;
        float f = getPowerForTimeMod(i, p_77615_1_);
        if (f >= 1.0F && !isCharged(p_77615_1_) && tryLoadProjectiles(p_77615_3_, p_77615_1_)) {
            SoundSource soundcategory = p_77615_3_ instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            p_77615_2_.playSound(null, p_77615_3_.getX(), p_77615_3_.getY(), p_77615_3_.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundcategory, 1.0F, 1.0F / (p_77615_2_.random.nextFloat() * 0.5F + 1.0F) + 0.2F);
        }
    }

    public float getPowerForTimeMod(int p_220031_0_, ItemStack p_220031_1_) {
        float f = (float)p_220031_0_ / (float)getChargeDurationMod(p_220031_1_);
        if (f > 1.0F) {
            f = 1.0F;
        }

        return f;
    }

    public int getChargeDurationMod(ItemStack crossbow) {
        int i = crossbow.getEnchantmentLevel(Enchantments.QUICK_CHARGE);
        return i == 0 ? this.chargeTime : this.chargeTime - 2 * i;
    }

    protected int isFrugal(ItemStack crossbow) {
        return crossbow.getEnchantmentLevel(ModEnchantments.ARROW_FRUGALITY.get());
    }

    protected boolean isInfinit(ItemStack crossbow) {
        return crossbow.getEnchantmentLevel(Enchantments.INFINITY) > 0;
    }

    protected boolean canBeInfinit(ItemStack crossbow) {
        return true;
    }

    protected boolean tryLoadProjectiles(LivingEntity pShooter, ItemStack pCrossbowStack) {
        List<ItemStack> list = drawMod(pCrossbowStack, pShooter.getProjectile(pCrossbowStack), pShooter);
        if (!list.isEmpty()) {
            ArrayList<ItemStack> itemStacks = new ArrayList<>(pCrossbowStack.getOrDefault(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.EMPTY).getItems());
            itemStacks.addAll(list);
            pCrossbowStack.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(itemStacks));
            return true;
        } else {
            return false;
        }
    }

    protected List<ItemStack> getLoadingProjectiles(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity shooter) {
        if (projectileStack.getItem() instanceof IArrowContainer container) {
            return container.getAndRemoveArrows(projectileStack).stream().toList();
        } else {
            return List.of(projectileStack);
        }
    }

    protected List<ItemStack> drawMod(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity shooter) {
        if (projectileStack.isEmpty()) {
            return List.of();
        } else {
            return getLoadingProjectiles(crossbowStack, projectileStack, shooter).stream().map(projectile -> useAmmo(crossbowStack, projectile, shooter, false)).toList();
        }
    }

    protected static ItemStack useAmmo(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity shooter, boolean infinite) {
        boolean flag = !infinite && !(shooter.hasInfiniteMaterials() || (projectileStack.getItem() instanceof ArrowItem && ((ArrowItem)projectileStack.getItem()).isInfinite(projectileStack, crossbowStack, shooter)));
        if (!flag) {
            ItemStack itemstack1 = projectileStack.copyWithCount(1);
            itemstack1.set(DataComponents.INTANGIBLE_PROJECTILE, Unit.INSTANCE);
            return itemstack1;
        } else {
            ItemStack itemstack = projectileStack.split(1);
            if (projectileStack.isEmpty() && shooter instanceof Player player) {
                player.getInventory().removeItem(projectileStack);
            }

            return itemstack;
        }
    }

    @Override
    public boolean canSelectAmmunition(ItemStack crossbow) {
        return true;
    }

    @Override
    public Optional<Item> getAmmunition(ItemStack crossbow) {
        return Optional.ofNullable(crossbow.get(ModDataComponents.SELECTED_AMMUNITION)).map(SelectedAmmunition::item);
    }

    @Override
    public void setAmmunition(ItemStack crossbow, @Nullable Item ammo) {
        if (ammo == null) {
            crossbow.remove(ModDataComponents.SELECTED_AMMUNITION);
        } else {
            crossbow.set(ModDataComponents.SELECTED_AMMUNITION, new SelectedAmmunition(ammo));
        }
    }

    @Override
    public Predicate<ItemStack> getSupportedProjectiles(ItemStack crossbow) {
        return getAllSupportedProjectiles().and(stack -> {
            if (canSelectAmmunition(crossbow)) {
                return getAmmunition(crossbow).map(restriction -> stack.getItem() == restriction).orElse(true);
            } else {
                return true;
            }
        });
    }
}
