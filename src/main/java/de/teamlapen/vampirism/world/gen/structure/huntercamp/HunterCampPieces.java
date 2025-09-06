package de.teamlapen.vampirism.world.gen.structure.huntercamp;

import de.teamlapen.vampirism.api.util.VResourceLocation;
import de.teamlapen.vampirism.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.*;
import org.jetbrains.annotations.NotNull;

public class HunterCampPieces {

    public static final int GENERATION_HEIGHT = 90;

    public static final ResourceLocation FLAGPOLE = VResourceLocation.mod("hunter_camp/flagpole");
    public static final ResourceLocation FLAG = VResourceLocation.mod("hunter_camp/flag");
    public static final ResourceLocation TENT = VResourceLocation.mod("hunter_camp/tent");
    public static final ResourceLocation TOWER = VResourceLocation.mod("hunter_camp/tower");
    public static final ResourceLocation CAMPFIRE = VResourceLocation.mod("hunter_camp/campfire");

    public static void addPieces(StructureTemplateManager templateManager, BlockPos centerPos, Rotation rotation, StructurePieceAccessor pieces, RandomSource random) {
        int increase = random.nextInt(4);
        int addonNumber = 2 + increase; // 2 - 5 tents and towers combined
        int radius = 6 + increase; // 8 - 11

        HunterCampPiece pole = new HunterCampPiece(
                templateManager,
                FLAGPOLE,
                centerPos,
                rotation
        );
        pieces.addPiece(pole);

        double campfireRadius = Math.min(3.0 + random.nextDouble() * 2.5 + increase / 1.5, radius / 2.0 + 2);
        double campfireAngle = random.nextDouble() * 2.3; // 0 rad = east (opposite of west where the flag is)

        pieces.addPiece(new HunterCampPiece(
                templateManager,
                CAMPFIRE,
                new BlockPos((int) (centerPos.getX() + Math.round(Math.cos(campfireAngle) * campfireRadius)), GENERATION_HEIGHT, (int) (centerPos.getZ() + Math.round(Math.sin(campfireAngle) * campfireRadius))),
                getClosestRotation(Math.toDegrees(campfireAngle))
        ));

        StructureTemplate template = templateManager.getOrCreate(FLAGPOLE);
        for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(pole.templatePosition(), pole.placeSettings(), Blocks.STRUCTURE_BLOCK)) {
            if (info.nbt() != null && "attach:flag".equals(info.nbt().getString("metadata"))) {
                pieces.addPiece(new FlagPiece(
                        templateManager,
                        FLAG,
                        info.pos(),
                        Rotation.COUNTERCLOCKWISE_90 // west
                ));
            }
        }

        double step = Math.PI * 2 / addonNumber;
        double angle = random.nextDouble() * 3;

        boolean isTent = true;

