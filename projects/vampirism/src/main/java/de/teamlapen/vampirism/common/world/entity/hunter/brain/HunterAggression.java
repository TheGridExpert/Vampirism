package de.teamlapen.vampirism.common.world.entity.hunter.brain;

import de.teamlapen.vampirism.common.tags.ModEntityTags;
import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class HunterAggression {

    public static boolean targetsHunters(Mob mob) {
        if (mob instanceof Hunter || mob.is(ModEntityTags.HUNTER)) return false;
        if (HunterThreats.isExplosive(mob) || HunterThreats.isAvoided(mob)) return false;

        return mob instanceof Monster || Helper.isVampire(mob);
    }

    public static @Nullable LivingEntity findCloserHunter(ServerLevel level, Mob mob, TargetingConditions conditions, AABB searchArea, @Nullable LivingEntity current) {
        if (!targetsHunters(mob)) return current;

        Hunter hunter = level.getNearestEntity(level.getEntitiesOfClass(Hunter.class, searchArea, _ -> true), conditions, mob, mob.getX(), mob.getEyeY(), mob.getZ());
        if (hunter == null) return current;
        if (current == null || mob.distanceToSqr(hunter) < mob.distanceToSqr(current)) return hunter;

        return current;
    }
}
