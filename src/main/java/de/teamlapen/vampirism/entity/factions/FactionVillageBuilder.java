package de.teamlapen.vampirism.entity.factions;

import de.teamlapen.vampirism.api.entity.CaptureEntityEntry;
import de.teamlapen.vampirism.api.entity.ITaskMasterEntity;
import de.teamlapen.vampirism.api.entity.factions.IFactionVillage;
import de.teamlapen.vampirism.api.entity.factions.IFactionVillageBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class FactionVillageBuilder implements IFactionVillageBuilder {

    Holder<MobEffect> badOmenEffect = null;
    Function<HolderLookup.Provider, ItemStack> bannerStack = (provider) -> new ItemStack(Items.WHITE_BANNER);
    List<CaptureEntityEntry<?>> captureEntities = Collections.emptyList();
    Supplier<VillagerProfession> factionVillageProfession = () -> VillagerProfession.NONE;
    Class<? extends Mob> guardSuperClass = Mob.class;
    Supplier<EntityType<? extends ITaskMasterEntity>> taskMasterEntity = () -> null;
    Supplier<? extends Block> fragileTotem = () -> Blocks.AIR;
    Supplier<? extends Block> craftedTotem = () -> Blocks.AIR;

    @Override
    public @NotNull FactionVillageBuilder badOmenEffect(Holder<MobEffect> badOmenEffect) {
        this.badOmenEffect = badOmenEffect;
        return this;
    }

    @Override
    public @NotNull FactionVillageBuilder banner(Function<HolderLookup.Provider, ItemStack> bannerItem) {
        this.bannerStack = bannerItem;
        return this;
    }

    @Override
    public @NotNull FactionVillageBuilder captureEntities(List<CaptureEntityEntry<?>> captureEntities) {
        this.captureEntities = captureEntities;
        return this;
    }

    @Override
    public @NotNull FactionVillageBuilder factionVillagerProfession(Supplier<VillagerProfession> profession) {
        this.factionVillageProfession = profession;
        return this;
    }

    @Override
    public @NotNull FactionVillageBuilder guardSuperClass(Class<? extends Mob> clazz) {
        this.guardSuperClass = clazz;
        return this;
    }

    @Override
    public <Z extends Entity & ITaskMasterEntity> @NotNull FactionVillageBuilder taskMaster(Supplier<EntityType<Z>> taskmaster) {
        //noinspection unchecked
        this.taskMasterEntity = (Supplier<EntityType<? extends ITaskMasterEntity>>) (Object) taskmaster;
        return this;
    }

    @Override
    public @NotNull FactionVillageBuilder totem(Supplier<? extends Block> fragile, Supplier<? extends Block> crafted) {
        this.fragileTotem = fragile;
        this.craftedTotem = crafted;
        return this;
    }

    @Override
    public @NotNull IFactionVillage build() {
        return new FactionVillage(this);
    }
}
