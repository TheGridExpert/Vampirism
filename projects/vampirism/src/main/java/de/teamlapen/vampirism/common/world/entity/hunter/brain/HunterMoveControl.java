package de.teamlapen.vampirism.common.world.entity.hunter.brain;

import de.teamlapen.vampirism.common.world.entity.ai.navigation.HunterPathNavigation;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HunterMoveControl extends MoveControl {

    private static final double CLIMB_THRESHOLD = 0.5D;
    private static final double MIN_COLUMN_CLIMB = 0.1D;
    private static final double MAX_CLIMB_HORIZONTAL_DISTANCE_SQR = 1.5D * 1.5D;
    private static final double WALL_OFFSET = 0.5D;
    private static final double ENTRY_OFFSET = -0.1D;
    private static final double HOLD_STRENGTH = 0.3D;
    private static final double ENTRY_DISTANCE_SQR = 0.6D * 0.6D;
    private static final double EDGE_PROBE_DISTANCE = 0.6D;
    private static final double MAX_JUMP_HEIGHT = 1.5D;
    private static final int MAX_EDGE_BLOCKED_TICKS = 20;

    private final Hunter hunter;
    private int edgeBlockedTicks;

    public HunterMoveControl(Hunter hunter) {
        super(hunter);
        this.hunter = hunter;
    }

    @Override
    public void setWantedPosition(double x, double y, double z, double gait) {
        super.setWantedPosition(x, y, z, this.hunter.calculateSpeedModifier(gait));
    }

    @Override
    public void tick() {
        if (this.operation == Operation.MOVE_TO && (tickClimbing() || tickEnteringLadder() || dropUnreachableWaypoint())) return;
        super.tick();
        if (this.operation == Operation.JUMPING && isInLadder() && this.wantedY - this.hunter.getY() < CLIMB_THRESHOLD) {
            this.operation = Operation.WAIT;
            if (this.hunter.getJumpControl() instanceof HunterJumpControl jumpControl) jumpControl.cancel();
        }
        if (this.hunter.onGround() && !this.hunter.onClimbable() && !isSafeAhead()) {
            stopMoving();
            this.edgeBlockedTicks++;
            if (this.edgeBlockedTicks > MAX_EDGE_BLOCKED_TICKS) {
                this.edgeBlockedTicks = 0;
                this.hunter.getNavigation().stop();
            }
        } else {
            this.edgeBlockedTicks = 0;
        }
    }

    private boolean tickClimbing() {
        BlockPos feet = this.hunter.blockPosition();
        BlockState state = this.hunter.level().getBlockState(feet);
        if (!(state.getBlock() instanceof LadderBlock) || !this.hunter.onClimbable()) return false;

        double dy = this.wantedY - this.hunter.getY();
        double dx = this.wantedX - this.hunter.getX();
        double dz = this.wantedZ - this.hunter.getZ();
        boolean inColumn = Mth.floor(this.wantedX) == feet.getX() && Mth.floor(this.wantedZ) == feet.getZ();
        if (!inColumn && (dy < CLIMB_THRESHOLD || dx * dx + dz * dz > MAX_CLIMB_HORIZONTAL_DISTANCE_SQR)) return false;

        Direction wall = state.getValue(LadderBlock.FACING).getOpposite();
        boolean up = dy > MIN_COLUMN_CLIMB;
        faceAndHold(feet, wall, up ? WALL_OFFSET : ENTRY_OFFSET);
        if (up) this.hunter.getJumpControl().jump();
        return true;
    }

    private boolean tickEnteringLadder() {
        if (this.hunter.onClimbable() || this.wantedY >= this.hunter.getY()) return false;

        BlockPos wanted = BlockPos.containing(this.wantedX, this.wantedY, this.wantedZ);
        BlockState state = this.hunter.level().getBlockState(wanted);
        if (!(state.getBlock() instanceof LadderBlock)) return false;

        double dx = wanted.getX() + 0.5D - this.hunter.getX();
        double dz = wanted.getZ() + 0.5D - this.hunter.getZ();
        if (dx * dx + dz * dz > ENTRY_DISTANCE_SQR) return false;

        faceAndHold(wanted, state.getValue(LadderBlock.FACING).getOpposite(), ENTRY_OFFSET);
        return true;
    }

    private boolean dropUnreachableWaypoint() {
        if (!this.hunter.onGround() || this.hunter.onClimbable()) return false;

        double dx = this.wantedX - this.hunter.getX();
        double dz = this.wantedZ - this.hunter.getZ();
        if (this.wantedY - this.hunter.getY() <= MAX_JUMP_HEIGHT || dx * dx + dz * dz >= Math.max(1.0F, this.hunter.getBbWidth())) return false;
        if (this.hunter.level().getBlockState(BlockPos.containing(this.wantedX, this.wantedY, this.wantedZ)).getBlock() instanceof LadderBlock) return false;

        this.operation = Operation.WAIT;
        stopMoving();
        this.hunter.getNavigation().stop();
        return true;
    }

    private boolean isInLadder() {
        return this.hunter.level().getBlockState(this.hunter.blockPosition()).getBlock() instanceof LadderBlock;
    }

    private void faceAndHold(BlockPos column, Direction wall, double offset) {
        this.operation = Operation.WAIT;
        float yaw = wall.toYRot();
        this.hunter.setYRot(yaw);
        this.hunter.setYBodyRot(yaw);
        stopMoving();

        Vec3 anchor = Vec3.atBottomCenterOf(column).add(wall.getStepX() * offset, 0.0D, wall.getStepZ() * offset);
        Vec3 movement = this.hunter.getDeltaMovement();
        this.hunter.setDeltaMovement((anchor.x - this.hunter.getX()) * HOLD_STRENGTH, movement.y, (anchor.z - this.hunter.getZ()) * HOLD_STRENGTH);
    }

    private void stopMoving() {
        this.hunter.setSpeed(0.0F);
        this.hunter.setZza(0.0F);
        this.hunter.setXxa(0.0F);
    }

    private boolean isSafeAhead() {
        float forward = this.hunter.zza;
        float strafe = this.hunter.xxa;
        if (forward == 0.0F && strafe == 0.0F) return true;

        float yaw = this.hunter.getYRot() * Mth.DEG_TO_RAD;
        double sin = Mth.sin(yaw);
        double cos = Mth.cos(yaw);
        double dirX = strafe * cos - forward * sin;
        double dirZ = strafe * sin + forward * cos;
        double length = Math.sqrt(dirX * dirX + dirZ * dirZ);

        int x = Mth.floor(this.hunter.getX() + dirX / length * EDGE_PROBE_DISTANCE);
        int z = Mth.floor(this.hunter.getZ() + dirZ / length * EDGE_PROBE_DISTANCE);
        BlockPos feet = this.hunter.blockPosition();
        if (x == feet.getX() && z == feet.getZ()) return true;
        return hasSupport(this.hunter.level(), x, feet.getY(), z, getAllowedFall());
    }

    private int getAllowedFall() {
        boolean dropPath = this.hunter.getNavigation() instanceof HunterPathNavigation navigation && navigation.isFollowingDropPath();
        return dropPath ? this.hunter.getDropFallDistance() : this.hunter.getMaxFallDistance();
    }

    public static boolean hasSupport(Level level, int x, int feetY, int z, int maxFall) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int depth = 0; depth <= maxFall + 1; depth++) {
            pos.set(x, feetY - depth, z);
            BlockState state = level.getBlockState(pos);
            if (state.getBlock() instanceof LadderBlock || !state.getFluidState().isEmpty() || !state.getCollisionShape(level, pos).isEmpty()) return true;
        }
        return false;
    }
}
