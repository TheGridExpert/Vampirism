package de.teamlapen.vampirism.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class TombstoneEditScreen extends AbstractSignEditScreen {

    public TombstoneEditScreen(SignBlockEntity sign, boolean isFrontText, boolean isFiltered) {
        super(sign, isFrontText, isFiltered);
    }

    @Override
    protected void renderSignBackground(@NotNull GuiGraphics guiGraphics) {

    }

    @Override
    protected @NotNull Vector3f getSignTextScale() {
        return new Vector3f(1.0F, 1.0F, 1.0F);
    }
}
