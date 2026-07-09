package com.kannuki_san.solarapocalypse.apocalypse;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

final class ChunkWork {

    private static final int COLUMNS_PER_CHUNK = 16 * 16;

    private final ChunkPos chunkPos;
    private int nextColumn;

    ChunkWork(ChunkPos chunkPos) {
        this.chunkPos = chunkPos;
    }

    void process(ServerLevel level, long day, ProcessingBudget budget, SurfaceProcessor processor, int maxColumns) {
        int processedColumns = 0;
        // チャンク内の16x16列を一度に全部処理せず、次tickへ続きから回す。
        while (nextColumn < COLUMNS_PER_CHUNK
                && processedColumns < maxColumns
                && budget.hasBlockChangeBudget()) {
            int localX = nextColumn & 15;
            int localZ = (nextColumn >> 4) & 15;
            int x = chunkPos.getMinBlockX() + localX;
            int z = chunkPos.getMinBlockZ() + localZ;
            processor.processColumn(level, x, z, day, budget);
            nextColumn++;
            processedColumns++;
        }
    }

    boolean isComplete() {
        return nextColumn >= COLUMNS_PER_CHUNK;
    }
}
