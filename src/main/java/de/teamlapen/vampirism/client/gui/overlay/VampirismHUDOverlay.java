package de.teamlapen.vampirism.client.gui.overlay;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import de.teamlapen.lib.lib.client.gui.GuiRenderer;
import de.teamlapen.lib.lib.util.FluidLib;
import de.teamlapen.vampirism.api.entity.IExtendedCreatureVampirism;
import de.teamlapen.vampirism.api.entity.hunter.IHunterMob;
import de.teamlapen.vampirism.api.entity.vampire.IVampireMob;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.core.ModFluids;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.items.StakeItem;
import de.teamlapen.vampirism.mixin.accessor.LivingEntityAccessor;
import de.teamlapen.vampirism.modcompat.IMCHandler;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.Optional;

public class VampirismHUDOverlay {

    public static final ResourceLocation CROSSHAIR_SPRITE = VResourceLocation.mc("hud/crosshair");
    public static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE = VResourceLocation.mc("hud/crosshair_attack_indicator_full");
    public static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE = VResourceLocation.mc("hud/crosshair_attack_indicator_background");
    public static final ResourceLocation CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE = VResourceLocation.mc("hud/crosshair_attack_indicator_progress");

    public static final ResourceLocation BLOOD_DROP_SPRITE = VResourceLocation.mod("blood_drop/blood_drop");
    public static final ResourceLocation BLOOD_DROP_POISON_SPRITE = VResourceLocation.mod("blood_drop/blood_drop_poison");
    public static final ResourceLocation BLOOD_DROP_EMPTY_SPRITE = VResourceLocation.mod("blood_drop/blood_drop_empty");

    public static final ResourceLocation FANG_SPRITE = VResourceLocation.mod("fang/fang");
    public static final ResourceLocation PROGRESS_BACKGROUND_SPRITE = VResourceLocation.mod("fang/progress_background");
    public static final ResourceLocation PROGRESS_FOREGROUND_SPRITE = VResourceLocation.mod("fang/progress_foreground");

    private final Minecraft mc;

    private int screenColor = 0;
    private int screenPercentage = 0;
    private int renderFullTick = 0;
    private int renderFullOn, renderFullOff, renderFullColor;

    private boolean addTempPoison;
    private MobEffectInstance addedTempPoison;

    public VampirismHUDOverlay(Minecraft mc) {
        this.mc = mc;
    }

    /**
     * Tints the entire screen with color given, blending in and out over time.
     *
     * @param fadeInTicks Duration (in ticks) to fade in
     * @param fadeOutTicks Duration (in ticks) to fade out
     * @param color Color (alpha is not supported)
     */
    public void makeRenderFullColor(int fadeInTicks, int fadeOutTicks, int color) {
        this.renderFullOn = fadeInTicks;
        this.renderFullOff = fadeOutTicks;
        this.renderFullTick = fadeInTicks + fadeOutTicks;

        if ((color >> 24 & 255) == 0) {
            color |= 0xFF000000;
        }
        this.renderFullColor = color;
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Pre event) {
        if (mc.player == null || !mc.player.isAlive()) {
            renderFullTick = 0;
            screenPercentage = 0;
            return;
        }

        //If we are supposed to render fullscreen, we overwrite the other values and only render fullscreen
        if (renderFullTick > 0) {
            screenColor = renderFullColor;
            if (renderFullTick > renderFullOff) {
                screenPercentage = (int) (100 * (1 - (renderFullTick - renderFullOff) / (float) renderFullOn));
            } else {
                screenPercentage = (int) (100 * renderFullTick / (float) renderFullOff);
            }
            renderFullTick--;
        }
    }

    @SubscribeEvent
    public void onRenderCrosshair(RenderGuiLayerEvent.@NotNull Pre event) {
        if (event.getName() != VanillaGuiLayers.CROSSHAIR) return;

        LocalPlayer player = mc.player;
        HitResult hitResult = mc.hitResult;
        if (player == null || !player.isAlive() || hitResult == null) return;

        Window window = mc.getWindow();

        if (hitResult instanceof EntityHitResult entityHit) {
            handleEntityHit(event, player, entityHit.getEntity(), window);
        } else if (hitResult instanceof BlockHitResult blockHit) {
            handleBlockHit(event, player, blockHit, window);
        }

        renderBloodFeedProgress(event);
    }

