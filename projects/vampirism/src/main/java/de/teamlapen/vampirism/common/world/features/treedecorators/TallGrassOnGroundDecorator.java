package de.teamlapen.vampirism.common.world.features.treedecorators;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.teamlapen.vampirism.common.core.ModFeatures;
import net.minecraft.core.BlockPos;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.TreeFeature;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.List;

public class TallGrassOnGroundDecorator extends TreeDecorator {

    public static final MapCodec<TallGrassOnGroundDecorator> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ExtraCodecs.POSITIVE_INT.fieldOf("tries").orElse(48).forGetter(d -> d.tries),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("radius").orElse(3).forGetter(d -> d.radius),
            ExtraCodecs.NON_NEGATIVE_INT.fieldOf("height").orElse(1).forGetter(d -> d.height)
    ).apply(i, TallGrassOnGroundDecorator::new));

    private final int tries;
    private final int radius;
    private final int height;

    public TallGrassOnGroundDecorator(int tries, int radius, int height) {
        this.tries = tries;
        this.radius = radius;
        this.height = height;
    }

    @Override
    protected TreeDecoratorType<?> type() {
        return ModFeatures.TALL_GRASS_ON_GROUND.get();
    }

    @Override
    public void place(Context context) {
        List<BlockPos> trunk = TreeFeature.getLowestTrunkOrRootOfTree(context);
        if (trunk.isEmpty()) {
            return;
        }
        BlockPos origin = trunk.getFirst();
        int minY = origin.getY();
        int minX = origin.getX(), maxX = origin.getX();
        int minZ = origin.getZ(), maxZ = origin.getZ();
        for (BlockPos pos : trunk) {
            if (pos.getY() == minY) {
                minX = Math.min(minX, pos.getX());
                maxX = Math.max(maxX, pos.getX());
                minZ = Math.min(minZ, pos.getZ());
                maxZ = Math.max(maxZ, pos.getZ());
            }
        }
        RandomSource random = context.random();
        BoundingBox area = new BoundingBox(minX, minY, minZ, maxX, minY, maxZ).inflatedBy(this.radius, this.height, this.radius);
        BlockPos.MutableBlockPos ground = new BlockPos.MutableBlockPos();
        for (int i = 0; i < this.tries; i++) {
            ground.set(
                    random.nextIntBetweenInclusive(area.minX(), area.maxX()),
                    random.nextIntBetweenInclusive(area.minY(), area.maxY()),
                    random.nextIntBetweenInclusive(area.minZ(), area.maxZ())
            );
            placeTallGrass(context, ground);
        }
    }

    private void placeTallGrass(Context context, BlockPos ground) {
        BlockPos lower = ground.above();
        BlockPos upper = lower.above();
        if (context.checkBlock(ground, BlockBehaviour.BlockStateBase::isSolidRender) && context.isAir(lower) && context.isAir(upper) && context.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, ground).getY() <= lower.getY()) {
            context.setBlock(lower, Blocks.TALL_GRASS.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER));
            context.setBlock(upper, Blocks.TALL_GRASS.defaultBlockState().setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER));
        }
    }
}
