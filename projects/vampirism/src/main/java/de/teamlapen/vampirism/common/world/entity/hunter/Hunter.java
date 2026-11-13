package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.faction.api.factions.village.VillageBanner;
import de.teamlapen.faction.common.core.FactionDataComponents;
import de.teamlapen.faction.common.world.effects.FactionBadOmenMobEffect;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.world.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.common.core.ModAttributes;
import de.teamlapen.vampirism.common.core.ModDebugSubscriptions;
import de.teamlapen.vampirism.common.core.ModFactions;
import de.teamlapen.vampirism.common.tags.ModStructureTags;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModHunterVariants;
import de.teamlapen.vampirism.common.core.ModItems;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.ai.navigation.HunterPathNavigation;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterAi;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterJumpControl;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterMoveControl;
import de.teamlapen.vampirism.common.world.items.CrucifixItem;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterGroupData;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquadDebugInfo;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
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
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

//TODO: Review despawn rules when new advanced hunters and new camps are added
@SuppressWarnings("deprecation")
public class Hunter extends PathfinderMob implements CrossbowAttackMob {

    private static final EntityDataAccessor<String> DATA_CLASS_TYPE_ID = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Holder<IHunterVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.HUNTER_VARIANT.get());
    private static final EntityDataAccessor<Integer> DATA_FACTION_LEVEL_ID = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Holder<Item>> DATA_QUARREL_TYPE_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.ITEM_HOLDER.get());
    private static final EntityDataAccessor<ItemStack> DATA_BANNER = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.ITEM_STACK);

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
    public static final String TAG_BANNER = "Banner";
    public static final String TAG_OMINOUS_BANNER = "OminousBanner";
    public static final String TAG_BANNER_COOLDOWN = "BannerCooldownUntil";
    public static final String TAG_FISHING_INTEREST = "FishingInterest";

    private static final int MIN_LEVEL = 1;
    protected static final int MAX_LEVEL = REFERENCE.HIGHEST_HUNTER_LEVEL;
    protected static final int MAX_LORD_LEVEL = REFERENCE.HIGHEST_HUNTER_LORD;
    private static final int MAX_TOTAL_LEVEL = MAX_LEVEL + MAX_LORD_LEVEL;
    private static final float ADVANCED_CHANCE = 0.15F;
    public static final int BASE_SQUAD_SIZE = 5;
    private static final int VETERAN_LEVEL = 12;
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

    private static final int CAMP_POST_RADIUS = 48;
    private static final int CAMPFIRE_SEARCH_RADIUS = 10;
    private static final int CAMPFIRE_SEARCH_HEIGHT = 4;
    private static final int OUTPOST_POST_RADIUS = 48;
    private static final int RELAXED_MAX_FALL_DISTANCE = 1;
    private static final double LOOKOUT_HOLD_DISTANCE_SQR = 1.5D * 1.5D;
    private static final double ENGAGE_MARGIN = 4.0D;
    private static final float POST_PATH_LENGTH_FACTOR = 2.0F;

    private static final double BANNER_PICKUP_REACH = 1.0D;
    private static final int CRUCIFIX_HOLD_TICKS = 10;
    public static final int CRUCIFIX_SLOT = 2;

    private final NonNullList<ItemStack> sheathedWeapons = NonNullList.withSize(3, ItemStack.EMPTY);
    private boolean ominousBanner;
    private long bannerCooldownUntil;
    private int crucifixHoldTicks;
    private boolean offhandAttackNext;
    private boolean allowingDrops;
    private final Map<Integer, ReachCheck> reachChecks = new HashMap<>();
    private int postRadius;
    private boolean needsPostCheck;
    private int focusTargetId = -1;
    private int focusShots;
    private @Nullable HunterSquad squad;
    private @Nullable UUID squadId;
    private float fishingInterest;

    public Hunter(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.fishingInterest = this.random.nextFloat();
        this.getNavigation().setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
        this.moveControl = new HunterMoveControl(this);
        this.jumpControl = new HunterJumpControl(this);
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
    public void aiStep() {
        this.updateSwingTime();
        super.aiStep();
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

        if (this.crucifixHoldTicks > 0) {
            this.crucifixHoldTicks--;
            if (this.crucifixHoldTicks == 0) putAwayCrucifix();
        }
        handleNaturalRegeneration(level);

        super.customServerAiStep(level);
    }

    @Override
    public void registerDebugValues(ServerLevel level, DebugValueSource.Registration registration) {
        super.registerDebugValues(level, registration);
        registration.register(ModDebugSubscriptions.HUNTER_SQUADS.get(), () -> HunterSquadDebugInfo.of(this));
        registration.register(ModDebugSubscriptions.HUNTER_LOOKOUTS.get(), () -> HunterLookoutDebugInfo.of(this));
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
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public boolean isPushable() {
        return super.isPushable() && !isAtLookoutSpot();
    }

    private boolean isAtLookoutSpot() {
        return getBrain().getMemory(ModMemoryTypes.LOOKOUT_SPOT.get())
                .filter(spot -> spot.distToCenterSqr(getX(), getY(), getZ()) < LOOKOUT_HOLD_DISTANCE_SQR)
                .isPresent();
    }

    @Override
    public int getMaxFallDistance() {
        return getTarget() == null && !this.allowingDrops ? RELAXED_MAX_FALL_DISTANCE : getDropFallDistance();
    }

    public int getDropFallDistance() {
        return getComfortableFallDistance(0.0F);
    }

    public boolean isAvoidingDrops() {
        return getMaxFallDistance() == RELAXED_MAX_FALL_DISTANCE;
    }

    public void setAllowingDrops(boolean allowingDrops) {
        this.allowingDrops = allowingDrops;
    }

    public boolean canReach(LivingEntity target) {
        long now = level().getGameTime();
        ReachCheck check = this.reachChecks.get(target.getId());
        if (check != null && now - check.time() < REACH_CHECK_TTL) return check.reachable();

        if (this.reachChecks.size() >= MAX_REACH_CHECKS) {
            this.reachChecks.values().removeIf(old -> now - old.time() >= REACH_CHECK_TTL);
        }
        boolean reachable = this.getNavigation() instanceof HunterPathNavigation pathNavigation && pathNavigation.canReach(target, REACH_RANGE);
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
        builder.define(DATA_BANNER, ItemStack.EMPTY);
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
        output.store(TAG_BANNER, ItemStack.OPTIONAL_CODEC, getBanner());
        output.putBoolean(TAG_OMINOUS_BANNER, this.ominousBanner);
        output.putLong(TAG_BANNER_COOLDOWN, this.bannerCooldownUntil);
        output.putFloat(TAG_FISHING_INTEREST, this.fishingInterest);
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
        if (isCrucifix(this.sheathedWeapons.get(1)) && this.sheathedWeapons.get(CRUCIFIX_SLOT).isEmpty()) {
            this.sheathedWeapons.set(CRUCIFIX_SLOT, this.sheathedWeapons.get(1));
            this.sheathedWeapons.set(1, ItemStack.EMPTY);
        }

        input.read(TAG_QUARREL_TYPE, Item.CODEC).ifPresent(quarrel -> this.entityData.set(DATA_QUARREL_TYPE_ID, quarrel));
        this.squadId = input.read(TAG_SQUAD_ID, UUIDUtil.CODEC).orElse(null);
        this.postRadius = input.getIntOr(TAG_POST_RADIUS, 0);
        updatePostPathLength();
        this.entityData.set(DATA_BANNER, input.read(TAG_BANNER, ItemStack.OPTIONAL_CODEC).orElse(ItemStack.EMPTY));
        this.ominousBanner = input.getBooleanOr(TAG_OMINOUS_BANNER, false);
        this.bannerCooldownUntil = input.getLongOr(TAG_BANNER_COOLDOWN, 0L);
        this.fishingInterest = input.getFloatOr(TAG_FISHING_INTEREST, this.fishingInterest);
        if (isCrucifix(getMainHandItem())) this.crucifixHoldTicks = 1;
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        RandomSource random = level.getRandom();
        HunterGroupData group = HunterGroupData.resolve(spawnGroupData);
        boolean newGroup = group != spawnGroupData;

        setHunterClass(chooseClass(group, random));
        this.squadId = group.squadId();
        setVariant(HunterVariant.getRandomVariant(DEFAULT_VARIANT, random));
        assignRandomFactionLevel(level, random);

        randomizeAttributes(difficulty, random);
        HunterEquipmentAssigner.assignRandomEquipment(this, level, random);

        HunterAi.initMemories(this);
        this.needsPostCheck = true;

        super.finalizeSpawn(level, difficulty, spawnReason, group);
        if (newGroup && canSpawnAdvanced(spawnReason) && random.nextFloat() < ADVANCED_CHANCE && group.claimAdvanced()) {
            AdvancedHunter.spawnInGroup(level, difficulty, spawnReason, group, blockPosition());
        }
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
        updatePostPathLength();
    }

    private void updatePostPathLength() {
        if (this.postRadius > 0) {
            getNavigation().setRequiredPathLength(this.postRadius * POST_PATH_LENGTH_FACTOR);
        }
    }

    public @Nullable HunterPost getPost() {
        if (this.postRadius <= 0) return null;
        return this.getBrain().getMemory(MemoryModuleType.HOME).map(home -> new HunterPost(home.pos(), this.postRadius)).orElse(null);
    }

    public int getPatrolRadius(int fallback) {
        return this.postRadius > 0 ? this.postRadius : fallback;
    }

    public @Nullable PatrolZone getPatrolZone() {
        PatrolArea area = getPatrolArea();
        if (area == null || this.squad == null || !(level() instanceof ServerLevel serverLevel)) return null;
        return this.squad.getPatrolZone(serverLevel, area);
    }

    public @Nullable PatrolArea getPatrolArea() {
        Brain<Hunter> brain = getBrain();
        Optional<PatrolArea> ordered = brain.getMemory(ModMemoryTypes.SQUAD_PATROL_AREA.get());
        return ordered.orElseGet(() -> brain.getMemory(MemoryModuleType.HOME).map(home -> new PatrolArea(home.pos(), getPatrolRadius(HunterAi.PATROL_RADIUS))).orElse(null));
    }

    public boolean isWithinEngageRange(Vec3 pos) {
        PatrolArea area = getPatrolArea();
        double centerX = area != null ? area.center().getX() + 0.5D : getX();
        double centerZ = area != null ? area.center().getZ() + 0.5D : getZ();
        double range = (area != null ? area.radius() : HunterAi.PATROL_RADIUS) + ENGAGE_MARGIN;
        double dx = pos.x - centerX;
        double dz = pos.z - centerZ;
        return dx * dx + dz * dz <= range * range;
    }

    private void resolveOutpostPost(ServerLevel level) {
        this.needsPostCheck = false;
        StructureStart outpost = level.structureManager().getStructureWithPieceAt(blockPosition(), ModStructureTags.HUNTER_OUTPOST);
        if (!outpost.isValid()) return;

        BlockPos center = outpost.getBoundingBox().getCenter();
        assignPost(new BlockPos(center.getX(), getBlockY(), center.getZ()), OUTPOST_POST_RADIUS);
    }

    protected HunterClassType chooseClass(HunterGroupData group, RandomSource random) {
        return group.nextClass(random);
    }

    public boolean isDualWielding() {
        return false;
    }

    public int getMaxSquadSize() {
        return getFactionLevel() >= VETERAN_LEVEL ? BASE_SQUAD_SIZE + 1 : BASE_SQUAD_SIZE;
    }

    public InteractionHand nextAttackHand() {
        if (!isDualWielding() || getOffhandItem().isEmpty()) return InteractionHand.MAIN_HAND;
        InteractionHand hand = this.offhandAttackNext ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
        this.offhandAttackNext = !this.offhandAttackNext;
        return hand;
    }

    public static EntityType<? extends Hunter> randomType(RandomSource random) {
        return random.nextFloat() < ADVANCED_CHANCE ? ModEntities.ADVANCED_HUNTER_NEW.get() : ModEntities.NEW_HUNTER.get();
    }

    private static boolean canSpawnAdvanced(EntitySpawnReason spawnReason) {
        return spawnReason == EntitySpawnReason.NATURAL || spawnReason == EntitySpawnReason.STRUCTURE;
    }

    private void assignRandomFactionLevel(ServerLevelAccessor level, RandomSource random) {
        float difficultyFactor = switch (level.getDifficulty()) {
            case PEACEFUL -> 2.25f;
            case EASY -> 1.75f;
            case NORMAL -> 1.0f;
            case HARD -> 0.5f;
        };

        setFactionLevel(calculateFactionLevel((float) Math.pow(random.nextFloat(), difficultyFactor)));
    }

    protected int calculateFactionLevel(float difficultyModifier) {
        int levelValue = MIN_LEVEL + Math.round(difficultyModifier * (MAX_LEVEL - MIN_LEVEL));
        return Mth.clamp(levelValue, MIN_LEVEL, MAX_LEVEL);
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

        float levelFactor = 0.6f + (float) Math.pow(factionLevel / (float) MAX_LEVEL, 0.8f) * 1.4f;
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

        float progressFactor = (float) Math.pow(getFactionLevel() / (float) MAX_TOTAL_LEVEL, growthPower);
        progressFactor = Mth.clamp(progressFactor, 0.0f, 1.0f);

        float base = min + (max - min) * progressFactor;

        float variance = 0.75f + random.nextFloat() * 0.5f;
        float result = base * variance;

        return Mth.clamp(Math.round(result), min, max);
    }

    // TODO: FactionRestriction only works for humans and thence npc hunters may not use all of their weapons' potential

    public void unsheatheWeapons() {
        lowerSpyglass();
        putAwayFishingRod();
        putAwayCrucifix();
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
        lowerSpyglass();
        putAwayFishingRod();
        putAwayCrucifix();
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();

        if (!main.isEmpty()) setSheathedWeapon(main.copy(), 0);
        if (!off.isEmpty()) setSheathedWeapon(off.copy(), 1);

        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
    }

    public ItemStack getSheathedWeapon(int index) {
        return this.sheathedWeapons.get(index);
    }

    public void setSheathedWeapon(ItemStack item, int index) {
        this.sheathedWeapons.set(index, item);
        if (!this.level().isClientSide()) {
            reevaluateHunterClass();
        }
    }

    public boolean raiseSpyglass() {
        if (!getMainHandItem().isEmpty()) return false;
        setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SPYGLASS));
        startUsingItem(InteractionHand.MAIN_HAND);
        playSound(SoundEvents.SPYGLASS_USE);
        return true;
    }

    public void lowerSpyglass() {
        if (!getMainHandItem().is(Items.SPYGLASS)) return;
        stopUsingItem();
        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        playSound(SoundEvents.SPYGLASS_STOP_USING);
    }

    public boolean isUsingSpyglass() {
        return isUsingItem() && getUseItem().is(Items.SPYGLASS);
    }

    public boolean takeOutFishingRod() {
        if (getMainHandItem().is(Items.FISHING_ROD)) return true;
        if (!getMainHandItem().isEmpty()) return false;
        setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
        return true;
    }

    public void putAwayFishingRod() {
        if (getMainHandItem().is(Items.FISHING_ROD)) setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
    }

    public float getFishingInterest() {
        return this.fishingInterest;
    }

    private static boolean isCrucifix(ItemStack stack) {
        return stack.getItem() instanceof CrucifixItem;
    }

    public ItemStack getSheathedCrucifix() {
        return this.sheathedWeapons.get(CRUCIFIX_SLOT);
    }

    public ItemStack drawCrucifix() {
        lowerSpyglass();
        putAwayFishingRod();
        ItemStack crucifix = getSheathedCrucifix();
        ItemStack main = getMainHandItem();
        if (crucifix.isEmpty() || (!main.isEmpty() && !this.sheathedWeapons.get(0).isEmpty())) return ItemStack.EMPTY;

        if (!main.isEmpty()) this.sheathedWeapons.set(0, main.copy());
        this.sheathedWeapons.set(CRUCIFIX_SLOT, ItemStack.EMPTY);
        setItemInHand(InteractionHand.MAIN_HAND, crucifix);
        this.crucifixHoldTicks = CRUCIFIX_HOLD_TICKS;
        return crucifix;
    }

    private void putAwayCrucifix() {
        this.crucifixHoldTicks = 0;
        ItemStack held = getMainHandItem();
        if (!isCrucifix(held) || !this.sheathedWeapons.get(CRUCIFIX_SLOT).isEmpty()) return;

        this.sheathedWeapons.set(CRUCIFIX_SLOT, held.copy());
        boolean weaponsDrawn = this.getBrain().hasMemoryValue(ModMemoryTypes.WEAPONS_UNSHEATHED.get());
        ItemStack main = weaponsDrawn ? this.sheathedWeapons.get(0) : ItemStack.EMPTY;
        if (weaponsDrawn) this.sheathedWeapons.set(0, ItemStack.EMPTY);
        setItemInHand(InteractionHand.MAIN_HAND, main.copy());
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
        lowerSpyglass();
        putAwayFishingRod();
        boolean ominous = !this.dead && hasOminousBanner();
        super.die(source);
        if (ominous && this.dead && level() instanceof ServerLevel serverLevel) {
            FactionBadOmenMobEffect.grantBadOmen(source.getEntity(), ModFactions.HUNTER);
            if (this.squad != null) this.squad.startBannerCooldown(serverLevel.getGameTime());
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        putAwayCrucifix();
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

    public ItemStack getBanner() {
        return this.entityData.get(DATA_BANNER);
    }

    public boolean hasBanner() {
        return !getBanner().isEmpty();
    }

    public boolean hasOminousBanner() {
        return this.ominousBanner && hasBanner();
    }

    public void makeBannerOminous() {
        if (hasBanner()) this.ominousBanner = true;
    }

    public void equipBanner(ItemStack banner, boolean ominous) {
        if (banner.isEmpty()) return;
        this.entityData.set(DATA_BANNER, banner);
        this.ominousBanner = ominous;
    }

    public ItemStack removeBanner() {
        ItemStack banner = getBanner();
        this.entityData.set(DATA_BANNER, ItemStack.EMPTY);
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
