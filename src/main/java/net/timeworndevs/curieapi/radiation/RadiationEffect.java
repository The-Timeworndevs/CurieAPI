package net.timeworndevs.curieapi.radiation;

import net.minecraft.server.network.ServerPlayerEntity;


import java.util.Map;

public interface RadiationEffect {
    // Applies the effect determined by the class.
    void applyEffect(ServerPlayerEntity player, Map<RadiationType, Float> types);
}
