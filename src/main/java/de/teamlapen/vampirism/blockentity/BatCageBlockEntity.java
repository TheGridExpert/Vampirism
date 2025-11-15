package de.teamlapen.vampirism.blockentity;

import de.teamlapen.lib.lib.blockentity.NetworkedBlockEntity;
import de.teamlapen.vampirism.core.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BatCageBlockEntity extends NetworkedBlockEntity {

    public static final String KEY_ENTITY_INSIDE = "EntityInside";

    private @Nullable CompoundTag entityTag;

    public BatCageBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BAT_CAGE.get(), pos, blockState);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.entityTag = tag.getCompound(KEY_ENTITY_INSIDE);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.entityTag != null) {
            tag.put(KEY_ENTITY_INSIDE, this.entityTag);
        }
    }

    @Override
    protected void loadSynced(CompoundTag tag, HolderLookup.Provider lookupProvider) {
        this.entityTag = tag.getCompound(KEY_ENTITY_INSIDE);
    }

    @Override
    protected void saveSynced(CompoundTag tag, HolderLookup.Provider registries) {
        if (this.entityTag != null) {
            tag.put(KEY_ENTITY_INSIDE, this.entityTag);
        }
    }

    public void setEntity(Entity entity) {
        if (this.level != null && this.level.isClientSide) return;

        CompoundTag tag = new CompoundTag();
        if (entity.saveAsPassenger(tag)) {
            this.entityTag = tag;
            entity.remove(Entity.RemovalReason.DISCARDED);
            setChanged();
        }
    }

    public void setEntityTag(@Nullable CompoundTag entityTag) {
        if (this.level != null && this.level.isClientSide) return;

        this.entityTag = entityTag;
        setChanged();
    }

    public boolean hasEntity() {
        return this.entityTag != null;
    }

    public @Nullable CompoundTag getEntityTag() {
        return entityTag;
    }

    public static boolean canContainEntity(Entity entity) {
        return entity instanceof Bat;
    }
}
