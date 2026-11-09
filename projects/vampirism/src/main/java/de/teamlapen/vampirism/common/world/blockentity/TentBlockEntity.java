package de.teamlapen.vampirism.common.world.blockentity;

import de.teamlapen.vampirism.api.VEnums;
import de.teamlapen.vampirism.common.config.ModConfig;
import de.teamlapen.vampirism.common.core.ModBlockEntities;
import de.teamlapen.vampirism.common.server.SimpleSpawnerLogic;
import de.teamlapen.vampirism.common.util.UtilLib;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * spawns hunters for tents
 */
public class TentBlockEntity extends BlockEntity {


    private final SimpleSpawnerLogic<Hunter> spawnerLogicHunter;
    private boolean spawn = false;
    private boolean advanced = false;

    public TentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TENT.get(), pos, state);
        this.spawnerLogicHunter = new SimpleSpawnerLogic<>(Hunter.class, Hunter::randomType).setActivateRange(64).setSpawnRange(6).setMinSpawnDelay(600).setMaxSpawnDelay(1000).setMaxNearbyEntities(2).setDailyLimit(ModConfig.balance().hunterTentMaxSpawn.get()).setLimitTotalEntities(VEnums.HUNTER_CATEGORY.getValue()).setOnSpawned(hunter -> hunter.makeCampHunter(this.worldPosition));
    }

    public boolean isSpawner() {
        return spawn;
    }

    @Override
    public void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.spawnerLogicHunter.deserialize(input.childOrEmpty("spawner_logic_1"));
        this.advanced = input.getBooleanOr("advanced", false);
        this.spawn = input.getBooleanOr("spawn", false);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        this.spawnerLogicHunter.serialize(output.child("spawner_logic_1"));
        output.putBoolean("spawn", this.spawn);
        output.putBoolean("advanced", this.advanced);
    }

    public void setAdvanced(boolean advanced) {
        this.advanced = advanced;
    }


    public void setSpawn(boolean spawn) {
        this.spawn = spawn;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, TentBlockEntity blockEntity) {
        if (blockEntity.spawn) {
            if (level.getGameTime() % 64 == 0) {
                if (UtilLib.isInsideStructure(level, pos, StructureTags.VILLAGE)) {
                    blockEntity.spawn = false; //Disable spawning inside villages
                }
            }
            blockEntity.spawnerLogicHunter.serverTick(level, pos);
        }
    }

    public boolean triggerEvent(int id, int type) {
        return this.spawnerLogicHunter.setDelayToMin(id, this.level) || super.triggerEvent(id, type);
    }
}
