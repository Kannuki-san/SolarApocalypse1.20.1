package com.kannuki_san.solarapocalypse.event;

import com.kannuki_san.solarapocalypse.apocalypse.ApocalypseScheduler;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ApocalypseTickHandler {

    private final Map<ResourceKey<Level>, ApocalypseScheduler> schedulers = new HashMap<>();

    @SubscribeEvent
    public void onLevelTick(TickEvent.LevelTickEvent event) {
        // END phaseだけで処理し、1tick中に同じ終末処理が二重に走らないようにする。
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (serverLevel.dimension() != Level.OVERWORLD || serverLevel.players().isEmpty()) {
            return;
        }

        // Minecraft内の日数は0日始まりなので、プレイヤー目線の1日目に合わせる。
        long day = (serverLevel.getDayTime() / 24000L) + 1L;
        ApocalypseScheduler scheduler = schedulers.computeIfAbsent(serverLevel.dimension(), key -> new ApocalypseScheduler());
        scheduler.enqueueAroundPlayers(serverLevel);
        scheduler.tick(serverLevel, day);
    }

    @SubscribeEvent
    public void onLevelUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel) {
            // ワールドを閉じた後に古いキューを残さない。
            schedulers.remove(serverLevel.dimension());
        }
    }
}
