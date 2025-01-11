package de.teamlapen.vampirism.items.consume;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.minion.VampireMinionEntity;
import de.teamlapen.vampirism.entity.player.vampire.BloodStats;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodConstants;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.ConsumableListener;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record BloodFoodProperties(int blood, float saturation, boolean canAlwaysEat) implements ConsumableListener {

    public static final Codec<BloodFoodProperties> DIRECT_CODEC = RecordCodecBuilder.create(
            p_366390_ -> p_366390_.group(
                            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("nutrition").forGetter(BloodFoodProperties::blood),
                            Codec.FLOAT.fieldOf("saturation").forGetter(BloodFoodProperties::saturation),
                            Codec.BOOL.optionalFieldOf("can_always_eat", false).forGetter(BloodFoodProperties::canAlwaysEat)
                    )
                    .apply(p_366390_, BloodFoodProperties::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, BloodFoodProperties> DIRECT_STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            BloodFoodProperties::blood,
            ByteBufCodecs.FLOAT,
            BloodFoodProperties::saturation,
            ByteBufCodecs.BOOL,
            BloodFoodProperties::canAlwaysEat,
            BloodFoodProperties::new
    );

    @Override
    public void onConsume(@NotNull Level level, @NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull Consumable consumable) {
        RandomSource randomsource = entity.getRandom();
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), consumable.sound().value(), SoundSource.NEUTRAL, 1.0F, randomsource.triangle(1.0F, 0.4F));
        switch (entity) {
            case Player player -> FactionPlayerHandler.get(player).factionPlayer(ModFactions.VAMPIRE).ifPresent(vampire -> {
                        ((BloodStats) vampire.getBloodStats()).eat(this);
                        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5F, Mth.randomBetween(randomsource, 0.9F, 1.0F));
                    });
            case VampireMinionEntity minion -> minion.eat(level, stack, this);
            default -> {
            }
        }
    }

    public static class Builder {
        private int blood;
        private float saturationModifier;
        private boolean canAlwaysEat;

        public Builder blood(int blood) {
            this.blood = blood;
            return this;
        }

        public Builder saturationModifier(float saturationModifier) {
            this.saturationModifier = saturationModifier;
            return this;
        }

        public Builder alwaysEdible() {
            this.canAlwaysEat = true;
            return this;
        }

        public BloodFoodProperties build() {
            float f = FoodConstants.saturationByModifier(this.blood, this.saturationModifier);
            return new BloodFoodProperties(this.blood, f, this.canAlwaysEat);
        }
    }
}
