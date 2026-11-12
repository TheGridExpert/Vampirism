package de.teamlapen.vampirism.common.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterLookoutDebugInfo;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquadDebugInfo;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.debug.DebugSubscription;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDebugSubscriptions {

    public static final DeferredRegister<DebugSubscription<?>> DEBUG_SUBSCRIPTIONS = DeferredRegister.create(Registries.DEBUG_SUBSCRIPTION, REFERENCE.MODID);

    public static final DeferredHolder<DebugSubscription<?>, DebugSubscription<HunterSquadDebugInfo>> HUNTER_SQUADS = DEBUG_SUBSCRIPTIONS.register("hunter_squads", () -> new DebugSubscription<>(HunterSquadDebugInfo.STREAM_CODEC));

    public static final DeferredHolder<DebugSubscription<?>, DebugSubscription<HunterLookoutDebugInfo>> HUNTER_LOOKOUTS = DEBUG_SUBSCRIPTIONS.register("hunter_lookouts", () -> new DebugSubscription<>(HunterLookoutDebugInfo.STREAM_CODEC));

    static void register(IEventBus bus) {
        DEBUG_SUBSCRIPTIONS.register(bus);
    }
}
