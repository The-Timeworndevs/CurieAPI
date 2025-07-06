package net.timeworndevs.quantum;

import com.google.gson.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.timeworndevs.quantum.event.PlayerConnectionHelper;
import net.timeworndevs.quantum.event.PlayerDisconnectionHelper;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.networking.ModMessages;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Quantum implements ModInitializer {
    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final String MOD_ID = "quantum";
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

    public static HashMap<String, JsonObject> radiation_data = new HashMap<>();
    public static HashMap<String, String> new_radiation_types = new HashMap<>();
    public static int cap = 100000;
    public static int div_constant = 4;
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Override
    public void onInitialize() {
        Set<String> files;
        try {
            Files.createDirectories(Paths.get(FabricLoader.getInstance().getConfigDir() + "/curie"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try (Stream<Path> stream = Files.list(Paths.get(FabricLoader.getInstance().getConfigDir() + "/curie"))) {
            files = stream.filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        LOGGER.warn("Loading Curie configuration from files: {}", files);

        for (int i=0; i<files.size(); i++) {
            File file = new File(FabricLoader.getInstance().getConfigDir() + "/curie/" + files.toArray()[i]);
            if (file.exists()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                JsonObject json = JsonParser.parseReader(br).getAsJsonObject();

                if (json.has("radiation_types")) {
                    for (JsonElement ele: json.get("radiation_types").getAsJsonArray()) {
                        new_radiation_types.put(ele.getAsJsonObject().get("name").getAsString(), ele.getAsJsonObject().get("color").getAsString());
                    }
                }
            }
        }

        for (String i: new_radiation_types.keySet()) {
            ModMessages.NEW_RADIATIONS_SYNC_ID.put(new Identifier(Quantum.MOD_ID, "radiation_"+i+"_sync"), new ModMessages.NewSyncPackage(i));

        }


        for (int i=0; i<files.size(); i++) {
            File file =  new File(FabricLoader.getInstance().getConfigDir() + "/curie/" + files.toArray()[i]);
            if (file.exists()) {
                BufferedReader br = null;
                try {
                    br = new BufferedReader(new FileReader(file));
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }
                JsonObject json = JsonParser.parseReader(br).getAsJsonObject();
                radiation_data.put(file.getName(), json);

            }
        }

        for (String key: Quantum.radiation_data.keySet()) {
            JsonObject rad = Quantum.radiation_data.get(key);
            if (rad.has("cap")) {
                if (cap != 100000) {
                    LOGGER.warn("Radiation Cap defined multiple times.");
                }
                cap = rad.get("cap").getAsInt();
            }
            if (rad.has("div_constant")) {
                if (div_constant != 4) {
                    LOGGER.warn("Div Constant was defined multiple times.");
                }
                div_constant = rad.get("div_constant").getAsInt();
            }
        }
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Computing wave-functions...");

        //ModRecipes.registerRecipes();
        LOGGER.info("Testing radiation...");

        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        ServerPlayConnectionEvents.JOIN.register(new PlayerConnectionHelper());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerDisconnectionHelper());
        LOGGER.info("Wormhole established!");
    }
}