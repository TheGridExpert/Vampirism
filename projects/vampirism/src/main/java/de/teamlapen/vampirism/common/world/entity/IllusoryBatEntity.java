package de.teamlapen.vampirism.common.world.entity;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;

public class IllusoryBatEntity extends Bat {

    public static final String TAG_LIFETIME = "Lifetime";

    private static final int DEFAULT_LIFETIME = 30;

    private static final EntityDataAccessor<Integer> LIFETIME = SynchedEntityData.defineId(IllusoryBatEntity.class, EntityDataSerializers.INT);

    public IllusoryBatEntity(EntityType<? extends Bat> type, Level level) {
        super(type, level);
        setNoAi(true);
        setNoGravity(true);
        setResting(false);
        noPhysics = true;
        setInvulnerable(true);
    }

    @Override
    public void tick() {
        baseTick();
        noPhysics = true;
        setNoGravity(true);
        setResting(false);
        // Bat#tick, which starts this, is not called here
        flyAnimationState.startIfStopped(tickCount);
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.90D));

        Vec3 motion = getDeltaMovement();
        if (motion.lengthSqr() > 0.0001D) {
            setYRot((float) (Math.atan2(motion.z, motion.x) * 180.0D / Math.PI) - 90.0F);
            setXRot((float) -(Math.atan2(motion.y, Math.sqrt(motion.x * motion.x + motion.z * motion.z)) * 180.0D / Math.PI));
            yRotO = getYRot();
            xRotO = getXRot();
        }

        if (!level().isClientSide() && tickCount >= getLifetime()) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(LIFETIME, DEFAULT_LIFETIME);
    }

    public void setLifetime(int lifetime) {
        this.entityData.set(LIFETIME, lifetime);
    }

    public int getLifetime() {
        return this.entityData.get(LIFETIME);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putInt(TAG_LIFETIME, getLifetime());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.getInt(TAG_LIFETIME).ifPresent(this::setLifetime);
    }

    public float getCurrentTransparency(float partialTick) {
        float progress = (tickCount + partialTick) / getLifetime();
        float fadeProgress = Math.max(0.0F, (progress - 0.45F) / 0.55F);
        return Math.max(0.0F, 1.0F - fadeProgress * fadeProgress);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }
}
