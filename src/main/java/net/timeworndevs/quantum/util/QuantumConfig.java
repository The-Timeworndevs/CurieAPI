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

    private static final Map<String, Consumer<JsonArray>> configHandlers = Map.of(
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
            Quantum.LOGGER.error("Couldn't create directory: " + path);
        }
        try (Stream<Path> stream = Files.list(path)) {
            stream.filter(Files::isRegularFile)
                .map(Path::toFile)
                .forEach(file -> {
                    JsonObject json;
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        json = JsonParser.parseReader(br).getAsJsonObject();
                        for (Map.Entry<String, Consumer<JsonArray>> entry : configHandlers.entrySet()) {
                            if (json.has(entry.getKey())) {
                                entry.getValue().accept(json.getAsJsonArray(entry.getKey()));
                            }
                        }
                        if (json.has("cap") && cap == 100000) {
                            cap = json.get("cap").getAsInt();
                        }
                        if (json.has("div_constant") && divConstant == 4) {
                            cap = json.get("div_constant").getAsInt();
                        }
                        ARMOR_INSULATORS.forEach(armorInsulator -> {
                            Quantum.LOGGER.info(String.valueOf(armorInsulator.armorItems()));
                        });
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
        } catch (IOException e) {
            Quantum.LOGGER.error("Couldn't read file: " + path);
        }
    }

    public static ArmorInsulator findSetForItem(Item item) {
        return ARMOR_INSULATORS.stream()
                .filter(set -> set.containsItem(item))
                .findFirst()
                .orElse(null);
    }

    private static void addRadiationTypes(JsonArray json) {
        for (JsonElement element: json) {
            JsonObject object = element.getAsJsonObject();
            String name = object.get("name").getAsString();
            if (!RadiationType.RADIATION_TYPES.containsKey(name)) {
                JsonArray array = object.getAsJsonArray("color");
                float[] color = new float[4];
                for (int i = 0; i < Math.min(array.size(), 4); i++) {
                    color[i] = array.get(i).getAsFloat();
                }
                RadiationType.register(name, color);
            } else {
                Quantum.LOGGER.warn(name + " has already been registered!");
            }
        }
    }
    private static void addBlocksToConfig(JsonArray json) {
        for (JsonElement element: json) {
            JsonObject object = element.getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(object.get("object").getAsString()));

            if (block != Blocks.AIR) {
                mapRadiationTypes(object).forEach(entry ->
                        BLOCK_RADIATION_VALUES.computeIfAbsent(block, data -> new HashMap<>()).put(entry.getKey(), entry.getValue()));
            }
        }
    }
    private static void addInsulatorsToConfig(JsonArray json) {
        for (JsonElement element: json) {
            JsonObject object = element.getAsJsonObject();
            Block block = Registries.BLOCK.get(new Identifier(object.get("object").getAsString()));

            if (block != Blocks.AIR) {
                mapRadiationTypes(object).forEach(entry ->
                        INSULATORS.computeIfAbsent(block, data -> new HashMap<>()).put(entry.getKey(), entry.getValue()));
            }
        }
    }
    private static void addBiomesToConfig(JsonArray json) {
        for (JsonElement element: json) {
            JsonObject object = element.getAsJsonObject();
            String biome = object.get("object").getAsString();

            mapRadiationTypes(object).forEach(entry ->
                    BIOME_RADIATION_VALUES.computeIfAbsent(biome, data -> new HashMap<>()).put(entry.getKey(), entry.getValue()));
        }
    }
    private static void addItemsToConfig(JsonArray json) {
        for (JsonElement element: json) {
            JsonObject object = element.getAsJsonObject();
            Item item = Registries.ITEM.get(new Identifier(object.get("object").getAsString()));
            if (item != Items.AIR) {
                mapRadiationTypes(object).forEach(entry ->
                        ITEM_RADIATION_VALUES.computeIfAbsent(item, data -> new HashMap<>()).put(entry.getKey(), entry.getValue()));
            }
        }
    }
    private static void addArmorInsulatorsToConfig(JsonArray json) {
        List<String> pieceList = List.of("helmet", "chestplate", "leggings", "boots");
        for (JsonElement element: json) {
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
                ARMOR_INSULATORS.add(new ArmorInsulator(armorItems, values, radiationValues));
            }
        }
    }

    private static Stream<Map.Entry<RadiationType, Integer>> mapRadiationTypes(JsonObject json) {
        return json.keySet().stream()
                .filter(key -> !key.equals("object"))
                .filter(type -> RadiationType.getRadiationType(type) != null)
                .map(key -> Map.entry(RadiationType.getRadiationType(key), json.get(key).getAsInt()));
    }
}