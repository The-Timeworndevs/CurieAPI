package net.timeworndevs.curieapi;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.api.ModInitializer;
import net.timeworndevs.curieapi.event.PlayerTickHandler;
import net.timeworndevs.curieapi.util.CurieAPIConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CurieAPI implements ModInitializer {
    public static final String MOD_ID = "curie-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Computing wave-functions...");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> CurieAPIConfig.readConfig());
        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        LOGGER.info("Wormhole established!");
    }
}