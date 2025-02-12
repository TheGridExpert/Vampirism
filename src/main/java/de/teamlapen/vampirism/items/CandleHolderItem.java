package de.teamlapen.vampirism.items;

import de.teamlapen.lib.lib.util.ModDisplayItemGenerator;
import de.teamlapen.vampirism.blocks.candle.CandleHolderBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class CandleHolderItem extends BlockItem implements ModDisplayItemGenerator.CreativeTabItemProvider {
    private final @Nullable List<CandleHolderBlock> allItems;

    public CandleHolderItem(Block block, Properties properties, @Nullable List<CandleHolderBlock> allItems) {
        super(block, properties.useBlockDescriptionPrefix());
        this.allItems = allItems;
    }

    public CandleHolderItem(Block block, Properties properties) {
        this(block, properties, null);
    }

    @Override
    public void generateCreativeTab(CreativeModeTab.@NotNull ItemDisplayParameters parameters, CreativeModeTab.Output output) {
        if (allItems != null) {
            for (int i = 0; i < allItems.size(); i++) {
                output.accept(allItems.get(i).asItem(), i <= 1 ? CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS : CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
            }
        }
    }
}
