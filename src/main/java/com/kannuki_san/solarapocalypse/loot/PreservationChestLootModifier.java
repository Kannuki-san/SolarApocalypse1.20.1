package com.kannuki_san.solarapocalypse.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public final class PreservationChestLootModifier extends LootModifier {

    public static final Codec<PreservationChestLootModifier> CODEC = RecordCodecBuilder.create(instance ->
            codecStart(instance).apply(instance, PreservationChestLootModifier::new));

    private static final List<ResourceLocation> TARGET_TABLES = List.of(
            BuiltInLootTables.ABANDONED_MINESHAFT,
            BuiltInLootTables.SIMPLE_DUNGEON,
            BuiltInLootTables.STRONGHOLD_CORRIDOR,
            BuiltInLootTables.STRONGHOLD_CROSSING,
            BuiltInLootTables.STRONGHOLD_LIBRARY,
            BuiltInLootTables.ANCIENT_CITY
    );

    private static final List<Item> PRESERVATION_ITEMS = List.of(
            Items.VINE,
            Items.OAK_SAPLING,
            Items.SPRUCE_SAPLING,
            Items.BIRCH_SAPLING,
            Items.JUNGLE_SAPLING,
            Items.ACACIA_SAPLING,
            Items.DARK_OAK_SAPLING,
            Items.CHERRY_SAPLING,
            Items.MANGROVE_PROPAGULE,
            Items.BAMBOO,
            Items.SUGAR_CANE,
            Items.CACTUS,
            Items.LILY_PAD,
            Items.KELP,
            Items.SEAGRASS,
            Items.SEA_PICKLE,
            Items.TUBE_CORAL,
            Items.BRAIN_CORAL,
            Items.BUBBLE_CORAL,
            Items.FIRE_CORAL,
            Items.HORN_CORAL,
            Items.TUBE_CORAL_FAN,
            Items.BRAIN_CORAL_FAN,
            Items.BUBBLE_CORAL_FAN,
            Items.FIRE_CORAL_FAN,
            Items.HORN_CORAL_FAN
    );

    public PreservationChestLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!TARGET_TABLES.contains(context.getQueriedLootTableId())) {
            return generatedLoot;
        }

        RandomSource random = context.getRandom();
        List<Item> candidates = new ArrayList<>(PRESERVATION_ITEMS);
        int itemTypes = 1 + random.nextInt(3);
        for (int i = 0; i < itemTypes && i < candidates.size(); i++) {
            // 地表壊滅後の救済なので、少量だけ混ぜて探索報酬の雰囲気を残す。
            Item item = candidates.remove(random.nextInt(candidates.size()));
            generatedLoot.add(new ItemStack(item, 1 + random.nextInt(5)));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return SolarApocalypseLootModifiers.PRESERVATION_CHEST_LOOT.get();
    }
}
