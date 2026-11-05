package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.Swim;

public class FordLikeSwim extends Swim<Hunter> {

    public FordLikeSwim(float chance) {
        super(chance);
    }

    public static boolean shouldRise(Hunter hunter) {
        if (!hunter.isInWater()) {
            return hunter.isInLava() || hunter.isInFluidType((_, fluidType, height) -> hunter.canSwimInFluidType(fluidType) && height > hunter.getFluidJumpThreshold());
        }

        // Ford (walk on the bottom) in shallow water
        if (hunter.isShallowWater() && !hunter.shouldTryExitWater()) {
            return false;
        }

        // Rise if there’s ground nearby to get out
        return hunter.isNearGround() || hunter.shouldTryExitWater();
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Mob body) {
        return body instanceof Hunter hunter && shouldRise(hunter);
    }
}
