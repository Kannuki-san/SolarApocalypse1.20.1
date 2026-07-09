package com.kannuki_san.solarapocalypse.util;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.Tags;

public final class BlockTransformUtil {

    private static final TagKey<Block> FORGE_CACTI = forgeBlockTag("cacti");
    private static final TagKey<Block> FORGE_CROPS = forgeBlockTag("crops");
    private static final TagKey<Block> FORGE_FLOWERS = forgeBlockTag("flowers");
    private static final TagKey<Block> FORGE_GRASS = forgeBlockTag("grass");
    private static final TagKey<Block> FORGE_MUSHROOMS = forgeBlockTag("mushrooms");
    private static final TagKey<Block> FORGE_PLANTS = forgeBlockTag("plants");
    private static final TagKey<Block> FORGE_SAPLINGS = forgeBlockTag("saplings");
    private static final TagKey<Block> FORGE_SUGAR_CANE = forgeBlockTag("sugar_cane");
    private static final TagKey<Block> FORGE_TALL_GRASS = forgeBlockTag("tall_grass");
    private static final TagKey<Block> FORGE_VINES = forgeBlockTag("vines");

    public static BlockState grassSnowOrIceReplacement(BlockState state) {
        // Day 2系の変換先をここに集約して、後からconfig対応しやすくする。
        if (state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.MYCELIUM)
                || state.is(Blocks.DIRT_PATH)
                || state.is(Blocks.MOSS_BLOCK)) {
            return Blocks.DIRT.defaultBlockState();
        }
        if (isSurfacePlant(state)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (isAridSurfacePlant(state)) {
            return Blocks.AIR.defaultBlockState();
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

    public static boolean isSurfacePlant(BlockState state) {
        // 地表の草花は日射で少しずつ枯れて消える対象にする。
        return state.is(Blocks.GRASS)
                || state.is(Blocks.FERN)
                || state.is(Blocks.TALL_GRASS)
                || state.is(Blocks.LARGE_FERN)
                || state.is(Blocks.SWEET_BERRY_BUSH)
                || state.is(Blocks.PINK_PETALS)
                || state.is(BlockTags.FLOWERS)
                || state.is(BlockTags.SAPLINGS)
                || state.is(FORGE_CROPS)
                || state.is(FORGE_FLOWERS)
                || state.is(FORGE_GRASS)
                || state.is(FORGE_MUSHROOMS)
                || state.is(FORGE_PLANTS)
                || state.is(FORGE_SAPLINGS)
                || state.is(FORGE_TALL_GRASS)
                || state.is(FORGE_VINES);
    }

    public static boolean isAridSurfacePlant(BlockState state) {
        // サトウキビとサボテンは根元の砂ガラス化も絡むので、通常の草花とは分けて扱う。
        return state.is(Blocks.SUGAR_CANE)
                || state.is(Blocks.CACTUS)
                || state.is(FORGE_SUGAR_CANE)
                || state.is(FORGE_CACTI);
    }

    public static BlockState sandToGlassReplacement(BlockState state) {
        if (state.is(Tags.Blocks.SAND)) {
            return Blocks.GLASS.defaultBlockState();
        }
        return null;
    }

    public static boolean isCombustibleApocalypseTarget(BlockState state) {
        // 原木だけでなく、村に多い木材建材や干し草も燃焼対象に含める。
        return state.is(BlockTags.LOGS_THAT_BURN)
                || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.PLANKS)
                || state.is(BlockTags.WOODEN_STAIRS)
                || state.is(BlockTags.WOODEN_SLABS)
                || state.is(BlockTags.WOODEN_FENCES)
                || state.is(BlockTags.WOODEN_DOORS)
                || state.is(BlockTags.WOODEN_TRAPDOORS)
                || state.is(BlockTags.WOODEN_BUTTONS)
                || state.is(BlockTags.WOODEN_PRESSURE_PLATES)
                || state.is(BlockTags.FENCE_GATES)
                || state.is(BlockTags.ALL_SIGNS)
                || state.is(BlockTags.ALL_HANGING_SIGNS)
                || state.is(BlockTags.BAMBOO_BLOCKS)
                || state.is(BlockTags.WOOL)
                || state.is(BlockTags.WOOL_CARPETS)
                || state.is(Tags.Blocks.BARRELS_WOODEN)
                || state.is(Tags.Blocks.BOOKSHELVES)
                || state.is(Tags.Blocks.CHESTS_WOODEN)
                || state.is(Tags.Blocks.FENCE_GATES_WOODEN)
                || state.is(Blocks.HAY_BLOCK)
                || state.is(Blocks.BOOKSHELF)
                || state.is(Blocks.CHISELED_BOOKSHELF)
                || state.is(Blocks.LECTERN)
                || state.is(Blocks.COMPOSTER)
                || state.is(Blocks.BEEHIVE)
                || state.is(Blocks.BEE_NEST)
                || state.is(Blocks.SCAFFOLDING)
                || state.is(Blocks.BAMBOO)
                || state.is(Blocks.BAMBOO_SAPLING)
                || state.is(Blocks.LADDER);
    }

    public static boolean isWaterEvaporationOrigin(BlockState state) {
        // 水場を見つける起点は水源だけにし、水流だけの場所から処理が始まらないようにする。
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
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

    private static TagKey<Block> forgeBlockTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("forge", path));
    }

    private BlockTransformUtil() {
    }
}
