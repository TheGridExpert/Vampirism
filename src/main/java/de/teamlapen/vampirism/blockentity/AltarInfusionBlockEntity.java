package de.teamlapen.vampirism.blockentity;

import de.teamlapen.lib.lib.inventory.InventoryHelper;
import de.teamlapen.vampirism.advancements.critereon.VampireActionCriterionTrigger;
import de.teamlapen.vampirism.blocks.AltarPillarBlock;
import de.teamlapen.vampirism.blocks.AltarTipBlock;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.vampire.VampireLeveling;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.DrinkBloodContext;
import de.teamlapen.vampirism.inventory.AltarInfusionMenu;
import de.teamlapen.vampirism.items.PureBloodItem;
import de.teamlapen.vampirism.particle.FlyingBloodParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class AltarInfusionBlockEntity extends BaseContainerBlockEntity {

    public static final String KEY_PLAYER_UUID = "PlayerUUID";
    public static final String KEY_RUN_TIME = "RunTime";

    public static final int DURATION_TICK = 450;
    public static final int MAX_PILLARS = 9;
    public static final int RISING_TICKS = 60;
    public static final float MAX_SPHERE_RITUAL_HEIGHT = 2.25F;

    private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private @Nullable Player player;
    private @Nullable UUID playerToLoadUUID;
    private List<BlockPos> tips;
    private int runTime;
    private int targetLevel;
    public int animationTime;
    public float rotation = 0.0F;
    public float prevRotation = 0.0F;
    public float targetRotation = 0.0F;
    public float verticalOffset = 0.0F;
    public int runningTicks = 0;
    public int stoppingTicks = 0;
    public float startHeight = 0.05F;

    public AltarInfusionBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALTAR_INFUSION.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("tile.vampirism.altar_infusion");
    }

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    public Result tryActivate(Player player) {
        if (isRunning()) return Result.STILL_RUNNING;

        this.player = null;
        this.targetLevel = VampirismPlayerAttributes.get(player).vampireLevel + 1;

        if (checkRequiredLevel() == -1) return Result.LEVEL_WRONG;
        if (player.level().isDay()) return Result.NIGHT_ONLY;
        if (!checkStructureLevel(checkRequiredLevel())) return Result.MISSING_PILLARS;
        if (!checkItemRequirements()) return Result.MISSING_ITEMS;

        return Result.SUCCESS;
    }

    private int checkRequiredLevel() {
        return VampireLeveling.getInfusionRequirement(this.targetLevel).map(VampireLeveling.AltarInfusionRequirements::getRequiredStructurePoints).orElse(-1);
    }

    private boolean checkStructureLevel(int required) {
        if (this.level == null) return false;

        List<ValuedPos> valuedTips = Arrays.stream(findTips())
                .map(tip -> {
                    int height = 0;
                    double totalValue = 0;

                    while (height < 3) {
                        BlockState stateBelow = this.level.getBlockState(tip.below(height + 1));
                        if (!stateBelow.is(ModBlocks.ALTAR_PILLAR.get())) break;

                        AltarPillarBlock.EnumPillarType type = stateBelow.getValue(AltarPillarBlock.PILLAR_TYPE);
                        totalValue += type.getValue();
                        height++;
                    }

                    int value = (int) (10 * totalValue);
                    return new ValuedPos(tip, value);
                })
                .sorted(Comparator.comparingInt(ValuedPos::value).reversed())
                .limit(MAX_PILLARS)
                .toList();

        int sum = valuedTips.stream().mapToInt(ValuedPos::value).sum();
        this.tips = valuedTips.stream().map(ValuedPos::pos).toList();

        return sum >= required * 10;
    }

    private BlockPos[] findTips() {
        if (this.level == null) return new BlockPos[0];

        List<BlockPos> tips = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        BlockPos origin = this.worldPosition;
        for (int x = -5; x <= 5; x++) {
            for (int y = 1; y <= 3; y++) {
                for (int z = -5; z <= 5; z++) {
                    pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                    if (this.level.getBlockState(pos).getBlock() instanceof AltarTipBlock) {
                        tips.add(pos.immutable());
                    }
                }
            }
        }

        return tips.toArray(BlockPos[]::new);
    }

    private boolean checkItemRequirements() {
        return VampireLeveling.getInfusionRequirement(targetLevel)
                .map(requirements -> {
                    ItemStack missing = InventoryHelper.checkItems(this,
                            new Item[] {
                                    PureBloodItem.getBloodItemForLevel(requirements.pureBloodLevel()),
                                    ModItems.HUMAN_HEART.get(),
                                    ModItems.VAMPIRE_BOOK.get()
                            },
                            new int[] {
                                    requirements.pureBloodQuantity(),
                                    requirements.humanHeartQuantity(),
                                    requirements.vampireBookQuantity()
                            },
                            (supplied, required) ->
                                    supplied.equals(required)
                                            || required == Items.AIR
                                            || (supplied instanceof PureBloodItem suppliedItem && required instanceof PureBloodItem requiredItem && suppliedItem.getLevel(supplied.getDefaultInstance()) >= requiredItem.getLevel(required.getDefaultInstance()))
                    );

                    return missing.isEmpty();
                })
                .orElse(false);
    }

    public void startRitual(Player player) {
        if (this.level == null) return;

        this.player = player;
        this.runTime = DURATION_TICK;
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, DURATION_TICK, MobEffectInstance.MAX_AMPLIFIER, false, false));

        // TODO: Currently, if the player exits the game, it just stops playing. Minecraft's sound engine is trash, think of some way to make this work
        this.level.playSound(null, this.worldPosition, ModSounds.SPHERE_SPINNING.get(), SoundSource.BLOCKS, 0.5f, 1.0f);

        if (!this.tips.isEmpty()) {
            for (BlockPos tip : this.tips) {
                ModParticles.spawnParticlesServer(this.level, new FlyingBloodParticleOptions(60, false, tip.getX() + 0.5, tip.getY() + 0.3, tip.getZ() + 0.5), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5, 3, 0.1, 0.1, 0.1, 0);
            }
        }

        updateClient();
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, AltarInfusionBlockEntity blockEntity) {
        if (blockEntity.playerToLoadUUID != null && blockEntity.loadRitual(blockEntity.playerToLoadUUID)) {
            blockEntity.playerToLoadUUID = null;
            blockEntity.updateClient();
        }

        if (blockEntity.runTime == DURATION_TICK && !level.isClientSide) {
            blockEntity.consumeItems();
            blockEntity.setChanged();
        }

        if (blockEntity.isRunning()) {
            blockEntity.runTime--;
            blockEntity.tickRitual();
            if (!blockEntity.isRunning()) {
                blockEntity.updateClient();
                blockEntity.setChanged();
            }
        }

        if (level.isClientSide) {
            sphereAnimationTick(blockEntity);
        }
    }

    private void tickRitual() {
        if (this.player == null || !this.player.isAlive()) {
            this.runTime = 1;
            return;
        }

        stopPlayerMovement(this.player);

        Phase phase = getCurrentPhase();

        if (this.level != null && this.level.isClientSide) {
            handleClientEffects(phase);
        }
        if (phase == Phase.LEVELUP) {
            handleLevelUp();
        }
        if (phase == Phase.CLEAN_UP) {
            endRitual();
        }
    }

    private static void stopPlayerMovement(Player player) {
        player.setDeltaMovement(0, 0, 0);
    }

    private void handleClientEffects(Phase phase) {
        if (phase == Phase.PARTICLE_SPREAD && this.runTime % 15 == 0 && tips != null) {
            BlockPos pos = this.worldPosition;
            RandomSource random = RandomSource.create();

            for (BlockPos tip : this.tips) {
                ModParticles.spawnParticlesClient(this.level, new FlyingBloodParticleOptions(60, false, tip.getX() + 0.5, tip.getY() + 0.3, tip.getZ() + 0.5), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 0, 0, 0, 5, 0.1, random);
            }
        }

        if (this.runTime == DURATION_TICK - 200 && this.player != null && this.player.isLocalPlayer()) {
            VampirismModClient.getInstance().getOverlay().makeRenderFullColor(DURATION_TICK - 250, 50, 0xFF0000);
        }
    }

    private void handleLevelUp() {
        if (this.level != null && this.level.isClientSide) {
            playLevelUpEffects();
            return;
        }

        if (this.player == null) return;

        FactionPlayerHandler handler = FactionPlayerHandler.get(this.player);
        if (handler.getCurrentLevel(ModFactions.VAMPIRE) != this.targetLevel - 1) return;

        handler.setFactionLevel(ModFactions.VAMPIRE, this.targetLevel);
        VampirePlayer.get(this.player).drinkBlood(Integer.MAX_VALUE, 0, false, DrinkBloodContext.none());

        if (this.player instanceof ServerPlayer serverPlayer) {
            ModAdvancements.TRIGGER_VAMPIRE_ACTION.get().trigger(serverPlayer, VampireActionCriterionTrigger.Action.PERFORM_RITUAL_INFUSION);
        }

        applyPostRitualEffects();
    }

    private void playLevelUpEffects() {
        if (this.level == null || this.player == null) return;

        this.player.playNotifySound(ModSounds.CHOIR_SHORT.get(), SoundSource.PLAYERS, 0.5f, 1.0f + (this.level.random.nextFloat() - 0.5f) / 5.0f);
    }

    private void applyPostRitualEffects() {
        if (this.player == null) return;

        this.player.addEffect(new MobEffectInstance(ModEffects.SATURATION, 400, 2));
        this.player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400, 2));
        this.player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 2));
    }

    private void endRitual() {
        this.player = null;
        this.tips = null;
        this.runTime = 0;
        updateClient();
        setChanged();
    }

    private void consumeItems() {
        VampireLeveling.getInfusionRequirement(targetLevel).ifPresent(requirements -> InventoryHelper.removeItems(this, requirements.pureBloodQuantity(), requirements.humanHeartQuantity(), requirements.vampireBookQuantity()));
    }

    private boolean loadRitual(UUID playerID) {
        if (this.level == null || this.level.players().isEmpty()) return false;

        this.player = this.level.getPlayerByUUID(playerID);
        if (this.player != null && this.player.isAlive()) {
            this.targetLevel = VampirismPlayerAttributes.get(player).vampireLevel + 1;
            checkStructureLevel(checkRequiredLevel());

            return true;
        }

        this.runTime = 0;
        this.tips = null;

        return false;
    }

    public static void sphereAnimationTick(AltarInfusionBlockEntity blockEntity) {
        blockEntity.prevRotation = blockEntity.rotation;

        boolean running = blockEntity.isRunning();

        float spinSpeed = (running || blockEntity.stoppingTicks < RISING_TICKS) ? 0.4F : 0.025F;
        blockEntity.targetRotation += spinSpeed;

        while (blockEntity.rotation >= (float) Math.PI) {
            blockEntity.rotation -= (float) (Math.PI * 2);
        }
        while (blockEntity.rotation < (float) -Math.PI) {
            blockEntity.rotation += (float) (Math.PI * 2);
        }
        while (blockEntity.targetRotation >= (float) Math.PI) {
            blockEntity.targetRotation -= (float) (Math.PI * 2);
        }
        while (blockEntity.targetRotation < (float) -Math.PI) {
            blockEntity.targetRotation += (float) (Math.PI * 2);
        }

        float rotationDifference = blockEntity.targetRotation - blockEntity.rotation;

        while (rotationDifference >= (float) Math.PI) {
            rotationDifference -= (float) (Math.PI * 2);
        }
        while (rotationDifference < (float) -Math.PI) {
            rotationDifference += (float) (Math.PI * 2);
        }

        blockEntity.rotation += rotationDifference * 0.4F;

        blockEntity.animationTime++;

        float bobbingHeight = (float) (Math.sin(blockEntity.animationTime * 0.1F) * 0.05F + 0.05F);

        if (running) {
            blockEntity.stoppingTicks = 0;

            if (blockEntity.runningTicks == 0) {
                blockEntity.startHeight = bobbingHeight;
            }

            if (blockEntity.runningTicks < RISING_TICKS) {
                blockEntity.runningTicks++;
                float progress = (float) blockEntity.runningTicks / RISING_TICKS;
                blockEntity.verticalOffset = blockEntity.startHeight + (MAX_SPHERE_RITUAL_HEIGHT - blockEntity.startHeight) * progress;
            } else {
                blockEntity.verticalOffset = MAX_SPHERE_RITUAL_HEIGHT;
            }
        } else {
            blockEntity.runningTicks = 0;

            if (blockEntity.stoppingTicks == 0) {
                blockEntity.startHeight = blockEntity.verticalOffset;
            }

            if (blockEntity.stoppingTicks < RISING_TICKS) {
                blockEntity.stoppingTicks++;
                float progress = (float) blockEntity.stoppingTicks / RISING_TICKS;
                blockEntity.verticalOffset = blockEntity.startHeight + (bobbingHeight - blockEntity.startHeight) * progress;
            } else {
                blockEntity.verticalOffset = bobbingHeight;
            }
        }
    }

    private void updateClient() {
        if (this.level == null) return;

        BlockState state = this.level.getBlockState(this.worldPosition);
        this.level.sendBlockUpdated(this.worldPosition, state, state, Block.UPDATE_ALL);
    }

    public Phase getCurrentPhase() {
        if (this.runTime < 1) return Phase.NOT_RUNNING;
        if (this.runTime == 1) return Phase.CLEAN_UP;
        if (this.runTime > DURATION_TICK - 100) return Phase.PARTICLE_SPREAD;
        if (this.runTime >= DURATION_TICK - 200 && this.runTime < DURATION_TICK - 160) return Phase.BEAM_CONNECT;
        if (this.runTime <= DURATION_TICK - 200 && this.runTime > 50) return Phase.BEAM_PLAYER;
        if (this.runTime == 50) return Phase.LEVELUP;
        if (this.runTime < 50) return Phase.ENDING;
        return Phase.WAITING;
    }

    private boolean isRunning() {
        return this.runTime > 0;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(isRunning() ? this.player : null);
    }

    public List<BlockPos> getTips() {
        return tips == null ? List.of() : tips;
    }

    public int getRunTime() {
        return this.runTime;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.loadAdditional(tag, lookup);
        ContainerHelper.loadAllItems(tag, this.items, lookup);
        this.runTime = tag.getInt(KEY_RUN_TIME);
        //This is used on both client and server side and has to be prepared for the world not being available yet
        if (isRunning() && tag.hasUUID(KEY_PLAYER_UUID)) {
            UUID id = tag.getUUID(KEY_PLAYER_UUID);
            if (!loadRitual(id)) {
                this.playerToLoadUUID = id;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider lookup) {
        super.saveAdditional(tag, lookup);
        ContainerHelper.saveAllItems(tag, this.items, lookup);
        tag.putInt(KEY_RUN_TIME, this.runTime);
        if (this.player != null) {
            tag.putUUID(KEY_PLAYER_UUID, this.player.getUUID());
        }
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider lookup) {
        return this.saveWithoutMetadata(lookup);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider lookupProvider) {
        if (this.hasLevel()) this.loadCustomOnly(pkt.getTag(), lookupProvider);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return AltarInfusionMenu.SLOT_PREDICATES.get(slot).test(stack);
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AltarInfusionMenu(containerId, inventory, this);
    }

    public enum Phase {
        NOT_RUNNING, PARTICLE_SPREAD, BEAM_CONNECT, BEAM_PLAYER, WAITING, LEVELUP, ENDING, CLEAN_UP
    }

    public enum Result implements StringRepresentable {
        SUCCESS("success"),
        STILL_RUNNING("still_running"),
        LEVEL_WRONG("level_wrong"),
        NIGHT_ONLY("night_only"),
        MISSING_PILLARS("missing_pillars"),
        MISSING_ITEMS("missing_items");

        public final String name;

        Result(String name) {
            this.name = name;
        }

        public Component getMessage() {
            return Component.translatable("text.vampirism.altar_infusion.ritual." + name);
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public record ValuedPos(BlockPos pos, int value) {}
}
