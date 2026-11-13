package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PastimePlanner {

    private static final int TRAVEL_MARGIN = 1200;

    public static void tick(ServerLevel level, HunterSquad squad) {
        List<Hunter> members = squad.members();
        if (level.isRaining()) {
            members.forEach(PastimePlanner::cancel);
            return;
        }
        if (level.dimensionType().hasFixedTime()) return;

        long time = level.getDefaultClockTime();
        DayPeriod period = DayPeriod.at(time);
        if (period == null) return;

        long key = period.key(time);
        if (members.stream().anyMatch(member -> member.getBrain().getMemory(ModMemoryTypes.PLANNED_PERIOD.get()).filter(planned -> planned == key).isPresent())) return;

        members.forEach(member -> member.getBrain().setMemory(ModMemoryTypes.PLANNED_PERIOD.get(), key));
        if (squad.isRetreating() || squad.isOnGuard(level.getGameTime())) return;

        if (Pastime.roll(period, level.getRandom()) == Pastime.FISHING) {
            startFishing(level, members, period);
        }
    }

    private static void startFishing(ServerLevel level, List<Hunter> members, DayPeriod period) {
        RandomSource random = level.getRandom();
        List<Hunter> available = members.stream().filter(PastimePlanner::isAvailable).toList();
        List<Hunter> chosen = new ArrayList<>(available.stream().filter(hunter -> random.nextFloat() < hunter.getFishingInterest()).toList());
        if (chosen.isEmpty()) {
            Hunter keenest = pickWeighted(available, random);
            if (keenest == null) return;
            chosen.add(keenest);
        }
        chosen.sort(Comparator.comparingDouble(Hunter::getFishingInterest).reversed());

        List<FishingSpot> spots = FishingSpots.findGroup(level, chosen.getFirst(), chosen.size());
        List<Hunter> fishers = new ArrayList<>(chosen.subList(0, Math.min(chosen.size(), spots.size())));
        for (FishingSpot spot : spots) {
            Vec3 stand = Vec3.atBottomCenterOf(spot.stand());
            Hunter nearest = fishers.stream().min(Comparator.comparingDouble(hunter -> hunter.distanceToSqr(stand))).orElseThrow();
            fishers.remove(nearest);

            int duration = (int) (period.length() * rollLengthFactor(random) * getInterestFactor(nearest.getFishingInterest()));
            int ttl = duration + TRAVEL_MARGIN;
            Brain<Hunter> brain = nearest.getBrain();
            brain.setMemoryWithExpiry(ModMemoryTypes.PASTIME.get(), Pastime.FISHING, ttl);
            brain.setMemoryWithExpiry(ModMemoryTypes.PASTIME_DURATION.get(), duration, ttl);
            brain.setMemoryWithExpiry(ModMemoryTypes.FISHING_SPOT.get(), spot, ttl);
        }
    }

    private static double rollLengthFactor(RandomSource random) {
        double x = 1.2D * random.nextFloat() - 0.75D;
        return 1.0D - x * x;
    }

    private static double getInterestFactor(float interest) {
        double x = interest - 1.0D;
        return 1.0D - x * x * x * x;
    }

    private static void cancel(Hunter hunter) {
        Brain<Hunter> brain = hunter.getBrain();
        brain.eraseMemory(ModMemoryTypes.PASTIME.get());
        brain.eraseMemory(ModMemoryTypes.PASTIME_DURATION.get());
        brain.eraseMemory(ModMemoryTypes.FISHING_SPOT.get());
    }

    private static boolean isAvailable(Hunter hunter) {
        Brain<Hunter> brain = hunter.getBrain();
        return !hunter.isFighting() && !hunter.isRetreating() && hunter.getPatrolArea() != null
                && !brain.hasMemoryValue(ModMemoryTypes.PASTIME.get())
                && !brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get());
    }

    private static @Nullable Hunter pickWeighted(List<Hunter> hunters, RandomSource random) {
        double total = hunters.stream().mapToDouble(Hunter::getFishingInterest).sum();
        if (total <= 0.0D) return null;

        double roll = random.nextDouble() * total;
        for (Hunter hunter : hunters) {
            roll -= hunter.getFishingInterest();
            if (roll < 0.0D) return hunter;
        }
        return hunters.getLast();
    }
}
