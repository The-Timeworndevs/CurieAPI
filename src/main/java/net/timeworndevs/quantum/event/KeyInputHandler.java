package net.timeworndevs.quantum.event;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.timeworndevs.quantum.Quantum;
import net.timeworndevs.quantum.networking.ModMessages;
import org.lwjgl.glfw.GLFW;

public class KeyInputHandler {
    public static final String KEY_CATEGORY_TEST = "key.category.quantum.test";
    public static final String KEY_ALPHA = "key.quantum.alpha";
    public static final String KEY_BETA = "key.quantum.beta";
    public static final String KEY_GAMMA = "key.quantum.gamma";
    public static KeyBinding radKey;
    public static KeyBinding radKey2;
    public static KeyBinding radKey3;

    public static void registerKeyInputs() {
        /*ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (radKey.wasPressed()) {
                ClientPlayNetworking.send(ModMessages.ALPHA_ID, PacketByteBufs.create());
            }
            if (radKey2.wasPressed()) {
                ClientPlayNetworking.send(ModMessages.BETA_ID, PacketByteBufs.create());
            }
            if (radKey3.wasPressed()) {
                ClientPlayNetworking.send(ModMessages.GAMMA_ID, PacketByteBufs.create());
            }
        });*/
    }

    public static void register() {
        radKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_ALPHA,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_O,
                KEY_CATEGORY_TEST
        ));
        radKey2 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_BETA,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_BACKSLASH,
                KEY_CATEGORY_TEST
        ));
        radKey3 = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                KEY_GAMMA,
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_F,
                KEY_CATEGORY_TEST
        ));
        registerKeyInputs();
    }


}
