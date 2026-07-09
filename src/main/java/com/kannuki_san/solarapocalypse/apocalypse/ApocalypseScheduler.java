package com.kannuki_san.solarapocalypse.apocalypse;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import com.kannuki_san.solarapocalypse.util.ExposureUtil;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;

public final class ApocalypseScheduler {

    private final SurfaceProcessor surfaceProcessor = new SurfaceProcessor();
    private final Random random = new Random();

    public void tick(ServerLevel level, long day) {
        // 1tickあたりのブロック更新量をここでまとめて制限する。
        ProcessingBudget budget = new ProcessingBudget(
                SolarApocalypseConfig.MAX_BLOCK_CHANGES_PER_TICK.get(),
                SolarApocalypseConfig.MAX_FIRE_PLACEMENTS_PER_TICK.get()
        );

        processRandomChunks(level, day, budget);

        if (day >= SolarApocalypseConfig.ENTITY_BURN_DAY.get()) {
            // エンティティ燃焼はブロック更新とは別に、プレイヤー周囲だけを対象にする。
            igniteEntitiesUnderSun(level);
        }
    }

    private void processRandomChunks(ServerLevel level, long day, ProcessingBudget budget) {
        if (level.players().isEmpty()) {
            return;
        }

        // サーバー演算距離いっぱいを候補にし、config側は必要な時だけ上限として使う。
        int radius = processingRadius(level);
        int attemptsPerChunk = SolarApocalypseConfig.RANDOM_ATTEMPTS_PER_CHUNK.get();
        int maxChunks = SolarApocalypseConfig.MAX_CHUNKS_PER_TICK.get();
        Set<ChunkPos> selectedChunks = new HashSet<>();
        int guard = maxChunks * 8;
        while (selectedChunks.size() < maxChunks && guard-- > 0 && budget.hasBlockChangeBudget()) {
            ServerPlayer player = level.players().get(random.nextInt(level.players().size()));
            ChunkPos center = player.chunkPosition();
            int dx = random.nextInt(radius * 2 + 1) - radius;
            int dz = random.nextInt(radius * 2 + 1) - radius;
            ChunkPos selected = new ChunkPos(center.x + dx, center.z + dz);
            if (selectedChunks.add(selected)) {
                surfaceProcessor.processRandomChunk(level, selected, day, budget, attemptsPerChunk);
            }
        }
    }

    private int processingRadius(ServerLevel level) {
        int configuredRadius = SolarApocalypseConfig.CHUNK_RADIUS.get();
        int simulationRadius = level.getServer().getPlayerList().getSimulationDistance();
        if (simulationRadius <= 0) {
            return configuredRadius;
        }
        return Math.max(1, Math.min(configuredRadius, simulationRadius));
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
