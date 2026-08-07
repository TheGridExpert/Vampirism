package de.teamlapen.vampirism.common.world.entity.player.vampire.actions;

import de.teamlapen.faction.api.factions.actions.IActionResult;
import de.teamlapen.faction.api.factions.actions.ILastingAction;
import de.teamlapen.faction.common.core.ModRegistries;
import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.world.entity.player.vampire.IDraculaPlayer;
import de.teamlapen.vampirism.api.world.entity.player.vampire.IVampirePlayer;
import de.teamlapen.vampirism.common.config.ModConfig;
import de.teamlapen.vampirism.common.core.ModAttachments;
import de.teamlapen.vampirism.common.core.ModEffects;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModItems;
import de.teamlapen.vampirism.common.world.entity.IllusoryBatEntity;
import de.teamlapen.vampirism.common.world.entity.player.vampire.VampirePlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForgeMod;

import java.util.Objects;

public class BatVampireAction extends DefaultVampireAction implements ILastingAction<IVampirePlayer> {

    public final static float BAT_EYE_HEIGHT = 0.85F * 0.6f;
    public static final EntityDimensions BAT_SIZE = EntityDimensions.fixed(0.6f, 0.8f).withEyeHeight(BAT_EYE_HEIGHT);

    private static final float PLAYER_WIDTH = 0.6F;
    private static final float PLAYER_HEIGHT = 1.8F;

    public BatVampireAction() {
        super();
    }

    @Override
    public IActionResult activateServer(IVampirePlayer vampire, ActivationContext context) {
        Player player = vampire.asEntity();
        setModifier(player, true);
        updatePlayer((VampirePlayer) vampire, true);
        IDraculaPlayer.get(player).ifPresent(d -> d.closeWings(true));
        createTransformationEffects(player, true);
        return IActionResult.SUCCESS;
    }