    private void handleEntityHit(RenderGuiLayerEvent.Pre event, LocalPlayer player, Entity targetEntity, Window window) {
        if (targetEntity.isInvisibleTo(player)) return;

        if (shouldRenderBloodDrop(player)) {
            ExtendedCreature.getBiteable(targetEntity).filter(biteable -> biteable.canBeBitten(null)).ifPresent(biteable -> {
                renderBloodDrop(event.getGuiGraphics(), window.getGuiScaledWidth(), window.getGuiScaledHeight(), Mth.clamp(biteable.getBloodLevelRelative(), 0f, 1f), isPoisonous(targetEntity));
                event.setCanceled(true);
            });
            return;
        }

        VampirismPlayerAttributes attributes = VampirismPlayerAttributes.get(player);

        if (shouldRenderBiteFangs(player, attributes)) {
            ExtendedCreature.getBiteable(targetEntity).filter(biteable -> biteable.canBeBitten(VampirePlayer.get(player))).ifPresent(biteable -> {
                int color = getBiteFangColor(targetEntity);
                renderBloodFangs(event.getGuiGraphics(), window.getGuiScaledWidth(), window.getGuiScaledHeight(), Mth.clamp(biteable.getBloodLevelRelative(), 0.2f, 1f), color);
                event.setCanceled(true);
            });
            return;
        }

        if (shouldRenderStakeIndicator(player, attributes, targetEntity)) {
            LivingEntity livingTarget = (LivingEntity) targetEntity;
            if (StakeItem.canKillInstantly(livingTarget, player) && livingTarget.getHealth() > 0) {
                renderStakeInstantKill(event.getGuiGraphics());
                event.setCanceled(true);
            }
        }
    }

    private boolean shouldRenderBloodDrop(LocalPlayer player) {
        return (player.getMainHandItem().is(ModItems.SYRINGE_EMPTY) || player.getOffhandItem().is(ModItems.SYRINGE_EMPTY) || player.getMainHandItem().is(ModItems.INJECTION_GARLIC) || player.getOffhandItem().is(ModItems.INJECTION_GARLIC)) && !player.isSpectator();
    }

    private boolean shouldRenderBiteFangs(LocalPlayer player, VampirismPlayerAttributes attributes) {
        return attributes.vampireLevel > 0 && !player.isSpectator() && !attributes.getVampSpecial().bat;
    }

    private boolean shouldRenderStakeIndicator(LocalPlayer player, VampirismPlayerAttributes attributes, Entity target) {
        return attributes.hunterLevel > 0 && !player.isSpectator() && player.getMainHandItem().getItem() == ModItems.STAKE.get() && target instanceof LivingEntity && target instanceof IVampireMob;
    }

    private int getBiteFangColor(Entity target) {
        return isPoisonous(target) ? ARGB.color(9, 144, 34) : ARGB.color(255, 0, 0);
    }

    private boolean isPoisonous(Entity entity) {
        return entity instanceof IHunterMob || ExtendedCreature.getFromEntity(entity).map(IExtendedCreatureVampirism::hasPoisonousBlood).orElse(false);
    }

    private void handleBlockHit(RenderGuiLayerEvent.Pre event, LocalPlayer player, BlockHitResult blockHit, Window window) {
        ClientLevel level = mc.level;
        if (level == null) return;

        BlockPos pos = blockHit.getBlockPos();
        BlockState state = level.getBlockState(pos);

        if (!VampirePlayer.isBlockBiteable(level, pos, blockHit.getDirection())) return;
        if (!VampirePlayer.get(player).wantsBlood()) return;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity == null) return;

