package de.teamlapen.vampirism.entity.ai.goals;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.util.GoalUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;

public abstract class GateInteractGoal extends Goal {
    protected Mob mob;
    protected BlockPos gatePos = BlockPos.ZERO;
    protected boolean hasGate;
    private boolean passed;
    private float gateOpenDirX;
    private float gateOpenDirZ;

    public GateInteractGoal(Mob mob) {
        this.mob = mob;
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            throw new IllegalArgumentException("Unsupported mob type for GateInteractGoal");
        }
    }

    protected void setOpen(boolean open) {
        if (hasGate) {
            Level level = mob.level();
            BlockState blockstate = mob.level().getBlockState(gatePos);
            if (blockstate.getBlock() instanceof FenceGateBlock gateBlock) {
                level.setBlock(gatePos, blockstate.setValue(FenceGateBlock.OPEN, open), 10);
                level.playSound(mob, gatePos, open ? gateBlock.openSound : gateBlock.closeSound, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
                level.gameEvent(mob, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, gatePos);
            }
        }
    }

    /**
     * Copied from FenceGateBlock#useWithoutItem.
     */
    public static void setGateOpen(Entity entity, Level level, BlockState blockState, BlockPos pos, boolean open) {
        if (blockState.getBlock() instanceof FenceGateBlock gateBlock && blockState.getValue(FenceGateBlock.OPEN) != open) {
            boolean isCurrentlyOpened = blockState.getValue(FenceGateBlock.OPEN);

            Direction direction = entity.getDirection();
            if (!isCurrentlyOpened && blockState.getValue(FenceGateBlock.FACING) == direction.getOpposite()) {
                blockState = blockState.setValue(FenceGateBlock.FACING, direction);
            }
            blockState.setValue(FenceGateBlock.OPEN, open);

            level.setBlock(pos, blockState, 10);

            level.playSound(entity, pos, isCurrentlyOpened ? gateBlock.openSound : gateBlock.closeSound, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
            level.gameEvent(entity, isCurrentlyOpened ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
        }
    }

    /**
     * Vanilla fence gates can only be wooden, so there's no alternative of DoorBlock#isWoodenDoor. It might work improperly in case another mod adds metal gates.
     */
    @Override
    public boolean canUse() {
        if (!GoalUtils.hasGroundPathNavigation(mob)) {
            return false;
        } else {
            GroundPathNavigation groundpathnavigation = (GroundPathNavigation) mob.getNavigation();
            Path path = groundpathnavigation.getPath();
            if (path != null && !path.isDone() && groundpathnavigation.canOpenDoors()) {
                for (int i = 0; i < Math.min(path.getNextNodeIndex() + 2, path.getNodeCount()); i++) {
                    Node node = path.getNode(i);
                    // This line is changed because of the different shape of the gate compared to a door, so the mob may sometimes not see the hit box of the gate.
                    gatePos = new BlockPos(node.x + mob.getRandom().nextInt(4) - 2, node.y, node.z + mob.getRandom().nextInt(4) - 2);
                    if (!(mob.distanceToSqr(gatePos.getX(), mob.getY(), gatePos.getZ()) > 2.25)) {
                        hasGate = mob.level().getBlockState(gatePos).getBlock() instanceof FenceGateBlock;
                        return hasGate;
                    }
                }

                return hasGate;
            } else {
                return false;
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        return !passed;
    }

    @Override
    public void start() {
        passed = false;
        gateOpenDirX = (float) ((double) gatePos.getX() + 0.5 - mob.getX());
        gateOpenDirZ = (float) ((double) gatePos.getZ() + 0.5 - mob.getZ());
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        float f = (float) ((double) gatePos.getX() + 0.5 - mob.getX());
        float f1 = (float) ((double) gatePos.getZ() + 0.5 - mob.getZ());
        float f2 = gateOpenDirX * f + gateOpenDirZ * f1;
        if (f2 < 0.0F) {
            passed = true;
        }
    }
}
