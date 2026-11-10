package de.teamlapen.vampirism.common.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.mojang.serialization.JsonOps;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.VReference;
import de.teamlapen.vampirism.common.util.supporter.Supporter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.RandomSource;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SupporterManager {
    private static final Logger LOGGER = LogManager.getLogger();

    private static final String NAME_KEY_PREFIX = "supporter.vampirism.";
    private static final String SUPPORTERS_PATH = "/supporters.json";

    private Map<Identifier, List<Supporter>> supporters = new HashMap<>();
    private Map<String, Supporter> supportersByPlayer = Map.of();

    /**
     * Returns a randomly picked hunter
     */
    public Supporter getRandomHunter(@NotNull RandomSource rnd) {
        List<Supporter> hunters = this.supporters.getOrDefault(VReference.HUNTER_FACTION_ID, List.of());
        if (!hunters.isEmpty()) {
            return hunters.get(rnd.nextInt(hunters.size()));
        }
        return Supporter.FALLBACK;
    }

    /**
     * Returns a randomly picked vampire
     */
    public Supporter getRandomVampire(@NotNull RandomSource rnd) {
        List<Supporter> vampires = this.supporters.getOrDefault(VReference.VAMPIRE_FACTION_ID, List.of());
        if (!vampires.isEmpty()) {
            return vampires.get(rnd.nextInt(vampires.size()));
        }
        return Supporter.FALLBACK;
    }

    /**
     * Load supporter when we have access to the registries
     */
    @SubscribeEvent(priority = EventPriority.HIGH)
    public void onLoadLevel(LevelEvent.Load event) {
        loadSupporter(event.getLevel().registryAccess());
    }

    public static String getNameKey(String player) {
        return NAME_KEY_PREFIX + player;
    }

    public @Nullable String findTranslatedName(String key, String language) {
        if (!key.startsWith(NAME_KEY_PREFIX)) return null;

        Supporter supporter = this.supportersByPlayer.get(key.substring(NAME_KEY_PREFIX.length()));
        if (supporter == null) return null;

        String exact = supporter.nameTranslations().get(language);
        if (exact != null && !exact.isBlank()) return exact;

        String languagePrefix = language.substring(0, language.indexOf('_') + 1);
        if (languagePrefix.isEmpty()) return null;
        return supporter.nameTranslations().entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(languagePrefix) && !entry.getValue().isBlank())
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    public void loadSupporter(RegistryAccess registryAccess) {
        List<Supporter> supporters = readSupporters(registryAccess);
        this.supporters = supporters.stream().collect(Collectors.groupingBy(Supporter::faction));
        this.supportersByPlayer = supporters.stream().collect(Collectors.toMap(Supporter::player, supporter -> supporter, (first, _) -> first));
    }

    private static List<Supporter> readSupporters(RegistryAccess registryAccess) {
        InputStream inputStream = VampirismMod.class.getResourceAsStream(SUPPORTERS_PATH);
        if (inputStream == null) return List.of();

        try (JsonReader jsonReader = new JsonReader(new InputStreamReader(inputStream))) {
            var ops = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
            return Supporter.CODEC.codec().listOf().parse(ops, JsonParser.parseReader(jsonReader)).getPartialOrThrow();
        } catch (JsonSyntaxException | IOException ex) {
            LOGGER.error("Failed to retrieve supporters from {}", SUPPORTERS_PATH, ex);
            return List.of();
        }
    }

    public Stream<Supporter> getSupporter() {
        return this.supporters.values().stream().flatMap(List::stream);
    }

}
