package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

import java.util.Optional;

public record BrewingStation(BlockPos pos, BlockPos stand, boolean portable, boolean placed) {

    public static final Codec<BrewingStation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BrewingStation::pos),
            BlockPos.CODEC.optionalFieldOf("stand").forGetter(station -> Optional.of(station.stand())),
            Codec.BOOL.fieldOf("portable").forGetter(BrewingStation::portable),
            Codec.BOOL.fieldOf("placed").forGetter(BrewingStation::placed)
    ).apply(instance, (pos, stand, portable, placed) -> new BrewingStation(pos, stand.orElse(pos), portable, placed)));

    public static BrewingStation existing(BlockPos pos) {
        return new BrewingStation(pos, pos, false, false);
    }

    public static BrewingStation portable(BlockPos pos, BlockPos stand) {
        return new BrewingStation(pos, stand, true, false);
    }

    public BrewingStation markPlaced() {
        return new BrewingStation(this.pos, this.stand, this.portable, true);
    }
}
