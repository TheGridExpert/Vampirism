package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.vampirism.common.tags.ModEntityTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;

public class HunterThreats {

    public static final double EXPLOSIVE_MIN_SHOOT_DISTANCE = 10.0D;
    public static final double CHARGED_EXPLOSIVE_MIN_SHOOT_DISTANCE = 14.0D;

    public static boolean isExplosive(LivingEntity entity) {
        return entity.is(ModEntityTags.HUNTER_THREAT_EXPLOSIVE);
    }

    public static boolean isAvoided(LivingEntity entity) {
        return entity.is(ModEntityTags.HUNTER_THREAT_AVOID);
    }

    public static boolean isPriority(LivingEntity entity) {
        return entity.is(ModEntityTags.HUNTER_THREAT_PRIORITY);
    }

    public static boolean canMelee(LivingEntity entity) {
        return !isExplosive(entity) && !entity.is(ModEntityTags.HUNTER_THREAT_RANGED_ONLY) && !isSubmerged(entity);
    }

    public static boolean canShoot(LivingEntity entity) {
        return !entity.is(ModEntityTags.HUNTER_THREAT_MELEE_ONLY) && !isSubmerged(entity);
    }

    public static boolean isInDeepWater(LivingEntity entity) {
        return entity.isInWater() && !Hunter.isShallowWater(entity.level(), entity.blockPosition());
    }

    public static boolean isSubmerged(LivingEntity entity) {
        return entity.isUnderWater() && isInDeepWater(entity);
    }

    public static boolean requiresProvocation(LivingEntity entity) {
        return entity.is(ModEntityTags.HUNTER_THREAT_PROVOKED_ONLY);
    }

    public static boolean isProvoked(LivingEntity entity) {
        return entity instanceof Mob mob && mob.getTarget() instanceof Hunter;
    }

    public static double getMinShootDistance(LivingEntity entity) {
        if (!isExplosive(entity)) return 0.0D;
        return entity instanceof Creeper creeper && creeper.isPowered() ? CHARGED_EXPLOSIVE_MIN_SHOOT_DISTANCE : EXPLOSIVE_MIN_SHOOT_DISTANCE;
    }

    public static boolean isActiveThreat(LivingEntity entity) {
        if (entity instanceof Player) return true;
        if (!(entity instanceof Mob mob)) return false;

        LivingEntity victim = mob.getTarget();
        return victim != null && (victim instanceof Hunter || victim instanceof Player || !(victim instanceof Enemy));
    }

    public static boolean shouldLure(LivingEntity target) {
        return isSubmerged(target) && isActiveThreat(target);
    }

    public static boolean isWorthEngaging(Hunter hunter, LivingEntity target) {
        if (isSubmerged(target)) return isActiveThreat(target);
        if (hunter.isRangedClass() && isActiveThreat(target)) return true;
        return hunter.canReach(target);
    }

    public static boolean canEngage(Hunter hunter, LivingEntity target) {
        return hunter.isRangedClass() ? canShoot(target) : canMelee(target);
    }

    public static double calculatePower(LivingEntity entity) {
        double damage = entity.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE) ? entity.getAttributeValue(Attributes.ATTACK_DAMAGE) : 2.0D;
        double weight = isExplosive(entity) ? 3.0D : isPriority(entity) ? 1.5D : 1.0D;
        return entity.getHealth() * Math.max(1.0D, damage) * weight;
    }
}
