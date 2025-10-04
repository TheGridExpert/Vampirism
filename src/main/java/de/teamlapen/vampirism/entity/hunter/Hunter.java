package de.teamlapen.vampirism.entity.hunter;

import com.mojang.serialization.Dynamic;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.core.ModHunterVariants;
import de.teamlapen.vampirism.core.ModRegistries;
import de.teamlapen.vampirism.entity.ai.navigation.HunterPathNavigation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.NeoForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Hunter extends PathfinderMob implements VariantHolder<Holder<IHunterVariant>> {

    private static final EntityDataAccessor<Holder<IHunterVariant>> DATA_VARIANT_ID = SynchedEntityData.defineId(Hunter.class, ModEntities.HUNTER_VARIANT.get());
    public static final Holder<IHunterVariant> DEFAULT_VARIANT = ModHunterVariants.HUNTER_5_SLIM;

    public static final String VARIANT_KEY = "variant";

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
        return HunterAi.makeBrain(this, this.brainProvider().makeBrain(dynamic));
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        ProfilerFiller profilerFiller = Profiler.get();
        profilerFiller.push("hunterBrain");
        this.getBrain().tick(level, this);
        profilerFiller.pop();
        HunterAi.updateActivity(getBrain());

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
        builder.define(DATA_VARIANT_ID, DEFAULT_VARIANT);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putString(VARIANT_KEY, Objects.requireNonNull(this.getVariant().unwrapKey().orElse(DEFAULT_VARIANT.getKey())).location().toString());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        Optional.ofNullable(ResourceLocation.tryParse(compound.getString(VARIANT_KEY)))
                .map(key -> ResourceKey.create(VampirismRegistries.Keys.HUNTER_VARIANT, key))
                .flatMap(ModRegistries.HUNTER_VARIANT::get)
                .ifPresent(this::setVariant);
    }

    @Nullable
    @Override
    @SuppressWarnings("deprecation")
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        this.setVariant(HunterVariant.getRandomVariant(DEFAULT_VARIANT, level.getRandom()));
        HunterAi.initMemories(this);
        return super.finalizeSpawn(level, difficulty, spawnReason, spawnGroupData);
    }
}
