package com.kannuki_san.solarapocalypse;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraft.world.level.material.Fluids;


import java.util.List;
import java.util.Random;



/*
基本的にはシンプルな実装で行きたいと思います。
オリジナル版は私がダウンロードする前に消えてしまっていたので「終末MODと動物MODでノアの箱舟」というニコニコにアップロードされている動画を参考にしたいと思います。
マイクラ内の時間で1日たつごとに太陽が近づいてきて、まずは2日目に地表の草が枯れてしまい、3日目には日光にさらされた水が消え始めます。さらに4日目には地上の草や木は燃えます。5日目にはついに地上のすべての動植物(playerやアイテムを含む)は燃えてしまいます。さらに、日光に直接あたっている砂は熱でガラスに変化します。そのためプレイヤーは地下に動物や食料を確保して地下で暮らさなければなりません。一応日光は見たいので、ガラスやいずれかのブロックの下では問題ないようにしようかなと思っています。
以下のリンクは元modのリンクです。
https://www.minecraftforum.net/forums/archive/alpha/minecraft-survival-servers/1048522-1-7-3-solar-apocalypse-no-whitelist-no-lag
*/

@Mod("solarapocalypse")
public class SolarApocalypse {

    public static final String MODID = "solarapocalypse";
    private final Random random = new Random();

    // 試行回数の調整
    private static final int GRASS_ATTEMPTS_PER_PLAYER = 20;
    private static final int WATER_ATTEMPTS_PER_PLAYER = 10;
    private static final int SAND_ATTEMPTS_PER_PLAYER = 30;
    private static final int BURN_ATTEMPTS_PER_PLAYER = 50;
    private static final int CHUNK_RADIUS = 16;

