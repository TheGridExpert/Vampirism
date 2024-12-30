package de.teamlapen.vampirism.entity.player.tasks.reward;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.task.ITaskRewardInstance;
import de.teamlapen.vampirism.core.ModTasks;
import de.teamlapen.vampirism.util.MapUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import org.jetbrains.annotations.Nullable;

public class MapReward extends ItemReward {

    public static final MapCodec<MapReward> CODEC = RecordCodecBuilder.mapCodec(inst ->
            inst.group(
                    TagKey.codec(Registries.STRUCTURE).fieldOf("destination").forGetter(i -> i.destination),
                    Codec.STRING.fieldOf("displayName").forGetter(i -> i.displayName),
                    MapDecorationType.CODEC.fieldOf("decorationType").forGetter(i -> i.decorationType)
            ).apply(inst, MapReward::new));

    public final TagKey<Structure> destination;
    private final String displayName;
    private final Holder<MapDecorationType> decorationType;

    public MapReward(TagKey<Structure> destination, String displayName, Holder<MapDecorationType> decorationType) {
        super(new ItemStack(Items.FILLED_MAP));
        this.destination = destination;
        this.displayName = displayName;
        this.decorationType = decorationType;
    }

    @Override
    public ITaskRewardInstance createInstance(@Nullable IFactionPlayer<?> player) {
        return new MapReward.Instance(destination, displayName, decorationType);
    }

    @Override
    public MapCodec<MapReward> codec() {
        return ModTasks.MAP_REWARD.get();
    }

    public record Instance(TagKey<Structure> destination, String displayName, Holder<MapDecorationType> decorationType) implements ITaskRewardInstance {

        public static final MapCodec<MapReward.Instance> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        TagKey.codec(Registries.STRUCTURE).fieldOf("destination").forGetter(i -> i.destination),
                        Codec.STRING.fieldOf("displayName").forGetter(i -> i.displayName),
                        MapDecorationType.CODEC.fieldOf("decorationType").forGetter(i -> i.decorationType)
                ).apply(inst, MapReward.Instance::new));

        @Override
        public void applyReward(IFactionPlayer<?> player) {
            ItemStack reward = MapUtil.getMap(player.asEntity(), destination, displayName, decorationType, 150);
            if (reward != null) {
                if (!player.asEntity().addItem(reward.copy())) {
                    player.asEntity().drop(reward.copy(), true);
                }
            }
        }

        @Override
        public MapCodec<? extends ITaskRewardInstance> codec() {
            return ModTasks.MAP_REWARD_INSTANCE.get();
        }
    }
}
