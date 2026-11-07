package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.faction.api.factions.village.VillageBanner;
import de.teamlapen.faction.common.core.FactionDataComponents;
import de.teamlapen.faction.common.world.effects.FactionBadOmenMobEffect;
import de.teamlapen.vampirism.api.world.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.common.core.ModAttributes;
import de.teamlapen.vampirism.common.core.ModDebugSubscriptions;
import de.teamlapen.vampirism.common.core.ModFactions;
import de.teamlapen.vampirism.common.tags.ModStructureTags;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModHunterVariants;
import de.teamlapen.vampirism.common.core.ModItems;
import de.teamlapen.vampirism.common.world.entity.ai.navigation.HunterPathNavigation;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterGroupData;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquadDebugInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.debug.DebugValueSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

public class Hunter extends PathfinderMob implements CrossbowAttackMob {

    private static final EntityDataAccessor<String> DATA_CLASS_TYPE_ID = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Holder<IHunterVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.HUNTER_VARIANT.get());
    private static final EntityDataAccessor<Integer> DATA_FACTION_LEVEL_ID = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Holder<Item>> DATA_QUARREL_TYPE_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.ITEM_HOLDER.get());

    public static final HunterClassType DEFAULT_CLASS_TYPE = HunterClassType.MELEE;
    public static final Holder<IHunterVariant> DEFAULT_VARIANT = ModHunterVariants.HUNTER_5_SLIM;
    public static final int DEFAULT_FACTION_LEVEL = 1;
    public static final boolean DEFAULT_IS_CHARGING_CROSSBOW = false;
    public static final Holder<Item> DEFAULT_QUARREL_TYPE = ModItems.QUARREL_NORMAL;

    public static final String TAG_CLASS_TYPE = "HunterClassType";
    public static final String TAG_VARIANT = "Variant";
    public static final String TAG_FACTION_LEVEL = "FactionLevel";
    public static final String TAG_SHEATHED_WEAPONS = "SheathedWeapons";
    public static final String TAG_QUARREL_TYPE = "QuarrelType";
    public static final String TAG_SQUAD_ID = "SquadId";
    public static final String TAG_POST_RADIUS = "PostRadius";
    public static final String TAG_STASHED_HELMET = "StashedHelmet";
    public static final String TAG_OMINOUS_BANNER = "OminousBanner";
    public static final String TAG_BANNER_COOLDOWN = "BannerCooldownUntil";

    private static final int MIN_LEVEL = 1;
    private static final int MAX_LEVEL = 14;
    public static final float ARROW_VELOCITY = 2.0F;

    public static final float WALK_SPEED = 1.0F;
    public static final float RUN_SPEED = 2.0F;
    private static final double PLAYER_SPRINT_FACTOR = 1.3D;
    private static final double GROUND_DRAG = 1.0D - 0.6D * 0.91D;

    private static final int REACH_CHECK_TTL = 40;
    private static final int REACH_RANGE = 2;
    private static final int MAX_REACH_CHECKS = 16;

    private static final float AIM_FOCUS_PER_SHOT = 0.1F;
    private static final int MAX_AIM_FOCUS_SHOTS = 4;

    private static final int CAMP_POST_RADIUS = 12;
    private static final int CAMPFIRE_SEARCH_RADIUS = 10;
    private static final int CAMPFIRE_SEARCH_HEIGHT = 4;
    private static final int OUTPOST_MIN_POST_RADIUS = 12;
    private static final int OUTPOST_MAX_POST_RADIUS = 24;

    private static final double BANNER_PICKUP_REACH = 1.0D;

    private final NonNullList<ItemStack> sheathedWeapons = NonNullList.withSize(2, ItemStack.EMPTY);
    private ItemStack stashedHelmet = ItemStack.EMPTY;
    private boolean ominousBanner;
    private long bannerCooldownUntil;
    private final Map<Integer, ReachCheck> reachChecks = new HashMap<>();
    private int postRadius;
    private boolean needsPostCheck;
    private int focusTargetId = -1;
    private int focusShots;
    private @Nullable HunterSquad squad;
    private @Nullable UUID squadId;

    public Hunter(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.moveControl = new HunterMoveControl(this);
    }

    public static boolean checkHunterSpawnRules(EntityType<Hunter> entityType, LevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random);
    }

    public static AttributeSupplier.Builder getAttributeBuilder() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 32.0)
                .add(Attributes.MOVEMENT_SPEED, 0.1F)
                .add(ModAttributes.SPRINT_SPEED, 1.0D)
                .add(NeoForgeMod.SWIM_SPEED, 2.5F)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(ModAttributes.ACCURACY, 1.0D);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<Hunter> getBrain() {
        return (Brain<Hunter>) super.getBrain();
    }

    @Override
    protected Brain<Hunter> makeBrain(Brain.Packed packedBrain) {
        return HunterAi.brainProvider().makeBrain(this, packedBrain);
    }

    @Override
    public @Nullable LivingEntity getTarget() {
        return this.getTargetFromBrain();
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.needsPostCheck) {
            resolveOutpostPost(level);
        }

        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("hunterSquad");
        HunterSquad.tickMember(level, this);
        profilerFiller.popPush("hunterBrain");
        this.getBrain().tick(level, this);
        profilerFiller.pop();
        HunterAi.updateActivity(this);

        handleNaturalRegeneration(level);

        super.customServerAiStep(level);
    }

    @Override
    public void registerDebugValues(ServerLevel level, DebugValueSource.Registration registration) {
        super.registerDebugValues(level, registration);
        registration.register(ModDebugSubscriptions.HUNTER_SQUADS.get(), () -> HunterSquadDebugInfo.of(this));
    }

    public double calculateVelocityFactor(double gait) {
        if (gait <= WALK_SPEED) return gait;
        double run = PLAYER_SPRINT_FACTOR * getAttributeValue(ModAttributes.SPRINT_SPEED);
        return Mth.lerp(Math.min(gait - WALK_SPEED, 1.0D), 1.0D, run);
    }

    // Mob ground velocity scales with the square of its speed, since setSpeed also sets the forward input
    public double calculateSpeedModifier(double gait) {
        double speed = getAttributeValue(Attributes.MOVEMENT_SPEED);
        return speed <= 0 ? 0 : Math.sqrt(calculateVelocityFactor(gait) / speed);
    }

    public double calculateRunVelocity() {
        return INPUT_FRICTION * getAttributeValue(Attributes.MOVEMENT_SPEED) * calculateVelocityFactor(RUN_SPEED) / GROUND_DRAG;
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new HunterPathNavigation(this, level);
    }

    @Override
    public int getMaxFallDistance() {
        return getComfortableFallDistance(0.0F);
    }

    public boolean canReach(LivingEntity target) {
        long now = level().getGameTime();
        ReachCheck check = this.reachChecks.get(target.getId());
        if (check != null && now - check.time() < REACH_CHECK_TTL) return check.reachable();

        if (this.reachChecks.size() >= MAX_REACH_CHECKS) {
            this.reachChecks.values().removeIf(old -> now - old.time() >= REACH_CHECK_TTL);
        }
        boolean reachable = this.getNavigation() instanceof HunterPathNavigation navigation && navigation.canReach(target, REACH_RANGE);
        this.reachChecks.put(target.getId(), new ReachCheck(now, reachable));
        return reachable;
    }

    private record ReachCheck(long time, boolean reachable) {
    }

    public boolean isNearGround() {
        return Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).map(direction -> level().getBlockState(blockPosition().below().relative(direction))).anyMatch(state -> !(state.is(Blocks.WATER)));
    }

    public boolean isShallowWater() {
        return isShallowWater(level(), blockPosition());
    }

    public static boolean isShallowWater(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.above().mutable();
        while (level.isEmptyBlock(cursor) && level.isInsideBuildHeight(cursor.getY())) {
            cursor.move(Direction.DOWN);
        }

        if (!level.getBlockState(cursor).getFluidState().is(FluidTags.WATER)) {
            return false;
        }

        BlockPos below = cursor.below();
        if (!level.getBlockState(below).isSolid()) {
            return false;
        }

        BlockPos aboveWater = cursor.above();
        return level.isEmptyBlock(aboveWater);
    }

    public boolean shouldTryExitWater() {
        return level().getBlockState(blockPosition().relative(getDirection())).isSolid();
    }

    public void setHunterClass(HunterClassType hunterClass) {
        this.entityData.set(DATA_CLASS_TYPE_ID, hunterClass.getSerializedName());
    }

    public HunterClassType getHunterClass() {
        return HunterClassType.get(this.entityData.get(DATA_CLASS_TYPE_ID));
    }

    public void setVariant(Holder<IHunterVariant> variant) {
        this.entityData.set(DATA_VARIANT_ID, variant);
    }

    public Holder<IHunterVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    public void setFactionLevel(int level) {
        this.entityData.set(DATA_FACTION_LEVEL_ID, level);
    }

    public int getFactionLevel() {
        return this.entityData.get(DATA_FACTION_LEVEL_ID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLASS_TYPE_ID, DEFAULT_CLASS_TYPE.getSerializedName());
        builder.define(DATA_VARIANT_ID, DEFAULT_VARIANT);
        builder.define(DATA_FACTION_LEVEL_ID, DEFAULT_FACTION_LEVEL);
        builder.define(DATA_IS_CHARGING_CROSSBOW, DEFAULT_IS_CHARGING_CROSSBOW);
        builder.define(DATA_QUARREL_TYPE_ID, DEFAULT_QUARREL_TYPE);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString(TAG_CLASS_TYPE, getHunterClass().getSerializedName());
        output.store(TAG_VARIANT, HunterVariant.CODEC, getVariant());
        output.putInt(TAG_FACTION_LEVEL, getFactionLevel());
        output.store(TAG_SHEATHED_WEAPONS, ItemStack.OPTIONAL_CODEC.listOf(), this.sheathedWeapons);
        output.store(TAG_QUARREL_TYPE, Item.CODEC, this.entityData.get(DATA_QUARREL_TYPE_ID));
        output.storeNullable(TAG_SQUAD_ID, UUIDUtil.CODEC, this.squadId);
        output.putInt(TAG_POST_RADIUS, this.postRadius);
        output.store(TAG_STASHED_HELMET, ItemStack.OPTIONAL_CODEC, this.stashedHelmet);
        output.putBoolean(TAG_OMINOUS_BANNER, this.ominousBanner);
        output.putLong(TAG_BANNER_COOLDOWN, this.bannerCooldownUntil);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.reevaluateHunterClass();

        input.getString(TAG_CLASS_TYPE).map(HunterClassType::get).ifPresent(this::setHunterClass);
        input.read(TAG_VARIANT, HunterVariant.CODEC).ifPresent(this::setVariant);
        input.getInt(TAG_FACTION_LEVEL).ifPresent(this::setFactionLevel);

        Collections.fill(this.sheathedWeapons, ItemStack.EMPTY);
        input.read(TAG_SHEATHED_WEAPONS, ItemStack.OPTIONAL_CODEC.listOf()).ifPresent(weapons -> {
            for (int i = 0; i < Math.min(weapons.size(), this.sheathedWeapons.size()); i++) {
                this.sheathedWeapons.set(i, weapons.get(i));
            }
        });

        input.read(TAG_QUARREL_TYPE, Item.CODEC).ifPresent(quarrel -> this.entityData.set(DATA_QUARREL_TYPE_ID, quarrel));
        this.squadId = input.read(TAG_SQUAD_ID, UUIDUtil.CODEC).orElse(null);
        this.postRadius = input.getIntOr(TAG_POST_RADIUS, 0);
        this.stashedHelmet = input.read(TAG_STASHED_HELMET, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY);
        this.ominousBanner = input.getBooleanOr(TAG_OMINOUS_BANNER, false);
        this.bannerCooldownUntil = input.getLongOr(TAG_BANNER_COOLDOWN, 0L);
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        HunterGroupData group = spawnGroupData instanceof HunterGroupData data && data.hasRoom() ? data : new HunterGroupData();

        setHunterClass(group.nextClass(random));
        this.squadId = group.squadId();
        setVariant(HunterVariant.getRandomVariant(DEFAULT_VARIANT, random));
        assignRandomFactionLevel(level, random);

        randomizeAttributes(difficulty, random);
        HunterEquipmentAssigner.assignRandomEquipment(this, level, random);

        HunterAi.initMemories(this);
        this.needsPostCheck = true;

        super.finalizeSpawn(level, difficulty, spawnReason, group);
        return group;
    }

    public @Nullable HunterSquad getSquad() {
        return this.squad;
    }

    public @Nullable UUID getSquadId() {
        return this.squadId;
    }

    public void setSquad(HunterSquad squad) {
        this.squad = squad;
        this.squadId = squad.id();
    }

    public void makeCampHunter(BlockPos tentPos) {
        BlockPos campfire = BlockPos.findClosestMatch(tentPos, CAMPFIRE_SEARCH_RADIUS, CAMPFIRE_SEARCH_HEIGHT, pos -> level().getBlockState(pos).is(BlockTags.CAMPFIRES)).orElse(tentPos);
        assignPost(campfire, CAMP_POST_RADIUS);
    }

    public void assignPost(BlockPos center, int radius) {
        this.getBrain().setMemory(MemoryModuleType.HOME, GlobalPos.of(this.level().dimension(), center));
        this.postRadius = radius;
        this.needsPostCheck = false;
    }

    public @Nullable HunterPost getPost() {
        if (this.postRadius <= 0) return null;
        return this.getBrain().getMemory(MemoryModuleType.HOME).map(home -> new HunterPost(home.pos(), this.postRadius)).orElse(null);
    }

    public int getPatrolRadius(int fallback) {
        return this.postRadius > 0 ? this.postRadius : fallback;
    }

    private void resolveOutpostPost(ServerLevel level) {
        this.needsPostCheck = false;
        StructureStart outpost = level.structureManager().getStructureWithPieceAt(blockPosition(), ModStructureTags.HUNTER_OUTPOST);
        if (!outpost.isValid()) return;

        BoundingBox box = outpost.getBoundingBox();
        int radius = Mth.clamp(Math.max(box.getXSpan(), box.getZSpan()) / 2, OUTPOST_MIN_POST_RADIUS, OUTPOST_MAX_POST_RADIUS);
        BlockPos center = box.getCenter();
        assignPost(new BlockPos(center.getX(), getBlockY(), center.getZ()), radius);
    }

    private void assignRandomFactionLevel(ServerLevelAccessor level, RandomSource random) {
        float difficultyFactor = switch (level.getDifficulty()) {
            case PEACEFUL -> 2.25f;
            case EASY -> 1.75f;
            case NORMAL -> 1.0f;
            case HARD -> 0.5f;
        };

        float randomFactor = random.nextFloat();
        float difficultyModifier = (float) Math.pow(randomFactor, difficultyFactor);

        int levelValue = MIN_LEVEL + Math.round(difficultyModifier * (MAX_LEVEL - MIN_LEVEL));
        setFactionLevel(Mth.clamp(levelValue, MIN_LEVEL, MAX_LEVEL));
    }

    private void randomizeAttributes(DifficultyInstance difficulty, RandomSource random) {
        int factionLevel = getFactionLevel();
        float levelFactor = (factionLevel - 1.0f) / MAX_LEVEL;

        float followRangeFactor = gaussianFactor(random, 1.0f + levelFactor * 0.4f, 0.2f);
        float sprintFactor = gaussianFactor(random, 1.03f + levelFactor * 0.3f, 0.05f);
        float damageFactor = gaussianFactor(random, 1.0f + levelFactor * 0.85f, 0.2f);
        float healthFactor = gaussianFactor(random, 1.0f + levelFactor * 0.75f, 0.2f);

        if (isRangedClass()) {
            followRangeFactor *= gaussianFactor(random, 1.15f, 0.1f);
        }

        multiplyAttributeIfPresent(Attributes.FOLLOW_RANGE, followRangeFactor, 0);
        multiplyAttributeIfPresent(ModAttributes.SPRINT_SPEED, sprintFactor);
        multiplyAttributeIfPresent(Attributes.ATTACK_DAMAGE, damageFactor);
        multiplyAttributeIfPresent(Attributes.MAX_HEALTH, healthFactor, 0);

        float accuracy = calculateBaseAccuracy(difficulty, factionLevel, random);
        multiplyAttributeIfPresent(ModAttributes.ACCURACY, gaussianFactor(random, accuracy, 0.05f));

        this.setHealth(this.getMaxHealth());
    }

    private float gaussianFactor(RandomSource random, float mean, float deviation) {
        float gaussian = (random.nextFloat() + random.nextFloat() + random.nextFloat()) / 3.0f;
        gaussian = (gaussian - 0.5f) * 2.0f;
        return Math.max(0.1f, mean + gaussian * deviation);
    }

    private float calculateBaseAccuracy(DifficultyInstance difficulty, int factionLevel, RandomSource random) {
        float difficultyFactor = switch (difficulty.getDifficulty()) {
            case PEACEFUL -> 0.75f;
            case EASY -> 0.9f;
            case NORMAL -> 1.0f;
            case HARD -> 1.3f;
        };

        float levelFactor = 0.6f + (float) Math.pow(factionLevel / 14f, 0.8f) * 1.4f;
        float variance = 0.9f + random.nextFloat() * 0.2f;

        return levelFactor * difficultyFactor * variance;
    }

    private void multiplyAttributeIfPresent(Holder<Attribute> attribute, double multiplier) {
        multiplyAttributeIfPresent(attribute, multiplier, 2);
    }

    private void multiplyAttributeIfPresent(Holder<Attribute> attribute, double multiplier, int decimalPlaces) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            double scale = Math.pow(10, decimalPlaces);
            double newValue = Math.round(instance.getBaseValue() * multiplier * scale) / scale;
            instance.setBaseValue(newValue);
        }
    }

    public int calculateTokens(RandomSource random, int min, int max) {
        float growthPower = 1.1f;

        float progressFactor = (float) Math.pow(getFactionLevel() / 14.0f, growthPower);
        progressFactor = Mth.clamp(progressFactor, 0.0f, 1.0f);

        float base = min + (max - min) * progressFactor;

        float variance = 0.75f + random.nextFloat() * 0.5f;
        float result = base * variance;

        return Mth.clamp(Math.round(result), min, max);
    }

    // TODO: FactionRestriction only works for humans and thence npc hunters may not use all of their weapons' potential

    public void unsheatheWeapons() {
        ItemStack main = this.sheathedWeapons.get(0);
        if (!main.isEmpty()) {
            this.setItemInHand(InteractionHand.MAIN_HAND, main.copy());
            setSheathedWeapon(ItemStack.EMPTY, 0);
        }

        ItemStack off = this.sheathedWeapons.get(1);
        if (!off.isEmpty()) {
            this.setItemInHand(InteractionHand.OFF_HAND, off.copy());
            setSheathedWeapon(ItemStack.EMPTY, 1);
        }
    }

    public void sheatheWeapons() {
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();

        if (!main.isEmpty()) setSheathedWeapon(main.copy(), 0);
        if (!off.isEmpty()) setSheathedWeapon(off.copy(), 1);

        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    public void setSheathedWeapon(ItemStack item, int index) {
        this.sheathedWeapons.set(index, item);
        if (!this.level().isClientSide()) {
            reevaluateHunterClass();
        }
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        boolean wasHurt = super.hurtServer(level, damageSource, amount);
        if (wasHurt && damageSource.getEntity() instanceof LivingEntity entity) {
            HunterAi.wasHurtBy(level, this, entity);
        }

        return wasHurt;
    }

    @Override
    public void die(DamageSource source) {
        boolean ominous = !this.dead && hasOminousBanner();
        super.die(source);
        if (ominous && this.dead && level() instanceof ServerLevel serverLevel) {
            FactionBadOmenMobEffect.grantBadOmen(source.getEntity(), ModFactions.HUNTER);
            if (this.squad != null) this.squad.startBannerCooldown(serverLevel.getGameTime());
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        if (hasBanner()) {
            ItemEntity banner = spawnAtLocation(level, removeBanner());
            if (banner != null && this.squad != null) this.squad.onBannerDropped(banner);
        }
        super.dropCustomDeathLoot(level, source, killedByPlayer);
    }

    public static ItemStack createSquadBanner(ServerLevel level) {
        VillageBanner banner = ModFactions.HUNTER.components().get(FactionDataComponents.VILLAGE_BANNER);
        return banner != null ? banner.createBanner(level.registryAccess()) : ItemStack.EMPTY;
    }

    public static boolean isBanner(ItemStack stack) {
        return stack.has(FactionDataComponents.IS_FACTION_BANNER);
    }

    public boolean hasBanner() {
        return isBanner(getItemBySlot(EquipmentSlot.HEAD));
    }

    public boolean hasOminousBanner() {
        return this.ominousBanner && hasBanner();
    }

    public void makeBannerOminous() {
        if (hasBanner()) this.ominousBanner = true;
    }

    public void equipBanner(ItemStack banner, boolean ominous) {
        if (banner.isEmpty()) return;
        if (!hasBanner()) this.stashedHelmet = getItemBySlot(EquipmentSlot.HEAD).copy();
        setItemSlot(EquipmentSlot.HEAD, banner);
        this.ominousBanner = ominous;
    }

    public ItemStack removeBanner() {
        if (!hasBanner()) return ItemStack.EMPTY;
        ItemStack banner = getItemBySlot(EquipmentSlot.HEAD);
        setItemSlot(EquipmentSlot.HEAD, this.stashedHelmet);
        this.stashedHelmet = ItemStack.EMPTY;
        this.ominousBanner = false;
        return banner;
    }

    public boolean isWithinPickupReach(ItemEntity item) {
        return getBoundingBox().inflate(BANNER_PICKUP_REACH).intersects(item.getBoundingBox());
    }

    public void pickUpBanner(ItemEntity item) {
        ItemStack stack = item.getItem();
        if (!isBanner(stack) || hasBanner()) return;

        take(item, 1);
        ItemStack banner = stack.split(1);
        if (stack.isEmpty()) {
            item.discard();
        } else {
            item.setItem(stack);
        }
        equipBanner(banner, false);
    }

    public long getBannerCooldownUntil() {
        return this.bannerCooldownUntil;
    }

    public void setBannerCooldownUntil(long gameTime) {
        this.bannerCooldownUntil = gameTime;
    }

    @Override
    public void setChargingCrossbow(boolean chargingCrossbow) {
        this.entityData.set(DATA_IS_CHARGING_CROSSBOW, chargingCrossbow);
    }

    @Override
    public void onCrossbowAttackPerformed() {
        this.noActionTime = 0;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        this.performCrossbowAttack(this, ARROW_VELOCITY);
        recordShotAt(target);
    }

    public float getAimFocus(LivingEntity target) {
        if (target.getId() != this.focusTargetId) return 1.0F;
        return 1.0F - AIM_FOCUS_PER_SHOT * Math.min(this.focusShots, MAX_AIM_FOCUS_SHOTS);
    }

    private void recordShotAt(LivingEntity target) {
        if (target.getId() != this.focusTargetId) {
            this.focusTargetId = target.getId();
            this.focusShots = 0;
        }
        this.focusShots++;
    }

    @Override
    public ItemStack getProjectile(ItemStack weaponStack) {
        if (weaponStack.getItem() instanceof CrossbowItem) {
            return CommonHooks.getProjectile(this, weaponStack, getQuarrelType().getDefaultInstance());
        }

        return super.getProjectile(weaponStack);
    }

    @Override
    public boolean canUseNonMeleeWeapon(ItemStack item) {
        return item.getItem() instanceof CrossbowItem;
    }

    public void setQuarrelType(Item quarrel) {
        this.entityData.set(DATA_QUARREL_TYPE_ID, quarrel.builtInRegistryHolder());
    }

    public Item getQuarrelType() {
        return this.entityData.get(DATA_QUARREL_TYPE_ID).value();
    }

    private void handleNaturalRegeneration(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        if (isFighting()) return;
        if (!this.isAlive() || this.getMaxHealth() == this.getHealth()) return;
        if (this.hasEffect(MobEffects.HUNGER)) return;

        int regenDelay = switch (level.getDifficulty()) {
            case EASY -> 300;
            case HARD -> 80;
            default -> 180;
        };

        if (this.tickCount % regenDelay == 0) {
            this.heal(1.0F);
        }
    }

    public void reevaluateHunterClass() {
        boolean hasRangedWeapon = isRangedHunterWeapon(this.getMainHandItem()) || isRangedHunterWeapon(this.getOffhandItem()) || this.sheathedWeapons.stream().anyMatch(this::isRangedHunterWeapon);

        HunterClassType currentClass = getHunterClass();
        HunterClassType evaluatedClass = hasRangedWeapon ? HunterClassType.RANGED : HunterClassType.MELEE;

        if (currentClass != evaluatedClass) {
            setHunterClass(evaluatedClass);
            HunterAi.updateActivity(this);
        }
    }

    private boolean isRangedHunterWeapon(ItemStack stack) {
        return stack.getItem() instanceof CrossbowItem;
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack, boolean insideTransaction) {
        super.setItemSlot(slot, stack, insideTransaction);
        if (!this.level().isClientSide()) {
            reevaluateHunterClass();
        }
    }

    public boolean isFighting() {
        return this.getBrain().isActive(Activity.FIGHT);
    }

    public boolean isRetreating() {
        return this.getBrain().isActive(Activity.AVOID);
    }

    public boolean isMeleeClass() {
        return getHunterClass() == HunterClassType.MELEE;
    }

    public boolean isRangedClass() {
        return getHunterClass() == HunterClassType.RANGED;
    }
}
