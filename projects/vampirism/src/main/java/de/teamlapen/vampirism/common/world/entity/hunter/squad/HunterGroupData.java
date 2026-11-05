package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.world.entity.hunter.HunterClassType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.SpawnGroupData;

import java.util.UUID;

public final class HunterGroupData implements SpawnGroupData {

    private static final int MELEE_SHARE = 5;
    private static final int RANGED_SHARE = 4;

    private final UUID squadId = UUID.randomUUID();
    private int melee;
    private int ranged;

    public boolean hasRoom() {
        return this.melee + this.ranged < HunterSquad.MAX_SIZE;
    }

    public UUID squadId() {
        return this.squadId;
    }

    public HunterClassType nextClass(RandomSource random) {
        HunterClassType type;
        if (this.melee + this.ranged == 0) {
            type = HunterClassType.getRandom(random);
        } else {
            type = (this.melee + 1) * RANGED_SHARE <= (this.ranged + 1) * MELEE_SHARE ? HunterClassType.MELEE : HunterClassType.RANGED;
        }

        if (type == HunterClassType.RANGED) {
            this.ranged++;
        } else {
            this.melee++;
        }
        return type;
    }
}
