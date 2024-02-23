package net.timeworndevs.quantum.screen;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantum.Quantum;

public class ModScreenHandlers {
//    public static final ScreenHandlerType<MicrowaveScreenHandler> MICROWAVE_SCREEN_HANDLER =
//            Registry.register(Registries.SCREEN_HANDLER, new Identifier(Quantum.MOD_ID, "microwaving"),
//                    new ExtendedScreenHandlerType<>(MicrowaveScreenHandler::new));

    public static void registerScreenHandlers() {
        Quantum.LOGGER.info("Tunneling' Screen Handlers with " + Quantum.MOD_ID);
    }
}