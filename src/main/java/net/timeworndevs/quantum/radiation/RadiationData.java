package net.timeworndevs.quantum.radiation;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.QuantumConfig;

public final class RadiationData {
    private RadiationData() {}

    // Adds radiation to the player.
    public static void addRad(IEntityDataSaver player, RadiationType type, int amount) {
        int rad = RadiationNBT.get(player, type.getName());
        rad = Math.min(rad + amount, QuantumConfig.cap);
        RadiationNBT.set(player, type, rad);
        syncRad(rad, type, (ServerPlayerEntity) player);
    }

    // Removes radiation from the player.
    public static void delRad(IEntityDataSaver player, RadiationType type, int amount) {
        int rad = RadiationNBT.get(player, type.getName());
        rad = Math.max(rad - amount, 0);

        RadiationNBT.set(player, type, rad);
        syncRad(rad, type, (ServerPlayerEntity) player);
    }

    // Sets the radiation of the player.
    public static void setRad(IEntityDataSaver player, RadiationType type, int rad) {
        RadiationNBT.set(player, type, Math.min(rad, QuantumConfig.cap));
        syncRad(rad, type, (ServerPlayerEntity) player);
    }

    // Sends a sync packet to the player.
    public static void syncRad(int rad, RadiationType type, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(rad);
        ServerPlayNetworking.send(player, type.getSyncID(), buffer);
    }
}
