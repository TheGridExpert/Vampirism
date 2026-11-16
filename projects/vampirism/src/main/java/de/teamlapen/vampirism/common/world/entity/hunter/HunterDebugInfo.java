package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.routine.BrewingStation;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public record HunterDebugInfo(List<String> lines) {

    public static final StreamCodec<RegistryFriendlyByteBuf, HunterDebugInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), HunterDebugInfo::lines,
            HunterDebugInfo::new
    );

    public static HunterDebugInfo of(Hunter hunter) {
        Brain<Hunter> brain = hunter.getBrain();
        List<String> lines = new ArrayList<>();

        String lord = hunter instanceof AdvancedHunter advanced ? " (lord " + advanced.getLordLevel() + ")" : "";
        String activity = brain.getActiveNonCoreActivity().map(Activity::getName).orElse("none");
        lines.add(hunter.getHunterClass().getSerializedName() + " L" + hunter.getFactionLevel() + lord + " | " + activity + " | " + Math.round(hunter.getHealth()) + "/" + Math.round(hunter.getMaxHealth()) + " HP");
        lines.add("Sheathed: " + describeSheathed(hunter));

        HunterPost post = hunter.getPost();
        String postText = post != null ? "Post r" + post.radius() + " at " + post.center().toShortString() : "No post";
        String pastime = brain.getMemory(ModMemoryTypes.PASTIME.get()).map(value -> " | pastime " + value.name().toLowerCase(Locale.ROOT)).orElse("");
        lines.add(postText + pastime + " | fishing interest " + String.format(Locale.ROOT, "%.2f", hunter.getFishingInterest()));

        if (hunter.isAlchemistClass()) {
            lines.add(describePotions(hunter, brain));
        }

        String state = describeState(hunter, brain);
        if (!state.isEmpty()) lines.add(state);

        String effects = hunter.getActiveEffects().stream()
                .map(effect -> effect.getEffect().value().getDisplayName().getString() + " " + effect.getDuration() / 20 + "s")
                .collect(Collectors.joining(", "));
        if (!effects.isEmpty()) lines.add("Effects: " + effects);

        return new HunterDebugInfo(lines);
    }

    private static String describePotions(Hunter hunter, Brain<Hunter> brain) {
        String text = "Potions " + hunter.getPotionCount() + "/" + Hunter.MAX_POTIONS;
        long cooldown = brain.getTimeUntilExpiry(ModMemoryTypes.POTION_COOLDOWN.get());
        if (cooldown > 0) text += " | throw in " + cooldown / 20 + "s";

        BrewingStation station = brain.getMemory(ModMemoryTypes.BREWING_STATION.get()).orElse(null);
        if (station == null) return hunter.needsPotions() ? text + " | brewing in the field" : text;

        String kind = station.portable() ? "own stand" : "station";
        String status = brain.hasMemoryValue(ModMemoryTypes.BREWING.get()) ? "brewing at " : "going to ";
        return text + " | " + status + kind + " " + station.pos().toShortString();
    }

    private static String describeState(Hunter hunter, Brain<Hunter> brain) {
        List<String> flags = new ArrayList<>();
        if (brain.hasMemoryValue(ModMemoryTypes.WEAPONS_UNSHEATHED.get())) flags.add("armed");
        if (brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())) flags.add("retreating");
        if (brain.hasMemoryValue(ModMemoryTypes.DANGER.get())) flags.add("evading danger");
        if (hunter.isUsingItem()) flags.add("using " + describe(hunter.getUseItem()));
        return String.join(" | ", flags);
    }

    private static String describeSheathed(Hunter hunter) {
        String sheathed = IntStream.rangeClosed(0, Hunter.CRUCIFIX_SLOT)
                .mapToObj(hunter::getSheathedWeapon)
                .filter(stack -> !stack.isEmpty())
                .map(HunterDebugInfo::describe)
                .collect(Collectors.joining(", "));
        return sheathed.isEmpty() ? "-" : sheathed;
    }

    private static String describe(ItemStack stack) {
        return stack.isEmpty() ? "-" : stack.getHoverName().getString();
    }
}
