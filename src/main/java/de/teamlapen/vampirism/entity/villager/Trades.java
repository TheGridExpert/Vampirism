package de.teamlapen.vampirism.entity.villager;

import de.teamlapen.vampirism.core.ModDataComponents;
import de.teamlapen.vampirism.core.ModItems;
import de.teamlapen.vampirism.entity.converted.ConvertedVillagerEntity;
import de.teamlapen.vampirism.items.BloodBottleItem;
import de.teamlapen.vampirism.items.component.BottleBlood;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class Trades {
    public static class ItemsForSouls implements VillagerTrades.ItemListing {
        private final int xp;
        private final Price price;
        private final ItemStack[] sellingItem;
        private final Price selling;
        private final int maxUses;

        public ItemsForSouls(Price priceIn, @NotNull ItemLike sellingItemIn, Price sellingIn) {
            this(priceIn, new ItemStack[]{new ItemStack(sellingItemIn.asItem())}, sellingIn, 8, 2);
        }

        public ItemsForSouls(Price priceIn, ItemStack[] sellingItemIn, Price sellingIn) {
            this(priceIn, sellingItemIn, sellingIn, 8, 2);
        }

        public ItemsForSouls(Price priceIn, @NotNull ItemLike sellingItemIn, Price sellingIn, int maxUsesIn, int xpIn) {
            this.price = priceIn;
            this.sellingItem = new ItemStack[]{new ItemStack(sellingItemIn.asItem())};
            this.selling = sellingIn;
            this.maxUses = maxUsesIn;
            this.xp = xpIn;
        }

        public ItemsForSouls(Price priceIn, ItemStack[] sellingItemIn, Price sellingIn, int maxUsesIn, int xpIn) {
            this.price = priceIn;
            this.sellingItem = sellingItemIn;
            this.selling = sellingIn;
            this.maxUses = maxUsesIn;
            this.xp = xpIn;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            return new MerchantOffer(new ItemCost(ModItems.SOUL_ORB_VAMPIRE.get(), price.getPrice(random)), new ItemStack(sellingItem[random.nextInt(sellingItem.length)].getItem(), selling.getPrice(random)), maxUses, xp, 0.2F);
        }
    }

    public static class ItemsForHeart implements VillagerTrades.ItemListing {
        private final int xp;
        private final Price price;
        private final ItemStack[] sellingItem;
        private final Price selling;
        private final int maxUses;

        public ItemsForHeart(Price priceIn, @NotNull ItemLike sellingItemIn, Price sellingIn) {
            this(priceIn, new ItemStack[]{new ItemStack(sellingItemIn.asItem())}, sellingIn, 8, 2);
        }

        public ItemsForHeart(Price priceIn, ItemStack[] sellingItemIn, Price sellingIn) {
            this(priceIn, sellingItemIn, sellingIn, 8, 2);
        }

        public ItemsForHeart(Price priceIn, @NotNull ItemLike sellingItemIn, Price sellingIn, int maxUsesIn, int xpIn) {
            this.price = priceIn;
            this.sellingItem = new ItemStack[]{new ItemStack(sellingItemIn.asItem())};
            this.selling = sellingIn;
            this.maxUses = maxUsesIn;
            this.xp = xpIn;
        }

        public ItemsForHeart(Price priceIn, ItemStack[] sellingItemIn, Price sellingIn, int maxUsesIn, int xpIn) {
            this.price = priceIn;
            this.sellingItem = sellingItemIn;
            this.selling = sellingIn;
            this.maxUses = maxUsesIn;
            this.xp = xpIn;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            return new MerchantOffer(new ItemCost(ModItems.HUMAN_HEART.get(), price.getPrice(random)), new ItemStack(sellingItem[random.nextInt(sellingItem.length)].getItem(), selling.getPrice(random)), maxUses, xp, 0.2F);
        }
    }

    public static class BloodBottleForEmeralds implements VillagerTrades.ItemListing {
        private final int emeraldAmount;
        private final int resultAmount;
        private final int maxUses;
        private final int givenXP;
        private final float priceMultiplier;

        public BloodBottleForEmeralds(int emeraldAmount, int resultAmount, int maxUses, int givenXP) {
            this(emeraldAmount, resultAmount, maxUses, givenXP, 0.05F);
        }

        public BloodBottleForEmeralds(int emeraldAmount, int resultAmount, int maxUses, int givenXP, float priceMultiplier) {
            this.emeraldAmount = emeraldAmount;
            this.resultAmount = resultAmount;
            this.maxUses = maxUses;
            this.givenXP = givenXP;
            this.priceMultiplier = priceMultiplier;
        }

        @NotNull
        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            ItemStack bottle = new ItemStack(ModItems.BLOOD_BOTTLE.get(), resultAmount);
            bottle.set(ModDataComponents.BOTTLE_BLOOD.get(), new BottleBlood(9));
            return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldAmount), bottle, this.maxUses, this.givenXP, this.priceMultiplier);
        }
    }

    public static class BloodBottleForHeart implements VillagerTrades.ItemListing {
        private final int xp;
        private final Price price;
        private final Price selling;
        private final int blood;
        private final int maxUses;

        public BloodBottleForHeart(Price priceIn, Price sellingIn, int damageIn) {
            this(priceIn, sellingIn, damageIn, 8, 2);
        }

        public BloodBottleForHeart(Price priceIn, Price sellingIn, int damageIn, int maxUsesIn, int xpIn) {
            this.price = priceIn;
            this.selling = sellingIn;
            this.blood = damageIn;
            this.maxUses = maxUsesIn;
            this.xp = xpIn;
        }

        @Nullable
        @Override
        public MerchantOffer getOffer(@NotNull Entity entity, @NotNull RandomSource random) {
            ItemStack bottle = new ItemStack(ModItems.BLOOD_BOTTLE.get(), selling.getPrice(random));
            bottle.set(ModDataComponents.BOTTLE_BLOOD.get(), new BottleBlood(blood));
            return new MerchantOffer(new ItemCost(ModItems.HUMAN_HEART.get(), price.getPrice(random)), bottle, maxUses, xp, 0.2F);
        }
    }

    /**
     * Provides a trade selling a map to the nearest vampire forest for emeralds.
     * Only works for {@link ConvertedVillagerEntity}, otherwise it will create a null offer
     */
    public static class VampireForestMapTrade implements VillagerTrades.ItemListing {
        private final int emeraldCost;
        private final String displayName;
        private final Holder<MapDecorationType> decorationType;
        private final int maxUses;
        private final int villagerXp;

        public VampireForestMapTrade(int emeraldCost, String displayName, Holder<MapDecorationType> decorationType, int maxUses, int villagerXp) {
            this.emeraldCost = emeraldCost;
            this.displayName = displayName;
            this.decorationType = decorationType;
            this.maxUses = maxUses;
            this.villagerXp = villagerXp;
        }

        @Nullable
        public MerchantOffer getOffer(@NotNull Entity trader, @NotNull RandomSource random) {
            if (trader instanceof ConvertedVillagerEntity convertedVillager && trader.level() instanceof ServerLevel serverLevel) {
                //This may block for a short amount of time if the vampire villager has not completed its forest search yet
                return convertedVillager.getClosestVampireForest(trader.level(), trader.blockPosition()).map(blockPos -> {
                    ItemStack itemstack = MapItem.create(trader.level(), blockPos.getX(), blockPos.getZ(), (byte) 3, true, true);
                    MapItem.renderBiomePreviewMap(serverLevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockPos, "+", decorationType);
                    itemstack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));
                    return new MerchantOffer(new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemstack, this.maxUses, this.villagerXp, 0.2F);
                }).orElse(null);
            }
            return null;
        }
    }

    public static class TreasureMapForEmeralds implements VillagerTrades.ItemListing {
        private final int emeraldCost;
        private final TagKey<Structure> destination;
        private final String displayName;
        private final Holder<MapDecorationType> decorationType;
        private final int maxUses;
        private final int villagerXp;

        public TreasureMapForEmeralds(int emeraldCost, TagKey<Structure> destination, String displayName, Holder<MapDecorationType> decorationType, int maxUses, int villagerXp) {
            this.emeraldCost = emeraldCost;
            this.destination = destination;
            this.displayName = displayName;
            this.decorationType = decorationType;
            this.maxUses = maxUses;
            this.villagerXp = villagerXp;
        }

        @javax.annotation.Nullable
        @Override
        public MerchantOffer getOffer(Entity trader, @NotNull RandomSource random) {
            if (!(trader.level() instanceof ServerLevel serverlevel)) {
                return null;
            } else {
                BlockPos blockpos = serverlevel.findNearestMapStructure(this.destination, trader.blockPosition(), 100, true);
                if (blockpos != null) {
                    ItemStack itemstack = MapItem.create(serverlevel, blockpos.getX(), blockpos.getZ(), (byte)2, true, true);
                    MapItem.renderBiomePreviewMap(serverlevel, itemstack);
                    MapItemSavedData.addTargetDecoration(itemstack, blockpos, "+", this.decorationType);
                    itemstack.set(DataComponents.ITEM_NAME, Component.translatable(this.displayName));
                    return new MerchantOffer(
                            new ItemCost(Items.EMERALD, this.emeraldCost), Optional.of(new ItemCost(Items.COMPASS)), itemstack, this.maxUses, this.villagerXp, 0.2F
                    );
                } else {
                    return null;
                }
            }
        }
    }

    public static class Price {
        private final int min;
        private final int max;

        public Price(int minIn, int maxIn) {
            this.max = maxIn;
            this.min = minIn;
        }

        int getPrice(@NotNull RandomSource rand) {
            if (min >= max) {
                return min;
            } else {
                return min + rand.nextInt(max - min);
            }
        }
    }
}
