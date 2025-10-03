package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.REFERENCE;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.schedule.Activity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModActivities {
    public static final DeferredRegister<Activity> ACTIVITIES = DeferredRegister.create(BuiltInRegistries.ACTIVITY, REFERENCE.MODID);

    public static final DeferredHolder<Activity, Activity> PATROL = registerActivity("patrol");

    private static DeferredHolder<Activity, Activity> registerActivity(String name) {
        return ACTIVITIES.register(name, () -> new Activity(name));
    }

    static void register(IEventBus bus) {
        ACTIVITIES.register(bus);
    }
}
