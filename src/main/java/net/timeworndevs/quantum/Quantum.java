package net.timeworndevs.quantum;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;

import net.fabricmc.api.ModInitializer;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.util.QuantumConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quantum implements ModInitializer {
    public static final String MOD_ID = "quantum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        LOGGER.info("Computing wave-functions...");

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            QuantumConfig.readConfig();
        });
        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        LOGGER.info("Wormhole established!");
    }
}