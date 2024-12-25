package de.teamlapen.vampirism.entity.ai.goals;

import de.teamlapen.vampirism.api.entity.hunter.IHunterMob;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class HunterHurtByTargetGoal extends HurtByTargetGoal {
    public HunterHurtByTargetGoal(PathfinderMob mob) {
        super(mob, IHunterMob.class);
    }

    /**
     * Spreads the alert to all hunters, not just those of the same type as the attacked one. So that advanced hunters also get angry when the ordinary one is attacked and vice versa.
     */
    @Override
    protected void alertOthers() {
        double radius = Math.max(this.getFollowDistance(), 14);
        if (this.mob.getLastHurtByMob() instanceof IVampire) radius *= 1.5;
        AABB aabb = AABB.unitCubeFromLowerCorner(this.mob.position()).inflate(radius, 10.0, radius);
        List<? extends Mob> list = this.mob.level().getEntitiesOfClass(Mob.class, aabb, EntitySelector.NO_SPECTATORS);

        for (Mob mob : list) {
            if (mob != this.mob && mob instanceof IHunterMob && mob.getTarget() == null && this.mob.getLastHurtByMob() != null && !mob.isAlliedTo(this.mob.getLastHurtByMob())) {
                this.alertOther(mob, this.mob.getLastHurtByMob());
            }
        }
    }
}
