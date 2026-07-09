package com.kannuki_san.solarapocalypse.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class SolarApocalypseConfig {

    public static final ForgeConfigSpec COMMON_SPEC;

    public static final ForgeConfigSpec.IntValue GRASS_DECAY_DAY;
    public static final ForgeConfigSpec.IntValue WATER_EVAPORATION_DAY;
    public static final ForgeConfigSpec.IntValue FIRE_DAY;
    public static final ForgeConfigSpec.IntValue ENTITY_BURN_DAY;
    public static final ForgeConfigSpec.IntValue SAND_TO_GLASS_DAY;

    public static final ForgeConfigSpec.IntValue CHUNK_RADIUS;
    public static final ForgeConfigSpec.IntValue ENTITY_BURN_RADIUS_CHUNKS;
    public static final ForgeConfigSpec.IntValue MAX_CHUNKS_PER_TICK;
    public static final ForgeConfigSpec.IntValue RANDOM_ATTEMPTS_PER_CHUNK;
    public static final ForgeConfigSpec.IntValue MAX_BLOCK_CHANGES_PER_TICK;
    public static final ForgeConfigSpec.IntValue MAX_FIRE_PLACEMENTS_PER_TICK;
    public static final ForgeConfigSpec.IntValue WATER_SCAN_DEPTH;
    public static final ForgeConfigSpec.IntValue MIN_WATER_CLUSTER_RADIUS;
    public static final ForgeConfigSpec.IntValue MAX_WATER_CLUSTER_RADIUS;

    public static final ForgeConfigSpec.DoubleValue EVAPORATION_SOUND_VOLUME;
    public static final ForgeConfigSpec.DoubleValue EVAPORATION_SOUND_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ICE_TURNS_TO_WATER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("stage_days");
        GRASS_DECAY_DAY = builder.comment(
                        "Day when grass, snow, and ice start breaking down.",
                        "草、雪、氷が壊れ始める日数。")
                .defineInRange("grassDecayDay", 2, 1, 365000);
        WATER_EVAPORATION_DAY = builder.comment(
                        "Day when exposed water starts evaporating.",
                        "空にさらされた水が蒸発し始める日数。")
                .defineInRange("waterEvaporationDay", 3, 1, 365000);
        FIRE_DAY = builder.comment(
                        "Day when exposed trees and leaves start burning away.",
                        "空にさらされた木や葉が燃え始める日数。")
                .defineInRange("fireDay", 4, 1, 365000);
        ENTITY_BURN_DAY = builder.comment(
                        "Day when exposed entities are set on fire.",
                        "空にさらされたエンティティが燃え始める日数。")
                .defineInRange("entityBurnDay", 5, 1, 365000);
        SAND_TO_GLASS_DAY = builder.comment(
                        "Day when exposed sand turns into glass.",
                        "空にさらされた砂がガラス化し始める日数。")
                .defineInRange("sandToGlassDay", 5, 1, 365000);
        builder.pop();

        builder.push("processing");
        CHUNK_RADIUS = builder.comment(
                        "Upper limit for random processing radius. The active server simulation distance is still used as the real cap.",
                        "ランダム処理範囲の上限。実際の上限にはサーバーの演算距離も使われます。")
                .defineInRange("maxProcessingChunkRadius", 32, 1, 32);
        ENTITY_BURN_RADIUS_CHUNKS = builder.comment(
                        "Radius, in chunks, around each player to scan for exposed entities.",
                        "空にさらされたエンティティを探す、各プレイヤー周囲の半径チャンク数。")
                .defineInRange("entityBurnRadiusChunks", 4, 1, 32);
        MAX_CHUNKS_PER_TICK = builder.comment(
                        "Random chunks selected for apocalypse processing each server tick.",
                        "1サーバーtickごとに終末処理へ選ばれるランダムチャンク数。")
                .defineInRange("maxChunksPerTick", 4, 1, 64);
        RANDOM_ATTEMPTS_PER_CHUNK = builder.comment(
                        "Random block positions tried inside each selected chunk.",
                        "選ばれた各チャンク内で試行するランダムブロック位置の数。")
                .defineInRange("randomAttemptsPerChunk", 8, 1, 128);
        MAX_BLOCK_CHANGES_PER_TICK = builder.comment(
                        "Maximum block changes caused by apocalypse processing per tick.",
                        "終末処理が1tickに変更できるブロック数の上限。")
                .defineInRange("maxBlockChangesPerTick", 8, 1, 4096);
        MAX_FIRE_PLACEMENTS_PER_TICK = builder.comment(
                        "Maximum fire blocks placed per tick.",
                        "1tickに設置できる火ブロック数の上限。")
                .defineInRange("maxRandomFirePlacementsPerTick", 3, 0, 1024);
        WATER_SCAN_DEPTH = builder.comment(
                        "How far below the surface each column scans for exposed water over time.",
                        "各柱状範囲で、地表からどれだけ下まで空にさらされた水を探すか。")
                .defineInRange("waterScanDepth", 80, 1, 384);
        MIN_WATER_CLUSTER_RADIUS = builder.comment(
                        "Minimum radius around a found water block to evaporate as a small cluster.",
                        "見つけた水ブロックの周囲を小さな塊として蒸発させる最小半径。")
                .defineInRange("minWaterClusterRadius", 3, 0, 8);
        MAX_WATER_CLUSTER_RADIUS = builder.comment(
                        "Maximum radius around a found water block to evaporate as a small cluster.",
                        "見つけた水ブロックの周囲を小さな塊として蒸発させる最大半径。")
                .defineInRange("maxWaterClusterRadius", 5, 0, 8);
        builder.pop();

        builder.push("effects");
        EVAPORATION_SOUND_VOLUME = builder.comment(
                        "Volume for occasional water evaporation sounds.",
                        "水の蒸発時に低確率で鳴る効果音の音量。")
                .defineInRange("evaporationSoundVolume", 0.1D, 0.0D, 2.0D);
        EVAPORATION_SOUND_CHANCE = builder.comment(
                        "Chance per evaporated block to play an evaporation sound.",
                        "蒸発したブロックごとに効果音を鳴らす確率。")
                .defineInRange("evaporationSoundChance", 0.05D, 0.0D, 1.0D);
        ICE_TURNS_TO_WATER = builder.comment(
                        "If true, exposed ice becomes water instead of disappearing.",
                        "trueの場合、空にさらされた氷は消える代わりに水になります。")
                .define("iceTurnsToWater", false);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private SolarApocalypseConfig() {
    }
}
