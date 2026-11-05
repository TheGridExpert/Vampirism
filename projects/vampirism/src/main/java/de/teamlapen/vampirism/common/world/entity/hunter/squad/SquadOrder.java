package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class SquadOrder {

    private @Nullable LivingEntity target;
    private @Nullable Vec3 position;
    private boolean retreat;
    private boolean groupRetreat;

    public SquadOrder target(@Nullable LivingEntity target) {
        this.target = target;
        return this;
    }

    public SquadOrder position(@Nullable Vec3 position) {
        this.position = position;
        return this;
    }

    public SquadOrder retreat() {
        this.retreat = true;
        return this;
    }

    public SquadOrder groupRetreat() {
        this.groupRetreat = true;
        return this;
    }

    public @Nullable LivingEntity getTarget() {
        return this.target;
    }

    public @Nullable Vec3 getPosition() {
        return this.position;
    }

    public boolean isRetreat() {
        return this.retreat;
    }

    public boolean isGroupRetreat() {
        return this.groupRetreat;
    }
}
