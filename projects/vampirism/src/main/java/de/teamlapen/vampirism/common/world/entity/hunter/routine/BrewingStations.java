package de.teamlapen.vampirism.common.world.entity.hunter.routine;

import de.teamlapen.vampirism.REFERENCE;
import de.teamlapen.vampirism.common.core.ModBlocks;
import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.LookoutSpots;
import de.teamlapen.vampirism.common.world.entity.hunter.squad.HunterSquad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import org.jspecify.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber(modid = REFERENCE.MODID)
public class BrewingStations {

    private static final int SEARCH_RADIUS = 16;
    private static final int SEARCH_HEIGHT = 4;
    private static final int PLACE_RADIUS = 2;
    private static final double CLAIM_SEARCH_RADIUS = 32.0D;

    public static @Nullable BrewingStation find(ServerLevel level, Hunter hunter) {
        BlockPos origin = hunter.blockPosition();
        List<BlockPos> workstations = BlockPos.betweenClosedStream(origin.offset(-SEARCH_RADIUS, -SEARCH_HEIGHT, -SEARCH_RADIUS), origin.offset(SEARCH_RADIUS, SEARCH_HEIGHT, SEARCH_RADIUS))
                .filter(pos -> isWorkstation(level.getBlockState(pos)))
                .map(BlockPos::immutable)
                .sorted(Comparator.comparingDouble(pos -> pos.distSqr(origin)))
                .toList();
        for (BlockPos pos : workstations) {
            if (!isClaimed(level, hunter, pos) && canReach(hunter, pos)) return new BrewingStation(pos, false, false);
        }

        return findPlacementSpot(level, hunter).map(pos -> new BrewingStation(pos, true, false)).orElse(null);
    }

    private static Optional<BlockPos> findPlacementSpot(ServerLevel level, Hunter hunter) {
        BlockPos origin = hunter.blockPosition();
        return BlockPos.betweenClosedStream(origin.offset(-PLACE_RADIUS, -1, -PLACE_RADIUS), origin.offset(PLACE_RADIUS, 1, PLACE_RADIUS))
                .filter(pos -> !pos.equals(origin) && canPlaceAt(level, pos) && hasStandingRoom(level, pos))
                .map(BlockPos::immutable)
                .filter(pos -> !isClaimed(level, hunter, pos))
                .min(Comparator.comparingDouble(pos -> pos.distSqr(origin)));
    }

    public static boolean canPlaceAt(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(pos).isAir()
                && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)
                && level.isUnobstructed(Blocks.BREWING_STAND.defaultBlockState(), pos, CollisionContext.empty());
    }

    private static boolean hasStandingRoom(ServerLevel level, BlockPos pos) {
        return Direction.Plane.HORIZONTAL.stream().anyMatch(direction -> LookoutSpots.canStandAt(level, pos.relative(direction)));
    }

    public static boolean isWorkstation(BlockState state) {
        return state.is(Blocks.BREWING_STAND) || state.is(ModBlocks.VAPOR_STILL.get());
    }

    public static boolean isRelaxed(ServerLevel level, Hunter hunter) {
        HunterSquad squad = hunter.getSquad();
        if (squad != null && (squad.isOnGuard(level.getGameTime()) || squad.isRetreating())) return false;
        return !hunter.isFighting() && !hunter.isRetreating()
                && hunter.getBrain().getMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()).map(List::isEmpty).orElse(true);
    }

    public static void placeStand(ServerLevel level, Hunter hunter, BlockPos pos) {
        BlockState stand = Blocks.BREWING_STAND.defaultBlockState();
        level.setBlockAndUpdate(pos, stand);
        hunter.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, stand.getSoundType().getPlaceSound(), SoundSource.NEUTRAL, 1.0F, 0.8F);
    }

    public static void takeBack(ServerLevel level, Hunter hunter, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.BREWING_STAND)) return;
        level.removeBlock(pos, false);
        Vec3 center = Vec3.atCenterOf(pos);
        hunter.getLookControl().setLookAt(center.x, center.y, center.z);
        hunter.swing(InteractionHand.MAIN_HAND);
        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.NEUTRAL, 0.2F, (hunter.getRandom().nextFloat() - hunter.getRandom().nextFloat()) * 1.4F + 2.0F);
    }

    public static void release(ServerLevel level, Hunter hunter) {
        hunter.getBrain().getMemory(ModMemoryTypes.BREWING_STATION.get())
                .filter(BrewingStation::placed)
                .filter(station -> level.getBlockState(station.pos()).is(Blocks.BREWING_STAND))
                .ifPresent(station -> level.removeBlock(station.pos(), false));
    }

    private static boolean isClaimed(ServerLevel level, Hunter hunter, BlockPos pos) {
        return !level.getEntitiesOfClass(Hunter.class, new AABB(pos).inflate(CLAIM_SEARCH_RADIUS), other -> other != hunter && other.getBrain().getMemory(ModMemoryTypes.BREWING_STATION.get())
                .map(station -> station.pos().equals(pos))
                .orElse(false)).isEmpty();
    }

    private static @Nullable Hunter findOwner(ServerLevel level, BlockPos pos) {
        return level.getEntitiesOfClass(Hunter.class, new AABB(pos).inflate(CLAIM_SEARCH_RADIUS), hunter -> hunter.isAlive() && hunter.getBrain().getMemory(ModMemoryTypes.BREWING_STATION.get())
                .map(station -> station.placed() && station.pos().equals(pos))
                .orElse(false)).stream().findFirst().orElse(null);
    }

    private static boolean canReach(Hunter hunter, BlockPos pos) {
        Path path = hunter.getNavigation().createPath(pos, 1);
        return path != null && path.canReach();
    }

    private static boolean protect(ServerLevel level, BlockPos pos) {
        if (!level.getBlockState(pos).is(Blocks.BREWING_STAND)) return false;
        Hunter owner = findOwner(level, pos);
        if (owner == null) return false;
        takeBack(level, owner, pos);
        return true;
    }

    @SubscribeEvent
    public static void onBreak(BreakBlockEvent event) {
        if (event.getLevel() instanceof ServerLevel level && protect(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onStartBreaking(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getAction() != PlayerInteractEvent.LeftClickBlock.Action.START) return;
        if (event.getLevel() instanceof ServerLevel level && protect(level, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (event.getLevel() instanceof ServerLevel level) {
            event.getAffectedBlocks().removeIf(pos -> protect(level, pos));
        }
    }
}
