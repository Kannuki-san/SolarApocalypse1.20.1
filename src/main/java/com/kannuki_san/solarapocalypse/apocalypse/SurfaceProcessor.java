package com.kannuki_san.solarapocalypse.apocalypse;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import com.kannuki_san.solarapocalypse.util.BlockTransformUtil;
import com.kannuki_san.solarapocalypse.util.ExposureUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class SurfaceProcessor {

    private static final int SURFACE_SCAN_PADDING = 4;
    private static final int TREE_SCAN_BELOW_SURFACE = 10;
    private static final int TREE_SCAN_ABOVE_SURFACE = 18;
    private static final double TREE_FIRE_CHANCE = 0.35D;
    private static final double COMBUSTIBLE_DECAY_CHANCE = 0.08D;
    private static final Direction[] HORIZONTAL_DIRECTIONS = {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.WEST,
            Direction.EAST
    };

    private final Random random = new Random();

    void processRandomChunk(ServerLevel level, ChunkPos chunkPos, long day, ProcessingBudget budget, int attempts) {
        // 選ばれたチャンク内のランダム地点を少しだけ試し、世界がじわじわ壊れるようにする。
        for (int i = 0; i < attempts && budget.hasBlockChangeBudget(); i++) {
            int x = chunkPos.getMinBlockX() + random.nextInt(16);
            int z = chunkPos.getMinBlockZ() + random.nextInt(16);
            processRandomPosition(level, x, z, day, budget);
        }
    }

    private void processRandomPosition(ServerLevel level, int x, int z, long day, ProcessingBudget budget) {
        // ランダムなcolumnで、現在の日数に応じた終末ステージだけ実行する。
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        if (surfaceY < level.getMinBuildHeight()) {
            return;
        }

        boolean changed = false;
        if (day >= SolarApocalypseConfig.GRASS_DECAY_DAY.get()) {
            changed = processGrassSnowAndIce(level, x, z, surfaceY, budget);
        }
        if (!changed && day >= SolarApocalypseConfig.WATER_EVAPORATION_DAY.get()) {
            changed = processWater(level, x, z, surfaceY, budget);
        }
        if (!changed && day >= SolarApocalypseConfig.FIRE_DAY.get()) {
            changed = processTrees(level, x, z, surfaceY, budget);
        }
        if (!changed && day >= SolarApocalypseConfig.SAND_TO_GLASS_DAY.get()) {
            processSand(level, x, z, surfaceY, budget);
        }
    }

    private boolean processGrassSnowAndIce(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 2日目以降: 草を土へ、雪と氷を消して地表を乾いた状態に近づける。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - SURFACE_SCAN_PADDING);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + SURFACE_SCAN_PADDING);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (BlockTransformUtil.isSurfacePlant(state)) {
                if (tryReplaceGrassSnowOrIce(level, pos, state, budget)) {
                    BlockPos below = pos.below();
                    if (budget.hasBlockChangeBudget()
                            && ExposureUtil.isExposedToOpenSky(level, below)) {
                        tryReplaceGrassSnowOrIce(level, below, level.getBlockState(below), budget);
                    }
                    return true;
                }
                continue;
            }

            if (tryReplaceGrassSnowOrIce(level, pos, state, budget)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryReplaceGrassSnowOrIce(ServerLevel level, BlockPos pos, BlockState state, ProcessingBudget budget) {
        BlockState replacement = BlockTransformUtil.grassSnowOrIceReplacement(state);
        if (replacement == null || !budget.consumeBlockChange()) {
            return false;
        }
        level.setBlockAndUpdate(pos, replacement);
        sendEvaporationEffects(level, pos, 2);
        return true;
    }

    private boolean processWater(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 3日目以降: 水を見つけたら半径ベースの小さな塊で蒸発させる。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - SolarApocalypseConfig.WATER_SCAN_DEPTH.get());
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + SURFACE_SCAN_PADDING);
        int firstY = minY + random.nextInt(Math.max(1, maxY - minY + 1));
        for (int checked = 0; checked <= maxY - minY && budget.hasBlockChangeBudget(); checked++) {
            int y = minY + Math.floorMod(firstY - minY - checked, maxY - minY + 1);
            BlockPos pos = new BlockPos(x, y, z);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState replacement = BlockTransformUtil.waterEvaporationReplacement(state);
            if (replacement != null) {
                return evaporateWaterCluster(level, pos, budget);
            }
        }
        return false;
    }

    private boolean evaporateWaterCluster(ServerLevel level, BlockPos center, ProcessingBudget budget) {
        int minRadius = SolarApocalypseConfig.MIN_WATER_CLUSTER_RADIUS.get();
        int maxRadius = Math.max(minRadius, SolarApocalypseConfig.MAX_WATER_CLUSTER_RADIUS.get());
        int radius = minRadius + random.nextInt(maxRadius - minRadius + 1);
        int changed = 0;
        if (budget.hasBlockChangeBudget() && tryEvaporateWaterAt(level, center, budget)) {
            changed++;
        }
        List<BlockPos> candidates = waterClusterCandidates(center, radius);
        Collections.shuffle(candidates, random);
        for (BlockPos pos : candidates) {
            if (!budget.hasBlockChangeBudget()) {
                break;
            }
            if (tryEvaporateWaterAt(level, pos, budget)) {
                changed++;
            }
        }
        return changed > 0;
    }

    private List<BlockPos> waterClusterCandidates(BlockPos center, int radius) {
        List<BlockPos> candidates = new ArrayList<>();
        int radiusSquared = radius * radius;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (dx == 0 && dy == 0 && dz == 0) {
                        continue;
                    }
                    if (dx * dx + dy * dy + dz * dz <= radiusSquared) {
                        candidates.add(center.offset(dx, dy, dz));
                    }
                }
            }
        }
        return candidates;
    }

    private boolean tryEvaporateWaterAt(ServerLevel level, BlockPos pos, ProcessingBudget budget) {
        if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
            return false;
        }
        BlockState replacement = BlockTransformUtil.waterEvaporationReplacement(level.getBlockState(pos));
        if (replacement == null || !budget.consumeBlockChange()) {
            return false;
        }
        level.setBlockAndUpdate(pos, replacement);
        sendEvaporationEffects(level, pos, 1);
        return true;
    }

    private boolean processTrees(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 4日目以降: 木、葉、木材建材、干し草を少しずつ燃やし、低確率でブロック自体も崩す。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - TREE_SCAN_BELOW_SURFACE);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + TREE_SCAN_ABOVE_SURFACE);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }

            if (!BlockTransformUtil.isCombustibleApocalypseTarget(state)) {
                continue;
            }
            if (random.nextDouble() < COMBUSTIBLE_DECAY_CHANCE && tryRemoveCombustible(level, pos, budget)) {
                return true;
            }
            if (random.nextDouble() < TREE_FIRE_CHANCE) {
                return placeLimitedFire(level, pos, budget);
            }
        }
        return false;
    }

    private boolean tryRemoveCombustible(ServerLevel level, BlockPos pos, ProcessingBudget budget) {
        if (!budget.consumeBlockChange()) {
            return false;
        }
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        sendSmoke(level, pos, 3);
        return true;
    }

    private boolean processSand(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 5日目以降: 日光にさらされた砂と赤い砂をガラス化する。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - SURFACE_SCAN_PADDING);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + SURFACE_SCAN_PADDING);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if ((state.is(Blocks.SAND) || state.is(Blocks.RED_SAND))
                    && ExposureUtil.isExposedToOpenSky(level, pos)
                    && budget.consumeBlockChange()) {
                level.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
                sendEvaporationEffects(level, pos, 4);
                return true;
            }
        }
        return false;
    }

    private boolean placeLimitedFire(ServerLevel level, BlockPos pos, ProcessingBudget budget) {
        // 火の設置は上限つき。燃え広がりによる負荷を抑える。
        BlockPos firePos = findFirePosition(level, pos);
        if (firePos == null || !budget.consumeFirePlacement()) {
            return false;
        }
        level.setBlockAndUpdate(firePos, Blocks.FIRE.defaultBlockState());
        sendSmoke(level, pos, 5);
        if (random.nextDouble() < 0.05D) {
            level.playSound(
                    null,
                    firePos,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.1F,
                    1.0F + (random.nextFloat() - 0.5F) * 0.2F
            );
        }
        return true;
    }

    private BlockPos findFirePosition(ServerLevel level, BlockPos target) {
        BlockPos above = target.above();
        if (canPlaceFire(level, above)) {
            return above;
        }
        for (int i = 0; i < HORIZONTAL_DIRECTIONS.length; i++) {
            Direction direction = HORIZONTAL_DIRECTIONS[random.nextInt(HORIZONTAL_DIRECTIONS.length)];
            BlockPos side = target.relative(direction);
            if (canPlaceFire(level, side)) {
                return side;
            }
        }
        return null;
    }

    private boolean canPlaceFire(ServerLevel level, BlockPos pos) {
        BlockState fire = Blocks.FIRE.defaultBlockState();
        return level.getBlockState(pos).isAir() && fire.canSurvive(level, pos);
    }

    private void sendEvaporationEffects(ServerLevel level, BlockPos pos, int particles) {
        // 音は低確率にして、水場を処理した時の爆音を避ける。
        level.sendParticles(
                ParticleTypes.CLOUD,
                pos.getX() + 0.5D,
                pos.getY() + 0.5D,
                pos.getZ() + 0.5D,
                particles,
                0.3D,
                0.2D,
                0.3D,
                0.01D
        );
        if (random.nextDouble() < SolarApocalypseConfig.EVAPORATION_SOUND_CHANCE.get()) {
            level.playSound(
                    null,
                    pos,
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.BLOCKS,
                    SolarApocalypseConfig.EVAPORATION_SOUND_VOLUME.get().floatValue(),
                    1.0F
            );
        }
    }

    private void sendSmoke(ServerLevel level, BlockPos pos, int particles) {
        // 木や葉の焼け表現用の軽い演出。
        level.sendParticles(
                ParticleTypes.SMOKE,
                pos.getX() + 0.5D,
                pos.getY() + 1.0D,
                pos.getZ() + 0.5D,
                particles,
                0.2D,
                0.1D,
                0.2D,
                0.01D
        );
    }
}
