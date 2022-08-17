package de.teamlapen.vampirism.advancements;

import com.google.gson.JsonObject;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.entity.factions.IPlayableFaction;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TriggerFaction extends SimpleCriterionTrigger<TriggerFaction.Instance> {
    public static final ResourceLocation ID = new ResourceLocation(REFERENCE.MODID, "faction");

    private final static Logger LOGGER = LogManager.getLogger();

    public static @NotNull Instance level(@Nullable IPlayableFaction<?> faction, int level) {
        return new Instance(Type.LEVEL, faction, level);
    }

    public static @NotNull Instance lord(@Nullable IPlayableFaction<?> faction, int lordLevel) {
        return new Instance(Type.LORD, faction, lordLevel);
    }

    @NotNull
    @Override
    public ResourceLocation getId() {
        return ID;
    }

    /**
     * Trigger this criterion
     */
    public void trigger(@NotNull ServerPlayer playerMP, IPlayableFaction<?> faction, int level, int lordLevel) {
        this.trigger(playerMP, (instance -> instance.test(faction, level, lordLevel)));
    }

    @NotNull
    @Override
    protected Instance createInstance(@NotNull JsonObject json, @NotNull EntityPredicate.Composite entityPredicate, @NotNull DeserializationContext conditionsParser) {
        IPlayableFaction<?> playableFaction = null;
        Type type = json.has("type") ? Type.valueOf(json.get("type").getAsString()) : Type.LEVEL;
        if (json.has("faction")) {
            String idStr = json.get("faction").getAsString();
            if (!"null".equals(idStr)) {
                ResourceLocation id = new ResourceLocation(json.get("faction").getAsString());
                IFaction<?> faction = VampirismAPI.factionRegistry().getFactionByID(id);
                if (faction instanceof IPlayableFaction<?> playableFaction1) {
                    playableFaction = playableFaction1;
                } else {
                    LOGGER.warn("Given faction name does not exist or is not a playable faction: {}", id);
                }
            }
        }
        int level = json.has("level") ? json.get("level").getAsInt() : 1;
        return new Instance(type, playableFaction, level);
    }

    public enum Type {
        LEVEL, LORD
    }

    static class Instance extends AbstractCriterionTriggerInstance {

        @NotNull
        private final Type type;
        @Nullable
        private final IPlayableFaction<?> faction;
        private final int level;

        Instance(@NotNull Type type, @Nullable IPlayableFaction<?> faction, int level) {
            super(ID, EntityPredicate.Composite.ANY);
            this.type = type;
            this.faction = faction;
            this.level = level;
        }

        @NotNull
        @Override
        public JsonObject serializeToJson(@NotNull SerializationContext serializer) {
            JsonObject json = super.serializeToJson(serializer);
            json.addProperty("type", type.name());
            json.addProperty("faction", faction == null ? "null" : faction.getID().toString());
            json.addProperty("level", level);
            return json;
        }

        public boolean test(IPlayableFaction<?> faction, int level, int lordLevel) {
            if (this.faction == null || this.faction.equals(faction)) {
                if (type == Type.LEVEL) {
                    return level >= this.level;
                } else if (type == Type.LORD) {
                    return lordLevel >= this.level;
                }
            }
            return false;
        }

    }
}
