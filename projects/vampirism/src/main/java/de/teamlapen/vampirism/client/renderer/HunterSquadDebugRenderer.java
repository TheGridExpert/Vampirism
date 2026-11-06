package de.teamlapen.vampirism.client.renderer;

import de.teamlapen.vampirism.common.core.ModDebugSubscriptions;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquadDebugInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.debug.DebugValueAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.UUID;

public class HunterSquadDebugRenderer implements DebugRenderer.SimpleDebugRenderer {

    private static final int TARGET_COLOR = ARGB.color(255, 255, 80, 80);
    private static final int POSITION_COLOR = ARGB.color(255, 80, 200, 255);
    private static final int TEXT_COLOR = ARGB.color(255, 255, 255, 255);
    private static final float LINK_WIDTH = 3.0F;
    private static final float TEXT_SCALE = 0.32F;
    private static final double HEAD_OFFSET = 0.3D;

    private final Minecraft minecraft;

    public HunterSquadDebugRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
    }

    @Override
    public void emitGizmos(double camX, double camY, double camZ, DebugValueAccess debugValues, Frustum frustum, float partialTicks) {
        ClientLevel level = this.minecraft.level;
        if (level == null) return;

        debugValues.forEachEntity(ModDebugSubscriptions.HUNTER_SQUADS.get(), (hunter, info) -> {
            int squadColor = getSquadColor(info.squadId());
            Vec3 head = getAboveHeadPosition(hunter, partialTicks);

            if (info.leaderId() == hunter.getId()) {
                Gizmos.point(head, squadColor, 12.0F);
            } else {
                Entity leader = level.getEntity(info.leaderId());
                if (leader != null) {
                    Gizmos.line(getAboveHeadPosition(leader, partialTicks), head, squadColor, LINK_WIDTH);
                }
            }

            info.targetId().map(level::getEntity).ifPresent(target -> Gizmos.arrow(hunter.getEyePosition(partialTicks), target.getPosition(partialTicks).add(0, target.getBbHeight() / 2, 0), TARGET_COLOR));

            info.position().ifPresent(position -> {
                Gizmos.line(hunter.getPosition(partialTicks), position, POSITION_COLOR);
                Gizmos.cuboid(new AABB(position.subtract(0.25, 0, 0.25), position.add(0.25, 0.5, 0.25)), GizmoStyle.stroke(POSITION_COLOR));
            });

            renderInfoLines(hunter, info, squadColor);
        });
    }

    private static void renderInfoLines(Entity hunter, HunterSquadDebugInfo info, int squadColor) {
        List<String> lines = info.lines();
        boolean leader = info.leaderId() == hunter.getId();
        for (int i = 0; i < lines.size(); i++) {
            int color = leader && i == 0 ? squadColor : TEXT_COLOR;
            Gizmos.billboardTextOverMob(hunter, lines.size() - 1 - i, lines.get(i), color, TEXT_SCALE);
        }
    }

    private static Vec3 getAboveHeadPosition(Entity entity, float partialTicks) {
        return entity.getPosition(partialTicks).add(0, entity.getBbHeight() + HEAD_OFFSET, 0);
    }

    private static int getSquadColor(UUID squadId) {
        float hue = (squadId.hashCode() & 0xFFFF) / (float) 0xFFFF;
        return Mth.hsvToArgb(hue, 0.75F, 1.0F, 255);
    }
}
