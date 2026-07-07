package de.teamlapen.vampirism.common.world.structures.crypt;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.common.core.ModStructures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.DimensionPadding;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.alias.PoolAliasLookup;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class CryptStructure extends Structure {

    public static final MapCodec<CryptStructure> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            settingsCodec(inst),
            StructureTemplatePool.CODEC.fieldOf("start_pool").forGetter(s -> s.startPool),
            Codec.intRange(0, JigsawStructure.MAX_DEPTH).fieldOf("size").forGetter(s -> s.maxDepth)
    ).apply(inst, CryptStructure::new));

    private static final LiquidSettings LIQUID_SETTINGS = LiquidSettings.IGNORE_WATERLOGGING;

    private final Holder<StructureTemplatePool> startPool;
    private final int maxDepth;

    public CryptStructure(StructureSettings settings, Holder<StructureTemplatePool> startPool, int maxDepth) {
        super(settings);
        this.startPool = startPool;
        this.maxDepth = maxDepth;
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos startPos = new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ());
        return JigsawPlacement.addPieces(context, this.startPool, Optional.empty(), this.maxDepth, startPos, false, Optional.of(Heightmap.Types.WORLD_SURFACE_WG), new JigsawStructure.MaxDistance(80, 80), PoolAliasLookup.EMPTY, DimensionPadding.ZERO, LIQUID_SETTINGS)
                .map(stub -> new GenerationStub(stub.position(), builder -> {
                    StructurePiecesBuilder jigsawBuilder = new StructurePiecesBuilder();
                    List<StructurePiece> pieces = stub.generator().map(generator -> {
                        generator.accept(jigsawBuilder);
                        return jigsawBuilder;
                    }, Function.identity()).build().pieces();
                    // the start piece is generated first, all other pieces are underground
                    for (int i = 0; i < pieces.size(); i++) {
                        if (pieces.get(i) instanceof PoolElementStructurePiece piece) {
                            builder.addPiece(new CryptPiece(context.structureTemplateManager(), piece, LIQUID_SETTINGS, i == 0 ? this.terrainAdaptation() : TerrainAdjustment.NONE));
                        } else {
                            builder.addPiece(pieces.get(i));
                        }
                    }
                }));
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.CRYPT_TYPE.get();
    }
}