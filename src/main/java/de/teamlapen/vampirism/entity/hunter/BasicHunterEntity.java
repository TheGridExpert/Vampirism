package de.teamlapen.vampirism.entity.hunter;

import de.teamlapen.lib.lib.util.UtilLib;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.difficulty.Difficulty;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.hunter.IBasicHunter;
import de.teamlapen.vampirism.api.entity.hunter.IHunterMob;
import de.teamlapen.vampirism.api.entity.hunter.IVampirismCrossbowUser;
import de.teamlapen.vampirism.api.items.IHunterCrossbow;
import de.teamlapen.vampirism.api.world.ICaptureAttributes;
import de.teamlapen.vampirism.config.BalanceMobProps;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.effects.BadOmenEffect;
import de.teamlapen.vampirism.entity.VampirismEntity;
import de.teamlapen.vampirism.entity.ai.goals.*;
import de.teamlapen.vampirism.entity.ai.navigation.HunterPathNavigation;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.minion.HunterMinionEntity;
import de.teamlapen.vampirism.entity.minion.management.MinionTasks;
import de.teamlapen.vampirism.entity.minion.management.PlayerMinionController;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.hunter.HunterLeveling;
import de.teamlapen.vampirism.entity.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.entity.vampire.VampireBaseEntity;
import de.teamlapen.vampirism.inventory.HunterBasicMenu;
import de.teamlapen.vampirism.util.HunterVillage;
import de.teamlapen.vampirism.world.MinionWorldData;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.PatrollingMonster;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Optional;

/**
 * Exists in {@link BasicHunterEntity#MAX_LEVEL}+1 different levels
 */
