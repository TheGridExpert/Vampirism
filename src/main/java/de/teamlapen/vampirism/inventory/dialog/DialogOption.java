package de.teamlapen.vampirism.inventory.dialog;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

public class DialogOption {

    private final Component text;
    private final DialogNode nextNode;

    public DialogOption(Component text, DialogNode nextNode) {
        this.text = text;
        this.nextNode = nextNode;
    }

    public Component getText() {
        return text;
    }

    public DialogNode getNextNode() {
        return nextNode;
    }

    public void onSelect(Player player) {
    }
}
