package de.teamlapen.vampirism.client.gui.components;

import com.mojang.blaze3d.systems.RenderSystem;
import de.teamlapen.vampirism.api.entity.player.actions.IAction;
import de.teamlapen.vampirism.api.entity.player.actions.ILastingAction;
import de.teamlapen.vampirism.api.entity.player.skills.IActionSkill;
import de.teamlapen.vampirism.api.entity.player.skills.ISkill;
import de.teamlapen.vampirism.api.util.SkillCallbacks;
import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModRegistries;
import de.teamlapen.vampirism.core.ModStats;
import de.teamlapen.vampirism.mixin.client.accessor.StatsScreenAccessor;
import de.teamlapen.vampirism.util.RegUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.achievement.StatsScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ActionStatisticsList extends ObjectSelectionList<ActionStatisticsList.SkillRow> {
    protected final List<StatType<ISkill<?>>> skillColumns;
    protected final List<StatType<IAction<?>>> actionColumns;
    private final ResourceLocation[] iconSprites = new ResourceLocation[] {
            VResourceLocation.mod("statistics/skills_unlocked"),
            VResourceLocation.mod("statistics/skills_forgotten"),
            VResourceLocation.mc("statistics/item_used"),
            VResourceLocation.mod("statistics/time"),
            null
    };
    private final ItemStack[] itemSprites = new ItemStack[] {
            null,
            null,
            null,
            null,
            Items.RED_BED.getDefaultInstance()
    };
    protected int headerPressed = -1;
    private final StatsScreenAccessor screen;
    private final Font font;
    protected final Comparator<SkillRow> itemStatSorter = new ActionRowComparator();
    @Nullable
    protected StatType<?> sortColumn;
    protected int sortOrder;

    public ActionStatisticsList(Minecraft minecraft, StatsScreen screen, int width, int height) {
        super(minecraft, width, height, 32, 20);
        this.skillColumns = List.of(ModStats.SKILL_UNLOCKED.get(), ModStats.SKILL_FORGOTTEN.get());
        this.actionColumns = List.of(ModStats.ACTION_USED.get(), ModStats.ACTION_TIME.get(), ModStats.ACTION_COOLDOWN_TIME.get());
        this.font = screen.font;
        this.screen = (StatsScreenAccessor) screen;
        Set<ISkill<?>> skills = new HashSet<>();
        skills.addAll(ModRegistries.SKILLS.stream().filter(x -> skillColumns.stream().mapToInt(y -> this.screen.getStats().getValue(y.get(x))).sum() > 0).collect(Collectors.toSet()));
        skills.addAll(ModRegistries.ACTIONS.stream().filter(x -> actionColumns.stream().mapToInt(y -> this.screen.getStats().getValue(y.get(x))).sum() > 0).map(IAction::asSkill).toList());
        skills.forEach(s -> addEntry(new SkillRow(s)));
    }

    int getColumnX(int p_331965_) {
        return 75 + 40 * p_331965_;
    }

    @Override
    protected void renderHeader(@NotNull GuiGraphics pGuiGraphics, int pX, int pY) {
        if (!this.minecraft.mouseHandler.isLeftPressed()) {
            this.headerPressed = -1;
        }
        for (int i = 0; i < this.iconSprites.length; i++) {
            ResourceLocation loc = this.headerPressed == i ? StatsScreenAccessor.getSLOT_SPRITE() : StatsScreenAccessor.getHEADER_SPRITE();
            pGuiGraphics.blitSprite(RenderType::guiTextured, loc, pX + getColumnX(i) - 18, pY + 1, 0, 18, 18);

        }


        if (this.sortColumn != null) {
            int j = getColumnX(this.getColumnIndex(this.sortColumn)) - 36;
            ResourceLocation resourcelocation1 = this.sortOrder == 1 ? StatsScreenAccessor.getSORT_UP_SPRITE() : StatsScreenAccessor.getSORT_DOWN_SPRITE();
            pGuiGraphics.blitSprite(RenderType::guiTextured, resourcelocation1, pX + j, pY + 1, 0, 18, 18);
        }

        for (int k = 0; k < this.iconSprites.length; ++k) {
            int l = this.headerPressed == k ? 1 : 0;
            ResourceLocation iconSprite = this.iconSprites[k];
            if (iconSprite != null) {
                pGuiGraphics.blitSprite(RenderType::guiTextured, this.iconSprites[k], pX + getColumnX(k) - 18 + l, pY + 1 + l, 0, 18, 18);
            } else {
                ItemStack itemSprite = this.itemSprites[k];
                if (itemSprite != null) {
                    pGuiGraphics.renderFakeItem(itemSprite, pX + getColumnX(k) - 18 + l, pY + 1 + l);
                }
            }
        }
    }

    @Override
    public int getRowWidth() {
        return 375;
    }

    @Override
    protected int scrollBarX() {
        return this.width / 2 + 140;
    }

    @Override
    public boolean mouseClicked(double p_313764_, double p_313832_, int p_313688_) {
        boolean flag = super.mouseClicked(p_313764_, p_313832_, p_313688_);
        return !flag && this.clickedHeader((int)(p_313764_ - ((double)this.getX() + (double)this.width / 2.0 - (double)this.getRowWidth() / 2.0)),
                (int)(p_313764_ - (double)this.getY()) + (int)this.scrollAmount() - 4) ? true : flag;
    }

    protected boolean clickedHeader(int p_97036_, int p_97037_) {
        this.headerPressed = -1;

        for (int i = 0; i < this.iconSprites.length; ++i) {
            int j = p_97036_ - getColumnX(i);
            if (j >= -36 && j <= 0) {
                this.headerPressed = i;
                break;
            }
        }

        if (this.headerPressed >= 0) {
            this.sortByColumn(this.getColumn(this.headerPressed));
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        } else {
            return false;
        }
    }

    private StatType<?> getColumn(int pIndex) {
        return pIndex < this.skillColumns.size() ? this.skillColumns.get(pIndex) : this.actionColumns.get(pIndex - this.skillColumns.size());
    }

    private int getColumnIndex(StatType<?> pStatType) {
        int i = this.skillColumns.indexOf(pStatType);
        if (i >= 0) {
            return i;
        } else {
            int j = this.actionColumns.indexOf(pStatType);
            return j >= 0 ? j + this.skillColumns.size() : -1;
        }
    }

    @Override
    protected void renderDecorations(@NotNull GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        if (pMouseY >= this.getY() && pMouseY <= this.getBottom()) {
            SkillRow statsscreen$itemstatisticslist$itemrow = this.getHovered();
            int i = (this.width - this.getRowWidth()) / 2;
            if (statsscreen$itemstatisticslist$itemrow != null) {
                if (pMouseX < i + 40 || pMouseX > i + 40 + 20) {
                    return;
                }

                ISkill<?> item = statsscreen$itemstatisticslist$itemrow.getSkill();
                pGuiGraphics.renderTooltip(this.font, this.getString(item), pMouseX, pMouseY);
            } else {
                Component component = null;
                int j = pMouseX - i;

                for (int k = 0; k < this.iconSprites.length; ++k) {
                    int l = getColumnX(k);
                    if (j >= l - 18 && j <= l) {
                        component = this.getColumn(k).getDisplayName();
                        break;
                    }
                }

                if (component != null) {
                    pGuiGraphics.renderTooltip(this.font, component, pMouseX, pMouseY);
                }
            }
        }
    }

    protected Component getString(ISkill<?> pItem) {
        return pItem.getName();
    }

    protected void sortByColumn(StatType<?> pStatType) {
        if (pStatType != this.sortColumn) {
            this.sortColumn = pStatType;
            this.sortOrder = -1;
        } else if (this.sortOrder == -1) {
            this.sortOrder = 1;
        } else {
            this.sortColumn = null;
            this.sortOrder = 0;
        }

        this.children().sort(this.itemStatSorter);
    }

    public class SkillRow extends ObjectSelectionList.Entry<SkillRow> {

        static final ResourceLocation SLOT_SPRITE = VResourceLocation.mc("container/slot");
        private final ISkill<?> skill;

        public SkillRow(ISkill<?> action) {
            this.skill = action;
        }

        public ISkill<?> getSkill() {
            return skill;
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public Component getNarration() {
            return Component.translatable("narrator.select", this.skill.getName());
        }

        @Override
        public void render(GuiGraphics pGuiGraphics, int pIndex, int pTop, int pLeft, int pWidth, int pHeight, int pMouseX, int pMouseY, boolean pHovering, float pPartialTick) {
            RenderSystem.setShaderColor(1, 1, 1, 1);
            pGuiGraphics.blitSprite(RenderType::guiTextured, SLOT_SPRITE, pLeft + 40 + 1, pTop + 1, 0, 18, 18);
            renderSkill(pGuiGraphics, pTop, pLeft);

            for (int i = 0; i < skillColumns.size(); ++i) {
                Stat<ISkill<?>> stat;
                if (getSkill() instanceof SkillCallbacks.EmptyActionSkill<?> empty) {
                    stat = null;
                } else if (getSkill() instanceof ISkill<?>) {
                    stat = skillColumns.get(i).get(this.getSkill());
                } else {
                    stat = null;
                }

                this.renderStat(pGuiGraphics, stat, pLeft + getColumnX(i), pTop, pIndex % 2 == 0);
            }

            for (int j = 0; j < actionColumns.size(); ++j) {
                Stat<IAction<?>> stat;
                if (this.skill instanceof IActionSkill<?> actionSkill) {
                    StatType<IAction<?>> stats = actionColumns.get(j);
                    IAction<?> action = actionSkill.action();
                    if (stats != ModStats.ACTION_TIME.get() || action instanceof ILastingAction<?>) {
                        stat = actionColumns.get(j).get(actionSkill.action());
                    } else {
                        stat = null;
                    }
                } else {
                    stat = null;
                }
                this.renderStat(pGuiGraphics, stat, pLeft + getColumnX(j + skillColumns.size()), pTop, pIndex % 2 == 0);
            }
        }

        public void renderSkill(GuiGraphics pGuiGraphics, int pTop, int pLeft) {
            ResourceLocation texture;
            if (skill instanceof IActionSkill<?> actionSkill) {
                ResourceLocation id = RegUtil.id(actionSkill.action());
                texture = id.withPath("textures/actions/" + id.getPath() + ".png");
            } else {
                ResourceLocation id = RegUtil.id(skill);
                texture = id.withPath("textures/skills/" + id.getPath() + ".png");
            }
            pGuiGraphics.blit(RenderType::guiTextured, texture, pLeft + 40 + 1 + 1, pTop + 1 + 1, 0, 0, 0, 16, 16, 16, 16);
        }

        protected void renderStat(GuiGraphics pGuiGraphics, @Nullable Stat<?> pStat, int pX, int pY, boolean pEvenRow) {
            Component component = pStat == null ? StatsScreenAccessor.getNO_VALUE_DISPLAY() : Component.literal(pStat.format(screen.getStats().getValue(pStat)));
            pGuiGraphics.drawString(font, component, pX - font.width(component), pY + 5, pEvenRow ? 16777215 : 9474192);
        }
    }

    public class ActionRowComparator implements Comparator<SkillRow> {
        public int compare(SkillRow pRow1, SkillRow pRow2) {
            ISkill<?> item = pRow1.getSkill();
            ISkill<?> item1 = pRow2.getSkill();
            int i;
            int j;
            if (ActionStatisticsList.this.sortColumn == null) {
                i = 0;
                j = 0;
            } else if (actionColumns.contains(sortColumn)) {
                StatType<IAction<?>> stattype1 = (StatType<IAction<?>>) ActionStatisticsList.this.sortColumn;
                i = item instanceof IActionSkill<?> actionSkill ? screen.getStats().getValue(stattype1, actionSkill.action()) : -1;
                j = item1 instanceof IActionSkill<?> actionSkill ? screen.getStats().getValue(stattype1, actionSkill.action()) : -1;
            } else {
                StatType<ISkill<?>> stattype1 = (StatType<ISkill<?>>) ActionStatisticsList.this.sortColumn;
                i = screen.getStats().getValue(stattype1, item);
                j = screen.getStats().getValue(stattype1, item1);
            }

            return i == j ? ActionStatisticsList.this.sortOrder * Integer.compare(getId(item), getId(item1)) : ActionStatisticsList.this.sortOrder * Integer.compare(i, j);
        }

        private int getId(ISkill<?> action) {
            return ModRegistries.SKILLS.getId(action);
        }
    }
}
