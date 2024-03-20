package net.timeworndevs.quantum.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.networking.packet.*;
import net.timeworndevs.quantum.util.Clamp;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;

import java.util.HashMap;

public class ModMessages {
    public static class NewPackage {
        private final String radiation_type;
        public NewPackage(String radiation_type) {
            this.radiation_type = radiation_type;

        }
        public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                            PacketByteBuf buf, PacketSender responseSender) {


            int radiationComputed = PlayerTickHandler.calculateRadiation((ServerWorld) player.getWorld(), player, this.radiation_type);

            double radiationDivision = PlayerTickHandler.calculateDivision(player, this.radiation_type);


            RadiationData.addRad((IEntityDataSaver) player, this.radiation_type, (int) Clamp.clamp(radiationComputed / radiationDivision, 1, 100));

            RadiationData.syncRad(((IEntityDataSaver) player).getPersistentData().getInt("radiation." + this.radiation_type),
                    this.radiation_type, player);


        }
    }
    public static class NewDelPackage {
        private final String radiation_type;
        public NewDelPackage(String radiation_type) {
            this.radiation_type = radiation_type;

        }
        public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                                   PacketByteBuf buf, PacketSender responseSender) {
            ServerWorld world = ((ServerWorld) player.getWorld());


            int alpha = PlayerTickHandler.calculateRadiation(world, player, this.radiation_type);
            if (alpha == 0) {
                RadiationData.delRad((IEntityDataSaver) player, this.radiation_type, 1);

                RadiationData.syncRad(((IEntityDataSaver) player).getPersistentData().getInt("radiation."+this.radiation_type),
                        this.radiation_type, player);
            }


        }
    }
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
    public static final Identifier ALPHA_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha");
    public static final Identifier ALPHA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_del");
    public static final Identifier ALPHA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_sync");

    public static final Identifier BETA_ID = new Identifier(Quantum.MOD_ID, "radiation_beta");
    public static final Identifier BETA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_del");
    public static final Identifier BETA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_sync");

    public static final Identifier GAMMA_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma");
    public static final Identifier GAMMA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_del");
    public static final Identifier GAMMA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_sync");

    public static HashMap<Identifier, NewPackage> NEW_RADIATIONS_ID = new HashMap<>();
    public static HashMap<Identifier, NewDelPackage> NEW_RADIATIONS_DEL_ID = new HashMap<>();
    public static HashMap<Identifier, NewSyncPackage> NEW_RADIATIONS_SYNC_ID = new HashMap<>();

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ALPHA_ID, AlphaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(ALPHA_DEL_ID, AlphaDelC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(BETA_ID, BetaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(BETA_DEL_ID, BetaDelC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(GAMMA_ID, GammaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(GAMMA_DEL_ID, GammaDelC2SPacket::receive);


        for (Identifier i: NEW_RADIATIONS_ID.keySet()) {
            ServerPlayNetworking.registerGlobalReceiver(i, NEW_RADIATIONS_ID.get(i)::receive);
            Quantum.LOGGER.info(String.valueOf(i));
        }
        for (Identifier i: NEW_RADIATIONS_DEL_ID.keySet()) {
            ServerPlayNetworking.registerGlobalReceiver(i, NEW_RADIATIONS_DEL_ID.get(i)::receive);
            Quantum.LOGGER.info(String.valueOf(i));
        }
    }

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