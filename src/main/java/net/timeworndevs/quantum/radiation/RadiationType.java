package net.timeworndevs.quantum.radiation;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.QuantumConfig;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RadiationType {
    // Stores all the radiation types found (config, other mods, etc.).
    public static HashMap<String, RadiationType> RADIATION_TYPES = new HashMap<>();

    private final String name;
    private final List<Float> color;
    private final Identifier syncID;
    private final RadiationPacket syncPacket;

    // Creates a new radiation type.
    public RadiationType(String name, List<Float> color) {
        this.name = name;
        this.color = color;
        this.syncID = new Identifier(Quantum.MOD_ID, "radiation_" + name + "_sync");
        this.syncPacket = new RadiationPacket(this, this.syncID);
    }
    // Checks if there is a radiation type with that name.
    public static RadiationType getRadiationType(String name) {
        return RADIATION_TYPES.get(name);
    }

    // Gets the color of the radiation type.
    public List<Float> getColor() {
        return this.color;
    }

    // Gets the name of the radiation type.
    public String getName() {
        return this.name;
    }

    // Gets the syncing identifier of the radiation type.
    public Identifier getSyncID() {
        return this.syncID;
    }

    // Gets the server packet of the radiation type.
    public RadiationPacket getSyncPacket() {
        return this.syncPacket;
    }

    // Registers a new radiation type.
    public static RadiationType register(String name, List<Float> color) {
        RadiationType type = new RadiationType(name, color);
        RADIATION_TYPES.putIfAbsent(name, type);
        return type;
    }

    public static class RadiationPacket {
        public static ArrayList<RadiationPacket> PACKETS = new ArrayList<>();

        private final RadiationType radiationType;
        private final Identifier id;
        // Creates a new packet that is used to sync the values of radiation.
        public RadiationPacket(RadiationType type, Identifier id) {
            this.radiationType = type;
            this.id = id;
            PACKETS.add(this);
        }
        // Registers all the packets when the game is ready.
        public static void registerPackets() {
            for (RadiationPacket packet : PACKETS) {
                packet.registerPacket();
            }
        }
        // Registers an individual packet.
        private void registerPacket() {
            ClientPlayNetworking.registerGlobalReceiver(this.id, this::receive);
        }

        // Updates the radiation value on the client.
        public void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                            PacketByteBuf buf, PacketSender responseSender) {
            int value = buf.readInt();
            if (client.player != null) {
                RadiationNBT.set((IEntityDataSaver) client.player, this.radiationType, Math.min(value, QuantumConfig.cap));
            }
        }
    }
}