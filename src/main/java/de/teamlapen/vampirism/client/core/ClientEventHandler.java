package de.teamlapen.vampirism.client.core;

import com.google.common.collect.Lists;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.api.items.IFactionExclusiveItem;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.client.VampirismModClient;
import de.teamlapen.vampirism.config.VampirismConfig;
import de.teamlapen.vampirism.core.ModBlocks;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.data.ClientSkillTreeData;
import de.teamlapen.vampirism.effects.VampirismPotion;
import de.teamlapen.vampirism.entity.player.LevelAttributeModifier;
import de.teamlapen.vampirism.items.component.AppliedOilContent;
import de.teamlapen.vampirism.util.Helper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Handle general client side events
 */
@OnlyIn(Dist.CLIENT)
public class ClientEventHandler {
    private final static Logger LOGGER = LogManager.getLogger();

    @SubscribeEvent
    public void onFovOffsetUpdate(@NotNull ComputeFovModifierEvent event) {
        if (VampirismConfig.CLIENT.disableFovChange.get() && Helper.isVampire(event.getPlayer())) {
            AttributeInstance speed = event.getPlayer().getAttribute(Attributes.MOVEMENT_SPEED);
            AttributeModifier vampirespeed = speed.getModifier(LevelAttributeModifier.ID);
            if (vampirespeed == null) {
                return;
            }
            //removes speed buffs, add speed buffs without the vampire speed
            event.setNewFovModifier((float) (((double) (event.getFovModifier()) * ((vampirespeed.amount() + 1) * (double) (event.getPlayer().getAbilities().getWalkingSpeed()) + speed.getValue())) / ((vampirespeed.amount() + 1) * ((double) (event.getPlayer().getAbilities().getWalkingSpeed()) + speed.getValue()))));
        }
    }

    @SubscribeEvent
    public void onToolTip(@NotNull ItemTooltipEvent event) {
        if (VampirismPotion.isHunterPotion(event.getItemStack(), true).map(Potion::getEffects).map(effectInstances -> effectInstances.stream().map(MobEffectInstance::getEffect).anyMatch(s -> s.value().isBeneficial())).orElse(false) && (event.getEntity() == null || !Helper.isHunter(event.getEntity()))) {
            event.getToolTip().add(Component.translatable("text.vampirism.hunter_potion.deadly").withStyle(ChatFormatting.DARK_RED));
        }

    }

    @SubscribeEvent
    public void onWorldClosed(LevelEvent.Unload event) {
        VampirismModClient.getINSTANCE().clearBossBarOverlay();
    }

    static void onModelRegistry(@NotNull ModelEvent.RegisterAdditional event) {
        for (DyeColor dye : DyeColor.values()) {
            event.register(VResourceLocation.mod("block/coffin/coffin_bottom_" + dye.getName()));
            event.register(VResourceLocation.mod("block/coffin/coffin_top_" + dye.getName()));
            event.register(VResourceLocation.mod("block/coffin/coffin_" + dye.getName()));
        }
    }

    /**
     * This event will handle all items except {@link de.teamlapen.vampirism.api.items.IFactionLevelItem}s. Their oil
     */
    @SubscribeEvent
    public void onItemToolTip(@NotNull ItemTooltipEvent event) {
        if (event.getItemStack().getItem() instanceof IFactionExclusiveItem) return;
        AppliedOilContent.getAppliedOil(event.getItemStack()).ifPresent(oil -> {
            List<Component> toolTips = event.getToolTip();
            int position = 1;
            if (!event.getItemStack().has(DataComponents.HIDE_ADDITIONAL_TOOLTIP)) {
                ArrayList<Component> additionalComponents = new ArrayList<>();
                event.getItemStack().getItem().appendHoverText(event.getItemStack(), event.getContext(), additionalComponents, event.getFlags());
                position += additionalComponents.size();
                Optional<Component> oilTooltip = oil.oil().value().getToolTipLine(event.getItemStack(), oil.oil().value(), oil.duration(), event.getFlags());
                if (oilTooltip.isPresent()) {
                    toolTips.add(position++, oilTooltip.get());
                }
            }
            List<Component> factionToolTips = new ArrayList<>();
            factionToolTips.add(Component.empty());
            factionToolTips.add(Component.translatable("text.vampirism.faction_exclusive", ModFactions.HUNTER.get().getName().plainCopy().withStyle(Minecraft.getInstance().player != null ? Helper.isHunter(Minecraft.getInstance().player) ? ChatFormatting.DARK_GREEN : ChatFormatting.DARK_RED : ChatFormatting.GRAY)).withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY));
            toolTips.addAll(Math.min(event.getToolTip().size(), position), factionToolTips);
        });
    }


    @SubscribeEvent
    public void onJoined(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientSkillTreeData.reset();
    }
}
