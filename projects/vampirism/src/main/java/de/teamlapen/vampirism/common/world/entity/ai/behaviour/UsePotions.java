package de.teamlapen.vampirism.common.world.entity.ai.behaviour;

import com.google.common.collect.ImmutableMap;
import de.teamlapen.vampirism.common.core.ModAttributes;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.AlchemistPotion;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.brain.HunterComrades;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

public class UsePotions extends Behavior<Hunter> {

    private static final int COOLDOWN = 60;
    private static final int MAX_DRINK_TICKS = 50;
    private static final double THROW_RANGE = 14.0D;
    private static final double SPLASH_RADIUS = AlchemistPotion.SPLASH_RANGE;
    private static final float HEAL_HEALTH_FRACTION = 0.75F;
    private static final float THROW_SPEED = 0.9F;
    private static final double THROW_INACCURACY = 0.75D;

    private static final int AIM_TICKS = 12;
    private static final int FAILED_AIM_COOLDOWN = 10;

    private @Nullable Plan plan;
    private @Nullable Plan aiming;
    private long releaseAt;
    private boolean drinking;

    public UsePotions() {
        super(ImmutableMap.of(
                ModMemoryTypes.POTION_COOLDOWN.get(), MemoryStatus.VALUE_ABSENT,
                MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES, MemoryStatus.REGISTERED,
                ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get(), MemoryStatus.REGISTERED,
                MemoryModuleType.ATTACK_TARGET, MemoryStatus.REGISTERED,
                MemoryModuleType.LOOK_TARGET, MemoryStatus.REGISTERED
        ), MAX_DRINK_TICKS);
    }

    @Override
    protected boolean checkExtraStartConditions(ServerLevel level, Hunter hunter) {
        if (!hunter.isAlchemistClass() || !hunter.hasPotions() || !hunter.getMainHandItem().isEmpty() || hunter.isUsingItem()) return false;
        this.plan = choosePlan(hunter);
        return this.plan != null;
    }

    @Override
    protected void start(ServerLevel level, Hunter hunter, long gameTime) {
        Plan plan = this.plan;
        this.plan = null;
        if (plan == null) return;

        if (plan.target() == null) {
            hunter.getBrain().setMemoryWithExpiry(ModMemoryTypes.POTION_COOLDOWN.get(), Unit.INSTANCE, COOLDOWN);
            this.drinking = hunter.drinkPotion(createPotion(Items.POTION, plan.potion()));
            if (this.drinking) hunter.consumePotion();
            return;
        }

        if (!hunter.holdPotion(createPotion(Items.SPLASH_POTION, plan.potion()))) return;
        this.aiming = plan;
        this.releaseAt = gameTime + AIM_TICKS;
        watch(hunter, plan.target());
    }