    public SolarApocalypse() {
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onWorldTick(TickEvent.LevelTickEvent event) {
        if (!(event.level instanceof ServerLevel serverLevel)) return;

        long day = (serverLevel.getDayTime() / 24000L) + 1;
        List<ServerPlayer> players = serverLevel.players();

        for (ServerPlayer player : players) {
            int centerX = player.blockPosition().getX();
            int centerZ = player.blockPosition().getZ();

            // 草枯れ（2日目以降）
            if (day >= 2) {
                tryGrassDecay(serverLevel, centerX, centerZ, GRASS_ATTEMPTS_PER_PLAYER);
            }
            // 水蒸発（3日目以降）
            if (day >= 3) {
                tryWaterEvaporation(serverLevel, centerX, centerZ, WATER_ATTEMPTS_PER_PLAYER);
            }
            //木の発火
            if(day >= 4) {
                tryBurnWoodAndLeaves(serverLevel, centerX, centerZ, BURN_ATTEMPTS_PER_PLAYER); // ←追加
            }
            //終焉
            if (day >= 5) {
                igniteEntitiesUnderSun(serverLevel,players);
                trySandToGlass(serverLevel, centerX, centerZ, SAND_ATTEMPTS_PER_PLAYER);
            }
        }
    }

    private void tryGrassDecay(ServerLevel serverLevel, int centerX, int centerZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int x = centerX + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int z = centerZ + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int y = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z) - 1;
            BlockPos pos = new BlockPos(x, y, z);

            if (isGrassAndExposed(serverLevel, pos)) {
                serverLevel.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
            }
        }
    }

    private void tryWaterEvaporation(ServerLevel serverLevel, int centerX, int centerZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int x = centerX + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int z = centerZ + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int y1 = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);

            for (int y = y1 - 10; y <= y1 + 10; y++) {
                if (y < serverLevel.getMinBuildHeight() || y > serverLevel.getMaxBuildHeight()) continue;
                BlockPos pos = new BlockPos(x, y, z);
                if (isWaterAndExposed(serverLevel, pos)) {
                    // ここから半径2（直径5）で一気に蒸発
                    int RADIUS = 3; // 半径2なら3x3x3、半径3なら7x7x7で10ブロック超
                    for (int dx = -RADIUS; dx <= RADIUS; dx++) {
                        for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                            for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                                BlockPos near = pos.offset(dx, dy, dz);
                                BlockState state = serverLevel.getBlockState(near);
                                if (state.is(Blocks.WATER)
                                        || state.is(Blocks.KELP)
                                        || state.is(Blocks.KELP_PLANT)
                                        || state.is(Blocks.SEAGRASS)
                                        || state.is(Blocks.TALL_SEAGRASS)) {
                                    serverLevel.setBlockAndUpdate(near, Blocks.AIR.defaultBlockState());
                                    // 煙（SMOKE_NORMAL）、もしくはCLOUDでもOK
                                    serverLevel.sendParticles(
                                            ParticleTypes.CLOUD, // Particle type
                                            near.getX() + 0.5, near.getY() + 0.5, near.getZ() + 0.5, // 中心座標
                                            1, // 数（1回で5個）
                                            0.3, 0.2, 0.3, // 拡がり方
                                            0.01 // 速度
                                    );
                                    if (random.nextFloat() < 0.05f) { // 10%の確率
                                        serverLevel.playSound(
                                                null,
                                                near,
                                                SoundEvents.FIRE_EXTINGUISH,
                                                SoundSource.BLOCKS,
                                                0.1F,
                                                1.0F
                                        );
                                    }
                                }
                            }
                        }
                    }
                    // もはやreturnしてもいい（1箇所見つけたらその塊のみ蒸発して次へ）
                }
            }
        }
    }

    private boolean isGrassAndExposed(ServerLevel serverLevel, BlockPos pos) {
        if (!serverLevel.getBlockState(pos).is(Blocks.GRASS_BLOCK)) return false;
        for (int y2 = pos.getY() + 1; y2 < serverLevel.getMaxBuildHeight(); y2++) {
            if (!serverLevel.getBlockState(new BlockPos(pos.getX(), y2, pos.getZ())).isAir()) {
                return false;
            }
        }
        return true;
    }

    private boolean isWaterAndExposed(ServerLevel serverLevel, BlockPos pos) {
        if (!serverLevel.getBlockState(pos).is(Blocks.WATER)) return false;
        for (int y2 = pos.getY() + 1; y2 < serverLevel.getMaxBuildHeight(); y2++) {
            if (!serverLevel.getBlockState(new BlockPos(pos.getX(), y2, pos.getZ())).isAir()) {
                return false;
            }
        }
        return true;
    }
    private void tryBurnWoodAndLeaves(ServerLevel serverLevel, int centerX, int centerZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int x = centerX + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int z = centerZ + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int y1 = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            // y1付近の±10ブロックをチェック（地表・高所両方OK）
            for (int y = y1 - 10; y <= y1 + 10; y++) {
                if (y < serverLevel.getMinBuildHeight() || y > serverLevel.getMaxBuildHeight()) continue;
                BlockPos pos = new BlockPos(x, y, z);
                var blockState = serverLevel.getBlockState(pos);
                if (isLogOrLeaves(blockState) && isBlockExposed(serverLevel, pos)) {
                    // 確率で発火
                    if (random.nextFloat() < 0.05f) { // 5%
                        BlockPos above = pos.above();
                        if (serverLevel.getBlockState(above).isAir()) { // 空気なら火を置ける
                            serverLevel.setBlockAndUpdate(above, Blocks.FIRE.defaultBlockState());
                            // おまけで煙
                            serverLevel.sendParticles(
                                    net.minecraft.core.particles.ParticleTypes.SMOKE,
                                    pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                                    5, 0.2, 0.1, 0.2, 0.01
                            );
                            // おまけで燃焼音
                            if (random.nextFloat() < 0.05f) {
                                serverLevel.playSound(
                                        null, pos.above(),
                                        net.minecraft.sounds.SoundEvents.FIRE_AMBIENT,
                                        net.minecraft.sounds.SoundSource.BLOCKS,
                                        0.1F, 1.0F + (random.nextFloat() - 0.5F) * 0.2F
                                );
                            }
                        }
                    }
                }
            }
        }
    }

    // 木や葉判定
    private boolean isLogOrLeaves(BlockState blockState) {
        return blockState.is(net.minecraft.tags.BlockTags.LOGS) || blockState.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    // 上にブロックが無い判定（再利用）
    private boolean isBlockExposed(ServerLevel serverLevel, BlockPos pos) {
        for (int y2 = pos.getY() + 1; y2 < serverLevel.getMaxBuildHeight(); y2++) {
            if (!serverLevel.getBlockState(new BlockPos(pos.getX(), y2, pos.getZ())).isAir()) {
                return false;
            }
        }
        return true;
    }

    private void igniteEntitiesUnderSun(ServerLevel serverLevel,List<ServerPlayer> players) {
        int range = 4 * 16; // 4チャンク分
        for (ServerPlayer player : players) {
            BlockPos center = player.blockPosition();
            // 範囲AABBを作成
            net.minecraft.world.phys.AABB box = new net.minecraft.world.phys.AABB(
                    center.getX() - range, serverLevel.getMinBuildHeight(), center.getZ() - range,
                    center.getX() + range, serverLevel.getMaxBuildHeight(), center.getZ() + range
            );
            // この範囲のエンティティだけ取得
            for (Entity entity : serverLevel.getEntities(null, box)) {
                BlockPos pos = entity.blockPosition();
                if (isExposedToSky(serverLevel, pos)) {
                    entity.setSecondsOnFire(8);
                }
            }
        }
    }

    // 日光下判定
    private boolean isExposedToSky(ServerLevel level, BlockPos pos) {
        for (int y = pos.getY() + 1; y < level.getMaxBuildHeight(); y++) {
            if (!level.getBlockState(new BlockPos(pos.getX(), y, pos.getZ())).isAir()) {
                return false;
            }
        }
        return true;
    }

    private void trySandToGlass(ServerLevel serverLevel, int centerX, int centerZ, int attempts) {
        for (int i = 0; i < attempts; i++) {
            int x = centerX + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int z = centerZ + random.nextInt(CHUNK_RADIUS * 16 * 2 + 1) - CHUNK_RADIUS * 16;
            int y1 = serverLevel.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
            for (int y = y1 - 10; y <= y1 + 10; y++) {
                if (y < serverLevel.getMinBuildHeight() || y > serverLevel.getMaxBuildHeight()) continue;
                BlockPos pos = new BlockPos(x, y, z);
                if (serverLevel.getBlockState(pos).is(Blocks.SAND) && isExposedToSky(serverLevel, pos)) {
                    serverLevel.setBlockAndUpdate(pos, Blocks.GLASS.defaultBlockState());
                    // パーティクルで焼ける雰囲気
                    serverLevel.sendParticles(
                            net.minecraft.core.particles.ParticleTypes.CLOUD,
                            pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5,
                            6, 0.3, 0.1, 0.3, 0.01
                    );
                }
            }
        }
    }

    @SubscribeEvent
    public void onFluidPlace(BlockEvent.FluidPlaceBlockEvent event) {
        BlockState newState = event.getNewState();
        if (newState.getBlock() == Blocks.WATER || newState.getBlock() == Blocks.BUBBLE_COLUMN) {
            BlockPos pos = event.getPos();
            var level = event.getLevel();
            boolean noBlockAbove = true;
            for (int y2 = pos.getY() + 1; y2 < level.getMaxBuildHeight(); y2++) {
                if (!level.getBlockState(new BlockPos(pos.getX(), y2, pos.getZ())).isAir()) {
                    noBlockAbove = false;
                    break;
                }
            }
            // "水源"だけでなく"水流"もキャンセル
            if (noBlockAbove) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onCreateFluidSource(BlockEvent.CreateFluidSourceEvent event) {
        // ここでevent.getPos()とevent.getFluid()が取得できる
        // 地上かつ水ならキャンセル
        if (event.getState().getFluidState().is(Fluids.WATER)) {
            // 地表判定（上にブロックがない場合のみキャンセル）
            var level = event.getLevel();
            int y = event.getPos().getY();
            boolean noBlockAbove = true;
            for (int y2 = y + 1; y2 < level.getMaxBuildHeight(); y2++) {
                if (!level.getBlockState(new BlockPos(event.getPos().getX(), y2, event.getPos().getZ())).isAir()) {
                    noBlockAbove = false;
                    break;
                }
            }
            if (noBlockAbove) {
                event.setResult(Event.Result.DENY); // ←水源化を拒否
            }
        }
    }
}


