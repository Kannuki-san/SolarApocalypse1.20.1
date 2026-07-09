package com.kannuki_san.solarapocalypse.util;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlockTransformUtil {

    public static BlockState grassSnowOrIceReplacement(BlockState state) {
        // Day 2系の変換先をここに集約して、後からconfig対応しやすくする。
        if (state.is(Blocks.GRASS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (isSnow(state)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (isIce(state)) {
            return SolarApocalypseConfig.ICE_TURNS_TO_WATER.get()
                    ? Blocks.WATER.defaultBlockState()
                    : Blocks.AIR.defaultBlockState();
        }
        return null;
    }

    public static BlockState waterEvaporationReplacement(BlockState state) {
        // waterlogged blockはブロック自体を壊さず、水だけ抜く。
        if (state.is(Blocks.WATER)
                || state.is(Blocks.BUBBLE_COLUMN)
                || state.is(Blocks.KELP)
                || state.is(Blocks.KELP_PLANT)
                || state.is(Blocks.SEAGRASS)
                || state.is(Blocks.TALL_SEAGRASS)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)
                && state.getValue(BlockStateProperties.WATERLOGGED)) {
            return state.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        return null;
    }

    private static boolean isSnow(BlockState state) {
        return state.is(Blocks.SNOW)
                || state.is(Blocks.SNOW_BLOCK)
                || state.is(Blocks.POWDER_SNOW);
    }

    private static boolean isIce(BlockState state) {
        return state.is(Blocks.ICE)
                || state.is(Blocks.PACKED_ICE)
                || state.is(Blocks.BLUE_ICE);
    }

    private BlockTransformUtil() {
    }
}
