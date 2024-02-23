package net.timeworndevs.quantum;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.timeworndevs.quantum.client.RadiationAlphaHudOverlay;
import net.timeworndevs.quantum.client.RadiationBetaHudOverlay;
import net.timeworndevs.quantum.client.RadiationGammaOverlay;
import net.timeworndevs.quantum.networking.ModMessages;
import net.timeworndevs.quantum.screen.ModScreenHandlers;

@Environment(EnvType.CLIENT)
public class QuantumClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Here we will put client-only registration code (thabks toast)
        HudRenderCallback.EVENT.register(new RadiationAlphaHudOverlay());
        HudRenderCallback.EVENT.register(new RadiationBetaHudOverlay());
        HudRenderCallback.EVENT.register(new RadiationGammaOverlay());
        ModMessages.registerS2CPackets();
        //HandledScreens.register(ModScreenHandlers.MICROWAVE_SCREEN_HANDLER, MicrowaveScreen::new);
    }
}