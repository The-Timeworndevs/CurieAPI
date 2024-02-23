package net.timeworndevs.quantum.networking.packet.clear;

import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;

public class ClearGammaC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {




        RadiationData.setRad((IEntityDataSaver) player, "gamma", 0);

        RadiationData.syncRad(((IEntityDataSaver) player).getPersistentData().getInt("radiation.gamma"),
                "gamma", player);



    }

}
