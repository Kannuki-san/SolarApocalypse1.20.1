package com.kannuki_san.solarapocalypse.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;

public final class ExposureUtil {

    public static boolean isExposedToOpenSky(LevelAccessor level, BlockPos pos) {
        // ガラスや葉など、air以外が上にあれば安全扱いにする。
        BlockPos.MutableBlockPos checkPos = new BlockPos.MutableBlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
        for (int y = pos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            checkPos.setY(y);
            if (!level.getBlockState(checkPos).isAir()) {
                return false;
            }
        }
        return true;
    }

    private ExposureUtil() {
    }
}
