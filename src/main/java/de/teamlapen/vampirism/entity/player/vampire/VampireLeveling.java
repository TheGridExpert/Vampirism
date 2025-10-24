package de.teamlapen.vampirism.entity.player.vampire;

import de.teamlapen.vampirism.REFERENCE;

import java.util.Arrays;
import java.util.Optional;

public class VampireLeveling {

    private static final VampireLevelRequirement[] LEVEL_REQUIREMENTS = new VampireLevelRequirement[] {
            null,
            null,
            new AltarInspirationRequirement(2, 40),
            new AltarInspirationRequirement(3, 70),
            new AltarInspirationRequirement(4, 100),
            new AltarInfusionRequirements(5, 0, 0, 5, 1),
            new AltarInfusionRequirements(6, 0, 1, 5, 1),
            new AltarInfusionRequirements(7, 0, 1, 10, 1),
            new AltarInfusionRequirements(8, 1, 1, 10, 1),
            new AltarInfusionRequirements(9, 1, 1, 10, 1),
            new AltarInfusionRequirements(10, 2, 1, 15, 1),
            new AltarInfusionRequirements(11, 2, 1, 15, 1),
            new AltarInfusionRequirements(12, 3, 1, 20, 1),
            new AltarInfusionRequirements(13, 3, 2, 20, 1),
            new AltarInfusionRequirements(14, 4, 2, 25, 1)
    };

    public static Optional<VampireLevelRequirement> getLevelRequirement(int targetLevel) {
        return targetLevel >= 0 && targetLevel <= REFERENCE.HIGHEST_VAMPIRE_LEVEL ? Optional.ofNullable(LEVEL_REQUIREMENTS[targetLevel]) : Optional.empty();
    }

    public static Optional<AltarInfusionRequirements> getInfusionRequirement(int targetLevel) {
        return getLevelRequirement(targetLevel).filter(AltarInfusionRequirements.class::isInstance).map(AltarInfusionRequirements.class::cast);
    }

    public static Optional<AltarInspirationRequirement> getInspirationRequirement(int targetLevel) {
        return getLevelRequirement(targetLevel).filter(AltarInspirationRequirement.class::isInstance).map(AltarInspirationRequirement.class::cast);
    }

    public static AltarInfusionRequirements[] getInfusionRequirements() {
        return Arrays.stream(LEVEL_REQUIREMENTS).filter(AltarInfusionRequirements.class::isInstance).map(AltarInfusionRequirements.class::cast).toArray(AltarInfusionRequirements[]::new);
    }

    public static AltarInspirationRequirement[] getInspirationRequirements() {
        return Arrays.stream(LEVEL_REQUIREMENTS).filter(AltarInspirationRequirement.class::isInstance).map(AltarInspirationRequirement.class::cast).toArray(AltarInspirationRequirement[]::new);
    }

    public interface VampireLevelRequirement {
        int targetLevel();
    }

    public record AltarInfusionRequirements(int targetLevel, int pureBloodLevel, int pureBloodQuantity, int humanHeartQuantity, int vampireBookQuantity) implements VampireLevelRequirement {

        public int getRequiredStructurePoints() {
            int maxPoints = 72;
            int minInfusionLevel = 5;
            int maxInfusionLevel = 14;

            if (this.targetLevel() < minInfusionLevel) return 0;

            float progress = (float) (this.targetLevel() - minInfusionLevel + 1) / (maxInfusionLevel - minInfusionLevel + 1);
            return Math.round(progress * maxPoints);
        }
    }

    public record AltarInspirationRequirement(int targetLevel, int bloodAmount) implements VampireLevelRequirement {
    }
}
