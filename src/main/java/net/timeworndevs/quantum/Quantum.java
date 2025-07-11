package net.timeworndevs.quantum;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.timeworndevs.quantum.event.PlayerConnectionHelper;
import net.timeworndevs.quantum.event.PlayerDisconnectionHelper;

import net.fabricmc.api.ModInitializer;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.util.QuantumConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Quantum implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "quantum";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Computing wave-functions...");
        QuantumConfig.readConfig();

        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        ServerPlayConnectionEvents.JOIN.register(new PlayerConnectionHelper());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerDisconnectionHelper());
        LOGGER.info("Wormhole established!");
    }
}