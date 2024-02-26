package net.timeworndevs.quantumui;

import com.google.gson.JsonArray;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.*;

import java.util.function.BiConsumer;

public class Quantum implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "quantumui";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier locate(String name) {
        return new Identifier(MOD_ID, name);
    }



    /*public Identifier getFabricId() {
        return new Identifier(Quantum.MOD_ID, "radiation_data");
    }
    public void reload(ResourceManager manager) {
        Map<Identifier, Resource> data = manager.findResources("radiation_data", path -> path.toString().endsWith(".json"));

        BiConsumer<Identifier, Resource> read = (i, resource) -> {
            Quantum.LOGGER.info(i + resource.toString());
        };
        data.forEach(read);
    }*/
    public static Map<String, JsonArray> radiation_data;

    @Override
    public void onInitialize() {



        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Computing wave-functions...");

        //ModRecipes.registerRecipes();
        LOGGER.info("Testing radiation...");

        LOGGER.info("Wormhole established!");
    }
}