public class BasicHunterEntity extends HunterBaseEntity implements IBasicHunter, ForceLookEntityGoal.TaskOwner, IVampirismCrossbowUser {
    private static final EntityDataAccessor<Integer> LEVEL = SynchedEntityData.defineId(BasicHunterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> WATCHED_ID = SynchedEntityData.defineId(BasicHunterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> TYPE = SynchedEntityData.defineId(BasicHunterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(BasicHunterEntity.class, EntityDataSerializers.BOOLEAN);


    private static final Logger LOGGER = LogManager.getLogger();

    private static final Component name = Component.translatable("container.hunter");

    public static AttributeSupplier.@NotNull Builder getAttributeBuilder() {
        return VampirismEntity.getAttributeBuilder()
                .add(Attributes.MAX_HEALTH, BalanceMobProps.mobProps.VAMPIRE_HUNTER_MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, BalanceMobProps.mobProps.VAMPIRE_HUNTER_ATTACK_DAMAGE)
                .add(Attributes.MOVEMENT_SPEED, BalanceMobProps.mobProps.VAMPIRE_HUNTER_SPEED);
    }


    private final int MAX_LEVEL = 3;
    /**
     * Player currently being trained otherwise null
     */
    @Nullable
    private Player trainee;
    @Nullable
    private ICaptureAttributes villageAttributes;
    //Village capture --------------------------------------------------------------------------------------------------
    private boolean attack;

    public BasicHunterEntity(EntityType<? extends BasicHunterEntity> type, Level world) {
        super(type, world, true);
        saveHome = true;
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);

        this.setDontDropEquipment();

        this.enableImobConversion();
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag nbt) {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("level", getEntityLevel());
        nbt.putBoolean("crossbow", isHoldingCrossbow());
        nbt.putBoolean("attack", attack);
        nbt.putInt("type", getEntityTextureType());
    }

    @Override
    public void attackVillage(ICaptureAttributes attributes) {
        this.villageAttributes = attributes;
        this.attack = true;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (trainee != null && !(trainee.containerMenu instanceof HunterBasicMenu)) {
            this.trainee = null;
        }
        if (!level().isClientSide) {
            LivingEntity target = getTarget();
            int id = target == null ? 0 : target.getId();
            this.updateWatchedId(id);
            if (this.tickCount % 512 == 0 && this.getRandom().nextInt(500) == 0) { //Very very very randomly decide to walk to a random location
                BlockPos randomDestination = new BlockPos(this.getRandom().nextInt(30000) - 15000, 100, this.getRandom().nextInt(30000) - 15000);
                randomDestination = this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, randomDestination);
                this.setHomeArea(randomDestination, 10);
            }
        }
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
                        boolean hasIncreasedStats = fph.getSkillHandler().map(s -> s.isSkillEnabled(HunterSkills.MINION_STATS_INCREASE)).orElse(false);
                        HunterMinionEntity.HunterMinionData data = new HunterMinionEntity.HunterMinionData("Minion", this.getEntityTextureType(), this.getEntityTextureType() % 4, false, hasIncreasedStats);
                        data.updateEntityCaps(this.serializeAttachments(lord.registryAccess()));
                        int id = controller.createNewMinionSlot(data, ModEntities.HUNTER_MINION.get());
                        if (id < 0) {
                            LOGGER.error("Failed to get minion slot");
                            return;
                        }
                        HunterMinionEntity minion = ModEntities.HUNTER_MINION.get().create(this.level(), EntitySpawnReason.CONVERSION);
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

    @Override
    public void defendVillage(ICaptureAttributes attributes) {
        this.villageAttributes = attributes;
        this.attack = false;
    }

    @Override
    public ICaptureAttributes getCaptureInfo() {
        return this.villageAttributes;
    }

    @Override
    public void die(@NotNull DamageSource cause) {
        if (this.villageAttributes == null) {
            BadOmenEffect.handlePotentialBannerKill(cause.getEntity(), this);
        }
        super.die(cause);
    }

    @NotNull
    @Override
    public Optional<Player> getForceLookTarget() {
        return Optional.ofNullable(trainee);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, @NotNull Entity entity) {
        boolean flag = super.doHurtTarget(level, entity);
        if (flag && this.getMainHandItem().isEmpty()) {
            this.swing(InteractionHand.MAIN_HAND);  //Swing stake if nothing else is held
        }
        return flag;
    }

    @Nullable
    @Override
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor worldIn, @NotNull DifficultyInstance difficultyIn, @NotNull EntitySpawnReason reason, @Nullable SpawnGroupData spawnDataIn) {
        if (!(reason == EntitySpawnReason.SPAWN_ITEM_USE || reason == EntitySpawnReason.BUCKET || reason == EntitySpawnReason.CONVERSION || reason == EntitySpawnReason.COMMAND) && this.getRandom().nextInt(50) == 0) {
            this.setItemSlot(EquipmentSlot.HEAD, HunterVillage.createBanner(worldIn.registryAccess()));
        }
        getEntityData().set(TYPE, this.getRandom().nextInt(TYPES));
        randomEquipments();

        return super.finalizeSpawn(worldIn, difficultyIn, reason, spawnDataIn);
    }

    protected void randomEquipments() {
        HatType[] hatTypes = HatType.values();
        HatType hat = hatTypes[this.getRandom().nextInt(hatTypes.length)];
        this.setItemSlot(EquipmentSlot.HEAD, hat.getHeadItem());

        EquipmentType equipment = switch (random.nextInt(4)) {
            case 1 -> EquipmentType.STAKE;
            case 2 -> EquipmentType.AXE;
            case 3 -> EquipmentType.CROSSBOW;
            default -> EquipmentType.NONE;
        };
        this.setItemSlot(EquipmentSlot.MAINHAND, equipment.getMainHand());
        this.setItemSlot(EquipmentSlot.OFFHAND, equipment.getOffHand());
        this.setDontDropEquipment();
    }

    @Override
    public int getMaxEntityLevel() {
        return MAX_LEVEL;
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
    public int getEntityTextureType() {
        return Math.max(0, getEntityData().get(TYPE));
    }

    @Override
    public boolean isDefendingVillage() {
        return villageAttributes != null && !attack;
    }

    @Override
    public boolean isLookingForHome() {
        return getHome() == null;
    }

    @Override
    public int getEntityLevel() {
        return getEntityData().get(LEVEL);
    }

    @Override
    public void setEntityLevel(int level) {
        if (level >= 0) {
            getEntityData().set(LEVEL, level);
            this.updateEntityAttributes();
            if (level == 3) {
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 1000000, 1, false, false));
            }
        }
    }

