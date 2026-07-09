package com.kannuki_san.solarapocalypse.mixin;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SpreadingSnowyDirtBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SpreadingSnowyDirtBlock.class)
public abstract class SpreadingSnowyDirtBlockMixin {

    @Inject(method = "randomTick", at = @At("HEAD"), cancellable = true)
    private void solarapocalypse$preventExposedSurfaceSpread(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random,
            CallbackInfo callback
    ) {
        long day = (level.getDayTime() / 24000L) + 1L;
        // 終末開始後、空が見える地上では草ブロックや菌糸ブロックの自然拡散を止める。
        if (level.dimension() == Level.OVERWORLD
                && day >= SolarApocalypseConfig.GRASS_DECAY_DAY.get()
                && level.canSeeSky(pos.above())) {
            callback.cancel();
        }
    }
}
