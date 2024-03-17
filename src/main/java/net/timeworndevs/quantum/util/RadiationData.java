package net.timeworndevs.quantum.util;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.networking.ModMessages;

public class RadiationData {
    public static void addRad(IEntityDataSaver player, String kind, int amount) {
        NbtCompound nbt = player.getPersistentData();
        int rad = nbt.getInt("radiation."+kind);
        if(rad + amount >= 1000) {
            rad = 1000;
        } else {
            rad += amount;
        }

        nbt.putInt("radiation."+kind, rad);
        syncRad(rad, kind, (ServerPlayerEntity) player);
    }

    public static void delRad(IEntityDataSaver player, String kind, int amount) {
        NbtCompound nbt = player.getPersistentData();
        int rad = nbt.getInt("radiation."+kind);
        if(rad - amount < 0) {
            rad = 0;
        } else {
            rad -= amount;
        }

        nbt.putInt("radiation."+kind, rad);
        syncRad(rad, kind, (ServerPlayerEntity) player);
    }
    public static void setRad(IEntityDataSaver player, String kind, int rad) {
        NbtCompound nbt = player.getPersistentData();


        nbt.putInt("radiation."+kind, rad);
        syncRad(rad, kind, (ServerPlayerEntity) player);
    }

    public static void syncRad(int rad, String kind, ServerPlayerEntity player) {
        PacketByteBuf buffer = PacketByteBufs.create();
        buffer.writeInt(rad);
        switch (kind) {
            case "alpha" -> {
                ServerPlayNetworking.send(player, ModMessages.ALPHA_SYNC_ID, buffer);
            }
            case "beta" -> {
                ServerPlayNetworking.send(player, ModMessages.BETA_SYNC_ID, buffer);
            }
            case "gamma" -> {
                ServerPlayNetworking.send(player, ModMessages.GAMMA_SYNC_ID, buffer);
            }
        }
        for (String i: Quantum.new_radiation_types.keySet()) {
            if (kind.equals(i)) {
                ServerPlayNetworking.send(player, new Identifier(Quantum.MOD_ID,"radiation_" + i+"_sync"), buffer);
            }
        }
    }
}
