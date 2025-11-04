/*
 * Licenced under GNU GPLv3. See LICENCE.txt in this package.
 * Credits to bl4ckscor3's Sit https://github.com/bl4ckscor3/Sit/
 */

package de.teamlapen.vampirism.sit;

import de.teamlapen.vampirism.core.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SitEntity extends Entity {

    public static @Nullable SitEntity createEntity(Level level, BlockPos pos, double offset) {
        SitEntity entity = ModEntities.SIT.get().create(level, EntitySpawnReason.MOB_SUMMONED);

        if (entity == null) return null;

        BlockState state = level.getBlockState(pos);

        float rotation = 0.0f;
        if (state.getBlock() instanceof ISittableBlock sittable) {
            rotation = sittable.getSitRotation(state);
        }
        entity.setYRot(rotation);

        entity.setPos(pos.getX() + 0.5D, pos.getY() + offset, pos.getZ() + 0.5D);
        entity.noPhysics = true;

        return entity;
    }

    public SitEntity(EntityType<SitEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void positionRider(Entity passenger, MoveFunction callback) {
        super.positionRider(passenger, callback);

        if (!passenger.getType().is(EntityTypeTags.CAN_TURN_IN_BOATS)) {
            this.clampEntityRotation(passenger);
        }
    }

    protected void clampEntityRotation(Entity entity) {
        entity.setYBodyRot(this.getYRot());
        float yawDifference = Mth.wrapDegrees(entity.getYRot() - this.getYRot());
        float clampedYaw = Mth.clamp(yawDifference, -120.0f, 120.0f);
        float correction = clampedYaw - yawDifference;
        entity.yRotO += correction;
        entity.setYRot(entity.getYRot() + correction);
        entity.setYHeadRot(entity.getYRot());
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        BlockPos pos = blockPosition();
        BlockState state = level().getBlockState(pos);
        Vec3 result = null;

        if (state.getBlock() instanceof ISittableBlock sittable) {
            result = sittable.getStandUpLocation(level(), pos, passenger, state.getValue(BlockStateProperties.HORIZONTAL_FACING));
        }

        discard();
        return result != null ? result : new Vec3(pos.getX() + 0.5, pos.getY() + 1.01, pos.getZ() + 0.5);
    }


    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();
        SitUtil.addSitEntity(level(), blockPosition(), this);
    }

    @Override
    public void remove(RemovalReason reason) {
        this.ejectPassengers();
        SitUtil.unregisterSitEntity(level(), blockPosition());

        super.remove(reason);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource damageSource, float amount) {
        return false;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }
}