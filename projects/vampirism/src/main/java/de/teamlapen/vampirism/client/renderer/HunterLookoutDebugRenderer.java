package de.teamlapen.vampirism.client.renderer;

import de.teamlapen.vampirism.common.core.ModDebugSubscriptions;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.gizmos.TextGizmo;
import net.minecraft.util.ARGB;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.phys.Vec3;

public class HunterLookoutDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    private static final int AREA_COLOR = ARGB.color(160, 120, 120, 255);
    private static final int CANDIDATE_COLOR = ARGB.color(255, 200, 200, 200);
    private static final int SPOT_COLOR = ARGB.color(255, 80, 255, 120);
    private static final int TEXT_COLOR = ARGB.color(255, 255, 220, 60);
    private static final double TEXT_OFFSET = 0.9D;

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        debugValues.forEachEntity(ModDebugSubscriptions.HUNTER_LOOKOUTS.get(), (hunter, info) -> {
            info.areaCenter().ifPresent(center -> Gizmos.circle(Vec3.atBottomCenterOf(center), info.areaRadius(), GizmoStyle.stroke(AREA_COLOR)));
            info.candidates().forEach(candidate -> Gizmos.cuboid(candidate, 0.1F, GizmoStyle.stroke(CANDIDATE_COLOR)));
            info.spot().ifPresent(spot -> {
                Gizmos.cuboid(spot, 0.0F, GizmoStyle.stroke(SPOT_COLOR));
                Gizmos.line(hunter.getPosition(partialTicks), Vec3.atBottomCenterOf(spot), SPOT_COLOR);
            });

            Vec3 textPos = hunter.getPosition(partialTicks).add(0, hunter.getBbHeight() + TEXT_OFFSET, 0);
            Gizmos.billboardText(info.status(), textPos, TextGizmo.Style.forColorAndCentered(TEXT_COLOR));
        });
    }
}
