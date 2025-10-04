package de.teamlapen.vampirism.core;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.hunter.IHunterVariant;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.entity.hunter.HunterVariant;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModHunterVariants {

    public static final DeferredRegister<IHunterVariant> HUNTER_VARIANTS = DeferredRegister.create(VampirismRegistries.Keys.HUNTER_VARIANT, REFERENCE.MODID);

    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_1 = registerVariant("hunter1");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_2 = registerVariant("hunter2");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_3 = registerVariant("hunter3");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_4 = registerVariant("hunter4");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_5_SLIM = registerVariant("hunter5_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_6_SLIM = registerVariant("hunter6_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_7_SLIM = registerVariant("hunter7_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_8_SLIM = registerVariant("hunter8_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_9 = registerVariant("hunter9");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_10 = registerVariant("hunter10");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_11 = registerVariant("hunter11");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_12_SLIM = registerVariant("hunter12_slim");
    public static final DeferredHolder<IHunterVariant, IHunterVariant> HUNTER_13 = registerVariant("hunter13");

    private static <U> DeferredHolder<IHunterVariant, IHunterVariant> registerVariant(String name) {
        return registerVariant(name, VResourceLocation.mod("textures/entity/hunter/" + name + ".png"));
    }
    
    private static <U> DeferredHolder<IHunterVariant, IHunterVariant> registerVariant(String name, ResourceLocation texture) {
        return HUNTER_VARIANTS.register(name, () -> new HunterVariant(texture));
    }

    static void register(IEventBus bus) {
        HUNTER_VARIANTS.register(bus);
    }
}
