package net.timeworndevs.curieapi;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.api.ModInitializer;
import net.timeworndevs.curieapi.event.PlayerTickHandler;
import net.timeworndevs.curieapi.radiation.RadiationType;
import net.timeworndevs.curieapi.util.CurieAPIConfig;
import net.timeworndevs.curieapi.util.PlayerCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

import static net.timeworndevs.curieapi.util.CurieAPIConfig.RADIATION_TYPES;

public class CurieAPI implements ModInitializer {
    public static final String MOD_ID = "curie-api";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static WeakHashMap<UUID, PlayerCache> CACHE;
    public static List<RadiationType> LOADED_TYPES;
    @Override
    public void onInitialize() {
        LOGGER.info("Computing wave-functions...");

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            LOGGER.info("reading config...");
            CurieAPIConfig.readConfig();
            LOADED_TYPES = new ArrayList<>(RADIATION_TYPES.values());
            CACHE = new WeakHashMap<>();
                });

        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        LOGGER.info("Wormhole established!");
    }
}