package de.teamlapen.vampirism.entity.converted;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.convertible.ICurableConvertedCreature;
import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.api.event.BloodDrinkEvent;
import de.teamlapen.vampirism.core.ModAttributes;
import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.entity.ConvertedCreature;
import de.teamlapen.vampirism.entity.CrossbowArrowEntity;
import de.teamlapen.vampirism.entity.SoulOrbEntity;
import de.teamlapen.vampirism.entity.ai.goals.AttackMeleeNoSunGoal;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.util.VampirismEventFactory;
import de.teamlapen.vampirism.world.ModDamageSources;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.LevelAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Supplier;

public interface CurableConvertedCreature<T extends PathfinderMob, Z extends PathfinderMob & ICurableConvertedCreature<T>> extends ConvertedCreature<T>, ICurableConvertedCreature<T> {

    class Data<T> extends ConvertedCreature.Data<T> {
        public boolean vulnerableToFire = true;
        public @NotNull EnumStrength garlicCache = EnumStrength.NONE;
        public boolean sundamageCache;
        public boolean dropSoul = false;
        public @Nullable Component name;
        public int conversionTime;
        public @Nullable UUID conversationStarter;
    }

    @Override
    Data<T> data();

    /**
     * return in {@link PathfinderMob#hurt(DamageSource, float)}
     */
    default boolean hurtC(ServerLevel level, DamageSource damageSource, float amount) {
        PathfinderMob entity = ((PathfinderMob) this);
        if (data().vulnerableToFire) {
            if (damageSource.is(DamageTypes.IN_FIRE)) {
                return DamageHandler.hurtModded(level, entity, ModDamageSources::vampireInFire, calculateFireDamage(amount));
            } else if (damageSource.is(DamageTypes.ON_FIRE)) {
                return DamageHandler.hurtModded(level, entity, ModDamageSources::vampireOnFire, calculateFireDamage(amount));
            }
        }
        return hurtSuper(level, damageSource, amount);
    }

