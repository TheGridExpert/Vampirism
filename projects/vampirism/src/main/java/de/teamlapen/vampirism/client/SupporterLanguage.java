package de.teamlapen.vampirism.client;

import de.teamlapen.vampirism.VampirismMod;
import net.minecraft.client.Minecraft;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.event.ClientResourceLoadFinishedEvent;
import org.jspecify.annotations.Nullable;

import java.util.Map;

public class SupporterLanguage extends Language {

    private final Language delegate;

    private SupporterLanguage(Language delegate) {
        this.delegate = delegate;
    }

    public static void install(ClientResourceLoadFinishedEvent event) {
        Language current = Language.getInstance();
        if (!(current instanceof SupporterLanguage)) {
            Language.inject(new SupporterLanguage(current));
        }
    }

    private @Nullable String findSupporterName(String key) {
        return VampirismMod.services().supporterManager().findTranslatedName(key, Minecraft.getInstance().getLanguageManager().getSelected());
    }

    @Override
    public String getOrDefault(String elementId, String defaultValue) {
        String name = findSupporterName(elementId);
        return name != null ? name : this.delegate.getOrDefault(elementId, defaultValue);
    }

    @Override
    public boolean has(String elementId) {
        return findSupporterName(elementId) != null || this.delegate.has(elementId);
    }

    @Override
    public boolean isDefaultRightToLeft() {
        return this.delegate.isDefaultRightToLeft();
    }

    @Override
    public FormattedCharSequence getVisualOrder(FormattedText logicalOrderText) {
        return this.delegate.getVisualOrder(logicalOrderText);
    }

    @Override
    public Map<String, String> getLanguageData() {
        return this.delegate.getLanguageData();
    }

    @Override
    public @Nullable Component getComponent(String key) {
        return this.delegate.getComponent(key);
    }
}
