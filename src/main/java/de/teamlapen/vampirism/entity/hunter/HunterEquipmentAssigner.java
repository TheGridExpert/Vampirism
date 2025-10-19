package de.teamlapen.vampirism.entity.hunter;

import de.teamlapen.vampirism.core.ModItems;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.NotNull;
import com.mojang.datafixers.util.Pair;

@SuppressWarnings("unchecked")
public class HunterEquipmentAssigner {

    private static final Pair<ItemLike, Integer>[] MELEE_MAIN_WEAPONS = new Pair[]{
            Pair.of(ModItems.HUNTER_AXE_NORMAL.get(), 2),
            Pair.of(ModItems.HUNTER_AXE_ENHANCED.get(), 4),
            Pair.of(ModItems.HUNTER_AXE_ULTIMATE.get(), 10)
    };

    private static final Pair<ItemLike, Integer>[] RANGED_MAIN_WEAPONS = new Pair[]{
            Pair.of(ModItems.BASIC_CROSSBOW.get(), 2),
            Pair.of(ModItems.BASIC_DOUBLE_CROSSBOW.get(), 3),
            Pair.of(ModItems.ENHANCED_CROSSBOW.get(), 5),
            Pair.of(ModItems.ENHANCED_DOUBLE_CROSSBOW.get(), 7)
    };

    private static final Pair<ItemLike, Integer>[] SECONDARY_WEAPONS = new Pair[]{
            Pair.of(ModItems.CRUCIFIX_NORMAL.get(), 2),
            Pair.of(ModItems.CRUCIFIX_ENHANCED.get(), 3),
            Pair.of(ModItems.CRUCIFIX_ULTIMATE.get(), 5)
    };

    private static final ItemLike[] RANGED_SPECIAL_ARROWS = {
            ModItems.CROSSBOW_ARROW_GARLIC.get(),
            ModItems.CROSSBOW_ARROW_VAMPIRE_KILLER.get(),
            ModItems.CROSSBOW_ARROW_BLEEDING.get()
    };

    private static final Pair<ItemLike, Integer>[][] MELEE_ARMOR = new Pair[][]{
            {
                Pair.of(ModItems.HUNTER_COAT_CHEST_NORMAL.get(), 3),
                Pair.of(ModItems.HUNTER_COAT_CHEST_ENHANCED.get(), 5),
                Pair.of(ModItems.HUNTER_COAT_CHEST_ULTIMATE.get(), 7)
            },
            {
                Pair.of(ModItems.HUNTER_COAT_FEET_NORMAL.get(), 2),
                Pair.of(ModItems.HUNTER_COAT_FEET_ENHANCED.get(), 3),
                Pair.of(ModItems.HUNTER_COAT_FEET_ULTIMATE.get(), 5)},
            {
                Pair.of(ModItems.HUNTER_COAT_LEGS_NORMAL.get(), 2),
                Pair.of(ModItems.HUNTER_COAT_LEGS_ENHANCED.get(), 4),
                Pair.of(ModItems.HUNTER_COAT_LEGS_ULTIMATE.get(), 6)
            },
            {
                Pair.of(ModItems.HUNTER_COAT_HEAD_NORMAL.get(), 2),
                Pair.of(ModItems.HUNTER_COAT_HEAD_ENHANCED.get(), 4),
                Pair.of(ModItems.HUNTER_COAT_HEAD_ULTIMATE.get(), 6)
            }
    };

    private static final Pair<ItemLike, Integer>[][] RANGED_ARMOR = new Pair[][]{
            {
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_CHEST_NORMAL.get(), 3),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_CHEST_ENHANCED.get(), 5),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_CHEST_ULTIMATE.get(), 7)
            },
            {
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_FEET_NORMAL.get(), 2),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_FEET_ENHANCED.get(), 3),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_FEET_ULTIMATE.get(), 5)
            },
            {
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_LEGS_NORMAL.get(), 2),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_LEGS_ENHANCED.get(), 4),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_LEGS_ULTIMATE.get(), 6)
            },
            {
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_HEAD_NORMAL.get(), 2),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_HEAD_ENHANCED.get(), 4),
                Pair.of(ModItems.ARMOR_OF_SWIFTNESS_HEAD_ULTIMATE.get(), 6)
            }
    };

    public static void assignRandomEquipment(@NotNull Hunter hunter, ServerLevelAccessor level, RandomSource random) {
        boolean isMelee = hunter.isMeleeClass();
        Pair<ItemLike, Integer>[][] armorSet = isMelee ? MELEE_ARMOR : RANGED_ARMOR;
        Pair<ItemLike, Integer>[] mainWeapons = isMelee ? MELEE_MAIN_WEAPONS : RANGED_MAIN_WEAPONS;

        int armorTokens = hunter.calculateTokens(random, level.getDifficulty().ordinal() * 2, 20);
        int weaponTokens = hunter.calculateTokens(random, 0, 10);
        int secondaryTokens = hunter.calculateTokens(random, 0, 5);

        float[] armorWeights = {0.3f, 0.2f, 0.25f, 0.25f};

        for (int i = 0; i < armorSet.length; i++) {
            Pair<ItemLike, Integer>[] slotTiers = armorSet[i];
            int slotBudget = Math.max(1, (int) (armorTokens * armorWeights[i]));
            for (int t = slotTiers.length - 1; t >= 0; t--) {
                if (slotBudget >= slotTiers[t].getSecond()) {
                    setArmorSlot(hunter, i, new ItemStack(slotTiers[t].getFirst()));
                    armorTokens -= slotTiers[t].getSecond();
                    break;
                }
            }
        }

        boolean weaponEquipped = false;
        for (int i = mainWeapons.length - 1; i >= 0; i--) {
            if (weaponTokens >= mainWeapons[i].getSecond()) {
                hunter.setSheathedWeapon(new ItemStack(mainWeapons[i].getFirst()), 0);
                weaponEquipped = true;
                break;
            }
        }
        if (!weaponEquipped) {
            hunter.setSheathedWeapon(new ItemStack(mainWeapons[0].getFirst()), 0);
        }

        for (int i = SECONDARY_WEAPONS.length - 1; i >= 0; i--) {
            if (secondaryTokens >= SECONDARY_WEAPONS[i].getSecond()) {

                break;
            }
        }

        if (hunter.isRangedClass() && random.nextFloat() <= 0.3f) {
            Item arrow = chooseRandomArrow(level.getDifficulty(), random);
            hunter.setArrowType(arrow);
        }
    }

    private static void setArmorSlot(Hunter hunter, int index, ItemStack item) {
        switch (index) {
            case 0 -> hunter.setItemSlot(EquipmentSlot.CHEST, item);
            case 1 -> hunter.setItemSlot(EquipmentSlot.FEET, item);
            case 2 -> hunter.setItemSlot(EquipmentSlot.LEGS, item);
            case 3 -> hunter.setItemSlot(EquipmentSlot.HEAD, item);
        }
    }

    private static Item chooseRandomArrow(Difficulty difficulty, RandomSource random) {
        float baseChance = switch (difficulty) {
            case PEACEFUL -> 0.02f;
            case EASY -> 0.05f;
            case NORMAL -> 0.15f;
            case HARD -> 0.3f;
        };
        if (random.nextFloat() >= baseChance) {
            return ModItems.CROSSBOW_ARROW_NORMAL.get();
        }
        return RANGED_SPECIAL_ARROWS[random.nextInt(RANGED_SPECIAL_ARROWS.length)].asItem();
    }
}
