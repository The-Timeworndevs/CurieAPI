package net.timeworndevs.quantum;

import com.google.gson.*;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.timeworndevs.quantum.block.ModBlocks;
import net.timeworndevs.quantum.event.KeyInputHandler;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.item.ModItems;
import net.timeworndevs.quantum.networking.ModMessages;
import net.timeworndevs.quantum.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.ParseJson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.util.*;

import static net.minecraft.server.command.CommandManager.*;

import java.util.function.BiConsumer;
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

    public static final ItemGroup RADIATION = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModItems.GEIGER_COUNTER))
            .displayName(Text.translatable("itemGroup.quantum.radiation"))
            .entries((context, entries) -> {
            })
            .build();
    public static final ItemGroup BUILD_BLOCKS = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.NUCLEAR_WASTE))
            .displayName(Text.translatable("itemGroup.quantum.building_blocks"))
            .entries((context, entries) -> {
            })
            .build();
    public static final ItemGroup QUANTUM = FabricItemGroup.builder()
            .icon(() -> new ItemStack(ModBlocks.NUCLEAR_WASTE))
            .displayName(Text.translatable("itemGroup.quantum.quantum"))
            .entries((context, entries) -> {
            })
            .build();


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
    public static HashMap<String, JsonObject> radiation_data;
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
        LOGGER.error(String.valueOf(files));

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
                if (null==radiation_data) {
                    radiation_data =new HashMap<>();
                }
                radiation_data.put(file.getName(), json);

            }
        }

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("clearrad")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    // For versions below 1.19, replace "Text.literal" with "new LiteralText".
                    // For versions below 1.20, remode "() ->" directly.
                    context.getSource().sendFeedback(() -> Text.literal("Clearing.."), true);
                    ClientPlayNetworking.send(ModMessages.CLEAR_ALPHA_ID, PacketByteBufs.create());
                    ClientPlayNetworking.send(ModMessages.CLEAR_BETA_ID, PacketByteBufs.create());
                    ClientPlayNetworking.send(ModMessages.CLEAR_GAMMA_ID, PacketByteBufs.create());
                    return 1;
                })));
        /*ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener((new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier("tutorial", "my_resources");
            }


            @Override
            public void reload(ResourceManager manager) {
                Map<Identifier, Resource> data = manager.findResources("radiation_data", path -> path.toString().endsWith(".json"));

                BiConsumer<Identifier, Resource> read = (i, resource) -> {
                    try(InputStream stream = manager.getResource(i).get().getInputStream()) {
                        Scanner s = new Scanner(stream).useDelimiter("\\A");
                        String result = s.hasNext() ? s.next() : "";

                        radiation_data = ParseJson.parseJson(result);

                    } catch(Exception e) {

                        LOGGER.error("Error occurred while loading resource json" + i.toString(), e);
                    }
                };
                data.forEach(read);
            }
        }));*/

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.
        LOGGER.info("Computing wave-functions...");
        ModBlocks.registerBlocks();
        ModBlocks.registerBlockItems();
        LOGGER.info("Analyzing external dimensions...");
        ModScreenHandlers.registerScreenHandlers();
        ModItems.registerItems();
        //ModRecipes.registerRecipes();
        LOGGER.info("Testing radiation...");
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "radiation"), RADIATION);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "building_blocks"), BUILD_BLOCKS);
        Registry.register(Registries.ITEM_GROUP, new Identifier(MOD_ID, "quantum"), QUANTUM);
        ModMessages.registerC2SPackets();
        KeyInputHandler.register();
        ServerTickEvents.START_SERVER_TICK.register(new PlayerTickHandler());
        LOGGER.info("Wormhole established!");
    }
}