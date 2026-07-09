package com.kannuki_san.solarapocalypse.loot;

import com.kannuki_san.solarapocalypse.SolarApocalypse;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class SolarApocalypseLootModifiers {

    private static final DeferredRegister<Codec<? extends IGlobalLootModifier>> LOOT_MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SolarApocalypse.MODID);

    public static final RegistryObject<Codec<PreservationChestLootModifier>> PRESERVATION_CHEST_LOOT =
            LOOT_MODIFIERS.register("preservation_chest_loot", () -> PreservationChestLootModifier.CODEC);

    public static void register(IEventBus modEventBus) {
        // Global Loot ModifierのCodecをmodイベントバスへ登録する。
        LOOT_MODIFIERS.register(modEventBus);
    }

    private SolarApocalypseLootModifiers() {
    }
}
