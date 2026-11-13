package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

public enum Pastime {
    FISHING(0.2F, 0.1F);

    private final float morningChance;
    private final float eveningChance;

    Pastime(float morningChance, float eveningChance) {
        this.morningChance = morningChance;
        this.eveningChance = eveningChance;
    }

    public float chance(DayPeriod period) {
        return switch (period) {
            case MORNING -> this.morningChance;
            case EVENING -> this.eveningChance;
        };
    }

    public static @Nullable Pastime roll(DayPeriod period, RandomSource random) {
        float roll = random.nextFloat();
        for (Pastime pastime : values()) {
            roll -= pastime.chance(period);
            if (roll < 0.0F) return pastime;
        }
        return null;
    }
}
