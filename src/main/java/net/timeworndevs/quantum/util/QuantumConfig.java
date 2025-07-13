package net.timeworndevs.quantum.util;

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
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.radiation.RadiationType;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class QuantumConfig {
    public static HashMap<Block, HashMap<RadiationType, Integer>> BLOCK_RADIATION_VALUES = new HashMap<>();
    public static HashMap<Item, HashMap<RadiationType, Integer>> ITEM_RADIATION_VALUES = new HashMap<>();
    public static HashMap<String, HashMap<RadiationType, Integer>> BIOME_RADIATION_VALUES = new HashMap<>();
    public static HashMap<Block, HashMap<RadiationType, Integer>> INSULATORS = new HashMap<>();
    public static ArrayList<ArmorInsulator> ARMOR_INSULATORS = new ArrayList<>();
    public static int cap = 100000;
    public static int divConstant = 4;

    private static final Map<String, Consumer<JsonElement>> configHandlers = Map.of(
            "radiation_types", QuantumConfig::addRadiationTypes,
            "blocks", QuantumConfig::addBlocksToConfig,
            "biomes", QuantumConfig::addBiomesToConfig,
            "items", QuantumConfig::addItemsToConfig,
            "insulators", QuantumConfig::addInsulatorsToConfig,
            "armor", QuantumConfig::addArmorInsulatorsToConfig
    );
    public static void readConfig() {
        Path path = Paths.get(FabricLoader.getInstance().getConfigDir() + "/curie");
        try {
            if (!Files.exists(path)) {
                Files.createDirectory(path);
            }
        } catch (IOException e) {
            Quantum.LOGGER.error("Couldn't create directory: {}", path);
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
                                entry.getValue().accept(json.get(entry.getKey()));
                            }
                        }
                        // Changes the max cap for radiation.
                        if (json.has("cap") && cap == 100000) {
                            cap = json.get("cap").getAsInt();
                        }
                        // Changes the division constant for radiation.
                        if (json.has("div_constant") && divConstant == 4) {
                            divConstant = json.get("div_constant").getAsInt();
                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (IOException e) {
            Quantum.LOGGER.error("Couldn't read file: {}", path);
        }
    }

    public static ArmorInsulator findSetForItem(Item item) {
        return ARMOR_INSULATORS.stream()
                .filter(set -> set.containsItem(item))
                .findFirst()
                .orElse(null);
    }

    // Adds new radiation types from the config.
    private static void addRadiationTypes(JsonElement json) {
        for (JsonElement element: json.getAsJsonArray()) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            if (!RadiationType.RADIATION_TYPES.containsKey(name)) {
                JsonArray array = object.getAsJsonArray("color");
                ArrayList<Float> color = new ArrayList<>();
                for (int i = 0; i < Math.min(array.size(), 4); i++) {
                    color.add(i, array.get(i).getAsFloat());
                }
                RadiationType.register(name, color);
            } else {
                Quantum.LOGGER.warn("{} has already been registered!", name);
            }
        }
    }

    // Adds all radioactive blocks from the config.
    private static void addBlocksToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(entry.getKey()));

            if (block != Blocks.AIR) {
                mapRadiationTypes(object).forEach(radiationEntry ->
                        BLOCK_RADIATION_VALUES.computeIfAbsent(block, data -> new HashMap<>()).put(radiationEntry.getKey(), radiationEntry.getValue()));
            }
        }
    }

    // Adds all insulator blocks (negates radiation) from the config.
    private static void addInsulatorsToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(entry.getKey()));

            if (block != Blocks.AIR) {
                mapRadiationTypes(object).forEach(insulatorEntry ->
                        INSULATORS.computeIfAbsent(block, data -> new HashMap<>()).put(insulatorEntry.getKey(), insulatorEntry.getValue()));
            }
        }
    }

    // Adds all biomes that are radioactive to the config.
    private static void addBiomesToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();

            mapRadiationTypes(object).forEach(biomeEntry ->
                    BIOME_RADIATION_VALUES.computeIfAbsent(entry.getKey(), data -> new HashMap<>()).put(biomeEntry.getKey(), biomeEntry.getValue()));
        }
    }

    // Adds all radioactive items to the config.
    private static void addItemsToConfig(JsonElement json) {
        for (Map.Entry<String, JsonElement> entry : json.getAsJsonObject().entrySet()) {
            JsonObject object = entry.getValue().getAsJsonObject();
            Item item = Registries.ITEM.get(new Identifier(entry.getKey()));
            if (item != Items.AIR) {
                mapRadiationTypes(object).forEach(itemEntry ->
                        ITEM_RADIATION_VALUES.computeIfAbsent(item, data -> new HashMap<>()).put(itemEntry.getKey(), itemEntry.getValue()));
            }
        }
    }

    // Adds all armor items that can reduce radiation to the config.
    private static void addArmorInsulatorsToConfig(JsonElement json) {
        List<String> pieceList = List.of("helmet", "chestplate", "leggings", "boots");
        for (JsonElement element: json.getAsJsonArray()) {
            ArrayList<Float> values = new ArrayList<>();
            ArrayList<Item> armorItems = new ArrayList<>();
            HashMap<RadiationType, Integer> radiationValues = new HashMap<>();
            element.getAsJsonObject().entrySet().stream()
                    .filter(entry -> pieceList.contains(entry.getKey()))
                    .map(entry -> entry.getValue().getAsJsonObject())
                    .filter(object -> object.has("item") && object.has("multiplier"))
                    .forEach(object -> {
                        Item item = Registries.ITEM.get(new Identifier(object.get("item").getAsString()));
                        if (item != Items.AIR) {
                            values.add(object.get("multiplier").getAsFloat());
                            armorItems.add(item);
                        }
                    });
            if (!armorItems.isEmpty()) {
                mapRadiationTypes(element.getAsJsonObject()).forEach(entry -> radiationValues.put(entry.getKey(), entry.getValue()));
                ArmorInsulator.register(armorItems, values, radiationValues);
            }
        }
    }

    // Creates a map of all the valid radiation types and their values to be used by the other methods.
    private static Stream<Map.Entry<RadiationType, Integer>> mapRadiationTypes(JsonObject json) {
        return json.keySet().stream()
                .filter(key -> !key.equals("object"))
                .filter(type -> RadiationType.getRadiationType(type) != null)
                .map(key -> Map.entry(RadiationType.getRadiationType(key), json.get(key).getAsInt()));
    }
}