package net.timeworndevs.quantum.networking;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.networking.packet.*;
import net.timeworndevs.quantum.networking.packet.clear.*;

public class ModMessages {
    public static final Identifier ALPHA_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha");
    public static final Identifier ALPHA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_del");
    public static final Identifier ALPHA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_sync");

    public static final Identifier BETA_ID = new Identifier(Quantum.MOD_ID, "radiation_beta");
    public static final Identifier BETA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_del");
    public static final Identifier BETA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_sync");

    public static final Identifier GAMMA_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma");
    public static final Identifier GAMMA_DEL_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_del");
    public static final Identifier GAMMA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_sync");



    public static final Identifier CLEAR_ALPHA_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_clear");
    public static final Identifier CLEAR_BETA_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_clear");
    public static final Identifier CLEAR_GAMMA_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_clear");
    public static final Identifier CLEAR_ALPHA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_alpha_clear_sync");
    public static final Identifier CLEAR_BETA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_beta_clear_sync");
    public static final Identifier CLEAR_GAMMA_SYNC_ID = new Identifier(Quantum.MOD_ID, "radiation_gamma_clear_sync");

    public static void registerC2SPackets() {
        ServerPlayNetworking.registerGlobalReceiver(ALPHA_ID, AlphaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(ALPHA_DEL_ID, AlphaDelC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(BETA_ID, BetaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(BETA_DEL_ID, BetaDelC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(GAMMA_ID, GammaC2SPacket::receive);
        ServerPlayNetworking.registerGlobalReceiver(GAMMA_DEL_ID, GammaDelC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(CLEAR_ALPHA_ID, ClearAlphaC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(CLEAR_BETA_ID, ClearBetaC2SPacket::receive);

        ServerPlayNetworking.registerGlobalReceiver(CLEAR_GAMMA_ID, ClearGammaC2SPacket::receive);
    }

    public static void registerS2CPackets() {

        ClientPlayNetworking.registerGlobalReceiver(ALPHA_SYNC_ID, AlphaSyncDataS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(BETA_SYNC_ID, BetaSyncDataS2CPacket::receive);
        ClientPlayNetworking.registerGlobalReceiver(GAMMA_SYNC_ID, GammaSyncDataS2CPacket::receive);

    }
}