package de.teamlapen.vampirism.items;

import com.mojang.datafixers.util.Pair;
import de.teamlapen.lib.lib.util.ModDisplayItemGenerator;
import de.teamlapen.vampirism.blocks.CandleHolderBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PairCandleHolderItem extends StandingAndWallBlockItem implements ModDisplayItemGenerator.CreativeTabItemProvider {
    private final @Nullable List<Pair<CandleHolderBlock, CandleHolderBlock>> allItems;

    public PairCandleHolderItem(Block block, Block wallBlock, @Nullable List<Pair<CandleHolderBlock, CandleHolderBlock>> allItems) {
        super(block, wallBlock, new Item.Properties(), Direction.DOWN);
        this.allItems = allItems;
    }

    public PairCandleHolderItem(Block block, Block wallBlock) {
        this(block, wallBlock, null);
    }

    @Override
    public void generateCreativeTab(CreativeModeTab.@NotNull ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (allItems != null) {
            for (int i = 0; i < allItems.size(); i++) {
                output.accept(allItems.get(i).getFirst().asItem(), i <= 1 ? CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS : CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        }
    }
}
