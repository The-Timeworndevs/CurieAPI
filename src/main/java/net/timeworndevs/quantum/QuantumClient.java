package net.timeworndevs.quantum;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.timeworndevs.quantum.radiation.RadiationType;


@Environment(EnvType.CLIENT)
public class QuantumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> RadiationType.RadiationPacket.registerPackets());
    }
}