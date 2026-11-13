package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import org.jspecify.annotations.Nullable;

public enum DayPeriod {
    MORNING(22500L, 3000L),
    EVENING(11000L, 3000L);

    private static final long DAY_LENGTH = 24000L;

    private final long start;
    private final long length;

    DayPeriod(long start, long length) {
        this.start = start;
        this.length = length;
    }

    public static @Nullable DayPeriod at(long time) {
        for (DayPeriod period : values()) {
            if (period.offset(time) < period.length) return period;
        }
        return null;
    }

    public long length() {
        return this.length;
    }

    public long key(long time) {
        return Math.floorDiv(time - this.start, DAY_LENGTH) * values().length + ordinal();
    }

    public long remaining(long time) {
        return this.length - offset(time);
    }

    private long offset(long time) {
        return Math.floorMod(time - this.start, DAY_LENGTH);
    }
}
