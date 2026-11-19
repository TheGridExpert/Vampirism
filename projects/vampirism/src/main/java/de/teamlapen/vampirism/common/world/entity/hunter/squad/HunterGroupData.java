package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterClassType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.SpawnGroupData;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class HunterGroupData implements SpawnGroupData {

    private final UUID squadId = UUID.randomUUID();
    private int size;
    private boolean hasAdvanced;

    public static HunterGroupData resolve(@Nullable SpawnGroupData spawnGroupData) {
        return spawnGroupData instanceof HunterGroupData data && data.hasRoom() ? data : new HunterGroupData();
    }

    public boolean claimAdvanced() {
        if (this.hasAdvanced) return false;
        this.hasAdvanced = true;
        return true;
    }

    public boolean hasRoom() {
        return this.size < Hunter.BASE_SQUAD_SIZE + (this.hasAdvanced ? 1 : 0);
    }

    public UUID squadId() {
        return this.squadId;
    }

    public HunterClassType nextClass(RandomSource random) {
        this.size++;
        return HunterClassType.getRandom(random);
    }
}
