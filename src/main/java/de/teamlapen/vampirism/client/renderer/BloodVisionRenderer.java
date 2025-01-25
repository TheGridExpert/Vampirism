package de.teamlapen.vampirism.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import de.teamlapen.lib.util.OptifineHandler;
import de.teamlapen.vampirism.api.entity.hunter.IHunter;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.CustomBufferSource;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.entity.ExtendedCreature;
import de.teamlapen.vampirism.entity.player.VampirismPlayerAttributes;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.MixinHooks;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BloodVisionRenderer {
    private static final int ENTITY_NEAR_SQ_DISTANCE = 600;
    private static final int BLOOD_VISION_FADE_TICKS = 80;
    public static final ResourceLocation BLUR_SHADER = VResourceLocation.mc("blur");
    public static final RenderType SOLID_TRANSPARENCY_ENTITY = RenderType.create(VResourceLocation.modString("solid_transparency_entity"),
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            256,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderType.POSITION_COLOR_SHADER)
                    .setTransparencyState(RenderType.TRANSLUCENT_TRANSPARENCY)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .createCompositeState(false));
    private boolean inBloodVisionRendering;
    @NotNull
    private final Minecraft mc;
    private CustomBufferSource bloodVisionBuffer;
    private boolean reducedBloodVision = false;
    private int bloodVisionTicks = 0;
    private int lastBloodVisionTicks = 0;
    private boolean hasGarlicVision;


    public BloodVisionRenderer(@NotNull Minecraft mc) {
        this.mc = mc;
    }

    public boolean isInBloodVisionRendering() {
        return this.inBloodVisionRendering;
    }

    public static void onRegisterStage(RenderLevelStageEvent.RegisterStageEvent event) {
        event.register(VResourceLocation.mod("after_blood_vision"), SOLID_TRANSPARENCY_ENTITY);
    }

    public void onClientSetup(FMLClientSetupEvent event) {
        this.bloodVisionBuffer = new CustomBufferSource(this.mc.renderBuffers().bufferSource());
    }

    @SubscribeEvent
    public void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shouldRenderBloodVision()) {
            this.reducedBloodVision = OptifineHandler.isShaders();
            if (this.reducedBloodVision) {
                MixinHooks.enforcingGlowing_bloodVision = true;
            }
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(@NotNull RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            if (shouldRenderBloodVision() && !reducedBloodVision) {
                this.inBloodVisionRendering = true;
                //noinspection DataFlowIssue
                this.hasGarlicVision = VampirismPlayerAttributes.get(mc.player).getVampSpecial().blood_vision_garlic;
                DeltaTracker deltaTracker = this.mc.getDeltaTracker();
                float partalTicks = deltaTracker.getGameTimeDeltaPartialTick(true);
                PostChain blur = this.mc.getShaderManager().getPostChain(BLUR_SHADER, LevelTargetBundle.MAIN_TARGETS);
                //noinspection DataFlowIssue
                blur.setUniform("Radius", 12 * getBloodVisionProgress(partalTicks));
                //noinspection deprecation
                blur.process(Minecraft.getInstance().getMainRenderTarget(), Minecraft.getInstance().gameRenderer.resourcePool);

                Vec3 vec3 = event.getCamera().getPosition();
                PoseStack poseStack = event.getPoseStack();
                List<LivingEntity> visibleEntities = event.getLevelRenderer().visibleEntities.stream().filter(x -> x instanceof LivingEntity).map(s -> (LivingEntity) s).toList();
                for (LivingEntity entity : visibleEntities) {
                    renderEntity(event.getLevelRenderer(), entity, vec3, poseStack, partalTicks, getBloodVisionProgress(partalTicks));
                }
                this.bloodVisionBuffer.endLastBatch();
                this.inBloodVisionRendering = false;
            }
        }
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            MixinHooks.enforcingGlowing_bloodVision = false;
        }
    }

    @SubscribeEvent
    public void onTick(ClientTickEvent.Pre event) {
        if (this.mc.level == null || this.mc.player == null || !mc.player.isAlive()) return;
        this.lastBloodVisionTicks = this.bloodVisionTicks;
        VampirePlayer vampire = VampirePlayer.get(mc.player);
        if (vampire.getSpecialAttributes().blood_vision && !VampirismConfig.CLIENT.disableBloodVisionRendering.get() && !vampire.isGettingSundamage(mc.player.level())) {
            if (this.bloodVisionTicks < BLOOD_VISION_FADE_TICKS) {
                this.bloodVisionTicks++;

            }
        } else {
            if (this.bloodVisionTicks > 0) {
                this.bloodVisionTicks -= 2;
            }
        }
    }

    @SubscribeEvent
    public void onWorldLoad(LevelEvent.Load event) {
        this.bloodVisionTicks = 0;
        this.lastBloodVisionTicks = 0;
    }

    private void renderEntity(LevelRenderer levelRenderer, LivingEntity entity, Vec3 cameraPos, PoseStack poseStack, float partialTicks, float progress) {
        adjustColor(entity, progress, cameraPos);
        levelRenderer.renderEntity(entity, cameraPos.x(), cameraPos.y(), cameraPos.z(), partialTicks, poseStack, this.bloodVisionBuffer);
    }

    private void adjustColor(LivingEntity entity, float progress, Vec3 cameraPos) {
        var dist = entity.distanceToSqr(cameraPos);

        if (dist > ENTITY_NEAR_SQ_DISTANCE) {
            progress *= (dist > 2 * ENTITY_NEAR_SQ_DISTANCE) ? 0 : (float)(1 - (dist - ENTITY_NEAR_SQ_DISTANCE) / ENTITY_NEAR_SQ_DISTANCE);
        }

        int trans = 200;
        int col = 100;
        this.bloodVisionBuffer.setColor(col, col, col, (int)(trans * progress));
        var creature = ExtendedCreature.getSafe(entity);
        if (creature.isPresent()) {
            ExtendedCreature extendedCreature = creature.get();
            if (extendedCreature.getBlood() > 0 && !extendedCreature.hasPoisonousBlood()) {
                float i = extendedCreature.getBlood() / (float) extendedCreature.getMaxBlood();
                this.bloodVisionBuffer.setColor((int) ((255-col) * i)+50,(int) (col * (1-i)),(int)(col * (1-i)),(int)( (((255-trans)*i)+trans) * progress));
            } else if (this.hasGarlicVision && (extendedCreature.hasPoisonousBlood() || entity instanceof IHunter)) {
                this.bloodVisionBuffer.setColor(0,255,0, (int)(trans * progress));
            }
        }
        this.bloodVisionBuffer.normalize(Math.max(0, Math.min(1, 1f - (float)(dist - 4 * 4) / (10 * 10 - 4 * 4))));
//        this.bloodVisionBuffer.normalize((float) (1f - (dist - 4f*4f) / (10f*10f - 4f*4f)));
    }

    public boolean shouldRenderBloodVision() {
        return this.bloodVisionTicks > 0 && this.mc.player != null;
    }

    private float getBloodVisionProgress(float partialTicks) {
        return (bloodVisionTicks + (bloodVisionTicks - lastBloodVisionTicks) * partialTicks) / (float) BLOOD_VISION_FADE_TICKS;
    }
}
