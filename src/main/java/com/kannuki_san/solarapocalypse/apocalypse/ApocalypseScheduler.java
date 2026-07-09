package com.kannuki_san.solarapocalypse.apocalypse;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import com.kannuki_san.solarapocalypse.util.ExposureUtil;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public final class ApocalypseScheduler {

    private final Map<ChunkPos, ChunkWork> queuedChunks = new LinkedHashMap<>();
    private final SurfaceProcessor surfaceProcessor = new SurfaceProcessor();

    public void enqueueAroundPlayers(ServerLevel level) {
        // 各プレイヤー周囲のチャンクを候補に入れ、重複分はMapで自然にまとめる。
        int radius = SolarApocalypseConfig.CHUNK_RADIUS.get();
        for (ServerPlayer player : level.players()) {
            ChunkPos center = player.chunkPosition();
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    ChunkPos chunkPos = new ChunkPos(center.x + dx, center.z + dz);
                    queuedChunks.computeIfAbsent(chunkPos, ChunkWork::new);
                }
            }
        }
    }

    public void tick(ServerLevel level, long day) {
        // 1tickあたりのブロック更新量をここでまとめて制限する。
        ProcessingBudget budget = new ProcessingBudget(
                SolarApocalypseConfig.MAX_BLOCK_CHANGES_PER_TICK.get(),
                SolarApocalypseConfig.MAX_FIRE_PLACEMENTS_PER_TICK.get()
        );

        // キュー先頭から少しずつ進め、読み込み範囲全体へ処理が広がるようにする。
        int processedChunks = 0;
        int maxChunks = SolarApocalypseConfig.MAX_CHUNKS_PER_TICK.get();
        int maxColumns = SolarApocalypseConfig.MAX_COLUMNS_PER_CHUNK_STEP.get();
        Iterator<Map.Entry<ChunkPos, ChunkWork>> iterator = queuedChunks.entrySet().iterator();
        while (iterator.hasNext() && processedChunks < maxChunks && budget.hasBlockChangeBudget()) {
            ChunkWork work = iterator.next().getValue();
            work.process(level, day, budget, surfaceProcessor, maxColumns);
            processedChunks++;
            if (work.isComplete()) {
                iterator.remove();
            }
        }

        if (day >= SolarApocalypseConfig.ENTITY_BURN_DAY.get()) {
            // エンティティ燃焼はブロック更新とは別に、プレイヤー周囲だけを対象にする。
            igniteEntitiesUnderSun(level);
        }
    }

    private void igniteEntitiesUnderSun(ServerLevel level) {
        int range = SolarApocalypseConfig.ENTITY_BURN_RADIUS_CHUNKS.get() * 16;
        for (ServerPlayer player : level.players()) {
            BlockPos center = player.blockPosition();
            AABB box = new AABB(
                    center.getX() - range, level.getMinBuildHeight(), center.getZ() - range,
                    center.getX() + range, level.getMaxBuildHeight(), center.getZ() + range
            );
            for (Entity entity : level.getEntities(null, box)) {
                if (ExposureUtil.isExposedToOpenSky(level, entity.blockPosition())) {
                    entity.setSecondsOnFire(8);
                }
            }
        }
    }
}