    @Override
    public void makeNormalHunter() {
        super.setHome(null);
        this.disableMoveTowardsRestriction();
    }

    @Override
    public void makeVillageHunter(AABB box) {
        super.setHome(box);
        this.setMoveTowardsRestriction(MOVE_TO_RESTRICT_PRIO, true);
    }

    //Entityactions ----------------------------------------------------------------------------------------------------


    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tagCompund) {
        super.readAdditionalSaveData(tagCompund);
        if (tagCompund.contains("level")) {
            setEntityLevel(tagCompund.getInt("level"));
        }

        if (tagCompund.contains("crossbow") && tagCompund.getBoolean("crossbow")) {
            this.setLeftHanded(true);
            this.setItemSlot(EquipmentSlot.MAINHAND, ModItems.BASIC_CROSSBOW.toStack());
        } else {
            this.setLeftHanded(false);
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
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
    public void stopVillageAttackDefense() {
        this.setCustomName(null);
        this.villageAttributes = null;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return super.removeWhenFarAway(distanceToClosestPlayer) && getHome() != null;
    }

    @Override
    public int suggestEntityLevel(@NotNull Difficulty d) {
        return switch (this.random.nextInt(6)) {
            case 0 -> (int) (d.minPercLevel() / 100F * MAX_LEVEL);
            case 1 -> (int) (d.avgPercLevel() / 100F * MAX_LEVEL);
            case 2 -> (int) (d.maxPercLevel() / 100F * MAX_LEVEL);
            default -> this.random.nextInt(MAX_LEVEL + 1);
        };

    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(LEVEL, -1);
        builder.define(WATCHED_ID, 0);
        builder.define(TYPE, -1);
        builder.define(IS_CHARGING_CROSSBOW, false);
    }

    @Override
    protected int getBaseExperienceReward(ServerLevel level) {
        this.xpReward = 6 + getEntityLevel();
        return super.getBaseExperienceReward(level);
    }

    //IMob -------------------------------------------------------------------------------------------------------------
    @Override
    protected @NotNull EntityType<?> getIMobTypeOpt(boolean iMob) {
        return iMob ? ModEntities.HUNTER_IMOB.get() : ModEntities.HUNTER.get();
    }

    @NotNull
    @Override
    protected InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) { //processInteract
        if (hand == InteractionHand.MAIN_HAND && tryCureSanguinare(player)) return InteractionResult.SUCCESS;
        int hunterLevel = VampirismPlayerAttributes.get(player).hunterLevel;
        if (this.isAlive() && !player.isShiftKeyDown() && hand == InteractionHand.MAIN_HAND) {
            if (!level().isClientSide) {
                if (HunterLeveling.getBasicHunterRequirement(hunterLevel + 1).isPresent()) {
                    if (trainee == null) {
                        player.openMenu(new SimpleMenuProvider((id, playerInventory, playerEntity) -> new HunterBasicMenu(id, playerInventory, this), name));
                        trainee = player;
                        this.getNavigation().stop();
                    } else {
                        player.displayClientMessage(Component.translatable("text.vampirism.i_am_busy_right_now"), true);
                    }
                    return InteractionResult.SUCCESS;
                } else if (hunterLevel > 0) {
                    FactionPlayerHandler fph = FactionPlayerHandler.get(player);
                    if (fph.getMaxMinions() > 0) {
                        ItemStack heldItem = player.getItemInHand(hand);

                        if (this.getEntityLevel() > 0) {
                            if (heldItem.getItem() == ModItems.HUNTER_MINION_EQUIPMENT.get()) {
                                player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.minion.unavailable"), true);
                            }
                        } else {
                            boolean freeSlot = MinionWorldData.getData(player.level()).map(data -> data.getOrCreateController(fph)).map(PlayerMinionController::hasFreeMinionSlot).orElse(false);
                            player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.minion.available"), false);
                            if (heldItem.getItem() == ModItems.HUNTER_MINION_EQUIPMENT.get()) {
                                if (!freeSlot) {
                                    player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.minion.no_free_slot"), false);
                                } else {
                                    player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.minion.start_serving"), false);
                                    convertToMinion(player);
                                    if (!player.getAbilities().instabuild) heldItem.shrink(1);
                                }
                            } else if (freeSlot) {
                                player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.minion.require_equipment", Component.translatable(ModItems.HUNTER_MINION_EQUIPMENT.get().getDescriptionId())), false);
                            }
                        }
                    } else {
                        player.displayClientMessage(Component.translatable("text.vampirism.basic_hunter.cannot_train_you_any_further"), false);
                    }
                    return InteractionResult.SUCCESS;
                }

            }
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void setChargingCrossbow(boolean p_213671_1_) {
        this.getEntityData().set(IS_CHARGING_CROSSBOW, p_213671_1_);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity p_82196_1_, float p_82196_2_) {
        this.performCrossbowAttack(this, 1.6f);
    }

    @Override
    public boolean isHoldingCrossbow() {
        return this.isHolding(stack -> stack.getItem() instanceof IHunterCrossbow);
    }

    @Override
    public boolean isChargingCrossbow() {
        return this.getEntityData().get(IS_CHARGING_CROSSBOW);
    }

    @Nonnull
    @Override
    public ItemStack getProjectile(ItemStack stack) {
        if (stack.getItem() instanceof IHunterCrossbow) {
            return net.neoforged.neoforge.common.CommonHooks.getProjectile(this, stack, ModItems.CROSSBOW_ARROW_NORMAL.get().getDefaultInstance());
        }
        return super.getProjectile(stack);
    }

    @Override
    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        return new HunterPathNavigation(this, level);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(1, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(1, new OpenGateGoal(this, true));
        this.goalSelector.addGoal(3, new ForceLookEntityGoal<>(this));
        this.goalSelector.addGoal(3, new RangedHunterCrossbowAttackGoal<>(this, 0.6, 60));
        this.goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(5, new MoveThroughVillageGoal(this, 0.7F, false, 300, () -> false));
        this.goalSelector.addGoal(6, new RandomStrollGoal(this, 0.7, 50));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 13F));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, VampireBaseEntity.class, 17F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(1, new HunterHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new AttackVillageGoal<>(this));
        this.targetSelector.addGoal(2, new DefendVillageGoal<>(this));//Should automatically be mutually exclusive with  attack village
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), true, false, false, false, null)));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, PathfinderMob.class, 5, true, false, VampirismAPI.factionRegistry().getSelector(getFaction(), false, true, false, false, null)) {
            @Override
            protected double getFollowDistance() {
                return super.getFollowDistance() / 2;
            }
        });
        this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Zombie.class, true, true));
        this.targetSelector.addGoal(7, new NearestAttackableTargetGoal<>(this, PatrollingMonster.class, 5, true, true, (living, level) -> UtilLib.isInsideStructure(living, StructureTags.VILLAGE)));        //Also check the priority of tasks that are dynamically added. See top of class
    }

    protected void updateEntityAttributes() {
        int l = Math.max(getEntityLevel(), 0);
        this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(BalanceMobProps.mobProps.VAMPIRE_HUNTER_MAX_HEALTH + BalanceMobProps.mobProps.VAMPIRE_HUNTER_MAX_HEALTH_PL * l);
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(BalanceMobProps.mobProps.VAMPIRE_HUNTER_ATTACK_DAMAGE + BalanceMobProps.mobProps.VAMPIRE_HUNTER_ATTACK_DAMAGE_PL * l);
        this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(BalanceMobProps.mobProps.VAMPIRE_HUNTER_SPEED);
    }

    private int getWatchedId() {
        return getEntityData().get(WATCHED_ID);
    }

    private void updateWatchedId(int id) {
        getEntityData().set(WATCHED_ID, id);
    }

    public static class IMob extends BasicHunterEntity implements net.minecraft.world.entity.monster.Enemy {

        public IMob(EntityType<? extends BasicHunterEntity> type, Level world) {
            super(type, world);
        }

    }
}
