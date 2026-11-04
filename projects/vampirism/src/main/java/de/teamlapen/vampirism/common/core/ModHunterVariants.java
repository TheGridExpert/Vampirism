package de.teamlapen.vampirism.common.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.util.VIdentifier;
import de.teamlapen.vampirism.api.world.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterVariant;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModHunterVariants {

    public static final DeferredRegister<IHunterVariant> HUNTER_VARIANTS = DeferredRegister.create(VampirismRegistries.Keys.HUNTER_VARIANT, REFERENCE.MODID);

    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_1 = register("hunter1");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_2 = register("hunter2");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_3 = register("hunter3");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_4 = register("hunter4");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_5_SLIM = register("hunter5_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_6_SLIM = register("hunter6_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_7_SLIM = register("hunter7_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_8_SLIM = register("hunter8_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_9 = register("hunter9");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_10 = register("hunter10");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_11 = register("hunter11");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_12_SLIM = register("hunter12_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_13 = register("hunter13");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_14 = register("hunter14");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_15 = register("hunter15");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_16 = register("hunter16");

    private static DeferredHolder<IHunterVariant, IHunterVariant> register(String name) {
        return register(name, VIdentifier.mod("textures/entity/hunter/" + name + ".png"));
    }

    private static DeferredHolder<IHunterVariant, IHunterVariant> register(String name, Identifier texture) {
        return HUNTER_VARIANTS.register(name, () -> new HunterVariant(texture));
    }

    static void register(IEventBus bus) {
        HUNTER_VARIANTS.register(bus);
    }
}
