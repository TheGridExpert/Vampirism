package de.teamlapen.vampirism.inventory;

import de.teamlapen.lib.lib.inventory.InventoryHelper;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.ModMenus;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.hunter.HunterTrainerEntity;
import de.teamlapen.vampirism.entity.player.hunter.HunterLeveling;
import de.teamlapen.vampirism.mixin.accessor.ItemCombinerMenuAccessor;
import net.minecraft.world.Container;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ItemCombinerMenu;
import net.minecraft.world.inventory.ItemCombinerMenuSlotDefinition;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * Container which handles hunter levelup at a hunter trainer
 */
public class HunterTrainerMenu extends ItemCombinerMenu {

    private final @NotNull Player player;
    private final @Nullable HunterTrainerEntity entity;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private @NotNull Optional<HunterLeveling.HunterTrainerRequirement> lvlRequirement;

    @SuppressWarnings("DeprecatedIsStillUsed")
    @Deprecated
    public HunterTrainerMenu(int id, @NotNull Inventory playerInventory) {
        this(id, playerInventory, null);
    }

    public HunterTrainerMenu(int id, @NotNull Inventory playerInventory, @Nullable HunterTrainerEntity trainer) {
        super(ModMenus.HUNTER_TRAINER.get(), id, playerInventory, trainer == null ? ContainerLevelAccess.NULL : ContainerLevelAccess.create(trainer.level(), trainer.blockPosition()), createInputSlotDefinitions(playerInventory.player));
        this.player = playerInventory.player;
        this.entity = trainer;
        this.lvlRequirement = HunterLeveling.getTrainerRequirement(FactionPlayerHandler.get(player).getCurrentLevel(ModFactions.HUNTER) + 1);
    }

    @Override
    protected boolean mayPickup(@NotNull Player pPlayer, boolean pHasStack) {
        return true;
    }

    @Override
    protected void onTake(@NotNull Player player, @NotNull ItemStack stack) {
    }

    @Override
    protected boolean isValidBlock(@NotNull BlockState pState) {
        return true;
    }

    @Override
    public void createResult() {
    }

    protected static @NotNull ItemCombinerMenuSlotDefinition createInputSlotDefinitions(Player player) {
        var lvlRequirement = HunterLeveling.getTrainerRequirement(FactionPlayerHandler.get(player).getCurrentLevel(ModFactions.HUNTER) + 1);
        return ModifiedItemCombinerMenuSlotDefinition.createWithoutResult()
                .withSlot(0, 27, 26, stack -> lvlRequirement.filter(req -> req.ironQuantity() > 0).isPresent() && stack.is(Items.IRON_INGOT))
                .withSlot(1, 57, 26, stack -> lvlRequirement.filter(req -> req.goldQuantity() > 0).isPresent() && stack.is(Items.GOLD_INGOT))
                .withSlot(2, 86, 26, stack -> lvlRequirement.map(req -> req.tableRequirement().resultIntelItem().get()).filter(stack::is).isPresent())
                .build();
    }

    @Override
    public void createResultSlot(@NotNull ItemCombinerMenuSlotDefinition definition) {

    }


    /**
     * @return If the player can levelup with the given tileInventory
     */
    public boolean canLevelup() {
        return this.lvlRequirement.map(req -> req.ironQuantity() <= getInputSlots().countItem(Items.IRON_INGOT) && req.goldQuantity() <= getInputSlots().countItem(Items.GOLD_INGOT) && getInputSlots().countItem(req.tableRequirement().resultIntelItem().get()) >= 1).orElse(false);
    }

    public Optional<HunterLeveling.HunterTrainerRequirement> getRequirement() {
        return lvlRequirement;
    }


    /**
     * Called via input packet, when the player clicks the levelup button.
     */
    public void onLevelupClicked() {
        if (canLevelup()) {
            this.lvlRequirement.ifPresent(req -> {
                FactionPlayerHandler.get(this.player).setFactionLevel(ModFactions.HUNTER, req.targetLevel());
                InventoryHelper.removeItems(getInputSlots(), req.ironQuantity(), req.goldQuantity(), 1);
                this.player.addEffect(new MobEffectInstance(ModEffects.SATURATION, 400, 2));
                this.lvlRequirement = HunterLeveling.getTrainerRequirement(req.targetLevel() + 1);
            });
        }
    }

    private Container getInputSlots() {
        return ((ItemCombinerMenuAccessor) this).getInputSlots();
    }


    @Override
    public boolean stillValid(@NotNull Player player) {
        if (this.entity == null) return false;
        return new Vec3(player.getX(), player.getY(), player.getZ()).distanceToSqr(new Vec3(this.entity.getX(), this.entity.getY(), this.entity.getZ())) < 64;
    }

}
