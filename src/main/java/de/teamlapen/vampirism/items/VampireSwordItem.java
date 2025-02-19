package de.teamlapen.vampirism.items;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.entity.player.skills.IRefinementHandler;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.api.items.IBloodChargeable;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.core.tags.ModEntityTags;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.player.vampire.skills.VampireSkills;
import de.teamlapen.vampirism.items.component.BloodCharged;
import de.teamlapen.vampirism.items.component.FactionRestriction;
import de.teamlapen.vampirism.items.component.PureLevel;
import de.teamlapen.vampirism.items.component.SwordTraining;
import de.teamlapen.vampirism.particle.FlyingBloodParticleOptions;
import de.teamlapen.vampirism.particle.GenericParticleOptions;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class VampireSwordItem extends VampirismSwordItem implements IBloodChargeable {

    /**
     * Speed modifier on max training
     */
    private final float trainedAttackSpeedIncrease;

    public VampireSwordItem(@NotNull ToolMaterial material, int attackDamage, float trainSpeedIncrease, @NotNull Properties prop) {
        super(material, attackDamage, material.speed(), FactionRestriction.apply(ModFactions.VAMPIRE, prop).component(ModDataComponents.BLOOD_CHARGED, new BloodCharged(0)));
        this.trainedAttackSpeedIncrease = trainSpeedIncrease;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        tooltip.add(Component.translatable("text.vampirism.purity", stack.getOrDefault(ModDataComponents.PURE_LEVEL, PureLevel.LOW).level() + 1).withStyle(ChatFormatting.DARK_RED));
        float charged = getChargePercentage(stack);
        float trained = getTrained(stack, VampirismMod.proxy.getClientPlayer());
        tooltip.add(Component.translatable("text.vampirism.sword_charged").append(Component.literal(" " + ((int) Math.ceil(charged * 100f)) + "%")).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("text.vampirism.sword_trained").append(Component.literal(" " + ((int) Math.ceil(trained * 100f)) + "%")).withStyle(ChatFormatting.DARK_AQUA));

        super.appendHoverText(stack, context, tooltip, flagIn);
    }

    @Override
    public boolean canBeCharged(@NotNull ItemStack stack) {
        return getChargePercentage(stack) < 1f;
    }

    @Override
    public int charge(@NotNull ItemStack stack, int amount) {
        float factor = getChargingFactor(stack);
        float charge = getChargePercentage(stack);
        float actual = Math.min(factor * amount, 1f - charge);
        this.setCharged(stack, charge + actual);
        return (int) (actual / factor);
    }

    /**
     * Prevent the player from being asked to name this item
     */
    public void doNotName(@NotNull ItemStack stack) {
        stack.set(ModDataComponents.DO_NOT_NAME, Unit.INSTANCE);
    }

    @Override
    public int getUseDuration(@NotNull ItemStack pStack, @NotNull LivingEntity p_344979_) {
        return 40;
    }

    @NotNull
    @Override
    public ItemStack finishUsingItem(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull LivingEntity entityLiving) {
        if (!(entityLiving instanceof Player player)) return stack;
        VampirePlayer vampire = VampirePlayer.get(player);
        int amount = (vampire.getRefinementHandler().isRefinementEquipped(ModRefinements.BLOOD_CHARGE_SPEED) ? VampirismConfig.BALANCE.vrBloodChargeSpeedMod.get() : 2);
        if (player.isCreative() || vampire.useBlood(amount, false)) {
            this.charge(stack, amount * VReference.FOOD_TO_FLUID_BLOOD);
        }
        if (getChargePercentage(stack) == 1) {
            tryName(stack, (Player) entityLiving);
        }
        return stack;
    }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        //Vampire Finisher skill
        if (attacker instanceof Player player && !Helper.isVampire(target) && !target.getType().is(ModEntityTags.IGNORE_VAMPIRE_SWORD_FINISHER)) {
            ISkillHandler<IVampirePlayer> skillHandler = VampirePlayer.get(player).getSkillHandler();
            IRefinementHandler<IVampirePlayer> refinementHandler = VampirePlayer.get(player).getRefinementHandler();
            double relTh = VampirismConfig.BALANCE.vsSwordFinisherMaxHealth.get() * (skillHandler.isSkillEnabled(VampireSkills.SWORD_FINISHER) ? (refinementHandler.isRefinementEquipped(ModRefinements.SWORD_FINISHER) ? VampirismConfig.BALANCE.vrSwordFinisherThresholdMod.get() : 1d) : 0d);
            if (relTh > 0 && target.getHealth() <= target.getMaxHealth() * relTh) {
                if (player.level() instanceof ServerLevel level) {
                    DamageHandler.hurtModded(level, target, s -> s.getPlayerAttackWithBypassArmor(player), 10000f);
                }
                Vec3 center = Vec3.atLowerCornerOf(target.blockPosition());
                center.add(0, target.getBbHeight() / 2d, 0);
                ModParticles.spawnParticlesServer(target.level(), new GenericParticleOptions(VResourceLocation.mc("effect_4"), 12, 0xE02020), center.x, center.y, center.z, 15, 0.5, 0.5, 0.5, 0);
            }
        }
        //Update training on kill
        if (target.getHealth() <= 0.0f && Helper.isVampire(attacker)) {
            float trained = getTrained(stack, attacker);
            int exp = target instanceof Player ? 10 : (attacker instanceof Player && attacker.level() instanceof ServerLevel serverLevel ? target.getExperienceReward(serverLevel, attacker) : 5);
            float newTrained = exp / 5f * (1.0f - trained) / 15f;
            if (attacker instanceof Player && VampirePlayer.get((Player) attacker).getRefinementHandler().isRefinementEquipped(ModRefinements.SWORD_TRAINED_AMOUNT)) {
                newTrained *= VampirismConfig.BALANCE.vrSwordTrainingSpeedMod.get();
            }
            trained += newTrained;
            setTrained(stack, attacker, trained);
        }
        //Consume blood
        float charged = getChargePercentage(stack);
        charged -= getChargeUsage(stack);
        setCharged(stack, charged);
        attacker.setItemInHand(InteractionHand.MAIN_HAND, stack);

        return super.hurtEnemy(stack, target, attacker);
    }

    public boolean isFullyCharged(@NotNull ItemStack stack) {
        return getChargePercentage(stack) == 1f;
    }

    @Override
    public boolean onEntitySwing(ItemStack stack, LivingEntity entity, InteractionHand hand) {
        return !Helper.isVampire(entity);
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level worldIn, @NotNull Entity entityIn, int itemSlot, boolean isSelected) {
        //Try to minimize execution time, but tricky since off hand selection is not directly available, but it can only be off hand if itemSlot 0
        if (worldIn.isClientSide && (isSelected || itemSlot == 0)) {
            float charged = getChargePercentage(stack);
            if (charged > 0 && entityIn.tickCount % ((int) (20 + 100 * (1f - charged))) == 0 && entityIn instanceof LivingEntity) {
                boolean secondHand = !isSelected && ((LivingEntity) entityIn).getItemInHand(InteractionHand.OFF_HAND).equals(stack);
                if (isSelected || secondHand) {
                    spawnChargedParticle((LivingEntity) entityIn, isSelected);
                }
            }
        }
    }

    @Override
    public void onUseTick(Level level, @NotNull LivingEntity player, ItemStack stack, int count) {
        if (player.getCommandSenderWorld().isClientSide) {
            if (count % 3 == 0) {
                spawnChargingParticle(player, player.getMainHandItem().equals(stack));
            }
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        return !Helper.isVampire(player);
    }

    /**
     * Might want to use {@link #charge(ItemStack, int)} instead to charge it with mB of blood
     *
     * @param value Is clamped between 0 and 1
     */
    public void setCharged(@NotNull ItemStack stack, float value) {
        stack.set(ModDataComponents.BLOOD_CHARGED, stack.getOrDefault(ModDataComponents.BLOOD_CHARGED, BloodCharged.EMPTY).charged(value));
    }

    /**
     * Sets the stored trained value for the given player
     *
     * @param value Clamped between 0 and 1
     */
    public void setTrained(@NotNull ItemStack stack, @NotNull LivingEntity player, float value) {
        stack.set(ModDataComponents.VAMPIRE_SWORD, stack.getOrDefault(ModDataComponents.VAMPIRE_SWORD, SwordTraining.EMPTY).addTraining(player.getUUID(), value));
    }

    /**
     * If the stack is not named and the player hasn't been named before, ask the player to name this stack
     */
    public void tryName(@NotNull ItemStack stack, @NotNull Player player) {
        if (!stack.has(DataComponents.CUSTOM_NAME) && player.level().isClientSide() && !stack.has(ModDataComponents.DO_NOT_NAME)) {
            VampirismMod.proxy.displayNameSwordScreen(stack);
            player.level().playLocalSound((player).getX(), (player).getY(), (player).getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1f, false);
        }
    }

    /**
     * Updated during {@link net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent}
     *
     * @return True if the cached value was updated
     */
    public boolean updateTrainedCached(@NotNull ItemStack stack, @NotNull LivingEntity player) {
        float cached = getTrained(stack);
        float trained = getTrained(stack, player);
        if (cached != trained) {
            stack.set(ModDataComponents.TRAINING_CACHE, trained);
            return true;
        }
        return false;
    }

    @NotNull
    @Override
    public InteractionResult use(@NotNull Level worldIn, @NotNull Player playerIn, @NotNull InteractionHand handIn) {
        ItemStack stack = playerIn.getItemInHand(handIn);
        VampirePlayer vampire = VampirePlayer.get(playerIn);
        if (vampire.getLevel() == 0) return InteractionResult.PASS;

        if (this.canBeCharged(stack) && playerIn.isShiftKeyDown() && vampire.getSkillHandler().isSkillEnabled(VampireSkills.BLOOD_CHARGE) && (playerIn.isCreative() || vampire.getBloodLevel() >= (vampire.getRefinementHandler().isRefinementEquipped(ModRefinements.BLOOD_CHARGE_SPEED) ? VampirismConfig.BALANCE.vrBloodChargeSpeedMod.get() : 2))) {
            playerIn.startUsingItem(handIn);
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    protected float getAttackDamageModifier(@NotNull ItemStack stack) {
        return getChargePercentage(stack) > 0 ? 0.8f : 0;
    }

    protected float getSpeedModifier(@NotNull ItemStack stack) {
        return getTrained(stack) * this.trainedAttackSpeedIncrease;
    }

    /**
     * @return The amount of charge consumed per hit
     */
    protected abstract float getChargeUsage(ItemStack stack);

    protected float getPurityArmorToughnessModifier(@NotNull ItemStack stack) {
        return switch (stack.getOrDefault(ModDataComponents.PURE_LEVEL, PureLevel.LOW).level()) {
            case 0 -> 0;
            case 1 -> 0.035f;
            case 2 -> 0.06f;
            case 3 -> 0.1f;
            default -> 0.15f;
        };
    }

    protected float getPurityInteractionRangeModifier(@NotNull ItemStack stack) {
        return Math.clamp((stack.getOrDefault(ModDataComponents.PURE_LEVEL, PureLevel.LOW).level()/4f) * 0.5f,0f,0.5f);
    }

    /**
     * Gets the charged value from the tag compound
     *
     * @return Value between 0 and 1
     */
    @Override
    public float getChargePercentage(@NotNull ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.BLOOD_CHARGED, BloodCharged.EMPTY).charged();
    }

    /**
     * @return Charging factor multiplied with amount to get charge percentage
     */
    protected abstract float getChargingFactor(ItemStack stack);

    /**
     * Gets a cached trained value from the tag compound
     *
     * @return Value between 0 and 1. Defaults to 0
     */
    protected float getTrained(@NotNull ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.TRAINING_CACHE, 0f);
    }

    /**
     * Gets the trained value from the tag compound
     *
     * @return Value between 0 and 1. Defaults to 0
     */
    protected float getTrained(@NotNull ItemStack stack, @Nullable LivingEntity player) {
        if (player == null) return getTrained(stack);
        return stack.getOrDefault(ModDataComponents.VAMPIRE_SWORD, SwordTraining.EMPTY).training().getOrDefault(player.getUUID(), 0f);
    }

    private void spawnChargedParticle(@NotNull LivingEntity player, boolean mainHand) {
        Vec3 mainPos = UtilLib.getItemPosition(player, mainHand);
        for (int j = 0; j < 3; ++j) {
            Vec3 pos = mainPos.add((player.getRandom().nextFloat() - 0.5f) * 0.1f, (player.getRandom().nextFloat() - 0.3f) * 0.9f, (player.getRandom().nextFloat() - 0.5f) * 0.1f);
            ModParticles.spawnParticleClient(player.getCommandSenderWorld(), new FlyingBloodParticleOptions((int) (4.0F / (player.getRandom().nextFloat() * 0.9F + 0.1F)), true, pos.x + (player.getRandom().nextFloat() - 0.5D) * 0.1D, pos.y + (player.getRandom().nextFloat() - 0.5D) * 0.1D, pos.z + (player.getRandom().nextFloat() - 0.5D) * 0.1D, VResourceLocation.mc("glitter_1")), pos.x, pos.y, pos.z);
        }
    }

    private void spawnChargingParticle(@NotNull LivingEntity player, boolean mainHand) {
        Vec3 pos = UtilLib.getItemPosition(player, mainHand);
        if (player.getAttackAnim(1f) > 0f) return;
        pos = pos.add((player.getRandom().nextFloat() - 0.5f) * 0.1f, (player.getRandom().nextFloat() - 0.3f) * 0.9f, (player.getRandom().nextFloat() - 0.5f) * 0.1f);
        Vec3 playerPos = new Vec3((player).getX(), (player).getY() + player.getEyeHeight() - 0.2f, (player).getZ());
        ModParticles.spawnParticleClient(player.getCommandSenderWorld(), new FlyingBloodParticleOptions((int) (4.0F / (player.getRandom().nextFloat() * 0.6F + 0.1F)), true, pos.x, pos.y, pos.z), playerPos.x, playerPos.y, playerPos.z);
    }

    protected float getPurityChargeUsageModifier(ItemStack stack) {
        var purity = stack.getOrDefault(ModDataComponents.PURE_LEVEL, PureLevel.LOW).level();
        return switch (purity) {
            case 0 -> 1f;
            case 1 -> 0.9f;
            case 2 -> 0.8f;
            case 3 -> 0.6f;
            default -> 0.4f;
        };
    }

    protected float getPurityChargeSpeedModifier(ItemStack stack) {
        var purity = stack.getOrDefault(ModDataComponents.PURE_LEVEL, PureLevel.LOW).level();
        return switch (purity) {
            case 0 -> 1f;
            case 1 -> 1.2f;
            case 2 -> 1.4f;
            case 3 -> 1.6f;
            default -> 2f;
        };
    }

}
