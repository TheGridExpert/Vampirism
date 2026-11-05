package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.function.ToDoubleBiFunction;
import java.util.function.ToIntFunction;

public final class TargetAllocator {

    public static Map<LivingEntity, List<Hunter>> allocate(List<Hunter> hunters, List<LivingEntity> targets, ToIntFunction<LivingEntity> capacity, ToDoubleBiFunction<Hunter, LivingEntity> cost) {
        List<Candidate> candidates = new ArrayList<>();
        for (Hunter hunter : hunters) {
            for (LivingEntity target : targets) {
                double value = cost.applyAsDouble(hunter, target);
                if (Double.isFinite(value)) candidates.add(new Candidate(hunter, target, value));
            }
        }
        candidates.sort(Comparator.comparingDouble(Candidate::cost));

        Set<Hunter> assigned = new HashSet<>();
        Map<LivingEntity, List<Hunter>> result = new LinkedHashMap<>();
        for (Candidate candidate : candidates) {
            if (assigned.contains(candidate.hunter())) continue;

            List<Hunter> engaged = result.computeIfAbsent(candidate.target(), target -> new ArrayList<>());
            if (engaged.size() >= capacity.applyAsInt(candidate.target())) continue;

            engaged.add(candidate.hunter());
            assigned.add(candidate.hunter());
        }
        result.values().removeIf(List::isEmpty);
        return result;
    }

    public static ToIntFunction<LivingEntity> proportionalCapacity(List<LivingEntity> targets, int hunters, ToIntFunction<LivingEntity> max) {
        double total = 0;
        for (LivingEntity target : targets) {
            total += HunterThreats.power(target);
        }
        double totalPower = Math.max(total, 1.0E-4);
        return target -> Mth.clamp(Mth.ceil(HunterThreats.power(target) / totalPower * hunters), 1, max.applyAsInt(target));
    }

    private record Candidate(Hunter hunter, LivingEntity target, double cost) {}
}
