package de.teamlapen.vampirism.common.world.entity.hunter;

import de.teamlapen.faction.api.factions.IPlayableFaction;
import de.teamlapen.faction.api.factions.lord.LordTitles;
import de.teamlapen.faction.common.core.FactionDataComponents;
import de.teamlapen.vampirism.VampirismMod;
import de.teamlapen.vampirism.api.world.entity.VampireBookLootProvider;
import de.teamlapen.vampirism.api.world.items.components.IVampireBook;
import de.teamlapen.vampirism.common.core.ModAttachments;
import de.teamlapen.vampirism.common.core.ModEntities;
import de.teamlapen.vampirism.common.core.ModFactions;
import de.teamlapen.vampirism.common.util.SupporterManager;
import de.teamlapen.vampirism.common.util.supporter.Supporter;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterGroupData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class AdvancedHunter extends Hunter implements VampireBookLootProvider {

    private static final int LORD_SQUAD_SIZE = 7;

    public AdvancedHunter(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static void spawnInGroup(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, HunterGroupData group, BlockPos pos) {
        AdvancedHunter hunter = ModEntities.ADVANCED_HUNTER_NEW.get().create(level.getLevel(), spawnReason);
        if (hunter == null) return;

        hunter.snapTo(pos, level.getRandom().nextFloat() * 360.0F, 0.0F);
        hunter.finalizeSpawn(level, difficulty, spawnReason, group);
        level.addFreshEntityWithPassengers(hunter);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        HunterGroupData group = HunterGroupData.resolve(spawnGroupData);
        group.claimAdvanced();
        setData(ModAttachments.SUPPORTER, VampirismMod.services().supporterManager().getRandomHunter(level.getRandom()));
        return super.finalizeSpawn(level, difficulty, spawnReason, group);
    }

    @Override
    protected HunterClassType chooseClass(HunterGroupData group, RandomSource random) {
        Supporter.Overrides overrides = getData(ModAttachments.SUPPORTER).overrides();
        if (overrides.dualWielding()) return HunterClassType.MELEE;
        return overrides.hunterClass().orElseGet(() -> super.chooseClass(group, random));
    }

    @Override
    public boolean isDualWielding() {
        return getData(ModAttachments.SUPPORTER).overrides().dualWielding();
    }

    @Override
    protected int calculateFactionLevel(float difficultyModifier) {
        return MAX_LEVEL + Math.min(MAX_LORD_LEVEL, 1 + (int) (difficultyModifier * MAX_LORD_LEVEL));
    }

    @Override
    public int getMaxSquadSize() {
        return LORD_SQUAD_SIZE + (getLordLevel() - 1) / 2;
    }

    public int getLordLevel() {
        return Math.clamp(getFactionLevel() - MAX_LEVEL, 1, MAX_LORD_LEVEL);
    }

    public @Nullable Component getTitle() {
        return ModFactions.HUNTER.components().getOrDefault(FactionDataComponents.LORD_TITLES, LordTitles.EMPTY).get(getLordLevel(), IPlayableFaction.TitleGender.UNKNOWN);
    }

    public Component getPersonalName() {
        Supporter supporter = getData(ModAttachments.SUPPORTER);
        String name = supporter.name().getString();
        if (name.isEmpty()) return super.getTypeName();
        return supporter.player().isEmpty() ? supporter.name() : Component.translatableWithFallback(SupporterManager.getNameKey(supporter.player()), name);
    }

    @Override
    protected Component getTypeName() {
        Component title = getTitle();
        return title == null ? getPersonalName() : Component.empty().append(title).append(" ").append(getPersonalName());
    }

    @Override
    public Optional<Holder<IVampireBook>> getBookLootId() {
        return getData(ModAttachments.SUPPORTER).book();
    }

    @Override
    public boolean shouldShowName() {
        return true;
    }
}
