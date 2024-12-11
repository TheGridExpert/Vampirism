package de.teamlapen.vampirism.items;

import de.teamlapen.lib.lib.storage.Attachment;
import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import de.teamlapen.vampirism.api.entity.player.ISkillPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.core.ModEffects;
import de.teamlapen.vampirism.entity.factions.FactionPlayerHandler;
import de.teamlapen.vampirism.entity.minion.MinionEntity;
import de.teamlapen.vampirism.entity.player.skills.SkillHandler;
import de.teamlapen.vampirism.items.consume.OblivionEffect;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OblivionItem extends Item {

    public static <T extends IFactionPlayer<T> & ISkillPlayer<T>> void applyEffect(@NotNull T factionPlayer) {
        Player player = factionPlayer.asEntity();
        ISkillHandler<?> skillHandler = factionPlayer.getSkillHandler();
        if (((SkillHandler<?>) skillHandler).noSkillEnabled()) {
            return;
        }
        boolean test = VampirismMod.inDev || REFERENCE.VERSION.isTestVersion();
        player.addEffect(new MobEffectInstance(ModEffects.OBLIVION, Integer.MAX_VALUE, test ? 100 : 4));
        if (factionPlayer instanceof Attachment syncable) {
            syncable.sync();
        }
    }

    public OblivionItem(@NotNull Properties properties) {
        super(properties.stacksTo(1).rarity(Rarity.UNCOMMON).component(DataComponents.CONSUMABLE, Consumables.defaultDrink().onConsume(new OblivionEffect()).build()));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltip, @NotNull TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        tooltip.add(Component.translatable("text.vampirism.oblivion_potion.resets_skills").withStyle(ChatFormatting.GRAY));
    }

}
