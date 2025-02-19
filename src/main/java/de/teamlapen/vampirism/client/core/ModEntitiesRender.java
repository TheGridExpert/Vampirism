package de.teamlapen.vampirism.client.core;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.model.*;
import de.teamlapen.vampirism.client.model.armor.*;
import de.teamlapen.vampirism.client.renderer.entity.*;
import de.teamlapen.vampirism.client.renderer.entity.layers.ConvertedVampireEntityLayer;
import de.teamlapen.vampirism.client.renderer.entity.layers.VampirePlayerHeadLayer;
import de.teamlapen.vampirism.client.renderer.entity.state.ConvertedOverlayRenderState;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.entity.converted.ConvertedDonkeyEntity;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.LayerDefinitions;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * Handles entity render registration
 */
public class ModEntitiesRender {
    public static final ModelLayerLocation HUNTER = new ModelLayerLocation(VResourceLocation.mod("hunter"), "main");
    public static final ModelLayerLocation HUNTER_SLIM = new ModelLayerLocation(VResourceLocation.mod("slim_hunter"), "main");
    public static final ModelLayerLocation COFFIN = new ModelLayerLocation(VResourceLocation.mod("coffin"), "main");
    public static final ModelLayerLocation WING = new ModelLayerLocation(VResourceLocation.mod("wing"), "main");
    public static final ModelLayerLocation BARON = new ModelLayerLocation(VResourceLocation.mod("baron"), "main");
    public static final ModelLayerLocation BARONESS = new ModelLayerLocation(VResourceLocation.mod("baroness"), "main");
    public static final ModelLayerLocation BARON_ATTIRE = new ModelLayerLocation(VResourceLocation.mod("baron"), "attire");
    public static final ModelLayerLocation BARONESS_ATTIRE = new ModelLayerLocation(VResourceLocation.mod("baroness"), "attire");
    public static final ModelLayerLocation CLOAK = new ModelLayerLocation(VResourceLocation.mod("cloak"), "main");
    public static final ModelLayerLocation CLOTHING_BOOTS = new ModelLayerLocation(VResourceLocation.mod("clothing"), "boots");
    public static final ModelLayerLocation CLOTHING_CROWN = new ModelLayerLocation(VResourceLocation.mod("clothing"), "crown");
    public static final ModelLayerLocation CLOTHING_PANTS = new ModelLayerLocation(VResourceLocation.mod("clothing"), "pants");
    public static final ModelLayerLocation CLOTHING_HAT = new ModelLayerLocation(VResourceLocation.mod("clothing"), "hat");
    public static final ModelLayerLocation HUNTER_HAT0 = new ModelLayerLocation(VResourceLocation.mod("hunter_hat0"), "main");
    public static final ModelLayerLocation HUNTER_HAT1 = new ModelLayerLocation(VResourceLocation.mod("hunter_hat1"), "main");
    public static final ModelLayerLocation HUNTER_EQUIPMENT = new ModelLayerLocation(VResourceLocation.mod("hunter_equipment"), "main");
    public static final ModelLayerLocation VILLAGER_WITH_ARMS = new ModelLayerLocation(VResourceLocation.mod("villager_with_arms"), "main");
    public static final ModelLayerLocation GENERIC_BIPED = new ModelLayerLocation(VResourceLocation.mod("generic_biped"), "main");
    public static final ModelLayerLocation GENERIC_BIPED_SLIM = new ModelLayerLocation(VResourceLocation.mod("generic_biped"), "main");
    public static final ModelLayerLocation GENERIC_BIPED_ARMOR_OUTER = new ModelLayerLocation(VResourceLocation.mod("generic_biped"), "outer_armor");
    public static final ModelLayerLocation GENERIC_BIPED_ARMOR_INNER = new ModelLayerLocation(VResourceLocation.mod("generic_biped"), "inner_armor");
    public static final ModelLayerLocation TASK_MASTER = new ModelLayerLocation(VResourceLocation.mod("task_master"), "main");
    public static final ModelLayerLocation REMAINS_DEFENDER = new ModelLayerLocation(VResourceLocation.mod("remains_defender"), "main");
    public static final ModelLayerLocation GHOST = new ModelLayerLocation(VResourceLocation.mod("ghost"), "main");
    public static final ModelLayerLocation DARK_SPRUCE_BOAT = new ModelLayerLocation(VResourceLocation.mod("boat/dark_spruce"), "main");
    public static final ModelLayerLocation DARK_SPRUCE_CHEST_BOAT = new ModelLayerLocation(VResourceLocation.mod("chest_boat/dark_spruce"), "main");
    public static final ModelLayerLocation CURSED_SPRUCE_BOAT = new ModelLayerLocation(VResourceLocation.mod("boat/cursed_spruce"), "main");
    public static final ModelLayerLocation CURSED_SPRUCE_CHEST_BOAT = new ModelLayerLocation(VResourceLocation.mod("chest_boat/cursed_spruce"), "main");


