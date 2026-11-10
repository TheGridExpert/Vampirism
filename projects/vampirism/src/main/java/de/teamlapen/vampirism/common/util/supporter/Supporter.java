package de.teamlapen.vampirism.common.util.supporter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.faction.api.Factions;
import de.teamlapen.vampirism.api.world.items.components.IVampireBook;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterClassType;
import net.minecraft.core.Holder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.attachment.IAttachmentHolder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public record Supporter(Identifier faction, Component name, Map<String, String> nameTranslations, String player, Map<String, String> appearance, Optional<Holder<IVampireBook>> book, Overrides overrides) {

    public static final Supporter FALLBACK = new Supporter(Factions.Keys.NEUTRAL, Component.empty(), Map.of(), "", Map.of(), Optional.empty(), Overrides.NONE);

    public static final MapCodec<Supporter> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Identifier.CODEC.fieldOf("faction").forGetter(Supporter::faction),
            ComponentSerialization.CODEC.fieldOf("name").forGetter(Supporter::name),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("name_translations", Map.of()).forGetter(Supporter::nameTranslations),
            Codec.STRING.fieldOf("player").forGetter(Supporter::player),
            Codec.unboundedMap(Codec.STRING, Codec.STRING).optionalFieldOf("appearance", Map.of()).forGetter(Supporter::appearance),
            IVampireBook.HOLDER_CODEC.optionalFieldOf("book").orElse(Optional.empty()).forGetter(Supporter::book),
            Overrides.CODEC.optionalFieldOf("overrides", Overrides.NONE).forGetter(Supporter::overrides)
    ).apply(inst, Supporter::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, Supporter> STREAM_CODEC = StreamCodec.composite(
            Identifier.STREAM_CODEC, Supporter::faction,
            ComponentSerialization.STREAM_CODEC, Supporter::name,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), Supporter::nameTranslations,
            ByteBufCodecs.STRING_UTF8, Supporter::player,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.STRING_UTF8), Supporter::appearance,
            ByteBufCodecs.optional(IVampireBook.HOLDER_STREAM_CODEC), Supporter::book,
            Overrides.STREAM_CODEC, Supporter::overrides,
            Supporter::new
    );

    public static Supporter defaultSupplier(IAttachmentHolder attachment) {
        return FALLBACK;
    }

    public record Overrides(Optional<HunterClassType> hunterClass, boolean dualWielding) {

        public static final Overrides NONE = new Overrides(Optional.empty(), false);

        public static final Codec<Overrides> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                HunterClassType.CODEC.optionalFieldOf("hunter_class").forGetter(Overrides::hunterClass),
                Codec.BOOL.optionalFieldOf("dual_wielding", false).forGetter(Overrides::dualWielding)
        ).apply(inst, Overrides::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Overrides> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.optional(HunterClassType.STREAM_CODEC), Overrides::hunterClass,
                ByteBufCodecs.BOOL, Overrides::dualWielding,
                Overrides::new
        );
    }
}
