package de.teamlapen.vampirism.inventory;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.core.ModMenus;
import de.teamlapen.vampirism.core.ModRecipes;
import de.teamlapen.vampirism.recipes.AlchemicalCauldronRecipe;
import de.teamlapen.vampirism.recipes.AlchemicalCauldronRecipeInput;
import de.teamlapen.vampirism.recipes.ITestableRecipeInput;
import net.minecraft.recipebook.ServerPlaceRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedItemContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;


public class AlchemicalCauldronMenu extends RecipeBookMenu {

    static final ResourceLocation EMPTY_SLOT_BOTTLE = ResourceLocation.withDefaultNamespace("container/slot/potion");

    public static final int FLUID_SLOT = 0;
    public static final int INGREDIENT_SLOT = 1;
    public static final int RESULT_SLOT = 2;
    public static final int FUEL_SLOT = 3;
    public static final int SLOT_COUNT = 4;
    public static final int DATA_COUNT = 4;

    private static final int INVENTORY_SLOT_START = 4;
    private static final int INVENTORY_SLOT_END = 31;
    private static final int USE_ROW_SLOT_START = 31;
    private static final int USE_ROW_SLOT_END = 40;

    private final Container container;
    private final ContainerData data;
    private final Level level;
    private final RecipeType<? extends AlchemicalCauldronRecipe> recipeType;
    private final RecipeBookType recipeBookType;

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public AlchemicalCauldronMenu(int id, @NotNull Inventory playerInventory) {
        this(id, playerInventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public AlchemicalCauldronMenu(int id, @NotNull Inventory playerInventory, @NotNull Container container, @NotNull ContainerData data) {
        super(ModMenus.ALCHEMICAL_CAULDRON.get(), id);

        this.recipeType = ModRecipes.ALCHEMICAL_CAULDRON_TYPE.get();
        this.recipeBookType = RecipeBookType.FURNACE;

        checkContainerSize(container, SLOT_COUNT - 1);
        checkContainerDataCount(data, DATA_COUNT);

        this.container = container;
        this.data = data;
        this.level = playerInventory.player.level();

        addSlot(new Slot(container, FLUID_SLOT, 44, 17) {
            @Override
            public ResourceLocation getNoItemIcon() {
                return EMPTY_SLOT_BOTTLE;
            }
        });
        addSlot(new Slot(container, INGREDIENT_SLOT, 68, 17));
        addSlot(new FurnaceResultSlot(playerInventory.player, container, RESULT_SLOT, 116, 35));
        addSlot(new FurnaceFuelSlot(this, container, FUEL_SLOT, 56, 53));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int i = 0; i < 9; i++) {
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }

        addDataSlots(data);
    }

    @Override
    public PostPlaceAction handlePlacement(boolean useMaxItems, boolean isCreative, RecipeHolder<?> recipe, ServerLevel level, Inventory playerInventory) {
        List<Slot> slots = List.of(this.getSlot(FLUID_SLOT), this.getSlot(INGREDIENT_SLOT), this.getSlot(RESULT_SLOT));
        return ServerPlaceRecipe.placeRecipe(new ServerPlaceRecipe.CraftingMenuAccess<>() {
            @Override
            public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
                AlchemicalCauldronMenu.this.fillCraftSlotsStackedContents(stackedItemContents);
            }

            @Override
            public void clearCraftingContent() {
                slots.forEach(slot -> slot.set(ItemStack.EMPTY));
            }

            @Override
            public boolean recipeMatches(RecipeHolder<AlchemicalCauldronRecipe> recipe) {
                return recipe.value().matches(new AlchemicalCauldronRecipeInput(AlchemicalCauldronMenu.this.container.getItem(INGREDIENT_SLOT), AlchemicalCauldronMenu.this.container.getItem(FLUID_SLOT)), level);
            }
        }, 1, 1, List.of(this.getSlot(FLUID_SLOT), this.getSlot(INGREDIENT_SLOT)), slots, playerInventory, (RecipeHolder<AlchemicalCauldronRecipe>) recipe, useMaxItems, isCreative);
    }

    @Override
    public void fillCraftSlotsStackedContents(StackedItemContents stackedItemContents) {
        if (this.container instanceof StackedContentsCompatible) {
            ((StackedContentsCompatible) this.container).fillStackedContents(stackedItemContents);
        }
    }

