package de.teamlapen.vampirism.network;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ClientboundRecipesPacket(List<RecipeHolder<?>> recipes) implements CustomPacketPayload {

    public static final Type<ClientboundRecipesPacket> TYPE = new Type<>(VResourceLocation.mod("recipes"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundRecipesPacket> CODEC = StreamCodec.composite(
            RecipeHolder.STREAM_CODEC.apply(ByteBufCodecs.list()), (ClientboundRecipesPacket s) -> s.recipes, ClientboundRecipesPacket::new);

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