        for (int i = 0; i < addonNumber; i++) {
            int jitter = random.nextInt(-1, 3);
            int dX = (int) Math.round(Math.cos(angle) * (radius + jitter));
            int dZ = (int) Math.round(Math.sin(angle) * (radius + jitter));
            BlockPos pos = new BlockPos(centerPos.getX() + dX, GENERATION_HEIGHT, centerPos.getZ() + dZ);

            pieces.addPiece(new HunterCampPiece(
                    templateManager,
                    isTent ? TENT : TOWER,
                    pos,
                    getClosestRotation(Math.toDegrees(angle))
            ));

            angle += step;
            isTent = !isTent;
        }
    }

    public static Rotation getClosestRotation(double angle) {
        angle = ((angle % 360) + 360) % 360;
        int index = (int) Math.round(angle / 90.0) % 4;
        return switch (index) {
            case 1 -> Rotation.CLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.COUNTERCLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static class HunterCampPiece extends TemplateStructurePiece {

        public HunterCampPiece(StructureTemplateManager templateManager, ResourceLocation location, BlockPos startPos, Rotation rotation) {
            super(ModStructures.HUNTER_CAMP_PIECE.get(), 0, templateManager, location, location.toString(), makeSettings(rotation), startPos);
        }

        public HunterCampPiece(StructureTemplateManager templateManager, CompoundTag tag) {
            super(ModStructures.HUNTER_CAMP_PIECE.get(), tag, templateManager, key -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
        }

        @Override
        protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rot", placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(@NotNull String name, @NotNull BlockPos pos, @NotNull ServerLevelAccessor level, @NotNull RandomSource random, @NotNull BoundingBox box) {
            handleExtensionMarkers(name, pos, level);
        }

        @Override
        public void postProcess(@NotNull WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator chunkGen, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos refPos) {
            ResourceLocation template = ResourceLocation.parse(templateName);

            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, templatePosition.getX(), templatePosition.getZ());
            int heightShift = CAMPFIRE.equals(template) ? -1 : 0;
            templatePosition = new BlockPos(templatePosition.getX(), surfaceY + heightShift, templatePosition.getZ());

            super.postProcess(level, structureManager, chunkGen, random, box, chunkPos, refPos);
        }
    }

    public static class FlagPiece extends TemplateStructurePiece {

        public FlagPiece(StructureTemplateManager templateManager, ResourceLocation location, BlockPos poleAttachPos, Rotation rotation) {
            super(ModStructures.HUNTER_CAMP_PIECE.get(), 0, templateManager, location, location.toString(), makeSettings(rotation), calculateAlignedPosition(templateManager, location, poleAttachPos, rotation));
        }

        private static BlockPos calculateAlignedPosition(StructureTemplateManager templateManager, ResourceLocation location, BlockPos poleAttachPos, Rotation rotation) {
            StructureTemplate template = templateManager.getOrCreate(location);

            BlockPos flagMarker = findAttachMarker(template, "attach:flagpole");
            if (flagMarker == null) {
                return poleAttachPos;
            }

            BlockPos rotatedMarker = StructureTemplate.calculateRelativePosition(makeSettings(rotation), flagMarker);

            return poleAttachPos.subtract(rotatedMarker);
        }

        public FlagPiece(StructureTemplateManager templateManager, CompoundTag tag) {
            super(ModStructures.HUNTER_CAMP_FLAG_PIECE.get(), tag, templateManager, key -> makeSettings(Rotation.valueOf(tag.getString("Rot"))));
        }

        @Override
        protected void addAdditionalSaveData(@NotNull StructurePieceSerializationContext context, @NotNull CompoundTag tag) {
            super.addAdditionalSaveData(context, tag);
            tag.putString("Rot", placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(@NotNull String name, @NotNull BlockPos pos, @NotNull ServerLevelAccessor level, @NotNull RandomSource random, @NotNull BoundingBox box) {
            handleExtensionMarkers(name, pos, level);

            if (name.equals("attach:flagpole")) {
                BlockState placeState = level.getBlockState(pos.above()).getBlock().defaultBlockState();

                BlockPos placePos = pos;
                while (level.getBlockState(placePos).canBeReplaced()) {
                    level.setBlock(placePos, placeState, Block.UPDATE_ALL);
                    placePos = placePos.below();
                }
            }
        }

        private static BlockPos findAttachMarker(StructureTemplate template, String markerName) {
            for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(BlockPos.ZERO, new StructurePlaceSettings(), Blocks.STRUCTURE_BLOCK)) {
                if (info.nbt() != null && markerName.equals(info.nbt().getString("metadata"))) {
                    return info.pos();
                }
            }
            return null;
        }

        @Override
        public void postProcess(WorldGenLevel level, @NotNull StructureManager structureManager, @NotNull ChunkGenerator chunkGen, @NotNull RandomSource random, @NotNull BoundingBox box, @NotNull ChunkPos chunkPos, @NotNull BlockPos refPos) {
            int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, templatePosition.getX(), templatePosition.getZ());
            int poleHeight = level.getRandom().nextInt(12, 16);
            templatePosition = new BlockPos(templatePosition.getX(), surfaceY + poleHeight, templatePosition.getZ());

            super.postProcess(level, structureManager, chunkGen, random, box, chunkPos, refPos);
        }
    }

    private static StructurePlaceSettings makeSettings(Rotation rotation) {
        return new StructurePlaceSettings()
                .setRotation(rotation)
                .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK)
                .setLiquidSettings(LiquidSettings.APPLY_WATERLOGGING);
    }

    public static void handleExtensionMarkers(@NotNull String name, @NotNull BlockPos pos, @NotNull ServerLevelAccessor level) {
        if (name.startsWith("extend:")) {
            String id = name.substring("extend:".length());
            ResourceLocation location = ResourceLocation.tryParse(id);

            if (location == null || !BuiltInRegistries.BLOCK.containsKey(location)) {
                return;
            }

            Block block = BuiltInRegistries.BLOCK.getValue(location);
            BlockState stateAbove = level.getBlockState(pos.above());
            BlockState placeState = stateAbove.is(block) ? stateAbove : block.defaultBlockState();

            BlockPos placePos = pos;

            if (level.getBlockState(placePos).canBeReplaced() || level.getBlockState(placePos).is(Blocks.SPRUCE_FENCE) || level.getBlockState(placePos).is(Blocks.SPRUCE_LOG)) {
                level.setBlock(placePos, applyWaterlogging(level, placePos, placeState), Block.UPDATE_ALL);
            }

            placePos = placePos.below();
            while (level.getBlockState(placePos).canBeReplaced() || level.getBlockState(placePos).is(Blocks.SPRUCE_LOG)) {
                level.setBlock(placePos, applyWaterlogging(level, placePos, placeState), Block.UPDATE_ALL);
                placePos = placePos.below();
            }
        }
    }

    public static BlockState applyWaterlogging(ServerLevelAccessor level, BlockPos pos, BlockState state) {
        if (state.hasProperty(BlockStateProperties.WATERLOGGED) && level.getFluidState(pos).is(FluidTags.WATER)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, true);
        }

        return state;
    }
}