    public Optional<RecipeHolder<AlchemicalCauldronRecipe>> checkRecipeNoSkills() {
        return this.level.recipeAccess() instanceof RecipeManager manager ? manager.getRecipeFor(getCastRecipeType(), new AlchemicalCauldronRecipeInput(this.container.getItem(INGREDIENT_SLOT), this.container.getItem(FLUID_SLOT), ITestableRecipeInput.TestType.BOTH), this.level) : Optional.empty();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);

        if (slots.size() > index && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index == FUEL_SLOT) {
                if (!moveItemStackTo(stack, INVENTORY_SLOT_START, USE_ROW_SLOT_END, true)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(stack, result);
            } else if (index != FLUID_SLOT && index != INGREDIENT_SLOT && index != RESULT_SLOT) {
                boolean asFluid = canSmeltAsFluid(stack) && !moveItemStackTo(stack, FLUID_SLOT, FLUID_SLOT + 1, false);
                boolean asIngredient = canSmeltAsIngredient(stack) && !moveItemStackTo(stack, INGREDIENT_SLOT, INGREDIENT_SLOT + 1, false);

                if (asFluid || asIngredient) return ItemStack.EMPTY;

                if (isFuel(stack)) {
                    if (!moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= INVENTORY_SLOT_START && index < INVENTORY_SLOT_END) {
                    if (!moveItemStackTo(stack, USE_ROW_SLOT_START, USE_ROW_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (index >= INVENTORY_SLOT_END && index < USE_ROW_SLOT_END) {
                    if (!moveItemStackTo(stack, INVENTORY_SLOT_START, INVENTORY_SLOT_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            } else if (!moveItemStackTo(stack, INVENTORY_SLOT_START, USE_ROW_SLOT_END, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) slot.setByPlayer(ItemStack.EMPTY);
            else slot.setChanged();

            if (stack.getCount() == result.getCount()) return ItemStack.EMPTY;
            slot.onTake(player, stack);
        }

        return result;
    }

    protected boolean canSmeltAsIngredient(ItemStack pStack) {
        return VampirismMod.proxy.recipeMap(this.level).getRecipesFor(getCastRecipeType(), new AlchemicalCauldronRecipeInput(pStack, this.container.getItem(FLUID_SLOT), ITestableRecipeInput.TestType.INPUT_1), this.level).findAny().isPresent();
    }

    protected boolean canSmeltAsFluid(ItemStack pStack) {
        return VampirismMod.proxy.recipeMap(this.level).getRecipesFor(getCastRecipeType(), new AlchemicalCauldronRecipeInput(this.container.getItem(INGREDIENT_SLOT), pStack, ITestableRecipeInput.TestType.INPUT_2), this.level).findAny().isPresent();
    }

    protected boolean isFuel(ItemStack pStack) {
        return pStack.getBurnTime(this.recipeType, level.fuelValues()) > 0;
    }

    public float getBurnProgress() {
        int burnTime = data.get(2);
        int totalBurnTime = data.get(3);
        return totalBurnTime > 0 ? Mth.clamp((float) burnTime / totalBurnTime, 0.0F, 1.0F) : 0.0F;
    }

    public float getLitProgress() {
        int litTime = data.get(0);
        int totalLitTime = data.get(1) == 0 ? 200 : data.get(1);
        return Mth.clamp((float) litTime / totalLitTime, 0.0F, 1.0F);
    }

    public boolean isLit() {
        return this.data.get(0) > 0;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return this.recipeBookType;
    }

    @SuppressWarnings("unchecked")
    public RecipeType<AlchemicalCauldronRecipe> getCastRecipeType() {
        return (RecipeType<AlchemicalCauldronRecipe>) recipeType;
    }

    public static class FurnaceFuelSlot extends Slot {

        private final AlchemicalCauldronMenu menu;

        public FurnaceFuelSlot(AlchemicalCauldronMenu cauldronMenu, Container container, int slot, int x, int y) {
            super(container, slot, x, y);
            this.menu = cauldronMenu;
        }

        /**
         * Check if the stack is allowed to be placed in this slot, used for armor slots as well as furnace fuel.
         */
        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.menu.isFuel(stack) || isBucket(stack);
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return isBucket(stack) ? 1 : super.getMaxStackSize(stack);
        }

        public static boolean isBucket(ItemStack stack) {
            return stack.is(Items.BUCKET);
        }
    }
}
