package de.teamlapen.vampirism.common.world.entity.hunter.squad;

import de.teamlapen.vampirism.common.core.ModMemoryTypes;
import de.teamlapen.vampirism.common.world.entity.hunter.Hunter;
import de.teamlapen.vampirism.common.world.entity.hunter.HunterThreats;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public final class SquadContext {

    public static final double ENGAGE_RADIUS = 24.0D;
    private static final int WOUNDED_RETREAT_TICKS = 400;

    private final ServerLevel level;
    private final HunterSquad squad;
    private final List<Hunter> members;
    private final List<LivingEntity> threats;
    private final Vec3 squadCenter;
    private final @Nullable Vec3 threatCenter;
    private final Vec3 front;
    private final Map<Hunter, SquadOrder> orders = new HashMap<>();
    private final Map<LivingEntity, Hunter> victims = new HashMap<>();

    private SquadContext(ServerLevel level, HunterSquad squad) {
        this.level = level;
        this.squad = squad;
        this.members = List.copyOf(squad.members());
        this.squadCenter = average(this.members);
        this.threats = collectThreats();
        this.threatCenter = this.threats.isEmpty() ? null : average(this.threats);
        this.front = computeFront();
        for (LivingEntity threat : this.threats) {
            if (threat instanceof Mob mob && mob.getTarget() instanceof Hunter victim && this.members.contains(victim)) {
                this.victims.put(threat, victim);
            }
        }
    }

    public @Nullable Hunter victimOf(LivingEntity threat) {
        return this.victims.get(threat);
    }

    public static SquadContext create(ServerLevel level, HunterSquad squad) {
        return new SquadContext(level, squad);
    }

    private List<LivingEntity> collectThreats() {
        Set<LivingEntity> found = new LinkedHashSet<>();
        for (Hunter member : this.members) {
            Brain<Hunter> brain = member.getBrain();
            brain.getMemory(ModMemoryTypes.NEAREST_VISIBLE_HOSTILES.get()).ifPresent(found::addAll);
            brain.getMemory(MemoryModuleType.ATTACK_TARGET).ifPresent(found::add);
        }

        List<LivingEntity> result = new ArrayList<>();
        for (LivingEntity threat : found) {
            if (threat instanceof Hunter || !threat.isAlive() || threat.level() != this.level) continue;
            if (HunterThreats.isAvoided(threat)) continue;
            if (threat.position().distanceTo(this.squadCenter) > ENGAGE_RADIUS) continue;
            result.add(threat);
        }
        return result;
    }

    private Vec3 computeFront() {
        Vec3 direction = this.threatCenter != null ? this.threatCenter.subtract(this.squadCenter) : this.squad.heading();
        Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
        if (horizontal.lengthSqr() < 1.0E-4) {
            Vec3 look = this.squad.leader() != null ? this.squad.leader().getLookAngle() : new Vec3(1, 0, 0);
            horizontal = new Vec3(look.x, 0, look.z);
        }
        return horizontal.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : horizontal.normalize();
    }

    public ServerLevel level() {
        return this.level;
    }

    public HunterSquad squad() {
        return this.squad;
    }

    public List<Hunter> members() {
        return this.members;
    }

    public List<LivingEntity> threats() {
        return this.threats;
    }

    public boolean hasThreats() {
        return !this.threats.isEmpty();
    }

    public Vec3 squadCenter() {
        return this.squadCenter;
    }

    public Vec3 threatCenter() {
        return Objects.requireNonNull(this.threatCenter, "No threats present");
    }

    public Vec3 front() {
        return this.front;
    }

    public Vec3 side() {
        return new Vec3(-this.front.z, 0, this.front.x);
    }

    public boolean isFree(Hunter hunter) {
        return !this.orders.containsKey(hunter);
    }

    public List<Hunter> free(Predicate<Hunter> filter) {
        List<Hunter> result = new ArrayList<>();
        for (Hunter member : this.members) {
            if (isFree(member) && filter.test(member)) result.add(member);
        }
        return result;
    }

    public SquadOrder claim(Hunter hunter) {
        return this.orders.computeIfAbsent(hunter, member -> new SquadOrder());
    }

    public List<Hunter> rangedMembers() {
        return this.members.stream().filter(Hunter::isRangedClass).toList();
    }

    public Vec3 rangedCenter() {
        List<Hunter> ranged = rangedMembers();
        return ranged.isEmpty() ? this.squadCenter : average(ranged);
    }

    public Vec3 ground(Vec3 pos, double referenceY) {
        int x = Mth.floor(pos.x);
        int z = Mth.floor(pos.z);
        int surface = this.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        double y = Math.abs(surface - referenceY) < 6 ? surface : referenceY;
        return new Vec3(x + 0.5, y, z + 0.5);
    }

    public boolean hasClearShot(Hunter shooter, Vec3 from, LivingEntity target) {
        Vec3 eye = from.add(0, shooter.getEyeHeight(), 0);
        return this.level.clip(new ClipContext(eye, target.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }

    public @Nullable LivingEntity nearestThreat(Vec3 pos, Predicate<LivingEntity> filter) {
        LivingEntity best = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity threat : this.threats) {
            if (!filter.test(threat)) continue;
            double distance = threat.position().distanceToSqr(pos);
            if (distance < bestDistance) {
                bestDistance = distance;
                best = threat;
            }
        }
        return best;
    }

    public void apply(int ttl) {
        for (Hunter member : this.members) {
            Brain<Hunter> brain = member.getBrain();
            SquadOrder order = this.orders.get(member);

            if (order != null && order.getTarget() != null) {
                brain.setMemoryWithExpiry(ModMemoryTypes.SQUAD_TARGET.get(), order.getTarget(), ttl);
            } else {
                brain.eraseMemory(ModMemoryTypes.SQUAD_TARGET.get());
            }

            if (order != null && order.getPosition() != null) {
                brain.setMemoryWithExpiry(ModMemoryTypes.SQUAD_POSITION.get(), order.getPosition(), ttl);
            } else {
                brain.eraseMemory(ModMemoryTypes.SQUAD_POSITION.get());
            }

            if (order != null && order.isGroupRetreat()) {
                brain.setMemoryWithExpiry(ModMemoryTypes.SQUAD_RETREAT.get(), Unit.INSTANCE, ttl);
                startRetreat(brain, ttl * 2L);
            } else if (order != null && order.isRetreat()) {
                startRetreat(brain, WOUNDED_RETREAT_TICKS);
            }
        }
    }

    private static void startRetreat(Brain<Hunter> brain, long duration) {
        if (brain.hasMemoryValue(ModMemoryTypes.SHOULD_RETREAT.get())) {
            if (brain.getTimeUntilExpiry(ModMemoryTypes.SHOULD_RETREAT.get()) >= duration) return;
        } else {
            brain.eraseMemory(MemoryModuleType.ATTACK_TARGET);
        }
        brain.setMemoryWithExpiry(ModMemoryTypes.SHOULD_RETREAT.get(), Unit.INSTANCE, duration);
    }

    private static Vec3 average(Collection<? extends LivingEntity> entities) {
        double x = 0, y = 0, z = 0;
        for (LivingEntity entity : entities) {
            x += entity.getX();
            y += entity.getY();
            z += entity.getZ();
        }
        int count = Math.max(1, entities.size());
        return new Vec3(x / count, y / count, z / count);
    }
}
