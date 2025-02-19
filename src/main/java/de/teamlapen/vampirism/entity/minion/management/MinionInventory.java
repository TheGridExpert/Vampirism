package de.teamlapen.vampirism.entity.minion.management;

import com.google.common.collect.ImmutableList;
import de.teamlapen.lib.lib.inventory.InventoryHelper;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;


public class MinionInventory implements de.teamlapen.vampirism.api.entity.minion.IMinionInventory {

    private final NonNullList<ItemStack> inventory = NonNullList.withSize(25, ItemStack.EMPTY);
    private final NonNullList<ItemStack> inventoryHands = NonNullList.withSize(2, ItemStack.EMPTY);
    private final NonNullList<ItemStack> inventoryArmor = NonNullList.withSize(4, ItemStack.EMPTY);
    private final List<NonNullList<ItemStack>> allInventories = ImmutableList.of(this.inventoryHands, this.inventoryArmor, this.inventory);
    private int availableSize;

    public MinionInventory(int availableSize) {
        assert availableSize == 9 || availableSize == 12 || availableSize == 15; //See {@link MinionContainer}
        this.availableSize = availableSize;
    }

    public MinionInventory() {
        this(9);
    }

    @Override
    public void addItemStack(@NotNull ItemStack stack) {

        while (!stack.isEmpty()) {
            int slot = InventoryHelper.getFirstSuitableSlotToAdd(inventory, this.getContainerSize() - 6 /*access only main inventory*/, stack, this.getMaxStackSize());
            if (slot == -1) {
                break;
            }
            int oldSize = stack.getCount();
            InventoryHelper.addStackToSlotWithoutCheck(this, slot + 6 /*access main inventory*/, stack);
            if (stack.getCount() >= oldSize) {
                break;
            }
        }
    }

    public void clearContent() {
        for (List<ItemStack> list : this.allInventories) {
            list.clear();
        }

    }

    @Override
    public int getContainerSize() {
        return 6 + availableSize;
    }

    @Override
    public int getAvailableSize() {
        return availableSize;
    }

    public @NotNull MinionInventory setAvailableSize(int newSize) {
        assert newSize == 9 || newSize == 12 || newSize == 15;
        this.availableSize = newSize;
        return this;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getInventoryArmor() {
        return inventoryArmor;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getInventoryHands() {
        return inventoryHands;
    }

    @Override
    public List<NonNullList<ItemStack>> getAllInventorys() {
        return allInventories;
    }

    @NotNull
    @Override
    public ItemStack getItem(int index) {
        assert index >= 0;
        if (index < 2) {
            return inventoryHands.get(index);
        } else if (index < 6) {
            return inventoryArmor.get(index - 2);
        } else if (index < 6 + availableSize) {
            return inventory.get(index - 6);
        }
        return ItemStack.EMPTY;
    }

    public void read(HolderLookup.Provider provider, @NotNull ListTag nbtTagListIn) {
        this.inventory.clear();
        this.inventoryArmor.clear();
        this.inventoryHands.clear();

        for (int i = 0; i < nbtTagListIn.size(); ++i) {
            CompoundTag compoundTag = nbtTagListIn.getCompound(i);
            int j = compoundTag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.parse(provider, compoundTag).orElse(ItemStack.EMPTY);
            if (!itemstack.isEmpty()) {
                if (j < this.inventoryHands.size()) {
                    this.inventoryHands.set(j, itemstack);
                } else if (j >= 10 && j < this.inventoryArmor.size() + 10) {
                    this.inventoryArmor.set(j - 10, itemstack);
                } else if (j >= 20 && j < this.inventory.size() + 20) {
                    this.inventory.set(j - 20, itemstack);
                }
            }
        }

    }

    public boolean isEmpty() {
        for (ItemStack itemstack : this.inventory) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack1 : this.inventoryHands) {
            if (!itemstack1.isEmpty()) {
                return false;
            }
        }

        for (ItemStack itemstack2 : this.inventoryArmor) {
            if (!itemstack2.isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @NotNull
    @Override
    public ItemStack removeItem(int index, int count) {
        ItemStack s = getItem(index);
        return !s.isEmpty() && count > 0 ? s.split(count) : ItemStack.EMPTY;
    }

    @NotNull
    @Override
    public ItemStack removeItemNoUpdate(int index) {
        ItemStack s = getItem(index);
        if (!s.isEmpty()) {
            this.setItem(index, ItemStack.EMPTY);
        }
        return s;
    }

    @Override
    public void setChanged() {

    }

    @Override
    public void setItem(int index, @NotNull ItemStack stack) {
        assert index >= 0;
        if (index < 2) {
            inventoryHands.set(index, stack);
        } else if (index < 6) {
            inventoryArmor.set(index - 2, stack);
        } else if (index < 6 + availableSize) {
            inventory.set(index - 6, stack);
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return true;
    }

    public ListTag write(HolderLookup.Provider provider, @NotNull ListTag nbt) {
        for (int i = 0; i < this.inventoryHands.size(); i++) {
            if (!this.inventoryHands.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) i);
                nbt.add(this.inventoryHands.get(i).save(provider, compoundTag));
            }
        }

        for (int i = 0; i < this.inventoryArmor.size(); ++i) {
            if (!this.inventoryArmor.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) (i + 10));
                nbt.add(this.inventoryArmor.get(i).save(provider, compoundTag));

            }
        }

        for (int i = 0; i < this.inventory.size(); ++i) {
            if (!this.inventory.get(i).isEmpty()) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putByte("Slot", (byte) (i + 20));
                nbt.add(this.inventory.get(i).save(provider, compoundTag));
            }
        }
        return nbt;
    }
}