    @Override
    public IActionResult canBeUsedBy(IVampirePlayer vampire) {
        Player player = vampire.asEntity();
        if (vampire.isGettingSundamage(player.level())) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.in_sun"));
        } else if (ModItems.UMBRELLA.get() == player.getMainHandItem().getItem()) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.has_umbrella"));
        } else if (vampire.isGettingGarlicDamage(player.level()) != EnumStrength.NONE || vampire.asEntity().hasEffect(ModEffects.GARLIC) && vampire.asEntity().getEffect(ModEffects.GARLIC).getAmplifier() > 0) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.effected_by_garlic"));
        } else if (ModConfig.server().batDimensionBlacklist.get().contains(player.level().dimension().identifier().toString())) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.dimension"));
        } else if (vampire.getActionHandler().isActionActive(VampireActions.VAMPIRE_RAGE)) {
            return IActionResult.fail(Component.translatable("message.factionapi.action.conflicts_with", Component.translatable(Util.makeDescriptionId("action", VampireActions.VAMPIRE_RAGE.getId()))));
        } else if (player.isInWater()) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.in_water"));
        } else if (player.getVehicle() != null) {
            return IActionResult.fail(Component.translatable("message.vampirism.action.bat.in_vehicle"));
        } else {
            return IActionResult.SUCCESS;
        }
    }

    @Override
    public int getCooldown(IVampirePlayer player) {
        return ModConfig.balance().vaBatCooldown.get() * 20 + 1;
    }

    @Override
    public int getDuration(IVampirePlayer player) {
        return Mth.clamp(ModConfig.balance().vaBatDuration.get(), 10, Integer.MAX_VALUE / 20 - 1) * 20;
    }

    @Override
    public boolean isEnabled() {
        return ModConfig.balance().vaBatEnabled.get();
    }

    @Override
    public void onActivatedClient(IVampirePlayer vampire) {
        if (!((VampirePlayer) vampire).getSkillProperties().bat) {
            updatePlayer((VampirePlayer) vampire, true);
            setModifier(vampire.asEntity(), true);
        }
        Identifier key = ModRegistries.ACTIONS.getKey(this);
        AttributeInstance fly = vampire.asEntity().getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
        if (fly != null && !fly.hasModifier(key)) {
            fly.addPermanentModifier(new AttributeModifier(key, 1, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @Override
    public void onDeactivated(IVampirePlayer vampire) {
        Player player = vampire.asEntity();
        setModifier(player, false);
        if (!player.onGround()) {
            player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 20, 100, false, false));
        }
        //player.addPotionEffect(new PotionEffect(MobEffects.REGENERATION, 20, 0, false, false));
        updatePlayer((VampirePlayer) vampire, false);
        player.removeData(ModAttachments.VAMPIRE_BAT);
        createTransformationEffects(player, false);
    }

    @Override
    public void onReActivatedServer(IVampirePlayer vampire) {
        setModifier(vampire.asEntity(), true);
        if (!((VampirePlayer) vampire).getSkillProperties().bat) {
            updatePlayer((VampirePlayer) vampire, true);
        }
    }

    @Override
    public boolean onUpdate(IVampirePlayer vampire) {
        if (vampire.asEntity() instanceof ServerPlayer player) {
            if (vampire.isGettingSundamage(player.level()) && !vampire.isRemote()) {
                player.sendSystemMessage(Component.translatable("message.vampirism.action.bat.cant_fly_day"));
                return true;
            } else if (ModItems.UMBRELLA.get() == player.getMainHandItem().getItem() && !vampire.isRemote()) {
                player.sendSystemMessage(Component.translatable("message.vampirism.action.bat.cant_fly_umbrella"));
                return true;
            } else if (vampire.isGettingGarlicDamage(player.level()) != EnumStrength.NONE && !vampire.isRemote()) {
                player.sendSystemMessage(Component.translatable("message.vampirism.action.bat.cant_fly_garlic"));
                return true;
            } else if (ModConfig.server().batDimensionBlacklist.get().contains(player.level().dimension().identifier().toString())) {
                player.sendSystemMessage(Component.translatable("message.vampirism.action.bat.cant_fly_dimension"));
                return true;
            } else {
                float exhaustion = ModConfig.balance().vaBatExhaustion.get().floatValue();
                if (exhaustion > 0) vampire.addExhaustion(exhaustion);
                return player.isInWater();
            }
        }
        return false;
    }

    /**
     * Set's flightspeed capability
     */
    private void setFlightSpeed(Player player, float speed) {
        player.getAbilities().setFlyingSpeed(speed);
    }

    private void setModifier(Player player, boolean enabled) {
        Identifier key = ModRegistries.ACTIONS.getKey(this);
        if (key == null) {
            return;
        }
        if (enabled) {
            AttributeInstance armor = player.getAttribute(Attributes.ARMOR);
            if (armor != null && !armor.hasModifier(key)) {
                armor.addPermanentModifier(new AttributeModifier(key, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            AttributeInstance armorToughness = player.getAttribute(Attributes.ARMOR_TOUGHNESS);
            if (armorToughness != null && !armorToughness.hasModifier(key)) {
                armorToughness.addPermanentModifier(new AttributeModifier(key, -1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            }
            AttributeInstance fly = player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT);
            if (fly != null && !fly.hasModifier(key)) {
                fly.addPermanentModifier(new AttributeModifier(key, 1, AttributeModifier.Operation.ADD_VALUE));
            }

            setFlightSpeed(player, ModConfig.balance().vaBatFlightSpeed.get().floatValue());
        } else {
            Objects.requireNonNull(player.getAttribute(Attributes.ARMOR)).removeModifier(key);
            Objects.requireNonNull(player.getAttribute(Attributes.ARMOR_TOUGHNESS)).removeModifier(key);
            Objects.requireNonNull(player.getAttribute(NeoForgeMod.CREATIVE_FLIGHT)).removeModifier(key);

            setFlightSpeed(player, 0.05F);
        }
        player.onUpdateAbilities();

    }

    /**
     * Adjust the players size and eye height to fit to the bat model
     */
    private void updatePlayer(VampirePlayer vampire, boolean bat) {
        Player player = vampire.asEntity();
        vampire.getSkillProperties().bat = bat;
        player.setForcedPose(bat ? Pose.STANDING : null);
        //Eye height is set in {@link ModPlayerEventHandler} on {@link EyeHeight} event
        //Entity size is hacked in via {@link ASMHooks}
        player.refreshDimensions();
        if (bat) {
            player.setPos(player.getX(), player.getY() + (PLAYER_HEIGHT - BAT_SIZE.height()), player.getZ());
        }
    }

    private static void createTransformationEffects(Player player, boolean enteringBatForm) {
        if (!(player.level() instanceof ServerLevel level)) return;

        double x = player.getX();
        double y = player.getY() + player.getBbHeight() * 0.5D;
        double z = player.getZ();
        level.sendParticles(ParticleTypes.SQUID_INK, x, y, z, 20, 0.45D, 0.6D, 0.45D, 0.03D);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, x, y, z, 28, 0.4D, 0.6D, 0.4D, 0.03D);
        if (enteringBatForm) {
            spawnIllusoryBats(level, player);
        }
        level.playSound(null, x, y, z, SoundEvents.BAT_AMBIENT, SoundSource.PLAYERS, 1.4F, enteringBatForm ? 0.72F : 0.9F);
        level.playSound(null, x, y, z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.55F, enteringBatForm ? 0.72F : 0.85F);
    }

    private static void spawnIllusoryBats(ServerLevel level, Player player) {
        for (int i = 0; i < 28; i++) {
            IllusoryBatEntity bat = new IllusoryBatEntity(ModEntities.ILLUSORY_BAT.get(), level);
            double angle = level.getRandom().nextDouble() * Math.PI * 2.0D;
            double horizontalSpeed = 0.24D + level.getRandom().nextDouble() * 0.08D;
            double verticalSpeed = (level.getRandom().nextDouble() - 0.35D) * 0.16D;
            bat.snapTo(player.getX(), player.getY() + player.getBbHeight() * (0.25D + level.getRandom().nextDouble() * 0.55D), player.getZ(), (float) (angle * 180.0D / Math.PI), 0.0F);
            bat.setDeltaMovement(Math.cos(angle) * horizontalSpeed, verticalSpeed + 0.04D, Math.sin(angle) * horizontalSpeed);
            bat.setLifetime(24 + level.getRandom().nextInt(15));
            bat.hurtMarked = true; // Send its initial velocity to tracking clients immediately.
            level.addFreshEntity(bat);
        }
    }
}
