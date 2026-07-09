package com.kannuki_san.solarapocalypse;

import com.kannuki_san.solarapocalypse.config.SolarApocalypseConfig;
import com.kannuki_san.solarapocalypse.event.ApocalypseTickHandler;
import com.kannuki_san.solarapocalypse.event.FluidEventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(SolarApocalypse.MODID)
public class SolarApocalypse {

    public static final String MODID = "solarapocalypse";

    public SolarApocalypse(FMLJavaModLoadingContext context) {
        // MOD本体は薄く保ち、設定とイベント処理だけ登録する。
        context.registerConfig(ModConfig.Type.SERVER, SolarApocalypseConfig.SERVER_SPEC);
        MinecraftForge.EVENT_BUS.register(new ApocalypseTickHandler());
        MinecraftForge.EVENT_BUS.register(new FluidEventHandler());
    }
}
