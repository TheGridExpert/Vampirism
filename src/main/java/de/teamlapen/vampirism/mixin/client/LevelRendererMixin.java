package de.teamlapen.vampirism.mixin.client;


import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.lib.util.Color;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.entity.player.hunter.HunterPlayer;
import de.teamlapen.vampirism.entity.player.hunter.actions.HunterActions;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.checkerframework.checker.units.qual.A;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class LevelRendererMixin {

//    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;shouldEntityAppearGlowing(Lnet/minecraft/world/entity/Entity;)Z"))
//    private boolean vampireGlowing(Minecraft instance, Entity pEntity, Operation<Boolean> original, @Share("renderVampireColor") LocalBooleanRef color) {
//        if (Helper.isHunter(instance.player) && pEntity.distanceToSqr(instance.player) < 256 && Helper.appearsAsVampire(instance.player, pEntity) && HunterPlayer.get(instance.player).getActionHandler().isActionActive(HunterActions.AWARENESS_HUNTER)) {
//            if (instance.player.hasLineOfSight(pEntity)) {
//                color.set(true);
//                return true;
//            }
//        }
//        color.set(false);
//        return original.call(instance, pEntity);
//    }

//    @WrapOperation(method = "renderLevel", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getTeamColor()I"))
//    private int color(Entity instance, Operation<Integer> original, @Share("renderVampireColor") LocalBooleanRef color) {
//        if (color.get()) {
//            return Color.RED.getRGB();
//        }
//        return original.call(instance);
//    }
}
