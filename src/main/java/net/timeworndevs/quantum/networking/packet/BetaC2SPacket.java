package net.timeworndevs.quantum.networking.packet;

import com.google.gson.JsonElement;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.event.PlayerTickHandler;
import net.timeworndevs.quantum.item.ModItems;
import net.timeworndevs.quantum.util.Clamp;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import net.timeworndevs.quantum.util.RadiationData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BetaC2SPacket {
    public static void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler,
                               PacketByteBuf buf, PacketSender responseSender) {



        int radiationComputed = PlayerTickHandler.calculateRadiation((ServerWorld) player.getWorld(), player, "beta");

        double radiationDivision = PlayerTickHandler.calculateDivision(player, "beta");


        RadiationData.addRad((IEntityDataSaver) player, "beta",  (int) Clamp.clamp(radiationComputed / radiationDivision, 1, 100));

        RadiationData.syncRad(((IEntityDataSaver) player).getPersistentData().getInt("radiation.beta"),
                "beta", player);


    }
}
