package de.teamlapen.vampirism.entity.vampire;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.difficulty.Difficulty;
import de.teamlapen.vampirism.api.entity.IEntityLeader;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.vampire.IDrinkBloodContext;
import de.teamlapen.vampirism.api.entity.vampire.IBasicVampire;
import de.teamlapen.vampirism.api.event.BloodDrinkEvent;
import de.teamlapen.vampirism.api.world.ICaptureAttributes;
import de.teamlapen.vampirism.config.BalanceMobProps;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.effects.BadOmenEffect;
import de.teamlapen.vampirism.entity.IEntityFollower;
import de.teamlapen.vampirism.entity.ai.goals.*;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.hunter.HunterBaseEntity;
import de.teamlapen.vampirism.entity.minion.VampireMinionEntity;
import de.teamlapen.vampirism.entity.minion.management.MinionTasks;
import de.teamlapen.vampirism.entity.player.vampire.skills.VampireSkills;
import de.teamlapen.vampirism.util.VampireVillage;
import de.teamlapen.vampirism.util.VampirismEventFactory;
import de.teamlapen.vampirism.world.MinionWorldData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.neoforged.neoforge.event.EventHooks.onEffectRemoved;

/**
 * Basic vampire mob.
 * Follows nearby advanced vampire
 */
public class BasicVampireEntity extends VampireBaseEntity implements IBasicVampire, IEntityFollower {

    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(BasicVampireEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(BasicVampireEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LEVEL = 2;
    private static final int ANGRY_TICKS_PER_ATTACK = 120;

    private static final Logger LOGGER = LogManager.getLogger();

    public static AttributeSupplier.@NotNull Builder getAttributeBuilder() {
        return VampireBaseEntity.getAttributeBuilder()
                .add(Attributes.MAX_HEALTH, 1)
                .add(Attributes.ATTACK_DAMAGE, BalanceMobProps.mobProps.VAMPIRE_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, BalanceMobProps.mobProps.VAMPIRE_SPEED);
    }

    /**
     * available actions for AI task & task
     */
    private int bloodtimer = 100;
    private @Nullable IEntityLeader advancedLeader = null;
    private int angryTimer = 0;
    private Goal tasks_avoidHunter;
    @Nullable
    private ICaptureAttributes villageAttributes;
    private boolean attack;

    public BasicVampireEntity(EntityType<? extends BasicVampireEntity> type, Level world) {
        super(type, world);
        this.canSuckBloodFromPlayer = true;
        hasArms = true;
        this.setSpawnRestriction(SpawnRestriction.SPECIAL);
        this.enableImobConversion();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("level", getEntityLevel());
        nbt.putInt("type", getEntityTextureType());
        nbt.putBoolean("attack", this.attack);
    }

    @Override
    public void attackVillage(ICaptureAttributes totem) {
        this.goalSelector.removeGoal(tasks_avoidHunter);
        this.villageAttributes = totem;
        this.attack = true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (bloodtimer > 0) {
            bloodtimer--;
        }
        if (angryTimer > 0) {
            angryTimer--;
        }

        if (this.tickCount % 9 == 3) {
            if (VampirismConfig.BALANCE.vpFireResistanceReplace.get() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                MobEffectInstance fireResistance = this.removeEffectNoUpdate(MobEffects.FIRE_RESISTANCE);
                assert fireResistance != null;
                onEffectRemoved(this, fireResistance);
                this.addEffect(new MobEffectInstance(ModEffects.FIRE_PROTECTION, fireResistance.getDuration(), fireResistance.getAmplifier()));
            }
        }
    }

    @Override
    public void defendVillage(ICaptureAttributes totem) {
        this.goalSelector.removeGoal(tasks_avoidHunter);
        this.villageAttributes = totem;
        this.attack = false;
    }

    /**
     * Assumes preconditions as been met. Check conditions but does not give feedback to user
     */
    public void convertToMinion(@NotNull Player lord) {
        FactionPlayerHandler fph = FactionPlayerHandler.get(lord);
        if (fph.getMaxMinions() > 0) {
            MinionWorldData.getData(lord.level()).map(w -> w.getOrCreateController(fph)).ifPresent(controller -> {
                if (controller.hasFreeMinionSlot()) {
                    if (IFaction.is(fph.getFaction(), this.getFaction())) {
                        boolean hasIncreasedStats = fph.getSkillHandler().map(skillHandler -> skillHandler.isSkillEnabled(VampireSkills.MINION_STATS_INCREASE)).orElse(false);
                        VampireMinionEntity.VampireMinionData data = new VampireMinionEntity.VampireMinionData("Minion", this.getEntityTextureType(), false, hasIncreasedStats);
                        data.updateEntityCaps(this.serializeAttachments(lord.registryAccess()));
                        int id = controller.createNewMinionSlot(data, ModEntities.VAMPIRE_MINION.get());
                        if (id < 0) {
                            LOGGER.error("Failed to get minion slot");
                            return;
                        }
                        VampireMinionEntity minion = ModEntities.VAMPIRE_MINION.get().create(this.level(), EntitySpawnReason.CONVERSION);
                        minion.claimMinionSlot(id, controller);
                        minion.copyPosition(this);
                        minion.markAsConverted();
                        controller.activateTask(0, MinionTasks.STAY.get());
                        UtilLib.replaceEntity(this, minion);

                    } else {
                        LOGGER.warn("Wrong faction for minion");
                    }
                } else {
                    LOGGER.warn("No free slot");
                }
            });


        } else {
            LOGGER.error("Can't have minions");
        }

    }

    /**
     * @return The advanced vampire this entity is following or null if none
     */
    @Nullable
    public IEntityLeader getAdvancedLeader() {
        return advancedLeader;
    }

    /**
     * Set an advanced vampire, this vampire should follow
     *
     * @param advancedLeader new leader
     */
    public void setAdvancedLeader(@Nullable IEntityLeader advancedLeader) {
        this.advancedLeader = advancedLeader;
    }

    @Override
    public boolean isFollowing() {
        return this.advancedLeader != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends LivingEntity & IEntityLeader> T getLeader() {
        return (T) this.advancedLeader;
    }

    @Override
    public <T extends LivingEntity & IEntityLeader> void setLeader(T leader) {
        this.advancedLeader = leader;
    }

    @Nullable
    @Override
    public ICaptureAttributes getCaptureInfo() {
        return villageAttributes;
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        if (this.villageAttributes == null) {
            BadOmenEffect.handlePotentialBannerKill(cause.getEntity(), this);
        }
        super.die(cause);
    }

    @Override
    public void drinkBlood(int amt, float saturationMod, boolean useRemaining, IDrinkBloodContext drinkContext) {
        BloodDrinkEvent.@NotNull EntityDrinkBloodEvent event = VampirismEventFactory.fireVampireDrinkBlood(this, amt, saturationMod, useRemaining, drinkContext);
        this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, event.getAmount() * 20));
        boolean dedicated = ServerLifecycleHooks.getCurrentServer().isDedicatedServer();
        bloodtimer += event.getAmount() * 40 + this.getRandom().nextInt(1000) * (dedicated ? 2 : 1);
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        if ((reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.STRUCTURE) && this.getRandom().nextInt(50) == 0) {
            this.setItemSlot(EquipmentSlot.HEAD, VampireVillage.createBanner(worldIn.registryAccess()));
        }
        getEntityData().set(TYPE, this.getRandom().nextInt(TYPES));

        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    @Override
    public int getMaxEntityLevel() {
        return MAX_LEVEL;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 2400;
    }

    @Nullable
    @Override
    public AABB getTargetVillageArea() {
        return villageAttributes == null ? null : villageAttributes.getVillageArea();
    }

    @Override
    public boolean isAttackingVillage() {
        return villageAttributes != null && attack;
    }

    @Override
    public boolean isDefendingVillage() {
        return villageAttributes != null && !attack;
    }

//IMob -------------------------------------------------------------------------------------------------------------

    @Override
    public boolean isIgnoringSundamage() {
        float health = this.getHealth() / this.getMaxHealth();
        return super.isIgnoringSundamage() || angryTimer > 0 && health < 0.7f || health < 0.3f;
    }

    @Override
    public int getEntityTextureType() {
        int i = getEntityData().get(TYPE);
        return Math.max(i, 0);
    }
    //Entityactions ----------------------------------------------------------------------------------------------------

    @Override
    public int getEntityLevel() {
        return getEntityData().get(LEVEL);
    }

    @Override
    public void setEntityLevel(int level) {
        if (level >= 0) {
            getEntityData().set(LEVEL, level);
            this.updateEntityAttributes();
            if (level == 2) {
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 1, false, false));
            }
            if (level == 1) {
                this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            } else {
                this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
            }

        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, @NotNull DamageSource damageSource, float amount) {
        boolean flag = super.hurtServer(level, damageSource, amount);
        if (flag) angryTimer += ANGRY_TICKS_PER_ATTACK;
        return flag;
    }

    @Override
    public void remove(@NotNull RemovalReason p_146834_) {
        super.remove(p_146834_);
        if (advancedLeader != null) {
            advancedLeader.decreaseFollowerCount();
        }
    }

    @Override
    public void stopVillageAttackDefense() {
        this.setCustomName(null);
        this.villageAttributes = null;
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tagCompund) {
        super.readAdditionalSaveData(tagCompund);
        if (tagCompund.contains("level")) {
            setEntityLevel(tagCompund.getInt("level"));
        }
        if (tagCompund.contains("attack")) {
            this.attack = tagCompund.getBoolean("attack");
        }
        if (tagCompund.contains("type")) {
            int t = tagCompund.getInt("type");
            getEntityData().set(TYPE, t < TYPES && t >= 0 ? t : -1);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (advancedLeader != null && !advancedLeader.asEntity().isAlive()) {
            advancedLeader = null;
        }
    }

    @Override
    public boolean wantsBlood() {
        return bloodtimer == 0;
    }

    @Override
    public int suggestEntityLevel(@NotNull Difficulty d) {
        return switch (this.random.nextInt(5)) {
            case 0 -> (int) (d.minPercLevel() / 100F * MAX_LEVEL);
            case 1 -> (int) (d.avgPercLevel() / 100F * MAX_LEVEL);
            case 2 -> (int) (d.maxPercLevel() / 100F * MAX_LEVEL);
            default -> this.random.nextInt(MAX_LEVEL + 1);
        };
    }

    @Override
    protected float calculateFireDamage(float amount) {
        float protectionMod = 1F;
        MobEffectInstance protection = this.getEffect(ModEffects.FIRE_PROTECTION);
        if (protection != null) {
            protectionMod = 1F / (2F + protection.getAmplifier());
        }

        return (float) (amount * protectionMod * BalanceMobProps.mobProps.VAMPIRE_FIRE_VULNERABILITY) * (getEntityLevel() * 0.5F + 1);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEVEL, -1);
        builder.define(TYPE, -1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.ENTITY_VAMPIRE_SCREAM.get();
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        return 6 + getEntityLevel();
    }

    @Override
    protected @NotNull EntityType<?> getIMobTypeOpt(boolean iMob) {
        return iMob ? ModEntities.VAMPIRE_IMOB.get() : ModEntities.VAMPIRE.get();
    }

    @NotNull
    @Override
    protected InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        if (this.isAlive() && !player.isShiftKeyDown()) {
            if (!level().isClientSide) {
                FactionPlayerHandler handler = FactionPlayerHandler.get(player);
                int vampireLevel = handler.getCurrentLevel(ModFactions.VAMPIRE);
                if (vampireLevel > 0) {
                    if (handler.getMaxMinions() > 0) {
                        ItemStack heldItem = player.getItemInHand(hand);
                        //noinspection Convert2MethodRef
                        boolean freeSlot = MinionWorldData.getData(player.level()).map(data -> data.getOrCreateController(handler)).map(c -> c.hasFreeMinionSlot()).orElse(false);
                        player.displayClientMessage(Component.translatable("text.vampirism.basic_vampire.minion.available"), true);
                        if (heldItem.getItem() == ModItems.VAMPIRE_MINION_BINDING.get()) {
                            if (!freeSlot) {
                                player.displayClientMessage(Component.translatable("text.vampirism.basic_vampire.minion.no_free_slot"), true);
                            } else {
                                String key = switch (this.getRandom().nextInt(3)) {
                                    case 0 -> "text.vampirism.basic_vampire.minion.start_serving1";
                                    case 1 -> "text.vampirism.basic_vampire.minion.start_serving2";
                                    default -> "text.vampirism.basic_vampire.minion.start_serving3";
                                };
                                player.displayClientMessage(Component.translatable(key), false);
                                convertToMinion(player);
                                if (!player.getAbilities().instabuild) heldItem.shrink(1);
                            }
                        } else if (freeSlot) {
                            player.displayClientMessage(Component.translatable("text.vampirism.basic_vampire.minion.require_binding", Component.translatable(ModItems.VAMPIRE_MINION_BINDING.get().getDescriptionId())), true);
                        }
                        return InteractionResult.SUCCESS;
                    }
                    return InteractionResult.PASS;
                }
            }
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new BreakDoorGoal(this, (difficulty) -> difficulty == net.minecraft.world.Difficulty.HARD));//Only break doors on hard difficulty
        this.tasks_avoidHunter = new AvoidEntityGoal<>(this, PathfinderMob.class, 10, 1.0, 1.1, VampirismAPI.factionRegistry().getPredicate(getFaction(), false, true, false, false, ModFactions.HUNTER));
        this.goalSelector.addGoal(2, this.tasks_avoidHunter);
        this.goalSelector.addGoal(2, new RestrictSunVampireGoal<>(this));
        this.goalSelector.addGoal(3, new FleeSunVampireGoal<>(this, 0.9, false));
        this.goalSelector.addGoal(4, new AttackMeleeNoSunGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new BiteNearbyEntityVampireGoal<>(this));
        this.goalSelector.addGoal(6, new FollowAdvancedVampireGoal(this, 1.0));
        this.goalSelector.addGoal(7, new MoveToBiteableVampireGoal<>(this, 0.75));
        this.goalSelector.addGoal(8, new MoveThroughVillageGoal(this, 0.6, true, 600, () -> false));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.7));
        this.goalSelector.addGoal(10, new LookAtClosestVisibleGoal(this, Player.class, 20F, 0.6F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, HunterBaseEntity.class, 17F));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(3, new VampireHurtByTargetGoal(this).setAlertOthers());
        this.targetSelector.addGoal(4, new AttackVillageGoal<>(this));
        this.targetSelector.addGoal(4, new DefendVillageGoal<>(this));//Should automatically be mutually exclusive with  attack village
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), true, false, true, false, null)));
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, PathfinderMob.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), false, true, false, false, null)));//TODO maybe make them not attack hunters, although it looks interesting
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, PatrollingMonster.class, 5, true, true, (living, level) -> UtilLib.isInsideStructure(living, StructureTags.VILLAGE)));
        this.targetSelector.addGoal(8, new DefendLeaderGoal<>(this));
    }

    protected void updateEntityAttributes() {
        int l = Math.max(getEntityLevel(), 0);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BalanceMobProps.mobProps.VAMPIRE_MAX_HEALTH + BalanceMobProps.mobProps.VAMPIRE_MAX_HEALTH_PL * l);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BalanceMobProps.mobProps.VAMPIRE_ATTACK_DAMAGE + BalanceMobProps.mobProps.VAMPIRE_ATTACK_DAMAGE_PL * l);
    }

    public static class IMob extends BasicVampireEntity implements net.minecraft.world.entity.monster.Enemy {

        public IMob(EntityType<? extends BasicVampireEntity> type, Level world) {
            super(type, world);
        }

    }
}