    static void onRegisterRenderers(EntityRenderersEvent.@NotNull RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.BLINDING_BAT.get(), BatRenderer::new);
        event.registerEntityRenderer(ModEntities.CONVERTED_CREATURE_IMOB.get(), ConvertedCreatureRenderer::new);
        event.registerEntityRenderer(ModEntities.CONVERTED_CREATURE.get(), (ConvertedCreatureRenderer::new));
        event.registerEntityRenderer(ModEntities.CONVERTED_HORSE.get(), convertedRenderer(context -> new HorseRenderer(context)));
        event.registerEntityRenderer(ModEntities.CONVERTED_DONKEY.get(), convertedRenderer(context -> new DonkeyRenderer<>(context, ModelLayers.DONKEY, ModelLayers.DONKEY_BABY, false)));
        event.registerEntityRenderer(ModEntities.CONVERTED_MULE.get(), convertedRenderer(context -> new DonkeyRenderer<>(context, ModelLayers.MULE, ModelLayers.MULE_BABY, true)));
        event.registerEntityRenderer(ModEntities.CONVERTED_SHEEP.get(), convertedRenderer(SheepRenderer::new));
        event.registerEntityRenderer(ModEntities.CONVERTED_COW.get(), convertedRenderer(CowRenderer::new));
        event.registerEntityRenderer(ModEntities.HUNTER.get(), (BasicHunterRenderer::new));
        event.registerEntityRenderer(ModEntities.HUNTER_IMOB.get(), (BasicHunterRenderer::new));
        event.registerEntityRenderer(ModEntities.VAMPIRE.get(), (BasicVampireRenderer::new));
        event.registerEntityRenderer(ModEntities.VAMPIRE_IMOB.get(), (BasicVampireRenderer::new));
        event.registerEntityRenderer(ModEntities.HUNTER_TRAINER.get(), e -> new HunterTrainerRenderer(e, true));
        event.registerEntityRenderer(ModEntities.VAMPIRE_BARON.get(), (VampireBaronRenderer::new));
        event.registerEntityRenderer(ModEntities.ADVANCED_HUNTER.get(), (AdvancedHunterRenderer::new));
        event.registerEntityRenderer(ModEntities.ADVANCED_HUNTER_IMOB.get(), (AdvancedHunterRenderer::new));
        event.registerEntityRenderer(ModEntities.ADVANCED_VAMPIRE.get(), (AdvancedVampireRenderer::new));
        event.registerEntityRenderer(ModEntities.ADVANCED_VAMPIRE_IMOB.get(), (AdvancedVampireRenderer::new));
        event.registerEntityRenderer(ModEntities.VILLAGER_CONVERTED.get(), convertedRenderer(VillagerRenderer::new));
        event.registerEntityRenderer(ModEntities.VILLAGER_ANGRY.get(), HunterVillagerRenderer::new);
        event.registerEntityRenderer(ModEntities.CROSSBOW_ARROW.get(), (CrossbowArrowRenderer::new));
        event.registerEntityRenderer(ModEntities.PARTICLE_CLOUD.get(), (NoopRenderer::new));
        event.registerEntityRenderer(ModEntities.THROWABLE_ITEM.get(), ThrowableItemRenderer::new);
        event.registerEntityRenderer(ModEntities.DARK_BLOOD_PROJECTILE.get(), (DarkBloodProjectileRenderer::new));
        event.registerEntityRenderer(ModEntities.SOUL_ORB.get(), SoulOrbRenderer::new);
        event.registerEntityRenderer(ModEntities.HUNTER_TRAINER_DUMMY.get(), e -> new HunterTrainerRenderer(e, false));
        event.registerEntityRenderer(ModEntities.DUMMY_CREATURE.get(), (DummyRenderer::new));
        event.registerEntityRenderer(ModEntities.VAMPIRE_MINION.get(), (VampireMinionRenderer::new));
        event.registerEntityRenderer(ModEntities.HUNTER_MINION.get(), (HunterMinionRenderer::new));
        event.registerEntityRenderer(ModEntities.TASK_MASTER_VAMPIRE.get(), (VampireTaskMasterRenderer::new));
        event.registerEntityRenderer(ModEntities.TASK_MASTER_HUNTER.get(), (HunterTaskMasterRenderer::new));
        event.registerEntityRenderer(ModEntities.dummy_sit_entity.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntities.CONVERTED_FOX.get(), convertedRenderer(FoxRenderer::new));
        event.registerEntityRenderer(ModEntities.CONVERTED_GOAT.get(), convertedRenderer(GoatRenderer::new));
        event.registerEntityRenderer(ModEntities.VULNERABLE_REMAINS_DUMMY.get(), DummyRenderer::new);
        event.registerEntityRenderer(ModEntities.REMAINS_DEFENDER.get(), RemainsDefenderRenderer::new);
        event.registerEntityRenderer(ModEntities.GHOST.get(), GhostRenderer::new);
        event.registerEntityRenderer(ModEntities.CONVERTED_CAMEL.get(), convertedRenderer(CamelRenderer::new));
        event.registerEntityRenderer(ModEntities.CONVERTED_CAT.get(), convertedRenderer(CatRenderer::new));
        event.registerEntityRenderer(ModEntities.DARK_SPRUCE_BOAT.get(), context -> new BoatRenderer(context, DARK_SPRUCE_BOAT));
        event.registerEntityRenderer(ModEntities.DARK_SPRUCE_CHEST_BOAT.get(), context -> new BoatRenderer(context, DARK_SPRUCE_CHEST_BOAT));
        event.registerEntityRenderer(ModEntities.CURSED_SPRUCE_BOAT.get(), context -> new BoatRenderer(context, CURSED_SPRUCE_BOAT));
        event.registerEntityRenderer(ModEntities.CURSED_SPRUCE_CHEST_BOAT.get(), context -> new BoatRenderer(context, CURSED_SPRUCE_CHEST_BOAT));
    }

    static void onRegisterLayers(EntityRenderersEvent.@NotNull RegisterLayerDefinitions event) {
        event.registerLayerDefinition(HUNTER, BasicHunterModel::createBodyLayer);
        event.registerLayerDefinition(HUNTER_SLIM, BasicHunterModel::createSlimBodyLayer);
        event.registerLayerDefinition(COFFIN, CoffinModel::createLayer);
        event.registerLayerDefinition(WING, WingModel::createLayer);
        event.registerLayerDefinition(BARON, BaronModel::createLayer);
        event.registerLayerDefinition(BARONESS, BaronessModel::createLayer);
        event.registerLayerDefinition(BARON_ATTIRE, BaronAttireModel::createLayer);
        event.registerLayerDefinition(BARONESS_ATTIRE, BaronessAttireModel::createLayer);
        event.registerLayerDefinition(CLOAK, CloakModel::createLayer);
        event.registerLayerDefinition(CLOTHING_BOOTS, ClothingBootsModel::createLayer);
        event.registerLayerDefinition(CLOTHING_CROWN, ClothingCrownModel::createLayer);
        event.registerLayerDefinition(CLOTHING_PANTS, ClothingPantsModel::createLayer);
        event.registerLayerDefinition(CLOTHING_HAT, VampireHatModel::createLayer);
        event.registerLayerDefinition(HUNTER_HAT0, HunterHatModel::createHat0Layer);
        event.registerLayerDefinition(HUNTER_HAT1, HunterHatModel::createHat1Layer);
        event.registerLayerDefinition(VILLAGER_WITH_ARMS, () -> VillagerWithArmsModel.createLayer(0));
        event.registerLayerDefinition(GENERIC_BIPED, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, false), 64, 64));
        event.registerLayerDefinition(GENERIC_BIPED_SLIM, () -> LayerDefinition.create(PlayerModel.createMesh(CubeDeformation.NONE, true), 64, 64));
        event.registerLayerDefinition(GENERIC_BIPED_ARMOR_INNER, () -> LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.INNER_ARMOR_DEFORMATION, 0.0F), 64, 32));
        event.registerLayerDefinition(GENERIC_BIPED_ARMOR_OUTER, () -> LayerDefinition.create(HumanoidModel.createMesh(LayerDefinitions.OUTER_ARMOR_DEFORMATION, 0.0F), 64, 32));
        event.registerLayerDefinition(TASK_MASTER, () -> LayerDefinition.create(VillagerModel.createBodyModel(), 64, 64));
        event.registerLayerDefinition(REMAINS_DEFENDER, RemainsDefenderModel::createBodyLayer);
        event.registerLayerDefinition(GHOST, GhostModel::createMesh);
        LayerDefinition boatDefinition = BoatModel.createBoatModel();
        LayerDefinition chestBoatDefinition = BoatModel.createChestBoatModel();
        event.registerLayerDefinition(DARK_SPRUCE_BOAT, () -> boatDefinition);
        event.registerLayerDefinition(DARK_SPRUCE_CHEST_BOAT, () -> chestBoatDefinition);
        event.registerLayerDefinition(CURSED_SPRUCE_BOAT, () -> boatDefinition);
        event.registerLayerDefinition(CURSED_SPRUCE_CHEST_BOAT, () -> chestBoatDefinition);

    }

    static void onAddLayers(EntityRenderersEvent.@NotNull AddLayers event) {
        _onAddLayers(event);
    }

    @SuppressWarnings("unchecked")
    private static <S extends Player, T extends PlayerRenderState, Q extends EntityModel<T>> void _onAddLayers(EntityRenderersEvent.@NotNull AddLayers event) {

        for (PlayerSkin.Model s : event.getSkins()) {
            LivingEntityRenderer<S, T, Q> renderPlayer = event.getSkin(s);
            if (renderPlayer != null && renderPlayer.getModel() instanceof PlayerModel) {
                LivingEntityRenderer<S, T, PlayerModel> renderPlayer2 = (LivingEntityRenderer<S, T, PlayerModel>) renderPlayer;
                renderPlayer2.addLayer(new VampirePlayerHeadLayer<>(renderPlayer2));
            }
        }
    }

    private static @NotNull <T extends LivingEntity, U extends LivingEntityRenderState, Z extends EntityModel<? super U>, O extends LivingEntityRenderState & ConvertedOverlayRenderState, P extends EntityModel<O>> EntityRendererProvider<T> convertedRenderer(LivingEntityRendererProvider<T,U,Z> provider) {
        return context -> {
            //noinspection unchecked
            var renderer = (LivingEntityRenderer<T, O, P>) provider.create(context);
            renderer.addLayer(new ConvertedVampireEntityLayer<>(renderer, false));
            return renderer;
        };
    }

    private interface LivingEntityRendererProvider<T extends LivingEntity, U extends LivingEntityRenderState, Z extends EntityModel<? super U>> extends EntityRendererProvider<T> {
        @Override
        @NotNull
        LivingEntityRenderer<T, U, Z> create(EntityRendererProvider.@NotNull Context pContext);
    }

}
