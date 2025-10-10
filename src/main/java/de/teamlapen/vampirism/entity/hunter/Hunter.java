package de.teamlapen.vampirism.entity.hunter;

import com.mojang.serialization.Dynamic;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.api.items.IHunterCrossbow;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModHunterVariants;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.core.ModRegistries;
import de.teamlapen.vampirism.entity.ai.navigation.HunterPathNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Hunter extends PathfinderMob implements VariantHolder<Holder<IHunterVariant>>, CrossbowAttackMob {

    private static final EntityDataAccessor<String> DATA_CLASS_TYPE_ID = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Holder<IHunterVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.HUNTER_VARIANT.get());
    private static final EntityDataAccessor<Boolean> DATA_IS_CHARGING_CROSSBOW = SynchedEntityData.defineId(Hunter.class, EntityDataSerializers.BOOLEAN);

    public static final ClassType DEFAULT_CLASS_TYPE = ClassType.MELEE;
    public static final Holder<IHunterVariant> DEFAULT_VARIANT = ModHunterVariants.HUNTER_5_SLIM;

    public static final String TAG_CLASS_TYPE = "ClassType";
    public static final String TAG_VARIANT = "Variant";
    public static final String TAG_SHEATHED_WEAPONS = "SheathedWeapons";

    public static final float ARROW_VELOCITY = 2.0F;

    private final NonNullList<ItemStack> sheathedWeapons = NonNullList.withSize(2, ItemStack.EMPTY);

    public Hunter(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        ((GroundPathNavigation) this.getNavigation()).setCanOpenDoors(true);
        this.getNavigation().setCanFloat(true);
        this.getNavigation().setRequiredPathLength(48.0F);
    }

    public static boolean checkHunterSpawnRules(EntityType<Hunter> entityType, LevelAccessor level, EntitySpawnReason spawnReason, BlockPos pos, RandomSource random) {
        return level.getDifficulty() != Difficulty.PEACEFUL && Mob.checkMobSpawnRules(entityType, level, spawnReason, pos, random);
    }

    public static AttributeSupplier.Builder getAttributeBuilder() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.MOVEMENT_SPEED, 0.55F)
                .add(NeoForgeMod.SWIM_SPEED, 2.5F)
                .add(Attributes.ATTACK_DAMAGE, 3.0);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Brain<Hunter> getBrain() {
        return (Brain<Hunter>) super.getBrain();
    }

    @Override
    protected Brain.Provider<Hunter> brainProvider() {
        return HunterAi.brainProvider();
    }

    @Override
    protected Brain<Hunter> makeBrain(Dynamic<?> dynamic) {
        this.setHunterClass(ClassType.getRandom(this.random));
        return HunterAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("hunterBrain");
        this.getBrain().tick(level, this);
        profilerFiller.pop();
        HunterAi.updateActivity(this);

        super.customServerAiStep(level);
    }

    @Override
    protected void sendDebugPackets() {
        super.sendDebugPackets();
        DebugPackets.sendEntityBrain(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new HunterPathNavigation(this, level);
    }

    public boolean isNearGround() {
        return Stream.of(Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST).map(direction -> level().getBlockState(blockPosition().below().relative(direction))).anyMatch(state -> !(state.is(Blocks.WATER)));
    }

    public boolean isShallowWater() {
        return isShallowWater(level(), blockPosition());
    }

    public static boolean isShallowWater(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.above().mutable();
        while (level.isEmptyBlock(cursor) && level.isInsideBuildHeight(pos.getY())) {
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

    public void setHunterClass(ClassType hunterClass) {
        this.entityData.set(DATA_CLASS_TYPE_ID, hunterClass.getSerializedName());
    }

    public ClassType getHunterClass() {
        return ClassType.get(this.entityData.get(DATA_CLASS_TYPE_ID));
    }

    @Override
    public void setVariant(Holder<IHunterVariant> variant) {
        this.entityData.set(DATA_VARIANT_ID, variant);
    }

    @Override
    public Holder<IHunterVariant> getVariant() {
        return this.entityData.get(DATA_VARIANT_ID);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CLASS_TYPE_ID, DEFAULT_CLASS_TYPE.getSerializedName());
        builder.define(DATA_VARIANT_ID, DEFAULT_VARIANT);
        builder.define(DATA_IS_CHARGING_CROSSBOW, false);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);

        compound.putString(TAG_CLASS_TYPE, getHunterClass().getSerializedName());
        compound.putString(TAG_VARIANT, Objects.requireNonNull(this.getVariant().unwrapKey().orElse(DEFAULT_VARIANT.getKey())).location().toString());

        ListTag weaponsTag = new ListTag();

        for (ItemStack stack : this.sheathedWeapons) {
            if (!stack.isEmpty()) {
                weaponsTag.add(stack.save(this.registryAccess()));
            } else {
                weaponsTag.add(new CompoundTag());
            }
        }

        compound.put(TAG_SHEATHED_WEAPONS, weaponsTag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);

        if (compound.contains(TAG_CLASS_TYPE)) {
            setHunterClass(ClassType.get(compound.getString(TAG_CLASS_TYPE)));
        }
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString(TAG_VARIANT)))
                .map(key -> ResourceKey.create(VampirismRegistries.Keys.HUNTER_VARIANT, key))
                .flatMap(ModRegistries.HUNTER_VARIANT::get)
                .ifPresent(this::setVariant);

        if (compound.contains(TAG_SHEATHED_WEAPONS, CompoundTag.TAG_LIST)) {
            ListTag weaponsTag = compound.getList(TAG_SHEATHED_WEAPONS, CompoundTag.TAG_COMPOUND);

            for (int i = 0; i < this.sheathedWeapons.size(); i++) {
                CompoundTag weaponUnitTag = weaponsTag.getCompound(i);
                this.sheathedWeapons.set(i, ItemStack.parseOptional(this.registryAccess(), weaponUnitTag));
            }
        } else {
            Collections.fill(this.sheathedWeapons, ItemStack.EMPTY);
        }
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(HunterVariant.getRandomVariant(DEFAULT_VARIANT, level.getRandom()));

        assignRandomEquipment(level.getRandom());

        HunterAi.initMemories(this);

        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }

    // TODO: FactionRestriction only works for humans and thence npc hunters may not use all of their weapons' potential

    private void assignRandomEquipment(RandomSource random) {
        ItemStack axe = ModItems.HUNTER_AXE_NORMAL.toStack();
        ItemStack crossbow = ModItems.BASIC_CROSSBOW.toStack();

        Hunter.ClassType classType = getHunterClass();

        if (classType == ClassType.MELEE) {
            this.sheathedWeapons.set(0, axe.copy());
            this.sheathedWeapons.set(1, random.nextDouble() < 0.2 ? axe.copy() : ItemStack.EMPTY);
        } else if (classType == ClassType.RANGED) {
            this.sheathedWeapons.set(0, crossbow.copy());
        }
    }

    public void unsheatheWeapons() {
        if (!this.sheathedWeapons.isEmpty()) {
            ItemStack main = this.sheathedWeapons.get(0);
            if (!main.isEmpty()) {
                this.setItemInHand(InteractionHand.MAIN_HAND, main.copy());
            }

            if (this.sheathedWeapons.size() > 1) {
                ItemStack off = this.sheathedWeapons.get(1);
                if (!off.isEmpty()) {
                    this.setItemInHand(InteractionHand.OFF_HAND, off.copy());
                }
            }
        }
    }

    public void sheatheWeapons() {
        ItemStack main = this.getMainHandItem();
        ItemStack off = this.getOffhandItem();

        if (!main.isEmpty()) this.sheathedWeapons.set(0, main.copy());
        if (!off.isEmpty()) this.sheathedWeapons.set(1, off.copy());

        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
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
    }

    @Override
    public ItemStack getProjectile(ItemStack weaponStack) {
        if (weaponStack.getItem() instanceof IHunterCrossbow) {
            return CommonHooks.getProjectile(this, weaponStack, ModItems.CROSSBOW_ARROW_NORMAL.get().getDefaultInstance());
        }

        return super.getProjectile(weaponStack);
    }

    @Override
    public boolean canFireProjectileWeapon(ProjectileWeaponItem projectileWeapon) {
        return projectileWeapon instanceof CrossbowItem;
    }

    public boolean isMeleeClass() {
        return getHunterClass() == ClassType.MELEE;
    }

    public boolean isRangedClass() {
        return getHunterClass() == ClassType.RANGED;
    }

    public enum ClassType implements StringRepresentable {
        MELEE("melee", 0), // 6
        RANGED("ranged", 10); // 4

        private final String name;
        private final int weight;

        ClassType(String name, int weight) {
            this.name = name;
            this.weight = weight;
        }

        public static ClassType getRandom(RandomSource random) {
            int totalWeight = 0;
            for (ClassType classType : values()) {
                totalWeight += classType.weight;
            }

            int roll = random.nextInt(totalWeight);
            for (ClassType classType : values()) {
                roll -= classType.weight;
                if (roll < 0) {
                    return classType;
                }
            }

            return MELEE;
        }

        public static @NotNull Hunter.ClassType get(String value) {
            try {
                return ClassType.valueOf(value.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return MELEE;
            }
        }

        public int getWeight() {
            return weight;
        }

        @Override
        public String toString() {
            return this.name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
