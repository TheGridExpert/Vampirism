package de.teamlapen.vampirism.items.consume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.EnumStrength;
import de.teamlapen.vampirism.api.entity.vampire.IVampire;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.player.vampire.VampirePlayer;
import de.teamlapen.vampirism.util.ByteBufferCodecUtil;
import de.teamlapen.vampirism.util.CodecUtil;
import de.teamlapen.vampirism.util.DamageHandler;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jetbrains.annotations.NotNull;

public record AffectGarlic(EnumStrength strength, float multiplier, boolean ambient) implements ConsumeEffect {

    public static final MapCodec<AffectGarlic> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            StringRepresentable.fromEnum(EnumStrength::values).fieldOf("strength").forGetter(AffectGarlic::strength),
            Codec.FLOAT.fieldOf("multiplier").forGetter(AffectGarlic::multiplier),
            Codec.BOOL.fieldOf("ambient").forGetter(AffectGarlic::ambient)
    ).apply(inst, AffectGarlic::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, AffectGarlic> STREAM_CODEC = StreamCodec.composite(
            NeoForgeStreamCodecs.enumCodec(EnumStrength.class), AffectGarlic::strength,
            ByteBufCodecs.FLOAT, AffectGarlic::multiplier,
            ByteBufCodecs.BOOL, AffectGarlic::ambient,
            AffectGarlic::new
    );

    public AffectGarlic(EnumStrength strength) {
        this(strength, 20, false);
    }

    @Override
    public @NotNull Type<? extends ConsumeEffect> getType() {
        return ModItems.AFFECT_GARLIC.get();
    }

    @Override
    public boolean apply(@NotNull Level level, @NotNull ItemStack stack, @NotNull LivingEntity entity) {
        if (Helper.isVampire(entity)) {
            IVampire vampire = null;
            if (entity instanceof IVampire) {
                vampire = (IVampire) entity;
            } else if (entity instanceof Player player) {
                vampire = VampirePlayer.get(player);
            }
            if (vampire != null) {
                DamageHandler.affectVampireGarlic(vampire, strength, multiplier, ambient);
            }
        }
        return false;
    }
}
