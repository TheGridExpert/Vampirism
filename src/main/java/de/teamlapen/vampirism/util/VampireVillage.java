package de.teamlapen.vampirism.util;

import com.google.common.collect.Lists;
import de.teamlapen.vampirism.api.entity.CaptureEntityEntry;
import de.teamlapen.vampirism.core.*;
import de.teamlapen.vampirism.entity.factions.FactionVillageBuilder;
import de.teamlapen.vampirism.entity.vampire.VampireBaseEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Unit;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import org.jetbrains.annotations.NotNull;

public class VampireVillage {

    public static @NotNull ItemStack createBanner(HolderLookup.Provider provider) {
        HolderLookup.RegistryLookup<BannerPattern> bannerPattern = provider.lookupOrThrow(Registries.BANNER_PATTERN);

        ItemStack itemStack = new ItemStack(Items.BLACK_BANNER);
        itemStack.set(DataComponents.HIDE_ADDITIONAL_TOOLTIP, Unit.INSTANCE);
        itemStack.set(DataComponents.CUSTOM_NAME, Component.translatable("block.minecraft.ominous_banner").withStyle(ChatFormatting.GOLD));
        BannerPatternLayers.Builder builder = new BannerPatternLayers.Builder();
        builder.add(bannerPattern.getOrThrow(BannerPatterns.TRIANGLES_BOTTOM), DyeColor.RED)
                .add(bannerPattern.getOrThrow(BannerPatterns.TRIANGLES_TOP), DyeColor.RED)
                .add(bannerPattern.getOrThrow(BannerPatterns.BORDER), DyeColor.PURPLE)
                .add(bannerPattern.getOrThrow(BannerPatterns.RHOMBUS_MIDDLE), DyeColor.RED)
                .add(bannerPattern.getOrThrow(BannerPatterns.STRAIGHT_CROSS), DyeColor.RED)
                .add(bannerPattern.getOrThrow(BannerPatterns.CIRCLE_MIDDLE), DyeColor.PURPLE);
        itemStack.set(DataComponents.BANNER_PATTERNS, builder.build());
        itemStack.set(ModDataComponents.IS_FACTION_BANNER, Unit.INSTANCE);
        return itemStack;
    }

    public static FactionVillageBuilder vampireVillage() {
        return new FactionVillageBuilder().badOmenEffect(ModEffects.BAD_OMEN_VAMPIRE)
                .captureEntities(Lists.newArrayList(new CaptureEntityEntry<>(ModEntities.VAMPIRE, 10), new CaptureEntityEntry<>(ModEntities.ADVANCED_VAMPIRE, 2)))
                .factionVillagerProfession(ModVillage.VAMPIRE_EXPERT)
                .guardSuperClass(VampireBaseEntity.class)
                .taskMaster(ModEntities.TASK_MASTER_VAMPIRE)
                .banner(VampireVillage::createBanner)
                .totem(ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE, ModBlocks.TOTEM_TOP_VAMPIRISM_VAMPIRE_CRAFTED);
    }
}
