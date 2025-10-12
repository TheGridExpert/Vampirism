package de.teamlapen.vampirism.core;

import com.mojang.serialization.Codec;
import de.teamlapen.vampirism.REFERENCE;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class ModMemoryModuleTypes {
    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULE_TYPES = DeferredRegister.create(BuiltInRegistries.MEMORY_MODULE_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> PATROL_COOLDOWN = registerMemory("patrol_cooldown", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> RETREAT_COOLDOWN = registerMemory("retreat_cooldown", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REPOSITIONING_COOLDOWN = registerMemory("repositioning_cooldown", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SHOULD_RETREAT = registerMemory("should_retreat", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> WEAPONS_UNSHEATHED = registerMemory("weapons_unsheathed", Unit.CODEC);
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Long>> WEAPON_SHEATH_COOLDOWN = registerMemory("weapon_sheath_cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Vec3>> AIM_TARGET = registerMemory("air_target");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Set<GlobalPos>>> GATES_TO_CLOSE = registerMemory("gates_to_close");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_HUNTERS = registerMemory("nearest_visible_hunters");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_HOSTILES = registerMemory("nearest_visible_hostiles");

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> registerMemory(String name, Codec<U> codec) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.of(codec)));
    }

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> registerMemory(String name) {
        return MEMORY_MODULE_TYPES.register(name, () -> new MemoryModuleType<>(Optional.empty()));
    }

    static void register(IEventBus bus) {
        MEMORY_MODULE_TYPES.register(bus);
    }
}
