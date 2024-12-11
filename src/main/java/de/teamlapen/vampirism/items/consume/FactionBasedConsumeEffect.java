package de.teamlapen.vampirism.items.consume;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.VampirismAPI;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.core.ModItems;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record FactionBasedConsumeEffect(HolderSet<IFaction<?>> faction, ConsumeEffect effects) implements ConsumeEffect {

    public static final MapCodec<FactionBasedConsumeEffect> CODEC = MapCodec.assumeMapUnsafe(RecordCodecBuilder.create(inst -> {
        return inst.group(
                RegistryCodecs.homogeneousList(VampirismRegistries.Keys.FACTION).fieldOf("faction").forGetter(FactionBasedConsumeEffect::faction),
                ConsumeEffect.CODEC.fieldOf("effect").forGetter(FactionBasedConsumeEffect::effects)
        ).apply(inst, FactionBasedConsumeEffect::new);
    }));

    public static final StreamCodec<RegistryFriendlyByteBuf, FactionBasedConsumeEffect> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.fromCodec(RegistryCodecs.homogeneousList(VampirismRegistries.Keys.FACTION)), FactionBasedConsumeEffect::faction,
            ConsumeEffect.STREAM_CODEC, FactionBasedConsumeEffect::effects,
            FactionBasedConsumeEffect::new
    );

    @Override
    public @NotNull Type<? extends ConsumeEffect> getType() {
        return ModItems.FACTION_BASED.get();
    }

    @Override
    public boolean apply(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity entity) {
        Holder<? extends IFaction<?>> faction1 = VampirismAPI.factionRegistry().getFaction(entity);
        if (faction1 == null) {
            faction1 = ModFactions.NEUTRAL;
        }
        if (IFaction.contains(faction, faction1)) {
           return effects.apply(level, stack, entity);
        }
        return false;
    }
}
