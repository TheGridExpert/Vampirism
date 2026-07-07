package de.teamlapen.vampirism.common.world.structures.crypt;

import de.teamlapen.vampirism.common.core.ModStructures;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.neoforged.neoforge.common.world.PieceBeardifierModifier;

public class CryptPiece extends PoolElementStructurePiece implements PieceBeardifierModifier {

    private final TerrainAdjustment terrainAdjustment;

    public CryptPiece(StructureTemplateManager templateManager, PoolElementStructurePiece piece, LiquidSettings liquidSettings, TerrainAdjustment terrainAdjustment) {
        super(templateManager, piece.getElement(), piece.getPosition(), piece.getGroundLevelDelta(), piece.getRotation(), piece.getBoundingBox(), liquidSettings);
        piece.getJunctions().forEach(this::addJunction);
        this.terrainAdjustment = terrainAdjustment;
    }

    public CryptPiece(StructurePieceSerializationContext context, CompoundTag tag) {
        super(context, tag);
        this.terrainAdjustment = tag.read("terrain_adjustment", TerrainAdjustment.CODEC).orElse(TerrainAdjustment.NONE);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        super.addAdditionalSaveData(context, tag);
        tag.store("terrain_adjustment", TerrainAdjustment.CODEC, this.terrainAdjustment);
    }

    @Override
    public StructurePieceType getType() {
        return ModStructures.CRYPT_PIECE.get();
    }

    @Override
    public BoundingBox getBeardifierBox() {
        return this.getBoundingBox();
    }

    @Override
    public TerrainAdjustment getTerrainAdjustment() {
        return this.terrainAdjustment;
    }
}