package net.timeworndevs.quantum.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.networking.packet.*;
import net.timeworndevs.quantum.util.IEntityDataSaver;

import java.util.HashMap;

public class ModMessages {
    public static class NewSyncPackage {
        private final String radiation_type;
        public NewSyncPackage(String radiation_type) {
            this.radiation_type = radiation_type;

        }
        public void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                                   PacketByteBuf buf, PacketSender responseSender) {
            if (client.player != null) {
                ((IEntityDataSaver) client.player).getPersistentData().putInt("radiation." + this.radiation_type, buf.readInt());
            }
        }
    }
    public static final Identifier ALPHA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_sync");

    public static final Identifier BETA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_sync");

    public static final Identifier GAMMA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_sync");

    public static HashMap<Identifier, NewSyncPackage> NEW_RADIATIONS_SYNC_ID = new HashMap<>();

    public static void registerS2CPackets() {

        ClientPlayNetworking.registerGlobalReceiver(ALPHA_SYNC_ID, AlphaSyncDataS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(BETA_SYNC_ID, BetaSyncDataS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(GAMMA_SYNC_ID, GammaSyncDataS2CPacket::receive);

        for (Identifier i: NEW_RADIATIONS_SYNC_ID.keySet()) {
            ClientPlayNetworking.registerGlobalReceiver(i, NEW_RADIATIONS_SYNC_ID.get(i)::receive);
            Quantum.LOGGER.info(String.valueOf(i));
        }
    }
}