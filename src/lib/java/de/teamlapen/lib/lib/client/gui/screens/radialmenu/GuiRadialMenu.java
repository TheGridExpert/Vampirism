/*
 * Note: This code has been modified from David Quintana's solution.
 * Below is the required copyright notice.
 * Copyright (c) 2015, David Quintana <gigaherz@gmail.com>
 * All rights reserved.
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the author nor the
 *       names of the contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package de.teamlapen.lib.lib.client.gui.screens.radialmenu;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.teamlapen.lib.util.GuiGraphicsAccessor;
import de.teamlapen.lib.util.KeyMappingAccessor;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Input;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.MovementInputUpdateEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

//@EventBusSubscriber(Dist.CLIENT)
public abstract class GuiRadialMenu<T> extends Screen {
    private static final float PRECISION = 5.0f;
    protected static final int MAX_SLOTS = 30;

    protected boolean closing;
    private final RadialMenu<T> radialMenu;
    private final boolean allowMouseDirection;
    protected final List<IRadialMenuSlot<T>> radialMenuSlots;
    protected final float OPEN_ANIMATION_LENGTH = 0.20f;
    protected float totalTime;
    protected float prevTick;
    protected float extraTick;
    /**
     * Zero-Based index
     */
    protected int selectedItem;


    public GuiRadialMenu(RadialMenu<T> radialMenu) {
        this(radialMenu, false);
    }

    public GuiRadialMenu(RadialMenu<T> radialMenu, boolean allowMouseDirection) {
        super(Component.literal(""));
        this.radialMenu = radialMenu;
        this.allowMouseDirection = allowMouseDirection;
        this.radialMenuSlots = this.radialMenu.getRadialMenuSlots();
        this.closing = false;
        this.minecraft = Minecraft.getInstance();
        this.selectedItem = -1;
    }

