package de.teamlapen.vampirism.common.world.structures.huntercamp;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.Pools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;

import static de.teamlapen.vampirism.common.core.ModStructures.createTemplatePool;
import static de.teamlapen.vampirism.common.world.structures.PoolExtensions.single;

public class HunterCampPools {

    public static final ResourceKey<StructureTemplatePool> CAMPFIRES = createTemplatePool("hunter_camp/campfires");
    public static final ResourceKey<StructureTemplatePool> CAMPFIRE_LOGS_1 = createTemplatePool("hunter_camp/campfire_logs_1");
    public static final ResourceKey<StructureTemplatePool> CAMPFIRE_LOGS_2 = createTemplatePool("hunter_camp/campfire_logs_2");
    public static final ResourceKey<StructureTemplatePool> FLAGPOLES = createTemplatePool("hunter_camp/flagpoles");
    public static final ResourceKey<StructureTemplatePool> FLAGPOLE_SEGMENTS = createTemplatePool("hunter_camp/flagpole_segments");
    public static final ResourceKey<StructureTemplatePool> FLAGS = createTemplatePool("hunter_camp/flags");
    public static final ResourceKey<StructureTemplatePool> TENTS = createTemplatePool("hunter_camp/tents");
    public static final ResourceKey<StructureTemplatePool> WATCHTOWERS = createTemplatePool("hunter_camp/watchtowers");
    public static final ResourceKey<StructureTemplatePool> COLD_WATCHTOWERS = createTemplatePool("hunter_camp/cold_watchtowers");

    public static void bootstrap(BootstrapContext<StructureTemplatePool> context) {
        HolderGetter<StructureTemplatePool> templatePools = context.lookup(Registries.TEMPLATE_POOL);
        Holder<StructureTemplatePool> empty = templatePools.getOrThrow(Pools.EMPTY);

        context.register(CAMPFIRES, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/campfire_place/campfire_place_1"), 1), Pair.of(single("hunter_camp/campfire_place/campfire_place_2"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(CAMPFIRE_LOGS_1, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/log/log_1"), 7), Pair.of(single("hunter_camp/log/log_none"), 3)), StructureTemplatePool.Projection.RIGID));
        context.register(CAMPFIRE_LOGS_2, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/log/log_2"), 7), Pair.of(single("hunter_camp/log/log_none"), 3)), StructureTemplatePool.Projection.RIGID));
        context.register(FLAGPOLES, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/flag/flagpole"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(FLAGPOLE_SEGMENTS, new StructureTemplatePool(templatePools.getOrThrow(FLAGS), ImmutableList.of(Pair.of(single("hunter_camp/flag/flagpole_segment"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(FLAGS, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/flag/flag"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(TENTS, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/tent/tent"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(WATCHTOWERS, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/watchtower/watchtower_1"), 1), Pair.of(single("hunter_camp/watchtower/watchtower_2"), 1)), StructureTemplatePool.Projection.RIGID));
        context.register(COLD_WATCHTOWERS, new StructureTemplatePool(empty, ImmutableList.of(Pair.of(single("hunter_camp/watchtower/watchtower_3"), 1)), StructureTemplatePool.Projection.RIGID));
    }
}
