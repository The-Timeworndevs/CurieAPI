package net.timeworndevs.quantum.networking.packet;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;

import net.timeworndevs.quantum.util.IEntityDataSaver;

public class AlphaSyncDataS2CPacket {
    public static void receive(MinecraftClient client, ClientPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {
        //only on client thingies

        if (client.player != null) {
            ((IEntityDataSaver) client.player).getPersistentData().putInt("radiation.alpha", buf.readInt());
        }
    }
}
