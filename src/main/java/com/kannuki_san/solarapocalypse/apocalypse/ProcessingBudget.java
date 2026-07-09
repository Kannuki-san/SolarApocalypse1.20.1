package com.kannuki_san.solarapocalypse.apocalypse;

final class ProcessingBudget {

    private final int maxBlockChanges;
    private final int maxFirePlacements;
    private int blockChanges;
    private int firePlacements;

    ProcessingBudget(int maxBlockChanges, int maxFirePlacements) {
        this.maxBlockChanges = maxBlockChanges;
        this.maxFirePlacements = maxFirePlacements;
    }

    boolean hasBlockChangeBudget() {
        return blockChanges < maxBlockChanges;
    }

    int remainingBlockChanges() {
        return maxBlockChanges - blockChanges;
    }

    boolean consumeBlockChange() {
        // setBlock系の呼び出し前に消費し、上限超過時は処理側で何もしない。
        if (!hasBlockChangeBudget()) {
            return false;
        }
        blockChanges++;
        return true;
    }

    boolean consumeFirePlacement() {
        // 火ブロックは負荷が高くなりやすいので、通常のブロック更新とは別枠でも制限する。
        if (firePlacements >= maxFirePlacements || !consumeBlockChange()) {
            return false;
        }
        firePlacements++;
        return true;
    }
}
