package de.teamlapen.vampirism.misc.mixin;

import de.teamlapen.faction.api.factions.IFactionEntity;
import de.teamlapen.vampirism.common.util.AndTargetSelector;
import de.teamlapen.vampirism.common.util.Helper;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterAggression;
import de.teamlapen.vampirism.misc.extension.INearestTargetGoal;
import de.teamlapen.vampirism.misc.mixin.accessor.TargetConditionAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.function.BiPredicate;

@Mixin(NearestAttackableTargetGoal.class)
public abstract class NearestAttackableTargetGoalMixin extends TargetGoal implements INearestTargetGoal {

    @Final
    @Shadow
    protected TargetingConditions targetConditions;

    @Shadow
    @Nullable
    protected LivingEntity target;

    @Final
    @Shadow
    protected Class<?> targetType;

    private NearestAttackableTargetGoalMixin(Mob mob, boolean mustSee) {
        super(mob, mustSee);
    }

    @Shadow
    protected abstract AABB getTargetSearchArea(double followDistance);

    @Shadow
    protected abstract TargetingConditions getTargetConditions();

    @Inject(method = "findTarget", at = @At("TAIL"))
    private void vampirism$considerHunters(CallbackInfo ci) {
        if (this.targetType != Player.class && this.targetType != ServerPlayer.class) return;
        if (!(this.mob.level() instanceof ServerLevel level)) return;

        this.target = HunterAggression.closerHunter(level, this.mob, getTargetConditions(), getTargetSearchArea(getFollowDistance()), this.target);
    }

    @Unique
    private static final BiPredicate<Mob, LivingEntity> vampirism$nonVampireCheck = (mob, target) -> !Helper.appearsAsVampire(target, mob);

    @Unique
    private static final TargetingConditions.Selector vampirism$noFactionEntityCheck = (entity, level) -> !(entity instanceof IFactionEntity);

    @Override
    public void vampirism$ignoreVampires(Mob mob) {
        TargetingConditions.Selector predicate = (l, level) -> vampirism$nonVampireCheck.test(mob, l);
        if (((TargetConditionAccessor) this.targetConditions).getSelector() != null) {
            predicate = new AndTargetSelector(predicate, ((TargetConditionAccessor) this.targetConditions).getSelector());
        }
        this.targetConditions.selector(predicate);
    }

    @Override
    public void vampirism$ignoreFactionEntities() {
        TargetingConditions.Selector predicate = vampirism$noFactionEntityCheck;
        if (((TargetConditionAccessor) this.targetConditions).getSelector() != null) {
            predicate = new AndTargetSelector(predicate, ((TargetConditionAccessor) this.targetConditions).getSelector());
        }
        this.targetConditions.selector(predicate);
    }

    @Override
    public void vampirism$ignoreLineOfSight() {
        this.targetConditions.ignoreLineOfSight();
    }
}
