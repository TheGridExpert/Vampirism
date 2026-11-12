package de.teamlapen.vampirism.common.world.entity.hunter;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class HunterMoveControl extends MoveControl {

    private static final double CLIMB_THRESHOLD = 0.5D;
    private static final double MIN_COLUMN_CLIMB = 0.1D;
    private static final double MAX_CLIMB_HORIZONTAL_DISTANCE_SQR = 1.5D * 1.5D;
    private static final double WALL_OFFSET = 0.5D;
    private static final double HOLD_STRENGTH = 0.3D;

    private final Hunter hunter;

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
        if (this.operation == Operation.MOVE_TO && tickClimbing()) return;
        super.tick();
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

        this.operation = Operation.WAIT;
        Direction wall = state.getValue(LadderBlock.FACING).getOpposite();
        float yaw = wall.toYRot();
        this.hunter.setYRot(yaw);
        this.hunter.setYBodyRot(yaw);
        this.hunter.setSpeed(0.0F);
        this.hunter.setZza(0.0F);
        this.hunter.setXxa(0.0F);
        holdOntoLadder(feet, wall);
        if (dy > MIN_COLUMN_CLIMB) this.hunter.getJumpControl().jump();
        return true;
    }

    private void holdOntoLadder(BlockPos feet, Direction wall) {
        Vec3 anchor = Vec3.atBottomCenterOf(feet).add(wall.getStepX() * WALL_OFFSET, 0.0D, wall.getStepZ() * WALL_OFFSET);
        Vec3 movement = this.hunter.getDeltaMovement();
        this.hunter.setDeltaMovement((anchor.x - this.hunter.getX()) * HOLD_STRENGTH, movement.y, (anchor.z - this.hunter.getZ()) * HOLD_STRENGTH);
    }
}
