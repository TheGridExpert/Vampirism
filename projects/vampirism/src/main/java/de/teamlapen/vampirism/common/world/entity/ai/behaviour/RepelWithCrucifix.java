package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.items.CrucifixItem;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.OneShot;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;

public class RepelWithCrucifix {

    public static OneShot<Hunter> create(double triggerDistance, int fleeTicks, float meleeHealthPercent) {
        return BehaviorBuilder.create(instance -> instance.group(
                instance.present(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()),
                instance.absent(ModMemoryTypes.CRUCIFIX_COOLDOWN.get()),
                instance.registered(ModMemoryTypes.CRUCIFIX_FLEE.get()),
                instance.registered(ModMemoryTypes.DANGER.get())
        ).apply(instance, (hostilesAcc, cooldownAcc, fleeAcc, dangerAcc) -> (level, hunter, _) -> {
            if (hunter.isMeleeClass() && hunter.getHealth() >= hunter.getMaxHealth() * meleeHealthPercent) return false;

            ItemStack sheathed = hunter.getSheathedCrucifix();
            if (!(sheathed.getItem() instanceof CrucifixItem crucifix)) return false;

            LivingEntity closest = findClosestRepelled(level, hunter, crucifix, instance.get(hostilesAcc), triggerDistance);
            if (closest == null) return false;

            ItemStack stack = hunter.drawCrucifix();
            if (stack.isEmpty()) return false;

            crucifix.activePush(level, hunter, stack);
            hunter.swing(InteractionHand.MAIN_HAND);
            cooldownAcc.setWithExpiry(Unit.INSTANCE, crucifix.getCooldownTicks(stack));
            fleeAcc.setWithExpiry(closest, fleeTicks);
            dangerAcc.set(closest);
            return true;
        }));
    }

    private static @Nullable LivingEntity findClosestRepelled(ServerLevel level, Hunter hunter, CrucifixItem crucifix, List<LivingEntity> hostiles, double triggerDistance) {
        return hostiles.stream()
                .filter(hostile -> hostile.isAlive() && hunter.distanceTo(hostile) < triggerDistance && crucifix.affectsEntity(hostile, level))
                .min(Comparator.comparingDouble(hunter::distanceToSqr))
                .orElse(null);
    }
}