    /**
     * return in {@link PathfinderMob#mobInteract(Player, InteractionHand)}
     */
    @SuppressWarnings("JavadocReference")
    default InteractionResult mobInteractC(@NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() == Items.GOLDEN_APPLE) {
            return interactWithCureItem(player, stack, ((PathfinderMob) this));
        }
        return mobInteractSuper(player, hand);
    }

    @Override
    default void drinkBlood(int amt, float saturationMod, boolean useRemaining, IDrinkBloodContext drinkContext) {
        BloodDrinkEvent.@NotNull EntityDrinkBloodEvent event = VampirismEventFactory.fireVampireDrinkBlood(this, amt, saturationMod, useRemaining, drinkContext);
        ((PathfinderMob) this).addEffect(new MobEffectInstance(MobEffects.REGENERATION, event.getAmount() * 20));
    }

    /**
     * return in {@link net.minecraft.world.entity.PathfinderMob#getTypeName()}
     */
    @SuppressWarnings("JavadocReference")
    default @NotNull Component getNameC(@NotNull Supplier<Component> baseName) {
        if (data().name == null) {
            this.data().name = Component.translatable("entity.vampirism.vampire").append(" ").append(baseName.get());
        }
        //noinspection DataFlowIssue
        return data().name;
    }

    /**
     * call in {@link PathfinderMob#handleEntityEvent(byte)}
     */
    default void handleEntityEventC(byte id) {
        if (!handleSound(id, ((PathfinderMob) this))) {
            handleEntityEventSuper(id);
        }
    }

    default boolean doesResistGarlic(@NotNull EnumStrength strength) {
        return !strength.isStrongerThan(EnumStrength.NONE);
    }

    @NotNull
    @Override
    default EnumStrength isGettingGarlicDamage(LevelAccessor iWorld, boolean forceRefresh) {
        if (forceRefresh) {
            data().garlicCache = Helper.getGarlicStrength(((PathfinderMob) this), iWorld);
        }
        return data().garlicCache;
    }

    @Override
    default boolean isGettingSundamage(LevelAccessor iWorld, boolean forceRefresh) {
        if (!forceRefresh) {
            return data().sundamageCache;
        }
        return (data().sundamageCache = Helper.gettingSundamge(((PathfinderMob) this), iWorld));
    }

    @Override
    default boolean isIgnoringSundamage() {
        return ((PathfinderMob) this).hasEffect(ModEffects.SUNSCREEN);
    }

    /**
     * call in {@link PathfinderMob#aiStep()}
     */
    default void aiStepC(ServerLevel level, @NotNull EntityType<T> originalType) {
        PathfinderMob entity = ((PathfinderMob) this);
        if (entity.isAlive() && this.isConverting(entity)) {
            --data().conversionTime;
            if (data().conversionTime <= 0 && net.neoforged.neoforge.event.EventHooks.canLivingConvert(entity, originalType, (timer) -> data().conversionTime = timer)) {
                this.cureEntity((ServerLevel) entity.level(), entity, originalType);
            }
        }
        if (entity.tickCount % REFERENCE.REFRESH_GARLIC_TICKS == 1) {
            isGettingGarlicDamage(entity.level(), true);
        }
        if (entity.tickCount % REFERENCE.REFRESH_SUNDAMAGE_TICKS == 2) {
            isGettingSundamage(entity.level(), true);
        }
            if (isGettingSundamage(entity.level()) && entity.tickCount % 40 == 11) {
                double dmg = entity.getAttribute(ModAttributes.SUNDAMAGE).getValue();
                if (dmg > 0) {
                    DamageHandler.hurtModded(level, entity, ModDamageSources::sunDamage, (float) dmg);
                }
            }
            if (isGettingGarlicDamage(entity.level()) != EnumStrength.NONE) {
                DamageHandler.affectVampireGarlicAmbient(this, isGettingGarlicDamage(entity.level()), entity.tickCount);
            }
            if (entity.isAlive() && entity.isInWater()) {
                entity.setAirSupply(300);
                if (entity.tickCount % 16 == 4) {
                    entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0));
                }
            }
    }

    /**
     * call in {@link PathfinderMob#die(DamageSource)}
     */
    default void dieC(@NotNull DamageSource cause) {
        if (cause.getDirectEntity() instanceof CrossbowArrowEntity && Helper.isHunter(cause.getEntity())) {
            data().dropSoul = true;
        } else if (cause.getDirectEntity() instanceof Player && Helper.isHunter(cause.getDirectEntity())) {
            ItemStack weapon = ((Player) cause.getDirectEntity()).getMainHandItem();
            if (!weapon.isEmpty() && weapon.has(ModDataComponents.DROP_VAMPIRE_SOUL)) {
                data().dropSoul = true;
            }
        } else {
            data().dropSoul = false;//In case a previous death has been canceled somehow
        }
    }

    /**
     * call in {@link PathfinderMob#readAdditionalSaveData(CompoundTag)}
     */
    @Override
    @SuppressWarnings("JavadocReference")
    default void readAdditionalSaveDataC(@NotNull CompoundTag compound) {
        ConvertedCreature.super.readAdditionalSaveDataC(compound);
        if (compound.contains("ConversionTime", 99) && compound.getInt("ConversionTime") > -1) {
            this.startConverting(compound.hasUUID("ConversionPlayer") ? compound.getUUID("ConversionPlayer") : null, compound.getInt("ConversionTime"), ((PathfinderMob) this));
        }
        if (compound.contains("source_entity", Tag.TAG_STRING)) {
            getSourceEntityDataParamOpt().ifPresent(s -> this.asEntity().getEntityData().set(s, compound.getString("source_entity")));
        }
    }

    /**
     * call in {@link PathfinderMob#addAdditionalSaveData(CompoundTag)}}
     */
    @Override
    @SuppressWarnings("JavadocReference")
    default void addAdditionalSaveDataC(@NotNull CompoundTag compound) {
        ConvertedCreature.super.addAdditionalSaveDataC(compound);
        compound.putInt("ConversionTime", this.isConverting(((PathfinderMob) this)) ? data().conversionTime : -1);
        if (data().conversationStarter != null) {
            compound.putUUID("ConversionPlayer", data().conversationStarter);
        }
        if (getSourceEntityId() != null) {
            compound.putString("source_entity", getSourceEntityId());
        }
    }

    @Override
    default void startConverting(@Nullable UUID conversionStarterIn, int conversionTimeIn, @NotNull PathfinderMob entity) {
        ICurableConvertedCreature.super.startConverting(conversionStarterIn, conversionTimeIn, entity);
        data().conversationStarter = conversionStarterIn;
        data().conversionTime = conversionTimeIn;
    }

    @Override
    default boolean useBlood(int amt, boolean allowPartial) {
        ((PathfinderMob) this).addEffect(new MobEffectInstance(MobEffects.WEAKNESS, amt * 20));
        return true;
    }

    @Override
    default boolean wantsBlood() {
        return false;
    }

    /**
     * Calculates the increased fire damage is this vampire creature is especially vulnerable to fire
     */
    default float calculateFireDamage(float amount) {
        return amount;
    }

    /**
     * call in {@link PathfinderMob#tickDeath()}
     */
    @SuppressWarnings("JavadocReference")
    default void tickDeathC() {
        PathfinderMob entity = ((PathfinderMob) this);
        if (entity.deathTime == 19) {
            if (entity.level() instanceof ServerLevel level && data().dropSoul && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                level.addFreshEntity(new SoulOrbEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), SoulOrbEntity.VARIANT.VAMPIRE));
            }
        }
    }

    /**
     * call in {@link PathfinderMob#registerGoals()}
     */
    @SuppressWarnings("JavadocReference")
    default void registerGoalsC() {
        PathfinderMob entity = ((PathfinderMob) this);
        entity.goalSelector.addGoal(1, new AvoidEntityGoal<>(entity, PathfinderMob.class, 10, 1, 1.1, VampirismAPI.factionRegistry().getPredicate(getFaction(), false, true, false, false, ModFactions.HUNTER)));
        entity.goalSelector.addGoal(4, new RestrictSunGoal(entity));
        entity.goalSelector.addGoal(5, new AttackMeleeNoSunGoal(entity, 0.9D, false));

        entity.goalSelector.addGoal(11, new RandomStrollGoal(entity, 0.7));
        entity.goalSelector.addGoal(13, new LookAtPlayerGoal(entity, Player.class, 6.0F));
        entity.goalSelector.addGoal(15, new RandomLookAroundGoal(entity));

        entity.targetSelector.addGoal(1, new HurtByTargetGoal(entity));
        entity.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(entity, Player.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), true, false, true, false, null)));
        entity.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(entity, PathfinderMob.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), false, false, false, false, null)));
    }

    @Override
    @NotNull
    default LivingEntity asEntity() {
        return ((PathfinderMob) this);
    }

    /**
     * implement as super call for {@link PathfinderMob#handleEntityEvent(byte)}
     */
    void handleEntityEventSuper(byte id);

    /**
     * implement as super call for {@link PathfinderMob#mobInteract(Player, InteractionHand)}
     */
    @SuppressWarnings("JavadocReference")
    InteractionResult mobInteractSuper(@NotNull Player player, @NotNull InteractionHand hand);

    /**
     * implement as super call for {@link PathfinderMob#hurt(DamageSource, float)}
     */
    boolean hurtSuper(ServerLevel level, DamageSource damageSource, float amount);

    static <T extends PathfinderMob, Z extends PathfinderMob & ICurableConvertedCreature<T>> void createFrom() {

    }
}