        Optional.ofNullable(level.getCapability(Capabilities.FluidHandler.BLOCK, pos, state, blockEntity, null)).ifPresent(handler -> {
            if (FluidLib.getFluidAmount(handler, ModFluids.BLOOD.get()) > 0) {
                renderBloodFangs(event.getGuiGraphics(), window.getGuiScaledWidth(), window.getGuiScaledHeight(), 1, ARGB.color(255, 0, 0));
                event.setCanceled(true);
            }
        });
    }

    private void renderBloodDrop(GuiGraphics graphics, int windowWidth, int windowHeight, float percent, boolean poisonous) {
        TextureAtlasSprite sprite = graphics.sprites.getSprite(BLOOD_DROP_EMPTY_SPRITE);

        int textureWidth = sprite.contents().width();
        int textureHeight = sprite.contents().height();

        float scale = 1.4f;

        int renderWidth = (int) (textureWidth * scale);
        int renderHeight = (int) (textureHeight * scale);

        int x = (windowWidth - renderWidth) / 2;
        int y = (windowHeight - renderHeight) / 2 - textureHeight / 4;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1);

        graphics.blitSprite(RenderType::guiTextured, BLOOD_DROP_EMPTY_SPRITE, textureWidth, textureHeight, 0, 0, 0, 0, textureWidth, textureHeight);

        int offsetHeight = (int) (textureHeight * percent);
        if (offsetHeight != textureHeight) offsetHeight--;

        int vStart = textureHeight - offsetHeight;

        graphics.blitSprite(RenderType::guiTextured, poisonous ? BLOOD_DROP_POISON_SPRITE : BLOOD_DROP_SPRITE, textureWidth, textureHeight, 0, vStart, 0, vStart, textureWidth, offsetHeight);

        pose.popPose();
    }

    private void renderBloodFangs(GuiGraphics graphics, int windowWidth, int windowHeight, float percent, int color) {
        TextureAtlasSprite sprite = graphics.sprites.getSprite(FANG_SPRITE);

        int textureWidth = sprite.contents().width();
        int textureHeight = sprite.contents().height();

        float scale = 1.4f;

        int renderWidth = (int) (textureWidth * scale);
        int renderHeight = (int) (textureHeight * scale);

        int x = (windowWidth - renderWidth) / 2;
        int y = (windowHeight - renderHeight) / 2;

        PoseStack pose = graphics.pose();
        pose.pushPose();
        pose.translate(x, y, 0);
        pose.scale(scale, scale, 1);

        graphics.blitSprite(RenderType::guiTextured, FANG_SPRITE, 0, 0, textureWidth, textureHeight);

        int offsetHeight = (int) (10 * (1f - percent));
        GuiRenderer.blitSpriteTiledOffset(graphics, FANG_SPRITE, 0, 0, textureWidth, textureHeight, 0, offsetHeight, color);

        pose.popPose();
    }

    private void renderStakeInstantKill(GuiGraphics graphics) {
        if (!mc.options.getCameraType().isFirstPerson() || mc.gameMode != null && mc.gameMode.getPlayerMode() == GameType.SPECTATOR) return;

        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        RenderSystem.setShaderColor(158f / 256, 0, 0, 1);

        graphics.blitSprite(RenderType::guiTextured, CROSSHAIR_SPRITE, (graphics.guiWidth() - 15) / 2, (graphics.guiHeight() - 15) / 2, 15, 15);

        Player player = mc.player;
        if (player == null) return;

        float attackStrength = player.getAttackStrengthScale(0.0F);
        boolean showFullIndicator = false;

        if (mc.crosshairPickEntity instanceof LivingEntity living && attackStrength >= 1.0F) {
            showFullIndicator = player.getCurrentItemAttackStrengthDelay() > 5.0F && living.isAlive();
        }

        int x = graphics.guiWidth() / 2 - 8;
        int y = graphics.guiHeight() / 2 - 7 + 16;

        if (showFullIndicator) {
            graphics.blitSprite(RenderType::guiTextured, CROSSHAIR_ATTACK_INDICATOR_FULL_SPRITE, x, y, 16, 16);
        } else if (attackStrength < 1.0F) {
            int progressWidth = (int) (attackStrength * 17.0F);
            graphics.blitSprite(RenderType::guiTextured, CROSSHAIR_ATTACK_INDICATOR_BACKGROUND_SPRITE, x, y, 16, 4);
            graphics.blitSprite(RenderType::guiTextured, CROSSHAIR_ATTACK_INDICATOR_PROGRESS_SPRITE, 16, 4, 0, 0, x, y, progressWidth, 4);
        }

        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
    }

    private void renderBloodFeedProgress(RenderGuiLayerEvent.Pre event) {
        LocalPlayer player = mc.player;
        if (mc.options.getCameraType().isFirstPerson() && player != null && mc.gameMode != null && mc.gameMode.getPlayerMode() != GameType.SPECTATOR) {
            float progress = VampirePlayer.get(player).getFeedProgress();
            if (progress > 0 && progress <= 1.0F) {
                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR, GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                int x = event.getGuiGraphics().guiWidth() / 2 - 8;
                int y = event.getGuiGraphics().guiHeight() / 2 + 9;
                int width = (int) (progress * 14.0F) + 2;

                event.getGuiGraphics().blitSprite(RenderType::guiTextured, PROGRESS_BACKGROUND_SPRITE, x, y, 16, 2);
                event.getGuiGraphics().blitSprite(RenderType::guiTextured, PROGRESS_FOREGROUND_SPRITE, 16, 2, 0, 0, x, y, width, 2);
            }
        }
    }

    @SubscribeEvent
    public void onRenderFoodBar(RenderGuiLayerEvent.Pre event) {
        if (mc.player == null || !mc.player.isAlive() || !Helper.isVampire(mc.player)) return;
        // Disable the food bar if the blood bar is rendered
        if (event.getName() == VanillaGuiLayers.FOOD_LEVEL && !IMCHandler.requestedToDisableBloodbar && mc.gameMode != null && mc.gameMode.hasExperience()) {
            event.setCanceled(true);
        }
        if (event.getName().equals(VanillaGuiLayers.AIR_LEVEL)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onRenderGameOverlay(RenderGuiEvent.Pre event) {
        if (screenPercentage <= 0 || !VampirismConfig.CLIENT.renderScreenOverlay.get()) return;
        GuiGraphics guiGraphics = event.getGuiGraphics();

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();

        int width = (guiGraphics.guiWidth());
        int height = (guiGraphics.guiHeight());
        int color = ARGB.color(
                (screenPercentage / 100) * (screenColor >> 24 & 255),
                screenColor >> 16 & 255,
                screenColor >> 8 & 255,
                screenColor & 255
        );

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = guiGraphics.bufferSource.getBuffer(RenderType.guiOverlay());
        buffer.addVertex(matrix, 0, height, 0).setColor(color);
        buffer.addVertex(matrix, width, height, 0).setColor(color);
        buffer.addVertex(matrix, width, 0, 0).setColor(color);
        buffer.addVertex(matrix, 0, 0, 0).setColor(color);

        guiGraphics.flush();

        poseStack.popPose();
    }

    @SubscribeEvent
    public void onRenderHealthBarPre(RenderGuiLayerEvent.@NotNull Pre event) {
        if (event.getName() != VanillaGuiLayers.PLAYER_HEALTH) return;

        Player player = mc.player;
        if (player == null) return;

        addTempPoison = player.hasEffect(ModEffects.POISON) && !((LivingEntityAccessor) player).getActiveEffects().containsKey(MobEffects.POISON);

        if (addTempPoison) { // Add temporary dummy potion effect to trick renderer
            if (addedTempPoison == null) {
                addedTempPoison = new MobEffectInstance(MobEffects.POISON, 100);
            }
            ((LivingEntityAccessor) player).getActiveEffects().put(MobEffects.POISON, addedTempPoison);
        }
    }

    @SubscribeEvent
    public void onRenderHealthBarPost(RenderGuiLayerEvent.@NotNull Post event) {
        if (event.getName() != VanillaGuiLayers.PLAYER_HEALTH) return;

        Player player = mc.player;
        if (addTempPoison && player != null) {
            ((LivingEntityAccessor) player).getActiveEffects().remove(MobEffects.POISON);
        }
    }
}