//    @SubscribeEvent
//    public static void updateInputEvent(MovementInputUpdateEvent event) {
//        if (Minecraft.getInstance().screen instanceof GuiRadialMenu<?> screen) {
//
//            screen.processInputEvent(event);
//        }
//    }

    @Override
    public void tick() {
        if (totalTime != OPEN_ANIMATION_LENGTH) {
            extraTick++;
        }
    }

    protected boolean isMouseOverMenuItems(double mouseDistanceToCenterOfScreen, float radiusIn, float radiusOut) {
        return allowMouseDirection ? mouseDistanceToCenterOfScreen >= 10 : mouseDistanceToCenterOfScreen >= radiusIn && mouseDistanceToCenterOfScreen < radiusOut;
    }

    @Override
    public void render(@NotNull GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        super.render(graphics, mouseX, mouseY, partialTicks);
        PoseStack pose = graphics.pose();

        float openAnimation = closing ? 1.0f - totalTime / OPEN_ANIMATION_LENGTH : totalTime / OPEN_ANIMATION_LENGTH;
        float currTick = minecraft.getDeltaTracker().getGameTimeDeltaTicks();
        totalTime += (currTick + extraTick - prevTick) / 20f;
        extraTick = 0;
        prevTick = currTick;


        float animProgress = Mth.clamp(openAnimation, 0, 1);
        animProgress = (float) (1 - Math.pow(1 - animProgress, 3));
        float radiusIn = Math.max(0.1f, 45 * animProgress);
        float radiusOut = radiusIn * 2;
        float itemRadius = (radiusIn + radiusOut) * 0.5f;

        int centerOfScreenX = width / 2;
        int centerOfScreenY = height / 2;
        int numberOfSlices = Math.min(MAX_SLOTS, radialMenuSlots.size());

        double mousePositionInDegreesInRelationToCenterOfScreen = Math.toDegrees(Math.atan2(mouseY - centerOfScreenY, mouseX - centerOfScreenX));
        double mouseDistanceToCenterOfScreen = Math.sqrt(Math.pow(mouseX - centerOfScreenX, 2) + Math.pow(mouseY - centerOfScreenY, 2));
        float slot0 = (((0 - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
        if (mousePositionInDegreesInRelationToCenterOfScreen < slot0) {
            mousePositionInDegreesInRelationToCenterOfScreen += 360;
        }

        pose.pushPose();
        RenderSystem.setShaderColor(1,1,1,1);


        boolean hasMouseOver = false;
        int mousedOverSlot = -1;

        if (!closing) {
            selectedItem = -1;
            for (int i = 0; i < numberOfSlices; i++) {
                float sliceBorderLeft = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
                float sliceBorderRight = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
                if (mousePositionInDegreesInRelationToCenterOfScreen >= sliceBorderLeft && mousePositionInDegreesInRelationToCenterOfScreen < sliceBorderRight && isMouseOverMenuItems(mouseDistanceToCenterOfScreen, radiusIn, radiusOut)) {
                    selectedItem = i;
                    break;
                }
            }
        }


        for (int i = 0; i < numberOfSlices; i++) {
            float sliceBorderLeft = (((i - 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
            float sliceBorderRight = (((i + 0.5f) / (float) numberOfSlices) + 0.25f) * 360;
            var item = this.radialMenuSlots.get((i + numberOfSlices / 2) % numberOfSlices);
            if (selectedItem == i) {
                drawSlice(item, true, graphics, centerOfScreenX, centerOfScreenY, 10, radiusIn, radiusOut, sliceBorderLeft, sliceBorderRight, 63, 161, 191, 60);
                hasMouseOver = true;
                mousedOverSlot = selectedItem;
            } else {
                drawSlice(item, false, graphics, centerOfScreenX, centerOfScreenY, 10, radiusIn, radiusOut, sliceBorderLeft, sliceBorderRight, 0, 0, 0, 64);
            }
        }

        RenderSystem.setShaderColor(1,1,1,1);
        pose.translate(0, 0, 50);

        if (hasMouseOver && mousedOverSlot != -1) {
            int adjusted = ((mousedOverSlot + (numberOfSlices / 2 + 1)) % numberOfSlices) - 1;
            adjusted = adjusted == -1 ? numberOfSlices - 1 : adjusted;
            Component component = radialMenuSlots.get(adjusted).slotName();
            graphics.drawCenteredString(font, component, width / 2, (height - font.lineHeight) / 2, Optional.ofNullable(component.getStyle().getColor()).map(TextColor::getValue).orElse(16777215));
        }


        pose.pushPose();
        pose.translate(0, 0, 50);

        for (int i = 0; i < numberOfSlices; i++) {
            ItemStack stack = new ItemStack(Blocks.DIRT);
            float angle1 = ((i / (float) numberOfSlices) - 0.25f) * 2 * (float) Math.PI;
            if (numberOfSlices % 2 != 0) {
                angle1 += (float) (Math.PI / numberOfSlices);
            }
            float posX = centerOfScreenX - 8 + itemRadius * (float) Math.cos(angle1);
            float posY = centerOfScreenY - 8 + itemRadius * (float) Math.sin(angle1);

//            RenderSystem.disableDepthTest();

            T primarySlotIcon = radialMenuSlots.get(i).primarySlotIcon();
            List<T> secondarySlotIcons = radialMenuSlots.get(i).secondarySlotIcons();
            if (primarySlotIcon != null) {
                RenderSystem.setShaderColor(1,1,1,1);

                radialMenu.drawIcon(primarySlotIcon, graphics, (int) posX, (int) posY, 16);
                if (secondarySlotIcons != null && !secondarySlotIcons.isEmpty()) {
                    drawSecondaryIcons(graphics, (int) posX, (int) posY, secondarySlotIcons);
                }
            }
            drawSliceName(graphics, String.valueOf(i + 1), stack, (int) posX, (int) posY);
        }
        pose.popPose();
        pose.popPose();

        if (mousedOverSlot != -1) {
            int adjusted = ((mousedOverSlot + (numberOfSlices / 2 + 1)) % numberOfSlices) - 1;
            adjusted = adjusted == -1 ? numberOfSlices - 1 : adjusted;
            selectedItem = adjusted;
        }
    }

    public void drawSecondaryIcons(GuiGraphics graphics, int positionXOfPrimaryIcon, int positionYOfPrimaryIcon, List<T> secondarySlotIcons) {
        if (!radialMenu.isShowMoreSecondaryItems()) {
            drawSecondaryIcon(graphics, secondarySlotIcons.getFirst(), positionXOfPrimaryIcon, positionYOfPrimaryIcon, radialMenu.getSecondaryIconStartingPosition());
        } else {
            SecondaryIconPosition currentSecondaryIconPosition = radialMenu.getSecondaryIconStartingPosition();
            for (T secondarySlotIcon : secondarySlotIcons) {
                drawSecondaryIcon(graphics, secondarySlotIcon, positionXOfPrimaryIcon, positionYOfPrimaryIcon, currentSecondaryIconPosition);
                currentSecondaryIconPosition = SecondaryIconPosition.getNextPositon(currentSecondaryIconPosition);
            }
        }
    }

    public void drawSecondaryIcon(GuiGraphics graphics, T item, int positionXOfPrimaryIcon, int positionYOfPrimaryIcon, SecondaryIconPosition secondaryIconPosition) {
        int offset = radialMenu.getOffset();
        switch (secondaryIconPosition) {
            case NORTH -> radialMenu.drawIcon(item, graphics, positionXOfPrimaryIcon + offset, positionYOfPrimaryIcon - 14 + offset, 10);
            case EAST -> radialMenu.drawIcon(item, graphics, positionXOfPrimaryIcon + 14 + offset, positionYOfPrimaryIcon + offset, 10);
            case SOUTH -> radialMenu.drawIcon(item, graphics, positionXOfPrimaryIcon + offset, positionYOfPrimaryIcon + 14 + offset, 10);
            case WEST -> radialMenu.drawIcon(item, graphics, positionXOfPrimaryIcon - 14 + offset, positionYOfPrimaryIcon + offset, 10);
        }
    }

    public void drawSliceName(GuiGraphics graphics, String sliceName, ItemStack stack, int posX, int posY) {
        if (!radialMenu.isShowMoreSecondaryItems()) {
            graphics.renderItemDecorations(font, stack, posX + 5, posY, sliceName);
        } else {
            graphics.renderItemDecorations(font, stack, posX + 5, posY + 5, sliceName);
        }
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int modifiers) {
        int adjustedKey = key - 48;
        if (adjustedKey >= 0 && adjustedKey <= radialMenuSlots.size()) {
            selectedItem = adjustedKey == 0 ? radialMenuSlots.size() : adjustedKey;
            selectedItem = selectedItem - 1; // Offset by 1 because 0 based indexing but users see 1 indexed
            mouseClicked(0, 0, 0);
            return true;
        }
        return super.keyPressed(key, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
            this.onClose();
        } else if (mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            if (this.selectedItem != -1) {
                radialMenu.setCurrentSlot(selectedItem);
                minecraft.player.closeContainer();
            }
        }
        return true;
    }

    public void drawSlice(IRadialMenuSlot<T> slot, boolean highlighted, GuiGraphics guiGraphics, float x, float y, float z, float radiusIn, float radiusOut, float startAngle, float endAngle, int r, int g, int b, int a) {
        float angle = endAngle - startAngle;
        int sections = Math.max(1, Mth.ceil(angle / PRECISION));

        startAngle = (float) Math.toRadians(startAngle);
        endAngle = (float) Math.toRadians(endAngle);
        angle = endAngle - startAngle;

        var buffer = ((GuiGraphicsAccessor)guiGraphics).bufferSource().getBuffer(RenderType.gui());

        for (int i = 0; i < sections; i++) {
            float angle1 = startAngle + (i / (float) sections) * angle;
            float angle2 = startAngle + ((i + 1) / (float) sections) * angle;

            float pos1InX = x + radiusIn * (float) Math.cos(angle1);
            float pos1InY = y + radiusIn * (float) Math.sin(angle1);
            float pos1OutX = x + radiusOut * (float) Math.cos(angle1);
            float pos1OutY = y + radiusOut * (float) Math.sin(angle1);
            float pos2OutX = x + radiusOut * (float) Math.cos(angle2);
            float pos2OutY = y + radiusOut * (float) Math.sin(angle2);
            float pos2InX = x + radiusIn * (float) Math.cos(angle2);
            float pos2InY = y + radiusIn * (float) Math.sin(angle2);

            buffer.addVertex(pos1OutX, pos1OutY, z).setColor(r, g, b, a);
            buffer.addVertex(pos1InX, pos1InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2InX, pos2InY, z).setColor(r, g, b, a);
            buffer.addVertex(pos2OutX, pos2OutY, z).setColor(r, g, b, a);
        }

        guiGraphics.flush();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

//    @SubscribeEvent
//    protected static void updateMovement(InputEvent.Key event) {
//        if (Minecraft.getInstance().screen instanceof GuiRadialMenu<?>) {
//            InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());
//            Options options = Minecraft.getInstance().options;
//            Stream.of(options.keyUp, options.keyDown, options.keyLeft, options.keyRight, options.keyJump, options.keyShift, options.keySprint).filter(x -> x.getKey() == key).forEach(x -> {
//                x.setDown(true);
//                ((KeyMappingAccessor) x).clicked();
//            });
//        }
//    }

//    protected void processInputEvent(MovementInputUpdateEvent event) {
//        Options settings = Minecraft.getInstance().options;
//        Input eInput = event.getInput().keyPresses;
//        var up = isKeyDown0(settings.keyUp);
//        var down = isKeyDown0(settings.keyDown);
//        var left = isKeyDown0(settings.keyLeft);
//        var right = isKeyDown0(settings.keyRight);
//
//        var jumping = isKeyDown0(settings.keyJump);
//        var shiftKeyDown = isKeyDown0(settings.keyShift);
//        var sprint = isKeyDown0(settings.keySprint);
//        event.getInput().keyPresses = new Input(up, down, left, right, jumping, shiftKeyDown, sprint);
//    }

//    private static boolean isKeyDown0(KeyMapping keybind) {
//        if (keybind.isUnbound()) {
//            return false;
//        }
//
//        return switch (keybind.getKey().getType()) {
//            case KEYSYM -> InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue());
//            case MOUSE -> GLFW.glfwGetMouseButton(Minecraft.getInstance().getWindow().getWindow(), keybind.getKey().getValue()) == GLFW.GLFW_PRESS;
//            default -> false;
//        };
//    }
}