package com.kannuki_san.solarapocalypse.event;

import com.kannuki_san.solarapocalypse.util.ExposureUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class FluidEventHandler {

    @SubscribeEvent
    public void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        BlockState newState = event.getNewState();
        // 地表で新しく水が広がる/水没状態になるケースをまとめて止める。
        boolean placesWater = newState.is(Blocks.WATER)
                || newState.is(Blocks.BUBBLE_COLUMN)
                || (newState.hasProperty(BlockStateProperties.WATERLOGGED)
                && newState.getValue(BlockStateProperties.WATERLOGGED));
        if (!placesWater) {
            return;
        }

        BlockPos pos = event.getPos();
        if (ExposureUtil.isExposedToOpenSky(event.getLevel(), pos)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onCreateFluidSource(BlockEvent.CreateFluidSourceEvent event) {
        // 無限水源化は地下なら許可し、空が開けている場所だけ拒否する。
        if (!event.getState().getFluidState().is(Fluids.WATER)) {
            return;
        }
        if (ExposureUtil.isExposedToOpenSky(event.getLevel(), event.getPos())) {
            event.setResult(Event.Result.DENY);
        }
    }
}
