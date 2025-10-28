package de.teamlapen.vampirism.blockentity;

import de.teamlapen.lib.lib.util.SingleItemHandler;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.api.items.IBloodChargeable;
import de.teamlapen.vampirism.core.ModBlockEntities;
import de.teamlapen.vampirism.core.ModFluids;
import de.teamlapen.vampirism.core.ModParticles;
import de.teamlapen.vampirism.items.VampireSwordItem;
import de.teamlapen.vampirism.particle.FlyingBloodParticleOption;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

public class PedestalBlockEntity extends BlockEntity {

    public static final String KEY_STACK_INSIDE = "StackInside";
    public static final String KEY_BLOOD_STORED = "BloodStored";
    public static final String KEY_CHARGING_TICKS = "ChargingTicks";

    private static final int COOLDOWN_TICKS = 40;
    private static final int CHARGE_RATE = 30;
    private static final int CHARGE_DURATION = 20;

    public final IItemHandler itemHandler;

    public ItemStack stackInside;
    public int bloodStored = 0;
    public int chargingTicks = 0;
    public int clientTicks = 0;

    public PedestalBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.BLOOD_PEDESTAL.get(), pos, blockState);
        this.itemHandler = new SingleItemHandler<>(this, blockEntity -> blockEntity.stackInside, (blockEntity, stack) -> blockEntity.stackInside = stack, stack -> stack.getItem() instanceof VampireSwordItem, 1, this::markAndUpdate);
        this.stackInside = ItemStack.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        // For backwards compatibility with 1.10 and lower
        if (tag.contains("item")) {
            stackInside = ItemStack.parseOptional(registries, tag.getCompound("item"));
        }
        stackInside = ItemStack.parseOptional(registries, tag.getCompound(KEY_STACK_INSIDE));
        bloodStored = tag.getInt(KEY_BLOOD_STORED);
        chargingTicks = tag.getInt(KEY_CHARGING_TICKS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put(KEY_STACK_INSIDE, stackInside.saveOptional(registries));
        tag.putInt(KEY_BLOOD_STORED, bloodStored);
        tag.putInt(KEY_CHARGING_TICKS, chargingTicks);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        return this.saveWithoutMetadata(provider);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        if (this.hasLevel()) this.loadCustomOnly(pkt.getTag(), lookupProvider);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity blockEntity) {
        if (blockEntity.chargingTicks < 0) {
            blockEntity.chargingTicks++;
            return;
        }

        IBloodChargeable chargeable = getChargeable(blockEntity.stackInside);
        if (chargeable == null) {
            return;
        }

        if (blockEntity.chargingTicks == 0 && !chargeable.canBeCharged(blockEntity.stackInside)) {
            blockEntity.chargingTicks = -COOLDOWN_TICKS;
        }

        if (blockEntity.chargingTicks > 0) {
            blockEntity.chargingTicks--;
            if (blockEntity.chargingTicks == 0) {
                if (blockEntity.bloodStored > 0) {
                    int charged = chargeable.charge(blockEntity.stackInside, blockEntity.bloodStored);
                    blockEntity.bloodStored -= Math.max(0, charged);
                }
                blockEntity.markAndUpdate();
            }
            return;
        }

        if (blockEntity.chargingTicks == 0) {
            if (blockEntity.bloodStored < CHARGE_RATE) {
                blockEntity.drainBlood();
            }
            if (blockEntity.bloodStored > 0) {
                blockEntity.chargingTicks = CHARGE_DURATION;
                blockEntity.markAndUpdate();
            } else {
                blockEntity.chargingTicks = -COOLDOWN_TICKS;
            }
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, PedestalBlockEntity blockEntity) {
        blockEntity.clientTicks++;
        if (blockEntity.chargingTicks > 0 && blockEntity.clientTicks % 8 == 0) {
            spawnChargedParticle(level, pos);
        }
    }

    private void drainBlood() {
        if (level == null) return;
        FluidUtil.getFluidHandler(this.level, this.worldPosition.below(), Direction.UP).ifPresent(handler -> {
            FluidStack drained = handler.drain(new FluidStack(ModFluids.BLOOD.get(), VReference.FOOD_TO_FLUID_BLOOD), IFluidHandler.FluidAction.SIMULATE);
            if (!drained.isEmpty() && drained.getAmount() == VReference.FOOD_TO_FLUID_BLOOD) {
                drained = handler.drain(new FluidStack(ModFluids.BLOOD.get(), VReference.FOOD_TO_FLUID_BLOOD), IFluidHandler.FluidAction.EXECUTE);
                bloodStored += drained.getAmount();
            }
        });
    }

    @Nullable
    private static IBloodChargeable getChargeable(ItemStack stack) {
        return stack.getItem() instanceof IBloodChargeable chargeable ? chargeable : null;
    }

    private void markAndUpdate() {
        if (level != null) {
            setChanged();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    public boolean hasStack() {
        return !this.stackInside.isEmpty();
    }

    public ItemStack removeStack() {
        ItemStack stack = this.stackInside;
        this.stackInside = ItemStack.EMPTY;
        return stack;
    }

    public ItemStack setStack(ItemStack stack) {
        this.chargingTicks = 0;
        ItemStack oldStack = this.stackInside;
        this.stackInside = stack;
        return oldStack;
    }

    public int getChargeProgress() {
        IBloodChargeable chargeable = getChargeable(this.stackInside);
        return chargeable != null ? (int) chargeable.getChargePercentage(this.stackInside) * 10 : 0;
    }

    private static void spawnChargedParticle(Level level, BlockPos pos) {
        RandomSource random = level.random;
        Vec3 base = Vec3.upFromBottomCenterOf(pos, 0.8);

        double[][] offsets = {
                { 0.20, 0.65, 0.20 },
                { 0.80, 0.65, 0.20 },
                { 0.20, 0.65, 0.80 },
                { 0.80, 0.65, 0.80 }
        };

        for (double[] offset : offsets) {
            Vec3 target = new Vec3(base.x + (1f - random.nextFloat()) * 0.1, base.y + (1f - random.nextFloat()) * 0.2, base.z + (1f - random.nextFloat()) * 0.1);
            int lifetime = (int) (4.0F / (random.nextFloat() * 0.9F + 0.1F));
            ModParticles.spawnParticleClient(level, new FlyingBloodParticleOption(target, lifetime, false), pos.getX() + offset[0], pos.getY() + offset[1], pos.getZ() + offset[2]);
        }
    }
}
