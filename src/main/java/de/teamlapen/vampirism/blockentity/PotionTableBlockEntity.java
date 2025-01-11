package de.teamlapen.vampirism.blockentity;

import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.api.items.IExtendedBrewingRecipeRegistry;
import de.teamlapen.vampirism.core.ModSounds;
import de.teamlapen.vampirism.core.ModTiles;
import de.teamlapen.vampirism.entity.player.hunter.HunterPlayer;
import de.teamlapen.vampirism.entity.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.inventory.PotionTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


public class PotionTableBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer, MenuProvider, ICapabilityProvider<PotionTableBlockEntity, Direction, IItemHandler> {

    /*
     * 0: Fuel
     * 1: Extra ingredient
     * 2: Main (vanilla) ingredient
     * 3-5: Main bottle slots
     * 6-7: Extra bottle slots
     */
    private static final int[] SLOTS_FOR_UP = new int[] {0, 1, 2};
    private static final int[] SLOTS_FOR_DOWN = new int[] {3, 4, 5, 1, 2};
    private static final int[] SLOTS_FOR_DOWN_EXTENDED = new int[] {3, 4, 5, 6, 7, 1, 2};
    private static final int[] OUTPUT_SLOTS = new int[] {3, 4, 5, 0};
    private static final int[] OUTPUT_SLOTS_EXTENDED = new int[] {3, 4, 5, 6, 7, 0};
    private final BrewingCapabilities config = new BrewingCapabilities();
    @Nullable
    private UUID ownerID;
    @Nullable
    private Component ownerName;
    private @NotNull NonNullList<ItemStack> brewingItemStacks = NonNullList.withSize(8, ItemStack.EMPTY);
    private int brewTime;
    private Item ingredientID;
    private Item extraIngredientID;
    private int fuel;
    protected final ContainerData syncedProperties = new ContainerData() {
        public int get(int index) {
            return switch (index) {
                case 0 -> PotionTableBlockEntity.this.brewTime;
                case 1 -> PotionTableBlockEntity.this.fuel;
                default -> 0;
            };
        }

        public void set(int index, int value) {
            switch (index) {
                case 0 -> PotionTableBlockEntity.this.brewTime = value;
                case 1 -> PotionTableBlockEntity.this.fuel = value;
            }

        }

        public int getCount() {
            return 2;
        }
    };

    public PotionTableBlockEntity(@NotNull BlockPos pos, @NotNull BlockState state) {
        super(ModTiles.POTION_TABLE.get(), pos, state);
    }

    @Override
    public boolean canOpen(@NotNull Player player) {
        if (super.canOpen(player)) {
            HunterPlayer hunter = HunterPlayer.get(player);
            if (hunter.getLevel() > 0) {
                if (ownerID == null) {
                    setOwnerID(player);
                    this.config.deriveFromHunter(hunter);
                    return true;
                } else if (ownerID.equals(player.getUUID())) {
                    this.config.deriveFromHunter(hunter);
                    return true;
                } else {
                    player.displayClientMessage(Component.translatable("text.vampirism.potion_table.other", getOwnerName()), true);
                }
            } else {
                player.displayClientMessage(Component.translatable("text.vampirism.unfamiliar"), true);
            }
            return false;
        }
        return false;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.brewingItemStacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> p_332640_) {
        this.brewingItemStacks = p_332640_;
    }

