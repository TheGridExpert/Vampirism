package de.teamlapen.vampirism.items.consume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.minion.MinionEntity;
import de.teamlapen.vampirism.items.OblivionItem;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class OblivionEffect implements ConsumeEffect {

    private static final OblivionEffect INSTANCE = new OblivionEffect();
    public static final MapCodec<OblivionEffect> CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, OblivionEffect> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public @NotNull Type<? extends ConsumeEffect> getType() {
        return ModItems.OBLIVION.get();
    }

    @Override
    public boolean apply(@NotNull Level level, @NotNull ItemStack itemstack, @NotNull LivingEntity livingEntity) {
        if (livingEntity instanceof Player player) {
            FactionPlayerHandler.get(player).getCurrentSkillPlayer().ifPresent(OblivionItem::applyEffect);
        }
        if (livingEntity instanceof MinionEntity<?> minion) {
            minion.getMinionData().ifPresent(d -> d.upgradeStat(-1, minion));
        }
        return true;
    }
}
