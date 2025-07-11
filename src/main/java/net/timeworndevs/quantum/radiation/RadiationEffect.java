package net.timeworndevs.quantum.radiation;

import net.minecraft.server.network.ServerPlayerEntity;

public abstract class RadiationEffect {

    public RadiationEffect() {}

    public abstract void applyEffect(ServerPlayerEntity player, RadiationType[] types);
}
