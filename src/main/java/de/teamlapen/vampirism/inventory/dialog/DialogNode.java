package de.teamlapen.vampirism.inventory.dialog;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DialogNode {

    private final Component text;
    private final List<DialogOption> options;
    private Optional<DialogNode> nextNode;

    public DialogNode(Component text) {
        this.text = text;
        this.options = new ArrayList<>();
        this.nextNode = Optional.empty();
    }

    public Component getText() {
        return text;
    }

    public List<DialogOption> getOptions() {
        return options;
    }

    public Optional<DialogNode> getNextNode() {
        return nextNode;
    }

    public void setNextNode(DialogNode nextNode) {
        this.nextNode = Optional.of(nextNode);
    }

    public void addOption(DialogOption option) {
        options.add(option);
    }

    public boolean isChoice() {
        return !options.isEmpty();
    }

    public boolean hasNext() {
        return nextNode.isPresent();
    }
}
