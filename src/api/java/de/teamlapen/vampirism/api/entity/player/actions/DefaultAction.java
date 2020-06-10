package de.teamlapen.vampirism.api.entity.player.actions;

import de.teamlapen.vampirism.api.entity.player.IFactionPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

/**
 * Default implementation for an action
 */
public abstract class DefaultAction<T extends IFactionPlayer> extends ForgeRegistryEntry<IAction> implements IAction {
    private ITextComponent name;

    /**
     * Can be overridden to check additional requirements
     */
    public boolean canBeUsedBy(T player) {
        return true;
    }

    @Override
    public IAction.PERM canUse(IFactionPlayer player) {
        if (!isEnabled())
            return IAction.PERM.DISABLED;
        if (this.getFaction().getFactionPlayerInterface().isInstance(player)) {
            //noinspection unchecked
            return (canBeUsedBy((T) player) ? IAction.PERM.ALLOWED : IAction.PERM.DISALLOWED);
        } else {
            throw new IllegalArgumentException("Faction player instance is of wrong class " + player.getClass() + " instead of " + this.getFaction().getFactionPlayerInterface());
        }

    }

    @Override
    public ITextComponent getName() {
        return name == null ? name = new TranslationTextComponent(getTranslationKey()) : name;
    }

    @Deprecated
    @Override
    public String getTranslationKey() {
        return "action." + getRegistryName().getNamespace() + "." + getRegistryName().getPath();
    }

    /**
     * @return Should return false if deactivated in configs
     */
    public abstract boolean isEnabled();

    @Override
    public boolean onActivated(IFactionPlayer player) {
        if (this.getFaction().getFactionPlayerInterface().isInstance(player)) {
            //noinspection unchecked
            return activate((T) player);
        } else {
            throw new IllegalArgumentException("Faction player instance is of wrong class " + player.getClass() + " instead of " + this.getFaction().getFactionPlayerInterface());
        }
    }

    @Override
    public String toString() {
        return this.getRegistryName() + " (" + this.getClass().getSimpleName() + ")";
    }

    /**
     * Called when the action is activated. Only called server side
     *
     * @return Whether the action was successfully activated. !Does not give any feedback to the user!
     */
    protected abstract boolean activate(T player);
}
