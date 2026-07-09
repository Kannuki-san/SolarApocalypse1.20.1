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
    public static final ForgeConfigSpec.IntValue WATER_CLUSTER_RADIUS;
    public static final ForgeConfigSpec.IntValue MIN_WATER_CLUSTER_CHANGES;
    public static final ForgeConfigSpec.IntValue MAX_WATER_CLUSTER_CHANGES;

    public static final ForgeConfigSpec.DoubleValue EVAPORATION_SOUND_VOLUME;
    public static final ForgeConfigSpec.DoubleValue EVAPORATION_SOUND_CHANCE;
    public static final ForgeConfigSpec.BooleanValue ICE_TURNS_TO_WATER;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.push("stage_days");
        GRASS_DECAY_DAY = builder.comment("Day when grass, snow, and ice start breaking down.")
                .defineInRange("grassDecayDay", 2, 1, 365000);
        WATER_EVAPORATION_DAY = builder.comment("Day when exposed water starts evaporating.")
                .defineInRange("waterEvaporationDay", 3, 1, 365000);
        FIRE_DAY = builder.comment("Day when exposed trees and leaves start burning away.")
                .defineInRange("fireDay", 4, 1, 365000);
        ENTITY_BURN_DAY = builder.comment("Day when exposed entities are set on fire.")
                .defineInRange("entityBurnDay", 5, 1, 365000);
        SAND_TO_GLASS_DAY = builder.comment("Day when exposed sand turns into glass.")
                .defineInRange("sandToGlassDay", 5, 1, 365000);
        builder.pop();

        builder.push("processing");
        CHUNK_RADIUS = builder.comment("Upper limit for random processing radius. The active server simulation distance is still used as the real cap.")
                .defineInRange("maxProcessingChunkRadius", 32, 1, 32);
        ENTITY_BURN_RADIUS_CHUNKS = builder.comment("Radius, in chunks, around each player to scan for exposed entities.")
                .defineInRange("entityBurnRadiusChunks", 4, 1, 32);
        MAX_CHUNKS_PER_TICK = builder.comment("Random chunks selected for apocalypse processing each server tick.")
                .defineInRange("maxChunksPerTick", 4, 1, 64);
        RANDOM_ATTEMPTS_PER_CHUNK = builder.comment("Random block positions tried inside each selected chunk.")
                .defineInRange("randomAttemptsPerChunk", 8, 1, 128);
        MAX_BLOCK_CHANGES_PER_TICK = builder.comment("Maximum block changes caused by apocalypse processing per tick.")
                .defineInRange("maxBlockChangesPerTick", 6, 1, 4096);
        MAX_FIRE_PLACEMENTS_PER_TICK = builder.comment("Maximum fire blocks placed per tick.")
                .defineInRange("maxRandomFirePlacementsPerTick", 3, 0, 1024);
        WATER_SCAN_DEPTH = builder.comment("How far below the surface each column scans for exposed water over time.")
                .defineInRange("waterScanDepth", 80, 1, 384);
        WATER_CLUSTER_RADIUS = builder.comment("Radius around a found water block to evaporate as a small cluster.")
                .defineInRange("waterClusterRadius", 1, 0, 4);
        MIN_WATER_CLUSTER_CHANGES = builder.comment("Minimum water blocks to try removing from one found cluster.")
                .defineInRange("minWaterClusterChanges", 2, 1, 64);
        MAX_WATER_CLUSTER_CHANGES = builder.comment("Maximum water blocks removed from one found cluster.")
                .defineInRange("maxRandomWaterClusterChanges", 5, 1, 64);
        builder.pop();

        builder.push("effects");
        EVAPORATION_SOUND_VOLUME = builder.comment("Volume for occasional water evaporation sounds.")
                .defineInRange("evaporationSoundVolume", 0.1D, 0.0D, 2.0D);
        EVAPORATION_SOUND_CHANCE = builder.comment("Chance per evaporated block to play an evaporation sound.")
                .defineInRange("evaporationSoundChance", 0.05D, 0.0D, 1.0D);
        ICE_TURNS_TO_WATER = builder.comment("If true, exposed ice becomes water instead of disappearing.")
                .define("iceTurnsToWater", false);
        builder.pop();

        COMMON_SPEC = builder.build();
    }

    private SolarApocalypseConfig() {
    }
}
