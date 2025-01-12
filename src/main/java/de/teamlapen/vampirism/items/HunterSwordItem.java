package de.teamlapen.vampirism.items;

import de.teamlapen.vampirism.api.entity.factions.IFaction;
import de.teamlapen.vampirism.api.items.IFactionSlayerItem;
import de.teamlapen.vampirism.api.items.IVampireFinisher;
import de.teamlapen.vampirism.core.ModFactions;
import de.teamlapen.vampirism.items.component.FactionRestriction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Basic sword for vampire hunters
 */
public abstract class HunterSwordItem extends VampirismSwordItem implements IFactionSlayerItem, IVampireFinisher {

    public HunterSwordItem(@NotNull ToolMaterial material, int attackDamage, float attackSpeed, @NotNull Properties props) {
        super(material, attackDamage, attackSpeed, props);
    }

    @Override
    public Holder<? extends IFaction<?>> getSlayedFaction() {
        return ModFactions.VAMPIRE;
    }
}