    @Override
    public boolean canPlaceItem(int index, @NotNull ItemStack stack) {
        if (index == 2) {
            return this.level.potionBrewing().isIngredient(stack);
        } else {
            Item item = stack.getItem();
            if (index == 0) {
                return item == Items.BLAZE_POWDER;
            } else {
                return this.level.potionBrewing().isInput(stack) && this.getItem(index).isEmpty();
            }
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, @NotNull ItemStack itemStackIn, @Nullable Direction direction) {
        return this.canPlaceItem(index, itemStackIn);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, @NotNull ItemStack stack, @NotNull Direction direction) {
        if (index == 1 || index == 2) {
            return stack.getItem() == Items.GLASS_BOTTLE;
        } else {
            return true;
        }
    }

    @Override
    public void clearContent() {
        this.brewingItemStacks.clear();
    }

    @Override
    public @Nullable IItemHandler getCapability(PotionTableBlockEntity object, Direction context) {
        return new SidedInvWrapper(this, context);
    }

    @NotNull
    @Override
    public Component getDisplayName() {
        return Component.translatable("tile.vampirism.potion_table.display", ownerName, Component.translatable("tile.vampirism.potion_table"));
    }

    @NotNull
    public Component getOwnerName() {
        return ownerName == null ? Component.literal("Unknown") : ownerName;
    }

    @Override
    public int getContainerSize() {
        return this.brewingItemStacks.size();
    }

    @NotNull
    @Override
    public int[] getSlotsForFace(@NotNull Direction side) {
        if (side == Direction.UP) {
            return SLOTS_FOR_UP;
        } else {
            return side == Direction.DOWN ? config.multiTaskBrewing ? SLOTS_FOR_DOWN_EXTENDED : SLOTS_FOR_DOWN : config.multiTaskBrewing ? OUTPUT_SLOTS_EXTENDED : OUTPUT_SLOTS;
        }
    }

    @NotNull
    @Override
    public ItemStack getItem(int index) {
        return index >= 0 && index < this.brewingItemStacks.size() ? this.brewingItemStacks.get(index) : ItemStack.EMPTY;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.brewingItemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    public boolean isExtended() {
        return this.config.isMultiTaskBrewing();
    }

    @Override
    public void loadAdditional(@NotNull CompoundTag compound, HolderLookup.Provider provider) {
        super.loadAdditional(compound, provider);
        this.brewingItemStacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.brewingItemStacks, provider);
        this.brewTime = compound.getShort("BrewTime");
        this.fuel = compound.getByte("Fuel");
        this.config.fromByte(compound.getByte("config"));
        this.ownerID = compound.hasUUID("owner") ? compound.getUUID("owner") : null;
        this.ownerName = compound.contains("owner_name") ? Component.Serializer.fromJsonLenient(compound.getString("owner_name"), provider) : null;
    }

    @NotNull
    @Override
    public ItemStack removeItem(int index, int count) {
        return ContainerHelper.removeItem(this.brewingItemStacks, index, count);
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        return ContainerHelper.takeItem(this.brewingItemStacks, index);
    }

    @Override
    public void saveAdditional(@NotNull CompoundTag compound, HolderLookup.Provider provider) {
        super.saveAdditional(compound, provider);
        compound.putShort("BrewTime", (short) this.brewTime);
        ContainerHelper.saveAllItems(compound, this.brewingItemStacks, provider);
        compound.putByte("Fuel", (byte) this.fuel);
        compound.putByte("config", this.config.toByte());
        if (ownerID != null) {
            compound.putUUID("owner", ownerID);
            compound.putString("owner_name", Component.Serializer.toJson(ownerName, provider));
        }
    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        if (index >= 0 && index < this.brewingItemStacks.size()) {
            this.brewingItemStacks.set(index, stack);
        }

    }

    public void setOwnerID(@NotNull Player player) {
        ownerID = player.getUUID();
        ownerName = player.getName();
        this.setChanged();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (!hasLevel()) return false;
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(player.distanceToSqr((double) this.worldPosition.getX() + 0.5D, (double) this.worldPosition.getY() + 0.5D, (double) this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    public static void tick(@NotNull Level level, BlockPos pos, BlockState state, @NotNull PotionTableBlockEntity blockEntity) {
        ItemStack itemstack = blockEntity.brewingItemStacks.getFirst();
        if (blockEntity.fuel <= 0 && itemstack.getItem() == Items.BLAZE_POWDER) {
            blockEntity.fuel = 20;
            itemstack.shrink(1);
            blockEntity.setChanged();
        }

        //Periodically update table capabilities if player is loaded
        if (blockEntity.ownerID != null && level.getGameTime() % 64 == 0) {
            Player owner = level.getPlayerByUUID(blockEntity.ownerID);
            if (owner != null) {
                blockEntity.config.deriveFromHunter(HunterPlayer.get(owner));
            }
        }

        boolean canBrew = blockEntity.canBrew();
        boolean isBrewing = blockEntity.brewTime > 0;
        if (isBrewing) {
            --blockEntity.brewTime;
            if (blockEntity.brewTime == 0 && canBrew) { //Finish brewing
                blockEntity.brewPotions();
                blockEntity.setChanged();
            } else if (!canBrew || blockEntity.ingredientID != blockEntity.brewingItemStacks.get(2).getItem() || blockEntity.extraIngredientID != blockEntity.brewingItemStacks.get(1).getItem()) {//Abort brewing if ingredients changed
                blockEntity.brewTime = 0;
                blockEntity.setChanged();
            }
        } else if (canBrew && blockEntity.fuel > 0) {
            --blockEntity.fuel;
            blockEntity.brewTime = blockEntity.config.isSwiftBrewing() ? 400 : 200;
            blockEntity.ingredientID = blockEntity.brewingItemStacks.get(2).getItem();
            blockEntity.extraIngredientID = blockEntity.brewingItemStacks.get(1).getItem();
            blockEntity.setChanged();
        }

    }

    @NotNull
    @Override
    protected AbstractContainerMenu createMenu(int id, @NotNull Inventory player) {
        return new PotionTableMenu(id, player, ContainerLevelAccess.create(this.level, this.getBlockPos()), this, this.config.multiTaskBrewing, syncedProperties);
    }

    @NotNull
    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.brewing");
    }

    private void brewPotions() {
        ItemStack ingredientStack = this.brewingItemStacks.get(2);
        ItemStack extraIngredient = this.brewingItemStacks.get(1);

        boolean brewed = VampirismAPI.extendedBrewingRecipeRegistry().brewPotions(this.level, brewingItemStacks, ingredientStack, extraIngredient, this.config, this.config.multiTaskBrewing ? OUTPUT_SLOTS_EXTENDED : OUTPUT_SLOTS, true);

        if (!brewed) {
            NonNullList<ItemStack> copiedBrewingItemStack = NonNullList.of(ItemStack.EMPTY, this.brewingItemStacks.get(3).copy(), this.brewingItemStacks.get(4).copy(), this.brewingItemStacks.get(5).copy(), this.brewingItemStacks.get(2).copy(), this.brewingItemStacks.get(0).copy());
            if (net.neoforged.neoforge.event.EventHooks.onPotionAttemptBrew(copiedBrewingItemStack)) {
                this.brewingItemStacks.set(3, copiedBrewingItemStack.get(0));
                this.brewingItemStacks.set(4, copiedBrewingItemStack.get(1));
                this.brewingItemStacks.set(5, copiedBrewingItemStack.get(2));
                this.brewingItemStacks.set(2, copiedBrewingItemStack.get(3));
                this.brewingItemStacks.set(0, copiedBrewingItemStack.get(4));
                return;
            }
            VampirismAPI.extendedBrewingRecipeRegistry().brewPotions(this.level, brewingItemStacks, ingredientStack, extraIngredient, this.config, this.config.multiTaskBrewing ? OUTPUT_SLOTS_EXTENDED : OUTPUT_SLOTS, false);
            copiedBrewingItemStack = NonNullList.of(ItemStack.EMPTY, this.brewingItemStacks.get(3).copy(), this.brewingItemStacks.get(4).copy(), this.brewingItemStacks.get(5).copy(), this.brewingItemStacks.get(2).copy(), this.brewingItemStacks.get(0).copy());
            net.neoforged.neoforge.event.EventHooks.onPotionBrewed(brewingItemStacks);
            this.brewingItemStacks.set(3, copiedBrewingItemStack.get(0));
            this.brewingItemStacks.set(4, copiedBrewingItemStack.get(1));
            this.brewingItemStacks.set(5, copiedBrewingItemStack.get(2));
            this.brewingItemStacks.set(2, copiedBrewingItemStack.get(3));
            this.brewingItemStacks.set(0, copiedBrewingItemStack.get(4));
        }


        BlockPos blockpos = this.getBlockPos();
        var remainder = ingredientStack.getCraftingRemainder();
        if (!remainder.isEmpty()) {
            if (ingredientStack.isEmpty()) {
                ingredientStack = remainder;
            } else if (!this.level.isClientSide) {
                Containers.dropItemStack(this.level, blockpos.getX(), blockpos.getY(), blockpos.getZ(), remainder);
            }
        }
        remainder = extraIngredient.getCraftingRemainder();
        if (!remainder.isEmpty()) {
            if (extraIngredient.isEmpty()) {
                extraIngredient = remainder;
            } else if (!this.level.isClientSide) {
                Containers.dropItemStack(this.level, blockpos.getX(), blockpos.getY(), blockpos.getZ(), remainder);
            }
        }

        this.brewingItemStacks.set(2, ingredientStack);
        this.brewingItemStacks.set(1, extraIngredient);
        this.level.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), ModSounds.POTION_TABLE_CRAFTING.get(), SoundSource.BLOCKS, 1f, 1f);

        this.level.levelEvent(1035, blockpos, 0);
    }

    private boolean canBrew() {
        ItemStack extraStack = this.brewingItemStacks.get(1);
        ItemStack ingredientStack = this.brewingItemStacks.get(2);
        if (!ingredientStack.isEmpty()) {
            return VampirismAPI.extendedBrewingRecipeRegistry().canBrew(this.level, brewingItemStacks, ingredientStack, extraStack, this.config, config.multiTaskBrewing ? OUTPUT_SLOTS_EXTENDED : OUTPUT_SLOTS); // divert to VanillaBrewingRegistry
        }

        return false;
    }

    protected static class BrewingCapabilities implements IExtendedBrewingRecipeRegistry.IExtendedBrewingCapabilities {
        boolean durableBrewing;
        boolean concentratedBrewing;
        boolean swiftBrewing;
        boolean masterBrewing;
        boolean efficientBrewing;
        boolean multiTaskBrewing;

        public void deriveFromHunter(@NotNull IHunterPlayer player) {
            ISkillHandler<IHunterPlayer> manager = player.getSkillHandler();
            durableBrewing = manager.isSkillEnabled(HunterSkills.DURABLE_BREWING) || manager.isSkillEnabled(HunterSkills.CONCENTRATED_DURABLE_BREWING);
            concentratedBrewing = manager.isSkillEnabled(HunterSkills.CONCENTRATED_BREWING) || manager.isSkillEnabled(HunterSkills.CONCENTRATED_DURABLE_BREWING);
            swiftBrewing = manager.isSkillEnabled(HunterSkills.SWIFT_BREWING);
            masterBrewing = manager.isSkillEnabled(HunterSkills.MASTER_BREWER);
            efficientBrewing = manager.isSkillEnabled(HunterSkills.EFFICIENT_BREWING);
            multiTaskBrewing = manager.isSkillEnabled(HunterSkills.MULTITASK_BREWING);
        }

        public void fromByte(byte d) {
            this.durableBrewing = (d & (0b1)) > 0;
            this.concentratedBrewing = (d & (0b1 << 1)) > 0;
            this.swiftBrewing = (d & (0b1 << 2)) > 0;
            this.masterBrewing = (d & (0b1 << 3)) > 0;
            this.efficientBrewing = (d & (0b1 << 4)) > 0;
            this.multiTaskBrewing = (d & (0b1 << 5)) > 0;
        }

        @Override
        public boolean isConcentratedBrewing() {
            return concentratedBrewing;
        }

        @Override
        public boolean isDurableBrewing() {
            return durableBrewing;
        }

        @Override
        public boolean isEfficientBrewing() {
            return efficientBrewing;
        }

        @Override
        public boolean isMasterBrewing() {
            return masterBrewing;
        }

        @Override
        public boolean isMultiTaskBrewing() {
            return multiTaskBrewing;
        }

        @Override
        public boolean isSwiftBrewing() {
            return swiftBrewing;
        }

        public void reset() {
            durableBrewing = concentratedBrewing = swiftBrewing = masterBrewing = efficientBrewing = multiTaskBrewing = false;
        }

        public byte toByte() {
            byte d = 0;
            if (durableBrewing) d |= 0b1;
            if (concentratedBrewing) d |= (0b1 << 1);
            if (swiftBrewing) d |= (0b1 << 2);
            if (masterBrewing) d |= (0b1 << 3);
            if (efficientBrewing) d |= (0b1 << 4);
            if (multiTaskBrewing) d |= (0b1 << 5);
            return d;
        }

    }
}
