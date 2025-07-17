package net.timeworndevs.quantum.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.timeworndevs.quantum.radiation.RadiationData;
import net.timeworndevs.quantum.radiation.RadiationType;
import net.timeworndevs.quantum.util.RadiationCalculator;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private int tick = 0;
    @Override
    public void onStartTick(MinecraftServer server) {
        if (tick >= 20) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getServerWorld();

                for (RadiationType type : RadiationType.RADIATION_TYPES.values()) {
                    int radiation = RadiationCalculator.calculateRadiationForType(world, player, type);
                    if (radiation > 0) {
                        RadiationData.addRad(player, type, radiation);
                    } else {
                        RadiationData.delRad(player, type, 1);
                    }
                }
            }
            tick = 0;
        } else {
            tick++;
        }
    }
}