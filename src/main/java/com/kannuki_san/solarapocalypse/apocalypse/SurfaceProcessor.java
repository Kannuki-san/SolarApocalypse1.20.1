package com.kannuki_san.solarapocalypse.apocalypse;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import com.kannuki_san.solarapocalypse.util.BlockTransformUtil;
import com.kannuki_san.solarapocalypse.util.ExposureUtil;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

final class SurfaceProcessor {

    private static final int SURFACE_SCAN_PADDING = 4;
    private static final int TREE_SCAN_BELOW_SURFACE = 10;
    private static final int TREE_SCAN_ABOVE_SURFACE = 18;
    private static final double LEAF_BURN_AWAY_CHANCE = 0.35D;
    private static final double LOG_FIRE_CHANCE = 0.03D;
    private static final double LOG_BURN_AWAY_CHANCE = 0.01D;

    private final Random random = new Random();

    void processColumn(ServerLevel level, int x, int z, long day, ProcessingBudget budget) {
        // column単位で、現在の日数に応じた終末ステージだけ実行する。
        int surfaceY = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
        if (surfaceY < level.getMinBuildHeight()) {
            return;
        }

        if (day >= SolarApocalypseConfig.GRASS_DECAY_DAY.get()) {
            processGrassSnowAndIce(level, x, z, surfaceY, budget);
        }
        if (day >= SolarApocalypseConfig.WATER_EVAPORATION_DAY.get()) {
            processWater(level, x, z, surfaceY, budget);
        }
        if (day >= SolarApocalypseConfig.FIRE_DAY.get()) {
            processTrees(level, x, z, surfaceY, budget);
        }
        if (day >= SolarApocalypseConfig.SAND_TO_GLASS_DAY.get()) {
            processSand(level, x, z, surfaceY, budget);
        }
    }

    private void processGrassSnowAndIce(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 2日目以降: 草を土へ、雪と氷を消して地表を乾いた状態に近づける。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - SURFACE_SCAN_PADDING);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + SURFACE_SCAN_PADDING);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState replacement = BlockTransformUtil.grassSnowOrIceReplacement(state);
            if (replacement != null && budget.consumeBlockChange()) {
                level.setBlockAndUpdate(pos, replacement);
                sendEvaporationEffects(level, pos, 2);
            }
        }
    }

    private void processWater(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 3日目以降: 水本体、水草、bubble column、waterloggedの水だけを蒸発させる。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - SolarApocalypseConfig.WATER_SCAN_DEPTH.get());
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + SURFACE_SCAN_PADDING);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            BlockState replacement = BlockTransformUtil.waterEvaporationReplacement(state);
            if (replacement != null && budget.consumeBlockChange()) {
                level.setBlockAndUpdate(pos, replacement);
                sendEvaporationEffects(level, pos, 1);
            }
        }
    }

    private void processTrees(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
        // 4日目以降: 大量の火ブロックに頼りすぎず、葉の消失と少量の発火で燃焼を表現する。
        int minY = Math.max(level.getMinBuildHeight(), surfaceY - TREE_SCAN_BELOW_SURFACE);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, surfaceY + TREE_SCAN_ABOVE_SURFACE);
        for (int y = maxY; y >= minY && budget.hasBlockChangeBudget(); y--) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = level.getBlockState(pos);
            if (!ExposureUtil.isExposedToOpenSky(level, pos)) {
                continue;
            }

            if (state.is(BlockTags.LEAVES) && random.nextDouble() < LEAF_BURN_AWAY_CHANCE) {
                if (budget.consumeBlockChange()) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    sendSmoke(level, pos, 4);
                }
                continue;
            }

            if (state.is(BlockTags.LOGS)) {
                if (random.nextDouble() < LOG_BURN_AWAY_CHANCE && budget.consumeBlockChange()) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    sendSmoke(level, pos, 6);
                    continue;
                }
                if (random.nextDouble() < LOG_FIRE_CHANCE) {
                    placeLimitedFire(level, pos, budget);
                }
            }
        }
    }

    private void processSand(ServerLevel level, int x, int z, int surfaceY, ProcessingBudget budget) {
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
            }
        }
    }

    private void placeLimitedFire(ServerLevel level, BlockPos pos, ProcessingBudget budget) {
        // 火の設置は上限つき。燃え広がりによる負荷を抑える。
        BlockPos above = pos.above();
        if (!level.getBlockState(above).isAir() || !budget.consumeFirePlacement()) {
            return;
        }
        level.setBlockAndUpdate(above, Blocks.FIRE.defaultBlockState());
        sendSmoke(level, pos, 5);
        if (random.nextDouble() < 0.05D) {
            level.playSound(
                    null,
                    above,
                    SoundEvents.FIRE_AMBIENT,
                    SoundSource.BLOCKS,
                    0.1F,
                    1.0F + (random.nextFloat() - 0.5F) * 0.2F
            );
        }
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
