package de.teamlapen.vampirism.entity.ai.behaviour;

import com.google.common.collect.Sets;
import de.teamlapen.vampirism.core.ModMemoryModuleTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nullable;
import java.util.*;

public class InteractWithGate {

    private static final int COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE = 20;
    private static final double SKIP_CLOSING_GATE_IF_FURTHER_AWAY_THAN = 3.0;
    private static final double MAX_DISTANCE_TO_HOLD_GATE_OPEN_FOR_OTHER_MOBS = 2.0;

    public static BehaviorControl<LivingEntity> create() {
        MutableObject<Node> lastNode = new MutableObject<>(null);
        MutableInt cooldown = new MutableInt(0);

        return BehaviorBuilder.create(instance ->
                instance.group(
                        instance.present(MemoryModuleType.PATH),
                        instance.registered(ModMemoryModuleTypes.GATES_TO_CLOSE.get()),
                        instance.registered(MemoryModuleType.NEAREST_LIVING_ENTITIES)
                ).apply(
                        instance,
                        (pathMem, gatesToCloseMem, nearestEntitiesMem) -> (level, entity, gameTime) -> {
                            Path path = instance.get(pathMem);
                            Optional<Set<GlobalPos>> rememberedGatesOpt = instance.tryGet(gatesToCloseMem);
                            
                            if (!path.notStarted() && !path.isDone()) {
                                if (Objects.equals(lastNode.getValue(), path.getNextNode())) {
                                    cooldown.setValue(COOLDOWN_BEFORE_RERUNNING_IN_SAME_NODE);
                                } else if (cooldown.decrementAndGet() > 0) {
                                    return false;
                                }

                                lastNode.setValue(path.getNextNode());

                                Node previousNode = path.getPreviousNode();
                                Node nextNode = path.getNextNode();

                                if (previousNode != null) {
                                    BlockPos previousGatePos = previousNode.asBlockPos();
                                    BlockState previousGateState = level.getBlockState(previousGatePos);
                                    if (previousGateState.getBlock() instanceof FenceGateBlock gateBlock) {
                                        if (!isGateOpen(previousGateState)) {
                                            setOpenGate(level, entity, previousGatePos, previousGateState, gateBlock, true);
                                        }

                                        rememberedGatesOpt = rememberGateToClose(gatesToCloseMem, rememberedGatesOpt, level, previousGatePos);
                                    }
                                }

                                BlockPos nextGatePos = nextNode.asBlockPos();
                                BlockState nextGateState = level.getBlockState(nextGatePos);
                                if (nextGateState.getBlock() instanceof FenceGateBlock gateBlock) {
                                    if (!isGateOpen(nextGateState)) {
                                        setOpenGate(level, entity, nextGatePos, nextGateState, gateBlock, true);

                                        rememberedGatesOpt = rememberGateToClose(gatesToCloseMem, rememberedGatesOpt, level, nextGatePos);
                                    }
                                }

                                rememberedGatesOpt.ifPresent(rememberedGates ->
                                        closeGatesThatIHaveOpenedOrPassedThrough(level, entity, previousNode, nextNode, rememberedGates, instance.tryGet(nearestEntitiesMem))
                                );
                                return true;
                            } else {
                                return false;
                            }
                        })
        );
    }

    public static boolean isGateOpen(BlockState state) {
        return state.getValue(FenceGateBlock.OPEN);
    }

    public static void setOpenGate(ServerLevel level, LivingEntity entity, BlockPos pos, BlockState state, FenceGateBlock gateBlock, boolean open) {
        level.setBlock(pos, state.setValue(FenceGateBlock.OPEN, open), 10);
        level.playSound(entity, pos, open ? gateBlock.openSound : gateBlock.closeSound, SoundSource.BLOCKS, 1.0F, level.getRandom().nextFloat() * 0.1F + 0.9F);
        level.gameEvent(entity, open ? GameEvent.BLOCK_OPEN : GameEvent.BLOCK_CLOSE, pos);
    }

    public static void closeGatesThatIHaveOpenedOrPassedThrough(ServerLevel level, LivingEntity entity, @Nullable Node previousNode, @Nullable Node nextNode, Set<GlobalPos> doorPositions, Optional<List<LivingEntity>> nearestLivingEntities) {
        Iterator<GlobalPos> iterator = doorPositions.iterator();

        while (iterator.hasNext()) {
            GlobalPos globalpos = iterator.next();
            BlockPos pos = globalpos.pos();

            if ((previousNode == null || !previousNode.asBlockPos().equals(pos)) && (nextNode == null || !nextNode.asBlockPos().equals(pos))) {
                if (isGateTooFarAway(level, entity, globalpos)) {
                    iterator.remove();
                } else {
                    BlockState state = level.getBlockState(pos);

                    if (!(state.getBlock() instanceof FenceGateBlock gateBlock)) {
                        iterator.remove();
                    } else if (!state.getValue(FenceGateBlock.OPEN)) {
                        iterator.remove();
                    } else if (areOtherMobsComingThroughGate(entity, pos, nearestLivingEntities)) {
                        iterator.remove();
                    } else {
                        setOpenGate(level, entity, pos, state, gateBlock, false);
                        iterator.remove();
                    }
                }
            }
        }
    }

    private static boolean areOtherMobsComingThroughGate(LivingEntity entity, BlockPos pos, Optional<List<LivingEntity>> nearbyEntities) {
        return nearbyEntities.isPresent() && nearbyEntities.get().stream()
                .filter(e -> e.getType() == entity.getType())
                .filter(e -> pos.closerToCenterThan(e.position(), MAX_DISTANCE_TO_HOLD_GATE_OPEN_FOR_OTHER_MOBS))
                .anyMatch(e -> isMobComingThroughGate(e.getBrain(), pos));
    }

    private static boolean isMobComingThroughGate(Brain<?> brain, BlockPos pos) {
        if (!brain.hasMemoryValue(MemoryModuleType.PATH)) return false;

        Path path = brain.getMemory(MemoryModuleType.PATH).get();
        if (path.isDone()) return false;

        Node previousNode = path.getPreviousNode();
        Node nextNode = path.getNextNode();

        return previousNode != null && pos.equals(previousNode.asBlockPos()) || pos.equals(nextNode.asBlockPos());
    }

    private static boolean isGateTooFarAway(ServerLevel level, LivingEntity entity, GlobalPos globalPos) {
        return globalPos.dimension() != level.dimension() || !globalPos.pos().closerToCenterThan(entity.position(), SKIP_CLOSING_GATE_IF_FURTHER_AWAY_THAN);
    }

    private static Optional<Set<GlobalPos>> rememberGateToClose(MemoryAccessor<?, Set<GlobalPos>> doorsToClose, Optional<Set<GlobalPos>> rememberedGatesOpt, ServerLevel level, BlockPos pos) {
        GlobalPos globalpos = GlobalPos.of(level.dimension(), pos);
        return Optional.of(rememberedGatesOpt.map(set -> {
            set.add(globalpos);
            return set;
        }).orElseGet(() -> {
            Set<GlobalPos> set = Sets.newHashSet(globalpos);
            doorsToClose.set(set);
            return set;
        }));
    }
}
