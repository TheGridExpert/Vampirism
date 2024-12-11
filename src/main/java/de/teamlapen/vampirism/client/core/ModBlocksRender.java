package de.teamlapen.vampirism.client.core;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.blockentity.AlchemicalCauldronBlockEntity;
import de.teamlapen.vampirism.blockentity.TotemBlockEntity;
import de.teamlapen.vampirism.blocks.TotemTopBlock;
import de.teamlapen.vampirism.client.extensions.BlockExtensions;
import de.teamlapen.vampirism.client.renderer.blockentity.*;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.core.ModFluids;
import de.teamlapen.vampirism.core.ModTiles;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Holder;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Handles all block render registration including TileEntities
 */
public class ModBlocksRender {

    public static void register() {
        registerRenderType();
    }

    static void registerBlockColors(RegisterColorHandlersEvent.@NotNull Block event) {
        event.register((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 1) {
                return 0x9966FF;
            }
            return 0x8855FF;
        }, ModBlocks.ALCHEMICAL_FIRE.get());
        event.register((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 255) {
                BlockEntity tile = (worldIn == null || pos == null) ? null : worldIn.getBlockEntity(pos);
                if (tile instanceof AlchemicalCauldronBlockEntity) {
                    return ((AlchemicalCauldronBlockEntity) tile).getLiquidColorClient();
                }
            }
            return 0xFFFFFF;
        }, ModBlocks.ALCHEMICAL_CAULDRON.get());
        event.register((state, worldIn, pos, tintIndex) -> {
            if (tintIndex == 255) {
                BlockEntity tile = (worldIn == null || pos == null) ? null : worldIn.getBlockEntity(pos);
                if (tile instanceof TotemBlockEntity) {
                    Holder<? extends IFaction<?>> f = ((TotemBlockEntity) tile).getControllingFaction();
                    if (f != null) return f.value().getColor();
                }
            }
            return 0xFFFFFF;
        }, TotemTopBlock.getBlocks().toArray(new TotemTopBlock[0]));
    }

    static void registerBlockEntityRenderers(EntityRenderersEvent.@NotNull RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModTiles.COFFIN.get(), CoffinBESR::new);
        event.registerBlockEntityRenderer(ModTiles.ALTAR_INFUSION.get(), AltarInfusionBESR::new);
        event.registerBlockEntityRenderer(ModTiles.BLOOD_PEDESTAL.get(), PedestalBESR::new);
        event.registerBlockEntityRenderer(ModTiles.TOTEM.get(), TotemBESR::new);
        event.registerBlockEntityRenderer(ModTiles.GARLIC_DIFFUSER.get(), GarlicDiffuserBESR::new);
        event.registerBlockEntityRenderer(ModTiles.BAT_CAGE.get(), BatCageBESR::new);
        event.registerBlockEntityRenderer(ModTiles.MOTHER_TROPHY.get(), MotherTrophyBESR::new);
        event.registerBlockEntityRenderer(ModTiles.FOG_DIFFUSER.get(), FogDiffuserBESR::new);
        event.registerBlockEntityRenderer(ModTiles.VAMPIRE_BEACON.get(), VampireBeaconBESR::new);
        event.registerBlockEntityRenderer(ModTiles.BLOOD_CONTAINER.get(), BloodContainerBESR::new);
        event.registerBlockEntityRenderer(ModTiles.ALTAR_INSPIRATION.get(), AltarInspirationBESR::new);
    }

    private static void registerRenderType() {
        ItemBlockRenderTypes.setRenderLayer(ModFluids.IMPURE_BLOOD.get(), RenderType.translucent());
        ItemBlockRenderTypes.setRenderLayer(ModFluids.BLOOD.get(), RenderType.translucent());
    }

    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(BlockExtensions.TENT, ModBlocks.TENT.get(), ModBlocks.TENT_MAIN.get());
    }

    static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (DyeColor value : DyeColor.values()) {
            event.register(VResourceLocation.mod("block/coffin/coffin_" + value.getName()));
            event.register(VResourceLocation.mod("block/coffin/coffin_bottom_" + value.getName()));
            event.register(VResourceLocation.mod("block/coffin/coffin_top_" + value.getName()));
        }
    }


}
