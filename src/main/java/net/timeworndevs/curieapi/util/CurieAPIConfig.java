package net.timeworndevs.curieapi.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.timeworndevs.curieapi.CurieAPI;
import net.timeworndevs.curieapi.radiation.RadiationEntry;
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CurieAPIConfig {
    public static LinkedHashMap<String, RadiationType> RADIATION_TYPES = new LinkedHashMap<>();
    public static Map<Block, RadiationEntry> BLOCK_RADIATION_VALUES = new HashMap<>();
    public static Map<Item, RadiationEntry> ITEM_RADIATION_VALUES = new HashMap<>();
    public static Map<String, RadiationEntry> BIOME_RADIATION_VALUES = new HashMap<>();
    public static Map<Block, RadiationEntry> INSULATORS = new HashMap<>();
    public static ArrayList<ArmorInsulator> ARMOR_INSULATORS = new ArrayList<>();
    private static final int defaultCap = 100000;
    private static final int defaultDivConstant = 4;
    private static final int defaultMaxItemIntake = 100;
    private static final int defaultMaxBlockIntake = 100;
    private static final int[] defaultPassiveDecay = new int[]{1,8};

    public static int CAP = defaultCap;
    public static int DIV_CONSTANT = defaultDivConstant;
    public static int MAX_ITEM_INTAKE = defaultMaxItemIntake;
    public static int MAX_BLOCK_INTAKE = defaultMaxBlockIntake;
    public static int[] PASSIVE_DECAY = defaultPassiveDecay;

    private static final Map<String, Consumer<JsonElement>> configHandlers = Map.of(
            "radiation_types", CurieAPIConfig::addRadiationTypes,
            "blocks", CurieAPIConfig::addBlocksToConfig,
            "biomes", CurieAPIConfig::addBiomesToConfig,
            "items", CurieAPIConfig::addItemsToConfig,
            "insulators", CurieAPIConfig::addInsulatorsToConfig,
            "armor", CurieAPIConfig::addArmorInsulatorsToConfig
    );
    public static void readConfig() {
        Path path = Paths.get(FabricLoader.getInstance().getConfigDir() + "/curie");
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            CurieAPI.LOGGER.error("Couldn't create directory: {}", path);
        }
        try (Stream<Path> stream = Files.list(path)) {
            stream.filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(file -> {
                    JsonObject json;
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        json = JsonParser.parseReader(br).getAsJsonObject();
                        // Loops through all possible config options and then extracts their values.
                        for (Map.Entry<String, Consumer<JsonElement>> entry : configHandlers.entrySet()) {
                            if (json.has(entry.getKey())) {
                                CurieAPI.LOGGER.info("Loaded config: {}", entry.getKey());
                                entry.getValue().accept(json.get(entry.getKey()));
                            }
                        }
                        // Changes the max CAP for radiation.
                        if (json.has("cap") && CAP == defaultCap) {
                            CAP = json.get("cap").getAsInt();
                        }
                        // Changes the division constant for radiation.
                        if (json.has("div_constant") && DIV_CONSTANT == defaultDivConstant) {
                            DIV_CONSTANT = json.get("div_constant").getAsInt();
                        }
                        if (json.has("max_block_intake") && MAX_BLOCK_INTAKE == defaultMaxBlockIntake) {
                            MAX_BLOCK_INTAKE = json.get("max_block_intake").getAsInt();
                        }
                        if (json.has("max_item_intake") && MAX_ITEM_INTAKE == defaultMaxItemIntake) {
                            MAX_ITEM_INTAKE = json.get("max_item_intake").getAsInt();
                        }
                        if (json.has("passive_radiation_decay") && PASSIVE_DECAY == defaultPassiveDecay) {
                            JsonArray array = json.getAsJsonArray("passive_radiation_decay");
                            PASSIVE_DECAY = new int[] {array.get(0).getAsInt(), array.get(1).getAsInt()};
                        }

                    } catch (IOException e) {
                        CurieAPI.LOGGER.error("Couldn't read file: {}", path);
                    }
                });
        } catch (IOException e) {
            CurieAPI.LOGGER.error("Couldn't read file: {}", path);
        }
    }

    // Adds new radiation LOADED_TYPES from the config.
    private static void addRadiationTypes(JsonElement json) {
        for (JsonElement element: json.getAsJsonArray()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();

            RADIATION_TYPES.computeIfAbsent(name, radiationType -> {
                JsonArray array = object.getAsJsonArray("color");
                ArrayList<Float> color = new ArrayList<>();
                for (int i = 0; i < Math.min(array.size(), 4); i++) {
                    color.add(i, array.get(i).getAsFloat());
                }
                return new RadiationType(name, color);
            });
        }
    }

    // Adds all radioactive blocks from the config.
    private static void addBlocksToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(entry.getKey()));
            if (block != Blocks.AIR) {
                BLOCK_RADIATION_VALUES.computeIfAbsent(block, data -> mapRadiationTypes(object));
            }
        }
    }

    // Adds all insulator blocks (negates radiation) from the config.
    private static void addInsulatorsToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(entry.getKey()));

            if (block != Blocks.AIR) {
                INSULATORS.computeIfAbsent(block, data -> mapRadiationTypes(object));
            }
        }
    }

    // Adds all biomes that are radioactive to the config.
    private static void addBiomesToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();

            BIOME_RADIATION_VALUES.computeIfAbsent(entry.getKey(), data -> mapRadiationTypes(object));
        }
    }

    // Adds all radioactive items to the config.
    private static void addItemsToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Item item = Registries.ITEM.get(new Identifier(entry.getKey()));
            if (item != Items.AIR) {
                ITEM_RADIATION_VALUES.computeIfAbsent(item, data -> mapRadiationTypes(object));
            }
        }
    }

    // Adds all armor items that can reduce radiation to the config.
    private static void addArmorInsulatorsToConfig(JsonElement json) {
        for (JsonElement element: json.getAsJsonArray()) {
            Map<Item, Float> armorValues = new HashMap<>();
            JsonObject object = element.getAsJsonObject();
            if (object.has("armor") && object.has("radiation")) {
                for (Map.Entry<String, JsonElement> entry : object.getAsJsonObject("armor").entrySet()) {
                    Item item = Registries.ITEM.get(new Identifier(entry.getKey()));
                    if (item != Items.AIR) {
                        armorValues.put(item, entry.getValue().getAsFloat());
                    }
                }

                if (!armorValues.isEmpty()) {
                    RadiationEntry entry = mapRadiationTypes(object.getAsJsonObject("radiation"));
                    ArmorInsulator.register(armorValues, entry);
                }
            }
        }
    }

    // Creates a map of all the valid radiation LOADED_TYPES and their values to be used by the other methods.
    private static RadiationEntry mapRadiationTypes(JsonObject json) {
        return new RadiationEntry(json.keySet().stream()
                .filter(type -> RadiationType.getRadiationType(type) != null)
                .map(key -> Map.entry(RadiationType.getRadiationType(key), json.get(key).getAsFloat()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }
}