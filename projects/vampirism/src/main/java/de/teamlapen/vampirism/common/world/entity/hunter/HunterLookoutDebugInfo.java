package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolZone;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.ai.Brain;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public record HunterLookoutDebugInfo(Optional<BlockPos> areaCenter, int areaRadius, List<BlockPos> candidates, Optional<BlockPos> spot, String status) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HunterLookoutDebugInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), HunterLookoutDebugInfo::areaCenter,
            ByteBufCodecs.VAR_INT, HunterLookoutDebugInfo::areaRadius,
            BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), HunterLookoutDebugInfo::candidates,
            ByteBufCodecs.optional(BlockPos.STREAM_CODEC), HunterLookoutDebugInfo::spot,
            ByteBufCodecs.STRING_UTF8, HunterLookoutDebugInfo::status,
            HunterLookoutDebugInfo::new
    );

    public static @Nullable HunterLookoutDebugInfo of(Hunter hunter) {
        if (!hunter.isRangedClass()) return null;

        Brain<Hunter> brain = hunter.getBrain();
        PatrolArea area = hunter.getPatrolArea();

        PatrolZone zone = hunter.getPatrolZone();
        String status = describeStatus(brain) + (zone != null ? " | zone " + zone.size() + (zone.contains(hunter.blockPosition()) ? "" : " (outside)") : "");

        return new HunterLookoutDebugInfo(
                Optional.ofNullable(area).map(PatrolArea::center),
                area != null ? area.radius() : 0,
                brain.getMemory(ModMemoryTypes.LOOKOUT_CANDIDATES.get()).orElse(List.of()),
                brain.getMemory(ModMemoryTypes.LOOKOUT_SPOT.get()),
                status
        );
    }

    private static String describeStatus(Brain<Hunter> brain) {
        String status = brain.getMemory(ModMemoryTypes.LOOKOUT_STATUS.get()).orElse("not tried yet");
        long cooldown = brain.getTimeUntilExpiry(ModMemoryTypes.LOOKOUT_COOLDOWN.get());
        return cooldown > 0 ? "Lookout: " + status + " | next in " + cooldown / 20 + "s" : "Lookout: " + status;
    }
}
