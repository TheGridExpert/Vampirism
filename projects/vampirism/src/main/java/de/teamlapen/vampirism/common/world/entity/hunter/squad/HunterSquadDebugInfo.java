package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.AdvancedHunter;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

public record HunterSquadDebugInfo(UUID squadId, int leaderId, Optional<Integer> targetId, Optional<Vec3> position, List<String> lines) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HunterSquadDebugInfo> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, HunterSquadDebugInfo::squadId,
            ByteBufCodecs.VAR_INT, HunterSquadDebugInfo::leaderId,
            ByteBufCodecs.optional(ByteBufCodecs.VAR_INT), HunterSquadDebugInfo::targetId,
            ByteBufCodecs.optional(Vec3.STREAM_CODEC), HunterSquadDebugInfo::position,
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), HunterSquadDebugInfo::lines,
            HunterSquadDebugInfo::new
    );

    public static @Nullable HunterSquadDebugInfo of(Hunter hunter) {
        HunterSquad squad = hunter.getSquad();
        if (squad == null) return null;

        Hunter leader = squad.leader();
        Brain<Hunter> brain = hunter.getBrain();
        Optional<LivingEntity> order = brain.getMemory(ModMemoryTypes.SQUAD_TARGET.get());
        Optional<LivingEntity> target = order.isPresent() ? order : brain.getMemory(MemoryModuleType.ATTACK_TARGET);

        List<String> lines = new ArrayList<>();
        if (hunter == leader) {
            String mode = squad.isOnGuard(hunter.level().getGameTime()) ? "on guard, threats " + squad.threatCount() : "relaxed";
            String post = squad.post() != null ? " | post r" + squad.post().radius() : "";
            String header = "Leader | " + squad.id().toString().substring(0, 4) + " | " + squad.members().size() + "/" + squad.maxSize() + " | " + mode + post + describeBanner(hunter, squad);
            lines.add(squad.isRetreating() ? header + " | RETREAT" : header);
        }
        String activity = brain.getActiveNonCoreActivity().map(Activity::getName).orElse("none");
        lines.add(hunter.getHunterClass().getSerializedName() + " L" + hunter.getFactionLevel() + (hunter instanceof AdvancedHunter advanced ? " (lord " + advanced.getLordLevel() + ")" : "") + " | " + activity + " | " + Math.round(hunter.getHealth()) + "/" + Math.round(hunter.getMaxHealth()) + " HP");
        lines.add(describeOrder(brain, order, target));

        return new HunterSquadDebugInfo(squad.id(), leader != null ? leader.getId() : hunter.getId(), target.map(LivingEntity::getId), brain.getMemory(ModMemoryTypes.SQUAD_POSITION.get()), lines);
    }

    private static String describeBanner(Hunter leader, HunterSquad squad) {
        long cooldown = squad.bannerCooldownUntil() - leader.level().getGameTime();
        String cooldownText = cooldown > 0 ? " " + cooldown / 20 + "s" : "";
        if (leader.hasOminousBanner()) return " | ominous banner";
        if (leader.hasBanner()) return " | banner" + cooldownText;
        return cooldown > 0 ? " | banner cooldown" + cooldownText : "";
    }

    private static String describeOrder(Brain<Hunter> brain, Optional<LivingEntity> order, Optional<LivingEntity> target) {
        if (brain.hasMemoryValue(ModMemoryTypes.SQUAD_RETREAT.get())) return "Order: group retreat";
        if (brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())) return "Retreating";
        String targetName = target.map(entity -> entity.getType().getDescription().getString().toLowerCase(Locale.ROOT)).orElse(null);
        if (order.isPresent()) return "Order: attack " + targetName;
        if (brain.hasMemoryValue(ModMemoryTypes.SQUAD_POSITION.get())) return targetName != null ? "Order: move | own target " + targetName : "Order: move";
        return targetName != null ? "Own target " + targetName : "No order";
    }
}
