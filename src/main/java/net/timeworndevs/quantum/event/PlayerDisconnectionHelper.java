package net.timeworndevs.quantum.event;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class PlayerDisconnectionHelper implements ServerPlayConnectionEvents.Disconnect {

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        PlayerTickHandler.removeConnectedPlayer(handler.getPlayer().getUuid());
    }
}
