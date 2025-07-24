package net.timeworndevs.curieapi.event;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.timeworndevs.curieapi.radiation.RadiationData;
import net.timeworndevs.curieapi.radiation.RadiationType;
import net.timeworndevs.curieapi.radiation.RadiationCalculator;
import net.timeworndevs.curieapi.util.PlayerCache;

import java.util.Random;

import static net.timeworndevs.curieapi.CurieAPI.LOADED_TYPES;
import static net.timeworndevs.curieapi.util.CurieAPIConfig.PASSIVE_DECAY;

public class PlayerTickHandler implements ServerTickEvents.StartTick {
    private int tick = 0;
    private static final Random random = new Random();

    private int index = 0;

    @Override
    public void onStartTick(MinecraftServer server) {
        if (LOADED_TYPES.isEmpty()) return;
        if (tick >= 20) {
            RadiationType type = LOADED_TYPES.get(index);
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerWorld world = player.getServerWorld();

                PlayerCache cache = PlayerCache.get(player);
                int radiation = RadiationCalculator.calculateRadiationForType(world, player, type, cache);

                if (radiation > 0) {
                    RadiationData.addRad(player, type, radiation);
                } else {
                    RadiationData.delRad(player, type, random.nextInt(PASSIVE_DECAY[0], PASSIVE_DECAY[1]));
                }
            }
            index++;
            if (index >= LOADED_TYPES.size()) {
                tick = 0;
                index = 0;
            }
        }
        tick++;
    }
}