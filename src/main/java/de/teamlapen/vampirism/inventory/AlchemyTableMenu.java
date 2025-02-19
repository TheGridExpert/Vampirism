package de.teamlapen.vampirism.inventory;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.core.ModMenus;
import de.teamlapen.vampirism.core.ModRecipes;
import de.teamlapen.vampirism.recipes.BrewingRecipeInput;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class AlchemyTableMenu extends AbstractContainerMenu {
    public static final int OIL_SLOT_1 = 0;
    public static final int OIL_SLOT_2 = 1;
    public static final int RESULT_SLOT_1 = 2;
    public static final int RESULT_SLOT_2 = 3;
    public static final int INGREDIENT_SLOT = 4;
    public static final int FUEL_SLOT = 5;
    private static final int INV_SLOT_START = 6;
    private static final int INV_SLOT_END = 33;
    private static final int USE_ROW_SLOT_START = 33;
    private static final int USE_ROW_SLOT_END = 42;
    private final @NotNull Container alchemyTable;
    private final @NotNull ContainerData alchemyTableData;
    private final @NotNull Slot ingredientSlot;

    public AlchemyTableMenu(int containerId, @NotNull Inventory playerInventory) {
        this(containerId, playerInventory.player.level(), playerInventory, new SimpleContainer(6), new SimpleContainerData(3));
    }

    public AlchemyTableMenu(int containerId, Level level, @NotNull Inventory playerInventory, @NotNull Container inventory, @NotNull ContainerData data) {
        super(ModMenus.ALCHEMICAL_TABLE.get(), containerId);
        checkContainerSize(inventory, 5);
        checkContainerDataCount(data, 3);
        this.alchemyTable = inventory;
        this.alchemyTableData = data;
        this.addSlot(new OilSlot(level, inventory, 0, 55, 16));
        this.addSlot(new OilSlot(level, inventory, 1, 79, 16));
        this.addSlot(new ResultSlot(inventory, 2, 112, 72));
        this.addSlot(new ResultSlot(inventory, 3, 140, 44));
        this.ingredientSlot = this.addSlot(new IngredientSlot(level, inventory, 4, 15, 25));
        this.addSlot(new FuelSlot(inventory, 5, 34, 69));
        this.addDataSlots(data);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInventory, j + i * 9 + 9, 8 + j * 18, 84 + 17 + i * 18));
            }
        }

        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInventory, k, 8 + k * 18, 142 + 17));
        }

    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.alchemyTable.stillValid(player);
    }

    public int getFuel() {
        return this.alchemyTableData.get(1);
    }

    public int getBrewingTicks() {
        return this.alchemyTableData.get(0);
    }

    public int getColor() {
        return this.alchemyTableData.get(2);
    }


    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotId < OIL_SLOT_1 || slotId > FUEL_SLOT) {
                if (FuelSlot.mayPlaceItem(itemstack)) {
                    if (this.moveItemStackTo(itemstack1, FUEL_SLOT, FUEL_SLOT + 1, false) || this.ingredientSlot.mayPlace(itemstack1) && !this.moveItemStackTo(itemstack1, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (this.ingredientSlot.mayPlace(itemstack1)) {
                    if (!this.moveItemStackTo(itemstack1, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (itemstack.getCount() == 1) {
                    if (!this.moveItemStackTo(itemstack1, OIL_SLOT_1, OIL_SLOT_2 + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotId >= INV_SLOT_START && slotId < INV_SLOT_END) {
                    if (!this.moveItemStackTo(itemstack1, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (slotId >= USE_ROW_SLOT_START && slotId < USE_ROW_SLOT_END) {
                    if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, INV_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, INV_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }


    static class OilSlot extends Slot {

        private final Level world;

        public OilSlot(Level worldPos, @NotNull Container inventory, int slotId, int xPos, int yPos) {
            super(inventory, slotId, xPos, yPos);
            this.world = worldPos;
        }

        public static boolean mayPlaceItem(@NotNull Level world, @NotNull ItemStack itemstack) {
            return VampirismMod.proxy.recipeMap(world).byType(ModRecipes.ALCHEMICAL_TABLE_TYPE.get()).stream().anyMatch(recipe -> recipe.value().isIngredient(itemstack));
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return mayPlaceItem(world, stack);
        }

        public int getMaxStackSize() {
            return 1;
        }
    }

    static class IngredientSlot extends Slot {
        private final Level world;

        public IngredientSlot(Level worldPos, @NotNull Container inventory, int slotId, int xPos, int yPos) {
            super(inventory, slotId, xPos, yPos);
            this.world = worldPos;

        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return true;
        }

        public int getMaxStackSize() {
            return 64;
        }
    }

    static class FuelSlot extends Slot {
        public FuelSlot(@NotNull Container inventory, int slotId, int xPos, int yPos) {
            super(inventory, slotId, xPos, yPos);
        }

        public boolean mayPlace(@NotNull ItemStack stack) {
            return mayPlaceItem(stack);
        }

        public static boolean mayPlaceItem(@NotNull ItemStack stack) {
            return stack.getItem() == Items.BLAZE_POWDER;
        }

        public int getMaxStackSize() {
            return 64;
        }
    }

    static class ResultSlot extends Slot {

        public ResultSlot(@NotNull Container inventory, int slotId, int xPos, int yPos) {
            super(inventory, slotId, xPos, yPos);
        }

        @Override
        public boolean mayPlace(@NotNull ItemStack stack) {
            return false;
        }
    }
}
