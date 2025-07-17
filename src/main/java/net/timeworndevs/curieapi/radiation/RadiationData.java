package net.timeworndevs.curieapi.radiation;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.timeworndevs.curieapi.util.IEntityDataSaver;
import net.timeworndevs.curieapi.util.CurieAPIConfig;

public final class RadiationData {
    private RadiationData() {}

    // Adds radiation to the player.
    public static void addRad(PlayerEntity player, RadiationType type, int amount) {
        int rad = RadiationNBT.get((IEntityDataSaver) player, type.getName());
        rad = Math.min(rad + amount, CurieAPIConfig.cap);

        RadiationNBT.set((IEntityDataSaver) player, type, rad);
        syncRad(rad, type, player);
    }

    // Removes radiation from the player.
    public static void delRad(PlayerEntity player, RadiationType type, int amount) {
        int rad = RadiationNBT.get((IEntityDataSaver) player, type.getName());
        rad = Math.max(rad - amount, 0);

        RadiationNBT.set((IEntityDataSaver) player, type, rad);
        syncRad(rad, type, player);
    }

    // Sets the radiation of the player.
    public static void setRad(PlayerEntity player, RadiationType type, int rad) {
        RadiationNBT.set((IEntityDataSaver) player, type, Math.min(rad, CurieAPIConfig.cap));
        syncRad(rad, type, player);
    }

    // Sends a sync packet to the player.
    public static void syncRad(int rad, RadiationType type, PlayerEntity player) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketByteBuf buffer = PacketByteBufs.create();
            buffer.writeInt(rad);
            ServerPlayNetworking.send(serverPlayer, type.getSyncID(), buffer);
        }
    }
}
