package de.teamlapen.vampirism.recipes;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.api.VampirismRegistries;
import de.teamlapen.vampirism.api.entity.player.hunter.IHunterPlayer;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.entity.player.skills.ISkillHandler;
import de.teamlapen.vampirism.core.ModRecipes;
import de.teamlapen.vampirism.core.ModRegistries;
import de.teamlapen.vampirism.util.Helper;
import de.teamlapen.vampirism.util.StreamCodecExtension;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public class AlchemicalCauldronRecipe implements Recipe<AlchemicalCauldronRecipeInput> {

    protected final String group;
    private final Either<Ingredient, FluidStack> fluid;
    protected final Ingredient ingredient;
    protected final ItemStack result;
    @NotNull
    private final List<Holder<ISkill<?>>> skills;
    private final int reqLevel;
    protected final float experience;
    protected final int cookingTime;
    @Nullable
    private PlacementInfo placementInfo;

    public AlchemicalCauldronRecipe(@NotNull String groupIn, @NotNull Ingredient ingredientIn, Either<Ingredient, FluidStack> fluidIn, @NotNull ItemStack resultIn, @NotNull List<Holder<ISkill<?>>> skillsIn, int reqLevelIn, int cookTimeIn, float exp) {
        this.group = groupIn;
        this.ingredient = ingredientIn;
        this.result = resultIn;
        this.experience = exp;
        this.cookingTime = cookTimeIn;
        this.fluid = fluidIn;
        this.skills = skillsIn;
        this.reqLevel = reqLevelIn;
    }

    public boolean canBeCooked(int level, @NotNull ISkillHandler<IHunterPlayer> skillHandler) {
        if (level < reqLevel) return false;
        return Helper.areSkillsEnabled(skillHandler, skills);
    }

    @Override
    public @NotNull PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(this.ingredient);
        }
        return this.placementInfo;
    }

    @Override
    public @NotNull RecipeBookCategory recipeBookCategory() {
        return ModRecipes.ALCHEMICAL_CAULDRON_CATEGORY.get();
    }

    @Override
    public ItemStack assemble(AlchemicalCauldronRecipeInput p_345149_, HolderLookup.Provider p_346030_) {
        return this.result.copy();
    }

    public float getExperience() {
        return this.experience;
    }

    public int getCookingTime() {
        return this.cookingTime;
    }

    @Override
    public RecipeType<AlchemicalCauldronRecipe> getType() {
        return ModRecipes.ALCHEMICAL_CAULDRON_TYPE.get();
    }

    public Either<Ingredient, FluidStack> getFluid() {
        return fluid;
    }

    public @NotNull Ingredient getIngredient() {
        return ingredient;
    }

    public int getRequiredLevel() {
        return reqLevel;
    }

    @NotNull
    public List<Holder<ISkill<?>>> getRequiredSkills() {
        return skills;
    }

    public ItemStack result() {
        return result;
    }

    public String group() {
        return this.group;
    }

    @NotNull
    @Override
    public RecipeSerializer<AlchemicalCauldronRecipe> getSerializer() {
        return ModRecipes.ALCHEMICAL_CAULDRON.get();
    }

    @Override
    public boolean matches(@NotNull AlchemicalCauldronRecipeInput inv, @NotNull Level worldIn) {
        boolean match = this.ingredient.test(inv.ingredient());
        Boolean fluidMatch = fluid.map(ingredient1 -> ingredient1.test(inv.fluid()), fluid1 -> FluidUtil.getFluidContained(inv.fluid()).map(s -> FluidStack.isSameFluidSameComponents(fluid1, s) && fluid1.getAmount() < s.getAmount()).orElse(false));
        return switch (inv.testType()) {
            case INPUT_1 -> match;
            case INPUT_2 -> fluidMatch;
            case BOTH -> match && fluidMatch;
        } && inv.skills().map(s -> s.areSkillsEnabled(this.skills)).orElse(true);
    }

    public static class Serializer implements RecipeSerializer<AlchemicalCauldronRecipe> {

        public static final MapCodec<AlchemicalCauldronRecipe> CODEC = RecordCodecBuilder.mapCodec(inst ->
                inst.group(
                        Codec.STRING.optionalFieldOf("group", "").forGetter(p_300832_ -> p_300832_.group),
                        Ingredient.CODEC.fieldOf("ingredient").forGetter(p_300833_ -> p_300833_.ingredient),
                        Codec.either(Ingredient.CODEC, FluidStack.CODEC).fieldOf("fluid").forGetter(s -> s.fluid),
                        ItemStack.CODEC.fieldOf("result").forGetter(p_300827_ -> p_300827_.result),
                        ModRegistries.SKILLS.holderByNameCodec().listOf().optionalFieldOf("skill", Collections.emptyList()).forGetter(p -> p.skills),
                        Codec.INT.optionalFieldOf("level", 1).forGetter(p -> p.reqLevel),
                        Codec.INT.optionalFieldOf("cookTime", 200).forGetter(p -> p.cookingTime),
                        Codec.FLOAT.optionalFieldOf("experience", 0.2F).forGetter(p -> p.experience)
                ).apply(inst, AlchemicalCauldronRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, AlchemicalCauldronRecipe> STREAM_CODEC = StreamCodecExtension.composite(
                ByteBufCodecs.STRING_UTF8, AlchemicalCauldronRecipe::group,
                Ingredient.CONTENTS_STREAM_CODEC, AlchemicalCauldronRecipe::getIngredient,
                ByteBufCodecs.either(Ingredient.CONTENTS_STREAM_CODEC, FluidStack.STREAM_CODEC), AlchemicalCauldronRecipe::getFluid,
                ItemStack.STREAM_CODEC, AlchemicalCauldronRecipe::result,
                ByteBufCodecs.holderRegistry(VampirismRegistries.Keys.SKILL).apply(ByteBufCodecs.list()), AlchemicalCauldronRecipe::getRequiredSkills,
                ByteBufCodecs.INT, AlchemicalCauldronRecipe::getRequiredLevel,
                ByteBufCodecs.INT, AlchemicalCauldronRecipe::getCookingTime,
                ByteBufCodecs.FLOAT, AlchemicalCauldronRecipe::getExperience,
                AlchemicalCauldronRecipe::new
        );

        @Override
        public @NotNull MapCodec<AlchemicalCauldronRecipe> codec() {
            return CODEC;
        }

        @Override
        public @NotNull StreamCodec<RegistryFriendlyByteBuf, AlchemicalCauldronRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
