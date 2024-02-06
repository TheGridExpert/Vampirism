package de.teamlapen.vampirism.entity.converted.converter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.entity.convertible.Converter;
import de.teamlapen.vampirism.api.entity.convertible.IConvertingHandler;
import de.teamlapen.vampirism.core.ModEntities;
import de.teamlapen.vampirism.datamaps.ConverterEntry;
import de.teamlapen.vampirism.entity.converted.DefaultConvertingHandler;
import de.teamlapen.vampirism.entity.converted.VampirismEntityRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DefaultConverter implements Converter {

    public static final Codec<DefaultConverter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ExtraCodecs.strictOptionalField(ConverterEntry.ConvertingAttributeModifier.CODEC, "attribute_helper", ConverterEntry.ConvertingAttributeModifier.DEFAULT).forGetter(i -> i.helper)
    ).apply(instance, DefaultConverter::new));

    protected final ConverterEntry.ConvertingAttributeModifier helper;

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public DefaultConverter(Optional<ConverterEntry.ConvertingAttributeModifier> helper) {
        this.helper = helper.orElse(ConverterEntry.ConvertingAttributeModifier.DEFAULT);
    }

    public DefaultConverter() {
        this.helper = ConverterEntry.ConvertingAttributeModifier.DEFAULT;
    }

    private DefaultConverter(ConverterEntry.ConvertingAttributeModifier entry) {
        this.helper = entry;
    }

    @Override
    public IConvertingHandler<?> createHandler(@Nullable ResourceLocation texture) {
        return new DefaultConvertingHandler<>(new VampirismEntityRegistry.DefaultHelper(this.helper), texture);
    }

    @Override
    public Codec<? extends Converter> codec() {
        return ModEntities.DEFAULT_CONVERTER.get();
    }
}
