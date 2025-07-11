package net.timeworndevs.quantum.radiation;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.util.IEntityDataSaver;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RadiationType {

    public static HashMap<String, RadiationType> RADIATION_TYPES = new HashMap<>();
    private final String name;
    private final float[] color;
    private final Identifier syncID;
    private final RadiationPacket syncPacket;
    private RadiationType(String name, float[] color) {
        this.name = name;
        this.color = color;
        this.syncID = new Identifier(Quantum.MOD_ID, "radiation_" + name + "_sync");
        this.syncPacket = new RadiationPacket(this.name, this.syncID);
    }

    public static RadiationType getRadiationType(String name) {
        return RADIATION_TYPES.get(name);
    }

    public float[] getColor() {
        return this.color;
    }

    public String getName() {
        return this.name;
    }

    public Identifier getSyncID() {
        return this.syncID;
    }

    public RadiationPacket getSyncPacket() {
        return this.syncPacket;
    }

    public static RadiationType register(String name, float[] color){
        RadiationType type = new RadiationType(name, color);
        RADIATION_TYPES.put(name, type);
        return type;
    };

    public static class RadiationPacket {
        public static ArrayList<RadiationPacket> PACKETS = new ArrayList<>();

        private final String radiationType;
        private final Identifier id;
        public RadiationPacket(String radiationType, Identifier id) {
            this.radiationType = radiationType;
            this.id = id;

        }

        public static void registerPackets() {
            for (RadiationPacket packet : PACKETS) {
                packet.registerPacket();
            }
        }
        private void registerPacket() {
            ClientPlayNetworking.registerGlobalReceiver(id, this::receive);
        }

        public void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                            PacketByteBuf buf, PacketSender responseSender) {
            if (client.player != null) {
                ((IEntityDataSaver) client.player).getPersistentData().putInt("radiation." + this.radiationType, buf.readInt());
            }
        }
    }
}