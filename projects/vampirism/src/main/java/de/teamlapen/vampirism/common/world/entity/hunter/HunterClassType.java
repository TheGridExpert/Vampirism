package de.teamlapen.vampirism.common.world.entity.hunter;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.neoforged.fml.common.asm.enumextension.IExtensibleEnum;

import java.util.Locale;

public enum HunterClassType implements StringRepresentable, IExtensibleEnum {
    MELEE("melee", 6),
    RANGED("ranged", 4);

    public static final Codec<HunterClassType> CODEC = StringRepresentable.fromEnum(HunterClassType::values);
    public static final StreamCodec<ByteBuf, HunterClassType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(HunterClassType::get, HunterClassType::getSerializedName);

    private final String name;
    private final int weight;

    HunterClassType(String name, int weight) {
        this.name = name;
        this.weight = weight;
    }

    public static HunterClassType getRandom(RandomSource random) {
        int totalWeight = 0;
        for (HunterClassType classType : values()) {
            totalWeight += classType.weight;
        }

        int roll = random.nextInt(totalWeight);
        for (HunterClassType classType : values()) {
            roll -= classType.weight;
            if (roll < 0) {
                return classType;
            }
        }

        return MELEE;
    }

    public static HunterClassType get(String value) {
        try {
            return HunterClassType.valueOf(value.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return MELEE;
        }
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return this.name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
