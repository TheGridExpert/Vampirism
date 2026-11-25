package de.teamlapen.vampirism.common.world.structures.huntercamp;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.common.config.ModConfig;
import de.teamlapen.vampirism.common.core.ModStructures;
import de.teamlapen.vampirism.common.tags.ModBiomeTags;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class HunterCampStructure extends Structure {

    public static final MapCodec<HunterCampStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            StructureTemplatePool.CODEC.fieldOf("campfire_pool").forGetter(structure -> structure.campfirePool),
            StructureTemplatePool.CODEC.fieldOf("flagpole_pool").forGetter(structure -> structure.flagpolePool),
            StructureTemplatePool.CODEC.fieldOf("tent_pool").forGetter(structure -> structure.tentPool),
            StructureTemplatePool.CODEC.fieldOf("tower_pool").forGetter(structure -> structure.towerPool),
            StructureTemplatePool.CODEC.fieldOf("cold_tower_pool").forGetter(structure -> structure.coldTowerPool),
            RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("cold_biomes").forGetter(structure -> structure.coldBiomes),
            IntProviders.codec(1, 6).fieldOf("tents").forGetter(structure -> structure.tents),
            IntProviders.codec(1, 6).fieldOf("towers").forGetter(structure -> structure.towers),
            IntProviders.codec(1, 16).fieldOf("core_offset").forGetter(structure -> structure.coreOffset),
            InclusiveRange.codec(Codec.INT, 4, 48).fieldOf("tent_distance").forGetter(structure -> structure.tentDistance),
            InclusiveRange.codec(Codec.INT, 4, 48).fieldOf("tower_distance").forGetter(structure -> structure.towerDistance),
            Codec.intRange(0, 4).fieldOf("search_radius").forGetter(structure -> structure.searchRadius),
            Codec.intRange(0, 16).fieldOf("max_height_difference").forGetter(structure -> structure.maxHeightDifference),
            Codec.intRange(0, 48).fieldOf("tree_free_radius").forGetter(structure -> structure.treeFreeRadius),
            IntProviders.codec(1, 8).fieldOf("jigsaw_depth").forGetter(structure -> structure.jigsawDepth)
    ).apply(instance, HunterCampStructure::new));

    private static final int COARSE_STEP = 8;
    private static final int FINE_STEP = 4;
    private static final int REFINED_SITES = 4;
    private static final int CORE_RADIUS = 12;
    private static final double CORE_WEIGHT = 2.0;
    private static final double WATER_PENALTY = 16.0;
    private static final int RADIUS_STEP = 4;
    private static final int ARC_STEP = 4;
    private static final double SECTOR_COVERAGE = 0.6;
    private static final int TOWER_ROTATIONS = 3;
    private static final double OFF_CENTER_PENALTY = 2.0;
    private static final int PIECE_PADDING = 3;
    private static final int BUILDING_TREE_MARGIN = 3;
    private static final double TOWER_HEIGHT_WEIGHT = 2.0;
    private static final int TOWER_EYE_HEIGHT = 7;
    private static final int[] TOWER_VIEW_DISTANCES = {6, 10, 14};
    private static final Direction TEMPLATE_FRONT = Direction.WEST;
    private static final double FULL_TURN = Math.PI * 2.0;
    private static final long WIND_SALT = 0x77696e64L;

    private final Holder<StructureTemplatePool> campfirePool;
    private final Holder<StructureTemplatePool> flagpolePool;
    private final Holder<StructureTemplatePool> tentPool;
    private final Holder<StructureTemplatePool> towerPool;
    private final Holder<StructureTemplatePool> coldTowerPool;
    private final HolderSet<Biome> coldBiomes;
    private final IntProvider tents;
    private final IntProvider towers;
    private final IntProvider coreOffset;
    private final InclusiveRange<Integer> tentDistance;
    private final InclusiveRange<Integer> towerDistance;
    private final int searchRadius;
    private final int maxHeightDifference;
    private final int treeFreeRadius;
    private final IntProvider jigsawDepth;

    public HunterCampStructure(StructureSettings settings, Holder<StructureTemplatePool> campfirePool, Holder<StructureTemplatePool> flagpolePool, Holder<StructureTemplatePool> tentPool, Holder<StructureTemplatePool> towerPool, Holder<StructureTemplatePool> coldTowerPool, HolderSet<Biome> coldBiomes, IntProvider tents, IntProvider towers, IntProvider coreOffset, InclusiveRange<Integer> tentDistance, InclusiveRange<Integer> towerDistance, int searchRadius, int maxHeightDifference, int treeFreeRadius, IntProvider jigsawDepth) {
        super(settings);
        this.campfirePool = campfirePool;
        this.flagpolePool = flagpolePool;
        this.tentPool = tentPool;
        this.towerPool = towerPool;
        this.coldTowerPool = coldTowerPool;
        this.coldBiomes = coldBiomes;
        this.tents = tents;
        this.towers = towers;
        this.coreOffset = coreOffset;
        this.tentDistance = tentDistance;
        this.towerDistance = towerDistance;
        this.searchRadius = searchRadius;
        this.maxHeightDifference = maxHeightDifference;
        this.treeFreeRadius = treeFreeRadius;
        this.jigsawDepth = jigsawDepth;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        if (!ModConfig.common().generateHunterCamps.get()) return Optional.empty();

        Terrain terrain = new Terrain(context);
        ChunkPos origin = context.chunkPos();
        List<Site> sites = new ArrayList<>();

        for (int dx = -this.searchRadius; dx <= this.searchRadius; dx++) {
            for (int dz = -this.searchRadius; dz <= this.searchRadius; dz++) {
                int x = origin.getMiddleBlockX() + dx * 16;
                int z = origin.getMiddleBlockZ() + dz * 16;
                if (!isValidBiome(context, terrain, x, z)) continue;

                int distance = dx * dx + dz * dz;
                calculateScore(terrain, x, z, COARSE_STEP).ifPresent(score -> sites.add(new Site(x, z, score, distance)));
            }
        }

        return sites.stream()
                .sorted(Site.ORDER)
                .limit(REFINED_SITES)
                .flatMap(site -> calculateScore(terrain, site.x(), site.z(), FINE_STEP).map(score -> new Site(site.x(), site.z(), score, site.distance())).stream())
                .sorted(Site.ORDER)
                .map(site -> layout(context, terrain, site.x(), site.z()))
                .flatMap(Optional::stream)
                .findFirst();
    }

    private static boolean isValidBiome(GenerationContext context, Terrain terrain, int x, int z) {
        int height = terrain.height(x, z);
        if (height == SurfaceSampler.WATER) return false;

        Holder<Biome> biome = getBiome(context, x, height, z);
        return context.validBiome().test(biome) && !biome.is(ModBiomeTags.HasFaction.IS_FACTION_BIOME);
    }

    private static Holder<Biome> getBiome(GenerationContext context, int x, int y, int z) {
        return context.chunkGenerator().getBiomeSource().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z), context.randomState().sampler());
    }

    private Optional<Double> calculateScore(Terrain terrain, int centerX, int centerZ, int coreStep) {
        int coreMinX = floorToStep(centerX - CORE_RADIUS, coreStep);
        int coreMaxX = ceilToStep(centerX + CORE_RADIUS, coreStep);
        int coreMinZ = floorToStep(centerZ - CORE_RADIUS, coreStep);
        int coreMaxZ = ceilToStep(centerZ + CORE_RADIUS, coreStep);

        int lowest = Integer.MAX_VALUE;
        int highest = Integer.MIN_VALUE;
        long sum = 0;
        int count = 0;

        for (int x = coreMinX; x <= coreMaxX; x += coreStep) {
            for (int z = coreMinZ; z <= coreMaxZ; z += coreStep) {
                int height = terrain.height(x, z);
                if (height == SurfaceSampler.WATER || terrain.isHollow(x, height, z)) return Optional.empty();

                lowest = Math.min(lowest, height);
                highest = Math.max(highest, height);
                sum += height;
                count++;
            }
        }

        int range = highest - lowest;
        if (range > this.maxHeightDifference) return Optional.empty();

        double base = (double) sum / count;
        double deviation = 0.0;
        int samples = 0;
        int reach = this.towerDistance.maxInclusive() + CORE_RADIUS / 2;

        for (int x = floorToStep(centerX - reach, COARSE_STEP); x <= ceilToStep(centerX + reach, COARSE_STEP); x += COARSE_STEP) {
            for (int z = floorToStep(centerZ - reach, COARSE_STEP); z <= ceilToStep(centerZ + reach, COARSE_STEP); z += COARSE_STEP) {
                if (x >= coreMinX && x <= coreMaxX && z >= coreMinZ && z <= coreMaxZ) continue;

                int height = terrain.height(x, z);
                deviation += height == SurfaceSampler.WATER ? WATER_PENALTY : Math.abs(height - base);
                samples++;
            }
        }

        return Optional.of(range * CORE_WEIGHT + (samples == 0 ? 0.0 : deviation / samples));
    }

    private Optional<GenerationStub> layout(GenerationContext context, Terrain terrain, int centerX, int centerZ) {
        RandomSource random = context.random();
        StructureTemplateManager manager = context.structureTemplateManager();
        int centerHeight = terrain.height(centerX, centerZ);
        List<Candidate> placed = new ArrayList<>();
        List<Double> angles = new ArrayList<>();

        int offset = this.coreOffset.sample(random);
        Direction axis = chooseAxis(terrain, centerX, centerZ, offset, random);
        for (Direction side : List.of(axis, axis.getOpposite())) {
            StructurePoolElement element = (side == axis ? this.campfirePool : this.flagpolePool).value().getRandomTemplate(random);
            int x = centerX + side.getStepX() * offset;
            int z = centerZ + side.getStepZ() * offset;
            double angle = Math.atan2(side.getStepZ(), side.getStepX());
            placed.add(createCandidate(manager, terrain, element, x, z, side.getOpposite(), angle));
            angles.add(angle);
        }

        Holder<StructureTemplatePool> towerPool = this.coldBiomes.contains(getBiome(context, centerX, centerHeight, centerZ)) ? this.coldTowerPool : this.towerPool;
        List<StructurePoolElement> towerElements = new ArrayList<>();
        int towerCount = this.towers.sample(random);
        for (int i = 0; i < towerCount; i++) {
            towerElements.add(towerPool.value().getRandomTemplate(random));
        }

        List<Candidate> towers = findTowers(manager, terrain, towerElements, centerX, centerZ, centerHeight, random.nextDouble() * FULL_TURN, placed);
        if (towers.isEmpty()) return Optional.empty();
        placed.addAll(towers);
        towers.forEach(tower -> angles.add(tower.angle()));

        int tentCount = this.tents.sample(random);
        int placedTents = 0;
        for (int i = 0; i < tentCount; i++) {
            StructurePoolElement element = this.tentPool.value().getRandomTemplate(random);
            Gap gap = largestGap(angles);
            Optional<Scored> tent = searchSector(manager, terrain, element, centerX, centerZ, gap.middle(), gap.width() * SECTOR_COVERAGE / 2.0, this.tentDistance, placed,
                    (candidate, deviation) -> -candidate.footprint().range() * 2.0 - Math.abs(candidate.footprint().median() - centerHeight) - Math.abs(deviation) * OFF_CENTER_PENALTY);
            angles.add(tent.map(scored -> scored.candidate().angle()).orElse(gap.middle()));
            if (tent.isEmpty()) continue;

            placed.add(tent.get().candidate());
            placedTents++;
        }

        if (placedTents == 0) return Optional.empty();

        BlockPos center = new BlockPos(centerX, centerHeight, centerZ);
        int jigsawDepth = this.jigsawDepth.sample(random);
        Rotation wind = Rotation.values()[RandomSource.create(context.seed() ^ WIND_SALT).nextInt(Rotation.values().length)];
        List<StructurePiece> pieces = new ArrayList<>(placed.stream().flatMap(candidate -> candidate.toPieces(context, jigsawDepth, wind).stream()).toList());
        if (this.treeFreeRadius > 0) {
            pieces.add(new HunterCampClearing(center, this.treeFreeRadius));
        }

        return Optional.of(new GenerationStub(center, builder -> pieces.forEach(builder::addPiece)));
    }

    public static boolean isTreeFree(WorldGenLevel level, BlockPos pos) {
        if (!(level instanceof WorldGenRegion region)) return false;

        Structure camp = region.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ModStructures.HUNTER_CAMP);
        if (camp == null) return false;

        StructureManager structureManager = region.getLevel().structureManager().forWorldGenRegion(region);
        int x = pos.getX();
        int z = pos.getZ();
        for (int chunkX = SectionPos.blockToSectionCoord(x - BUILDING_TREE_MARGIN); chunkX <= SectionPos.blockToSectionCoord(x + BUILDING_TREE_MARGIN); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(z - BUILDING_TREE_MARGIN); chunkZ <= SectionPos.blockToSectionCoord(z + BUILDING_TREE_MARGIN); chunkZ++) {
                for (StructureStart start : structureManager.startsForStructure(SectionPos.of(chunkX, 0, chunkZ), camp)) {
                    for (StructurePiece piece : start.getPieces()) {
                        if (piece instanceof HunterCampClearing clearing && clearing.blocksTree(pos)) return true;
                        if (piece instanceof HunterCampPiece && piece.getBoundingBox().intersects(x - BUILDING_TREE_MARGIN, z - BUILDING_TREE_MARGIN, x + BUILDING_TREE_MARGIN, z + BUILDING_TREE_MARGIN)) return true;
                    }
                }
            }
        }

        return false;
    }

    private Direction chooseAxis(Terrain terrain, int centerX, int centerZ, int offset, RandomSource random) {
        int centerHeight = terrain.height(centerX, centerZ);
        Direction best = Direction.NORTH;
        int bestDifference = Integer.MAX_VALUE;

        for (Direction direction : Direction.Plane.HORIZONTAL.shuffledCopy(random)) {
            int fire = terrain.height(centerX + direction.getStepX() * offset, centerZ + direction.getStepZ() * offset);
            int flag = terrain.height(centerX - direction.getStepX() * offset, centerZ - direction.getStepZ() * offset);
            if (fire == SurfaceSampler.WATER || flag == SurfaceSampler.WATER) continue;

            int difference = Math.abs(fire - centerHeight) + Math.abs(flag - centerHeight);
            if (difference < bestDifference) {
                bestDifference = difference;
                best = direction;
            }
        }

        return best;
    }

    private List<Candidate> findTowers(StructureTemplateManager manager, Terrain terrain, List<StructurePoolElement> elements, int centerX, int centerZ, int centerHeight, double start, List<Candidate> placed) {
        double sector = FULL_TURN / elements.size();
        List<Candidate> best = List.of();
        double bestScore = Double.NEGATIVE_INFINITY;

        for (int rotation = 0; rotation < TOWER_ROTATIONS; rotation++) {
            double offset = start + rotation * sector / TOWER_ROTATIONS;
            List<Candidate> occupied = new ArrayList<>(placed);
            List<Candidate> towers = new ArrayList<>();
            double total = 0.0;

            for (int i = 0; i < elements.size(); i++) {
                Optional<Scored> tower = searchSector(manager, terrain, elements.get(i), centerX, centerZ, offset + i * sector, sector * SECTOR_COVERAGE / 2.0, this.towerDistance, occupied,
                        (candidate, deviation) -> blocksView(terrain, candidate) ? Double.NEGATIVE_INFINITY : (candidate.footprint().median() - centerHeight) * TOWER_HEIGHT_WEIGHT - candidate.footprint().range() - Math.abs(deviation) * OFF_CENTER_PENALTY);
                if (tower.isEmpty()) continue;

                occupied.add(tower.get().candidate());
                towers.add(tower.get().candidate());
                total += tower.get().score();
            }

            if (towers.size() > best.size() || (towers.size() == best.size() && total > bestScore)) {
                best = towers;
                bestScore = total;
            }
        }

        return best;
    }

    private Optional<Scored> searchSector(StructureTemplateManager manager, Terrain terrain, StructurePoolElement element, int centerX, int centerZ, double middle, double halfWidth, InclusiveRange<Integer> distance, List<Candidate> placed, CandidateScore scorer) {
        Scored best = null;

        for (int radius = distance.minInclusive(); radius <= distance.maxInclusive(); radius += RADIUS_STEP) {
            double angleStep = (double) ARC_STEP / radius;
            int steps = (int) (halfWidth / angleStep);
            for (int step = -steps; step <= steps; step++) {
                double deviation = step * angleStep;
                Candidate candidate = createCandidate(manager, terrain, element, centerX, centerZ, radius, middle + deviation);
                if (!fits(candidate, placed)) continue;

                double score = scorer.score(candidate, deviation);
                if (score > Double.NEGATIVE_INFINITY && (best == null || score > best.score())) {
                    best = new Scored(candidate, score);
                }
            }
        }

        return Optional.ofNullable(best);
    }

    private boolean fits(Candidate candidate, List<Candidate> placed) {
        Footprint footprint = candidate.footprint();
        if (!footprint.solid() || footprint.range() > this.maxHeightDifference) return false;

        BoundingBox padded = candidate.box().inflatedBy(PIECE_PADDING);
        return placed.stream().noneMatch(other -> other.box().intersects(padded));
    }

    private static boolean blocksView(Terrain terrain, Candidate candidate) {
        double cos = Math.cos(candidate.angle());
        double sin = Math.sin(candidate.angle());
        int eyeHeight = candidate.footprint().median() + TOWER_EYE_HEIGHT;

        for (int distance : TOWER_VIEW_DISTANCES) {
            int height = terrain.height(candidate.x() + Mth.floor(cos * distance + 0.5), candidate.z() + Mth.floor(sin * distance + 0.5));
            if (height > eyeHeight) return true;
        }

        return false;
    }

    private static Gap largestGap(List<Double> angles) {
        double[] sorted = angles.stream().mapToDouble(angle -> Mth.positiveModulo(angle, FULL_TURN)).sorted().toArray();
        double bestStart = 0.0;
        double bestGap = -1.0;

        for (int i = 0; i < sorted.length; i++) {
            double next = i + 1 < sorted.length ? sorted[i + 1] : sorted[0] + FULL_TURN;
            double gap = next - sorted[i];
            if (gap > bestGap) {
                bestGap = gap;
                bestStart = sorted[i];
            }
        }

        return new Gap(bestStart + bestGap / 2.0, bestGap);
    }

    private static Candidate createCandidate(StructureTemplateManager manager, Terrain terrain, StructurePoolElement element, int centerX, int centerZ, int radius, double angle) {
        int x = centerX + Mth.floor(Math.cos(angle) * radius + 0.5);
        int z = centerZ + Mth.floor(Math.sin(angle) * radius + 0.5);
        Direction front = Direction.getApproximateNearest(centerX - x, 0, centerZ - z);

        return createCandidate(manager, terrain, element, x, z, front, angle);
    }

    private static Candidate createCandidate(StructureTemplateManager manager, Terrain terrain, StructurePoolElement element, int x, int z, Direction front, double angle) {
        Rotation rotation = Arrays.stream(Rotation.values()).filter(value -> value.rotate(TEMPLATE_FRONT) == front).findFirst().orElse(Rotation.NONE);
        Vec3i size = element.getSize(manager, Rotation.NONE);
        BlockPos anchor = StructureTemplate.transform(new BlockPos((size.getX() - 1) / 2, 0, (size.getZ() - 1) / 2), Mirror.NONE, rotation, BlockPos.ZERO);
        BlockPos position = new BlockPos(x - anchor.getX(), 0, z - anchor.getZ());
        BoundingBox box = element.getBoundingBox(manager, position, rotation);

        return new Candidate(element, x, z, position, rotation, box, terrain.footprint(box), angle);
    }

    private static int floorToStep(int value, int step) {
        return Math.floorDiv(value, step) * step;
    }

    private static int ceilToStep(int value, int step) {
        return -Math.floorDiv(-value, step) * step;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.HUNTER_CAMP_TYPE.get();
    }

    private record Site(int x, int z, double score, int distance) {

        private static final Comparator<Site> ORDER = Comparator.comparingDouble(Site::score).thenComparingInt(Site::distance);
    }

    private interface CandidateScore {

        double score(Candidate candidate, double deviation);
    }

    private record Scored(Candidate candidate, double score) {
    }

    private record Gap(double middle, double width) {
    }

    private record Footprint(int min, int max, int median, boolean solid) {

        private int range() {
            return this.max - this.min;
        }
    }

    private record Candidate(StructurePoolElement element, int x, int z, BlockPos position, Rotation rotation, BoundingBox box, Footprint footprint, double angle) {

        private List<HunterCampPiece> toPieces(GenerationContext context, int jigsawDepth, Rotation wind) {
            StructureTemplateManager manager = context.structureTemplateManager();
            int y = context.chunkGenerator().getFirstFreeHeight(this.x, this.z, Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor(), context.randomState());
            BlockPos placedPosition = this.position.atY(y);
            HunterCampPiece root = new HunterCampPiece(manager, this.element, placedPosition, this.rotation, this.element.getBoundingBox(manager, placedPosition, this.rotation), true);
            List<HunterCampPiece> pieces = new ArrayList<>();
            pieces.add(root);
            expand(manager, context.registryAccess().lookupOrThrow(Registries.TEMPLATE_POOL), root, null, 0, jigsawDepth, wind, context.random(), pieces);
            return pieces;
        }

        private static void expand(StructureTemplateManager manager, Registry<StructureTemplatePool> pools, HunterCampPiece piece, @Nullable BlockPos entry, int depth, int maxDepth, Rotation wind, RandomSource random, List<HunterCampPiece> pieces) {
            if (depth > maxDepth) return;

            for (StructureTemplate.JigsawBlockInfo source : piece.getElement().getShuffledJigsawBlocks(manager, piece.getPosition(), piece.getRotation(), random)) {
                if (source.info().pos().equals(entry)) continue;

                Optional<Holder.Reference<StructureTemplatePool>> pool = pools.get(source.pool());
                if (pool.isEmpty()) continue;

                Holder<StructureTemplatePool> selected = depth < maxDepth ? pool.get() : pool.get().value().getFallback();
                StructurePoolElement child = selected.value().getRandomTemplate(random);
                attach(manager, source, child, wind, random).ifPresent(attached -> {
                    pieces.add(attached);
                    expand(manager, pools, attached, source.info().pos().relative(JigsawBlock.getFrontFacing(source.info().state())), depth + 1, maxDepth, wind, random, pieces);
                });
            }
        }

        private static Optional<HunterCampPiece> attach(StructureTemplateManager manager, StructureTemplate.JigsawBlockInfo source, StructurePoolElement child, Rotation wind, RandomSource random) {
            Direction front = JigsawBlock.getFrontFacing(source.info().state());
            BlockPos target = source.info().pos().relative(front);
            boolean vertical = front.getAxis().isVertical();
            List<Rotation> rotations = Rotation.getShuffled(random);
            if (vertical) {
                rotations.remove(wind);
                rotations.addFirst(wind);
            }

            for (Rotation rotation : rotations) {
                for (StructureTemplate.JigsawBlockInfo jigsaw : child.getShuffledJigsawBlocks(manager, BlockPos.ZERO, rotation, random)) {
                    if (!JigsawBlock.canAttach(source, jigsaw)) continue;

                    BlockPos position = target.subtract(jigsaw.info().pos());
                    return Optional.of(new HunterCampPiece(manager, child, position, rotation, child.getBoundingBox(manager, position, rotation), !vertical));
                }
            }

            return Optional.empty();
        }
    }

    private static class Terrain {

        private static final int UNKNOWN = Integer.MAX_VALUE;

        private final SurfaceSampler sampler;
        private final Long2IntOpenHashMap heights = new Long2IntOpenHashMap();

        private Terrain(GenerationContext context) {
            this.sampler = SurfaceSampler.create(context);
            this.heights.defaultReturnValue(UNKNOWN);
        }

        private int height(int x, int z) {
            long key = ChunkPos.pack(x, z);
            int height = this.heights.get(key);
            if (height == UNKNOWN) {
                height = this.sampler.sample(x, z);
                this.heights.put(key, height);
            }

            return height;
        }

        private boolean isHollow(int x, int surface, int z) {
            return this.sampler.isHollow(x, surface, z);
        }

        private Footprint footprint(BoundingBox box) {
            int[] xs = {box.minX(), (box.minX() + box.maxX()) / 2, box.maxX()};
            int[] zs = {box.minZ(), (box.minZ() + box.maxZ()) / 2, box.maxZ()};
            int[] samples = new int[xs.length * zs.length];
            int count = 0;

            for (int x : xs) {
                for (int z : zs) {
                    int height = height(x, z);
                    if (height == SurfaceSampler.WATER) {
                        return new Footprint(0, 0, 0, false);
                    }
                    samples[count] = height;
                    count++;
                }
            }

            Arrays.sort(samples);
            boolean solid = !isHollow(xs[1], height(xs[1], zs[1]), zs[1]);

            return new Footprint(samples[0], samples[samples.length - 1], samples[samples.length / 2], solid);
        }
    }
}
