package de.teamlapen.vampirism.client.renderer;

import de.teamlapen.vampirism.common.core.ModDebugSubscriptions;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquadDebugInfo;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;

import java.util.List;

public class HunterInfoDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    private static final int TEXT_COLOR = ARGB.color(255, 255, 230, 140);
    private static final float TEXT_SCALE = 0.32F;

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        debugValues.forEachEntity(ModDebugSubscriptions.HUNTER_INFO.get(), (hunter, info) -> {
            HunterSquadDebugInfo squadInfo = debugValues.getEntityValue(ModDebugSubscriptions.HUNTER_SQUADS.get(), hunter);
            int offset = squadInfo != null ? squadInfo.lines().size() : 0;

            List<String> lines = info.lines();
            for (int i = 0; i < lines.size(); i++) {
                Gizmos.billboardTextOverMob(hunter, offset + lines.size() - 1 - i, lines.get(i), TEXT_COLOR, TEXT_SCALE);
            }
        });
    }
}
