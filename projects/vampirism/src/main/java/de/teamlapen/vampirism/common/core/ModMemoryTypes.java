package de.teamlapen.vampirism.common.core;

import com.mojang.serialization.Codec;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.common.util.serialization.ModCodecs;
import de.teamlapen.vampirism.common.world.entity.ai.memory.HurtByEntities;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.PatrolArea;
import de.teamlapen.vampirism.common.world.entity.ai.system.AiSystem;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ModMemoryTypes {

    public static final DeferredRegister<MemoryModuleType<?>> MEMORY_MODULES = DeferredRegister.create(Registries.MEMORY_MODULE_TYPE, REFERENCE.MODID);

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> ACTION_ACTIVE = unit("action.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> ACTION_COOLDOWN = unit("action.cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<HurtByEntities>> HURT_BY_ENTITIES = MEMORY_MODULES.register("hurt_by_entities", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Set<UUID>>> ALLIES = MEMORY_MODULES.register("allies", () -> new MemoryModuleType<>(Optional.of(ModCodecs.set(UUIDUtil.CODEC))));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<AiSystem<?>>> AI_SYSTEM = MEMORY_MODULES.register("ai_system", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Long>> ACTION_ACTIVE_SINCE = longInt("action.active_since");

    //<editor-fold desc="Dracula">
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> DRACULA_PHASE_1 = unit("dracula.phase1");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> DRACULA_PHASE_2 = unit("dracula.phase2");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> DRACULA_PHASE_3 = unit("dracula.phase3");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Long>> SURROUNDED_SINCE = longInt("dracula.surrounded_timer");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SURROUNDED_COOLDOWN = unit("dracula.surrounded_cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_ATTACKABLE = MEMORY_MODULES.register("nearest_attackable", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<NearestVisibleLivingEntities>> NEAREST_VISIBLE_ATTACKABLE = MEMORY_MODULES.register("nearest_visible_attackable", () -> new MemoryModuleType<>(Optional.empty()));
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> KNOCKED_BACK = unit("dracula.knocked_back");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Set<UUID>>> AGGRESSORS = MEMORY_MODULES.register("dracula.aggressors", () -> new MemoryModuleType<>(Optional.of(ModCodecs.set(UUIDUtil.CODEC))));
    //<editor-fold desc="Action">
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_PROTECTOR_ACTIVE = unit("action.summon.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_PROTECTOR_COOLDOWN = unit("action.summon.cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<UUID>>> SUMMONS = MEMORY_MODULES.register("action.summon.summons", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC.listOf())));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_VAMPIRE_BATS_ACTIVE = unit("action.vampire_bat.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SUMMON_VAMPIRE_BATS_COOLDOWN = unit("action.vampire_bat.cooldown");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_ACTIVE = unit("action.flying_sword.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_COOLDOWN = unit("action.flying_sword.cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_EQUIPPED = unit("action.flying_sword.equipped");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_SWORD_SHOT = unit("action.flying_sword.shot");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_NEEDLE_ACTIVE = unit("action.flying_needle.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> FLYING_NEEDLE_COOLDOWN = unit("action.flying_needle.cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<UUID>>> FLYING_NEEDLES = MEMORY_MODULES.register("action.flying_needle.needles", () -> new MemoryModuleType<>(Optional.of(UUIDUtil.CODEC.listOf())));

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BACKSTAB_ACTIVE = unit("action.backstab.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BACKSTAB_COOLDOWN = unit("action.backstab.cooldown");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_PROJECTILES_ACTIVE = unit("action.blood_projectiles.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_PROJECTILES_COOLDOWN = unit("action.blood_projectiles.cooldown");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REGENERATION_ACTIVE = unit("action.regeneration.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REGENERATION_COOLDOWN = unit("action.regeneration.cooldown");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_SIPHON_ACTIVE = unit("action.blood_siphon.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BLOOD_SIPHON_COOLDOWN = unit("action.blood_siphon.cooldown");

    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BAT_SWARM_DASH_ACTIVE = unit("action.bat_swarm_dash.active");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> BAT_SWARM_DASH_COOLDOWN = unit("action.bat_swarm_dash.cooldown");
    //</editor-fold>
    //</editor-fold>

    //<editor-fold desc="Hunter">
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> PATROL_COOLDOWN = unit("hunter.patrol_cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> REPOSITIONING_COOLDOWN = unit("hunter.repositioning_cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SHOULD_RETREAT = unit("hunter.should_retreat");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> WEAPONS_UNSHEATHED = unit("hunter.weapons_unsheathed");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Long>> WEAPON_SHEATH_COOLDOWN = transientMemory("hunter.weapon_sheath_cooldown");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Vec3>> AIM_TARGET = transientMemory("hunter.aim_target");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> CURE_TARGET = transientMemory("hunter.cure_target");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> SEE_TIME = transientMemory("hunter.see_time");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Set<GlobalPos>>> GATES_TO_CLOSE = transientMemory("hunter.gates_to_close");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_HUNTERS = transientMemory("hunter.nearest_visible_hunters");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_HOSTILES = transientMemory("hunter.nearest_visible_hostiles");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<List<LivingEntity>>> NEAREST_VISIBLE_INFECTED_ENTITIES = transientMemory("hunter.nearest_visible_infected_entities");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> DANGER = transientMemory("hunter.danger");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<LivingEntity>> SQUAD_TARGET = transientMemory("hunter.squad_target");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Vec3>> SQUAD_POSITION = transientMemory("hunter.squad_position");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> SQUAD_RETREAT = transientMemory("hunter.squad_retreat");
    public static final DeferredHolder<MemoryModuleType<?>, MemoryModuleType<PatrolArea>> SQUAD_PATROL_AREA = transientMemory("hunter.squad_patrol_area");
    //</editor-fold>


    static void register(IEventBus bus) {
        MEMORY_MODULES.register(bus);
    }

    private static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Unit>> unit(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.of(Unit.CODEC)));
    }

    @SuppressWarnings("SameParameterValue")
    private static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Integer>> integer(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.of(Codec.INT)));
    }

    private static DeferredHolder<MemoryModuleType<?>, MemoryModuleType<Long>> longInt(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.of(Codec.LONG)));
    }

    private static <U> DeferredHolder<MemoryModuleType<?>, MemoryModuleType<U>> transientMemory(String name) {
        return MEMORY_MODULES.register(name, () -> new MemoryModuleType<>(Optional.empty()));
    }
}