    @Override
    protected boolean canStillUse(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.drinking) return hunter.isUsingItem();
        return this.aiming != null && this.aiming.target() != null && this.aiming.target().isAlive() && hunter.isHoldingPotion();
    }

    @Override
    protected void tick(ServerLevel level, Hunter hunter, long gameTime) {
        Plan plan = this.aiming;
        if (plan == null || plan.target() == null) return;

        LivingEntity target = plan.target();
        watch(hunter, target);
        if (gameTime < this.releaseAt) return;

        this.aiming = null;
        if (isInThrowRange(hunter, target) && hunter.hasLineOfSight(target)) {
            hunter.getBrain().setMemoryWithExpiry(ModMemoryTypes.POTION_COOLDOWN.get(), Unit.INSTANCE, COOLDOWN);
            throwPotion(level, hunter, plan, target);
        } else {
            hunter.getBrain().setMemoryWithExpiry(ModMemoryTypes.POTION_COOLDOWN.get(), Unit.INSTANCE, FAILED_AIM_COOLDOWN);
        }
    }

    @Override
    protected void stop(ServerLevel level, Hunter hunter, long gameTime) {
        if (this.drinking || this.aiming != null || hunter.isHoldingPotion()) hunter.putAwayPotion();
        this.drinking = false;
        this.aiming = null;
    }

    private static void watch(Hunter hunter, LivingEntity target) {
        hunter.getBrain().setMemory(MemoryModuleType.LOOK_TARGET, new EntityTracker(target, true));
        Vec3 aim = target.getEyePosition();
        hunter.getLookControl().setLookAt(aim.x, aim.y, aim.z);
    }

    private static @Nullable Plan choosePlan(Hunter hunter) {
        if (needsHealing(hunter)) return new Plan(Potions.REGENERATION, null, AlchemistPotion.Audience.ALLIES);

        LivingEntity ally = pickSplashTarget(hunter, findVisible(hunter, entity -> HunterComrades.isComrade(hunter, entity) && needsHealing(entity)));
        if (ally != null) return new Plan(Potions.REGENERATION, ally, AlchemistPotion.Audience.ALLIES);

        if (!hunter.getBrain().hasMemoryValue(MemoryModuleType.ATTACK_TARGET)) return null;
        LivingEntity foe = pickSplashTarget(hunter, findFoes(hunter));
        if (foe != null) return new Plan(Potions.POISON, foe, AlchemistPotion.Audience.ENEMIES);
        return null;
    }

    private static List<LivingEntity> findFoes(Hunter hunter) {
        Brain<Hunter> brain = hunter.getBrain();
        List<LivingEntity> foes = new ArrayList<>();
        brain.getMemory(MemoryModuleType.ATTACK_TARGET).filter(target -> isPoisonable(hunter, target)).ifPresent(foes::add);
        for (LivingEntity hostile : brain.getMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()).orElse(List.of())) {
            if (!foes.contains(hostile) && isPoisonable(hunter, hostile)) foes.add(hostile);
        }
        return foes;
    }

    private static boolean isPoisonable(Hunter hunter, LivingEntity target) {
        return isInThrowRange(hunter, target) && HunterComrades.isFoe(hunter, target) && canReceive(target, MobEffects.POISON);
    }

    private static List<LivingEntity> findVisible(Hunter hunter, Predicate<LivingEntity> filter) {
        List<LivingEntity> result = new ArrayList<>();
        NearestVisibleLivingEntities visible = hunter.getBrain().getMemory(MemoryModuleType.NEAREST_VISIBLE_LIVING_ENTITIES).orElse(NearestVisibleLivingEntities.empty());
        for (LivingEntity entity : visible.findAll(entity -> isInThrowRange(hunter, entity) && filter.test(entity))) {
            result.add(entity);
        }
        return result;
    }

    private static @Nullable LivingEntity pickSplashTarget(Hunter hunter, List<LivingEntity> candidates) {
        Comparator<LivingEntity> byCoverage = Comparator.comparingLong(candidate -> countWithinSplash(candidate, candidates));
        Comparator<LivingEntity> byCloseness = Comparator.comparingDouble(candidate -> -hunter.distanceToSqr(candidate));
        return candidates.stream().max(byCoverage.thenComparing(byCloseness)).orElse(null);
    }

    private static long countWithinSplash(LivingEntity center, List<LivingEntity> candidates) {
        return candidates.stream().filter(other -> other.distanceToSqr(center) <= SPLASH_RADIUS * SPLASH_RADIUS).count();
    }

    private static boolean needsHealing(LivingEntity entity) {
        return entity.isAlive() && entity.getHealth() < entity.getMaxHealth() * HEAL_HEALTH_FRACTION && canReceive(entity, MobEffects.REGENERATION);
    }

    private static boolean canReceive(LivingEntity entity, Holder<MobEffect> effect) {
        return !entity.hasEffect(effect) && entity.canBeAffected(new MobEffectInstance(effect));
    }

    private static boolean isInThrowRange(Hunter hunter, LivingEntity entity) {
        return entity != hunter && hunter.distanceToSqr(entity) <= THROW_RANGE * THROW_RANGE;
    }

    private static void throwPotion(ServerLevel level, Hunter hunter, Plan plan, LivingEntity target) {
        ItemStack stack = createPotion(Items.SPLASH_POTION, plan.potion());
        hunter.consumePotion();
        hunter.swing(InteractionHand.MAIN_HAND);
        hunter.putAwayPotion();

        AlchemistPotion potion = new AlchemistPotion(level, hunter, stack, plan.audience());
        Vec3 direction = PotionAim.calculateThrowDirection(potion.position(), target, THROW_SPEED);
        float inaccuracy = (float) (THROW_INACCURACY / Math.max(0.1D, hunter.getAttributeValue(ModAttributes.ACCURACY)));
        potion.shoot(direction.x, direction.y, direction.z, THROW_SPEED, inaccuracy);
        level.addFreshEntity(potion);

        RandomSource random = hunter.getRandom();
        hunter.playSound(SoundEvents.SPLASH_POTION_THROW, 0.5F, 0.4F / (random.nextFloat() * 0.4F + 0.8F));
    }

    private static ItemStack createPotion(Item item, Holder<Potion> potion) {
        return PotionContents.createItemStack(item, potion);
    }

    private record Plan(Holder<Potion> potion, @Nullable LivingEntity target, AlchemistPotion.Audience audience) {
    }
}
