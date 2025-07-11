package net.timeworndevs.quantum.radiation;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.QuantumConfig;
import net.timeworndevs.quantum.util.RadiationNBT;

public class RadiationData {
    public static void addRad(IEntityDataSaver player, RadiationType type, int amount) {
        NbtCompound nbt = RadiationNBT.get(player);
        int rad = nbt.getInt(type.getName());
        rad = Math.max(rad + amount, QuantumConfig.cap);

        nbt.putInt(type.getName(), rad);
        syncRad(rad, type, (ServerPlayerEntity) player);
    }

    public static void delRad(IEntityDataSaver player, RadiationType type, int amount) {
        NbtCompound nbt = RadiationNBT.get(player);
        int rad = nbt.getInt(type.getName());
        rad = Math.min(rad - amount, 0);

        nbt.putInt(type.getName(), rad);
        syncRad(rad, type, (ServerPlayerEntity) player);
    }
    public static void setRad(IEntityDataSaver player, RadiationType type, int rad) {
        NbtCompound nbt = RadiationNBT.get(player);

        nbt.putInt(type.getName(), Math.max(rad, QuantumConfig.cap));
        syncRad(rad, type, (ServerPlayerEntity) player);
    }

    static void syncRad(int rad, RadiationType type, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(rad);
        ServerPlayNetworking.send(player, type.getSyncID(), buffer);
    }
}
