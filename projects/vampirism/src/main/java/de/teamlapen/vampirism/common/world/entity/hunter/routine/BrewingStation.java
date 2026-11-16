package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;

public record BrewingStation(BlockPos pos, boolean portable, boolean placed) {

    public static final Codec<BrewingStation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(BrewingStation::pos),
            Codec.BOOL.fieldOf("portable").forGetter(BrewingStation::portable),
            Codec.BOOL.fieldOf("placed").forGetter(BrewingStation::placed)
    ).apply(instance, BrewingStation::new));

    public BrewingStation markPlaced() {
        return new BrewingStation(this.pos, this.portable, true);
    }
}
