package de.teamlapen.vampirism.entity.ai.behaviour;

import de.teamlapen.vampirism.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.behavior.Swim;

public class FordLikeSwim extends Swim<Hunter> {

    public FordLikeSwim(float chance) {
        super(chance);
    }

    public static boolean shouldRise(Hunter hunter) {
        if (!hunter.isInWater()) {
            return hunter.isInLava() || hunter.isInFluidType((fluidType, height) -> hunter.canSwimInFluidType(fluidType) && height > hunter.getFluidJumpThreshold());
        }

        // Ford (walk on the bottom) in shallow water
        if (hunter.isShallowWater() && !hunter.shouldTryExitWater()) {
            return false;
        }

        // Rise if there’s ground nearby to get out
        return hunter.isNearGround() || hunter.shouldTryExitWater();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter owner) {
        return shouldRise(owner);
    }
}
