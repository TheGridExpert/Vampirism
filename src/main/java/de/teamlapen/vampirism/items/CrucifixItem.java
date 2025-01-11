package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.items.IFactionExclusiveItem;
import de.teamlapen.vampirism.api.items.IFactionLevelItem;
import de.teamlapen.vampirism.api.items.IItemWithTier;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.ModRefinements;
import de.teamlapen.vampirism.core.tags.ModFactionTags;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.hunter.skills.HunterSkills;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.entity.vampire.AdvancedVampireEntity;
import de.teamlapen.vampirism.entity.vampire.VampireBaronEntity;
import de.teamlapen.vampirism.mixin.accessor.EntityAccessor;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;


public class CrucifixItem extends Item implements IItemWithTier, IFactionExclusiveItem, IFactionLevelItem<IHunterPlayer> {

    private final static String baseRegName = "crucifix";
    private final TIER tier;
    private static final ResourceLocation COOLDOWN_GROUP = VResourceLocation.mod("crucifix");

    public CrucifixItem(TIER tier, Item.Properties properties) {
        super(properties.stacksTo(1).component(DataComponents.USE_COOLDOWN, new UseCooldown( switch (tier) {
            case NORMAL -> 7;
            case ENHANCED -> 5;
            case ULTIMATE -> 3;
        }, Optional.of(COOLDOWN_GROUP))));
        this.tier = tier;
    }

    @Override
    public int getMinLevel(@NotNull ItemStack stack) {
        return 1;
    }

    @Nullable
    @Override
    public Holder<ISkill<?>> requiredSkill(@NotNull ItemStack stack) {
        if (tier == TIER.ULTIMATE) return HunterSkills.ULTIMATE_CRUCIFIX;
        return HunterSkills.CRUCIFIX_WIELDER;
    }

    @Override
    public @NotNull TagKey<IFaction<?>> getExclusiveFaction(@NotNull ItemStack stack) {
        return ModFactionTags.IS_HUNTER;
    }

    @Override
    public TIER getVampirismTier() {
        return tier;
    }

    @Override
    public @NotNull ItemUseAnimation getUseAnimation(ItemStack p_77661_1_) {
        return ItemUseAnimation.NONE;
    }


    @Override
    public @NotNull InteractionResult use(Level world, @NotNull Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        this.addTierInformation(tooltip);
        this.addFactionToolTips(stack, context, tooltip, flagIn, VampirismMod.proxy.getClientPlayer());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int slot, boolean held) {
        if (entity instanceof LivingEntity living && entity.tickCount % 16 == 8 && (living.getOffhandItem() == stack || living.getMainHandItem() == stack)) {
            if (Helper.isVampire(entity)) {
                ((LivingEntity) entity).addEffect(new MobEffectInstance(ModEffects.POISON, 20, 1));
                if (entity instanceof Player player) {
                    player.getInventory().removeItem(stack);
                    player.drop(stack, true);
                }
            }
        }
    }

    protected boolean affectsEntity(@NotNull LivingEntity e, Level level) {
        return e.getType().is(EntityTypeTags.UNDEAD) || Helper.isVampire(e);
    }


    @Override
    public boolean releaseUsing(ItemStack stack, Level world, LivingEntity entity, int p_77615_4_) {
        return true;
    }

    @Override
    public int getUseDuration(ItemStack pStack, LivingEntity p_344979_) {
        return 72000;
    }

    protected static int determineEntityTier(LivingEntity e) {
        if (e instanceof Player) {
            int level = VampirismPlayerAttributes.get((Player) e).vampireLevel;
            int tier = 1;
            if (level == ModFactions.VAMPIRE.value().getHighestReachableLevel()) {
                tier = 3;
            } else if (level >= 8) {
                tier = 2;
            }
            if (VampirePlayer.get((Player) e).getRefinementHandler().isRefinementEquipped(ModRefinements.CRUCIFIX_RESISTANT)) {
                tier++;
            }
            return tier;
        } else if (e instanceof VampireBaronEntity) {
            return 3;
        } else if (e instanceof AdvancedVampireEntity) {
            return 2;
        }
        return 1;
    }

    protected double determineSlowdown(int entityTier) {
        return switch (tier) {
            case NORMAL -> entityTier > 1 ? 0.1 : 0.5;
            case ENHANCED -> entityTier > 2 ? 0.1 : 0.5;
            case ULTIMATE -> entityTier > 3 ? 0.3 : 0.5;
        };
    }

    protected int getRange(ItemStack stack) {
        return switch (tier) {
            case ENHANCED -> 8;
            case ULTIMATE -> 10;
            default -> 4;
        };
    }

    @Override
    public void onUseTick(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, int count) {
        if (level instanceof ServerLevel serverLevel) {
            for (LivingEntity nearbyEntity : serverLevel.getNearbyEntities(LivingEntity.class, TargetingConditions.forCombat().selector(this::affectsEntity), entity, entity.getBoundingBox().inflate(getRange(stack)))) {
                Vec3 baseVector = entity.position().subtract(nearbyEntity.position()).multiply(1, 0, 1).normalize(); //Normalized horizontal (xz) vector giving the direction towards the holder of this crucifix
                Vec3 oldDelta = nearbyEntity.getDeltaMovement();
                Vec3 horizontalDelta = oldDelta.multiply(1, 0, 1);
                double parallelScale = baseVector.dot(horizontalDelta);
                if (parallelScale > 0) {
                    Vec3 parallelPart = baseVector.scale(parallelScale); //Part of delta that is parallel to baseVector
                    double scale = determineSlowdown(determineEntityTier(nearbyEntity));
                    Vec3 newDelta = oldDelta.subtract(parallelPart.scale(scale)); //Substract parallel part from old Delta (scaled to still allow some movement)
                    if (newDelta.lengthSqr() > oldDelta.lengthSqr()) { //Just to make sure we do not speed up the movement even though this should not be possible
                        newDelta = Vec3.ZERO;
                    }
                    //Unfortunately, Vanilla converts y-collision with ground into forward movement later on (in #move)
                    //Therefore, we check for collision here and remove any y component if entity would collide with ground
                    Vec3 collisionDelta = ((EntityAccessor) nearbyEntity).invoke_collide(newDelta);
                    if (collisionDelta.y != newDelta.y && newDelta.y < 0) {
                        newDelta = newDelta.multiply(1, 0, 1);
                    }

                    nearbyEntity.setDeltaMovement(newDelta);
                }
            }
        }
    }

}
