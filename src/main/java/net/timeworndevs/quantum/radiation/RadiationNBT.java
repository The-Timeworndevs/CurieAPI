package net.timeworndevs.quantum.radiation;

import net.minecraft.nbt.NbtCompound;
import net.timeworndevs.quantum.util.IEntityDataSaver;

public class RadiationNBT {
    // Gets the radiation values of the player.
    public static NbtCompound get(IEntityDataSaver player) {
        return player.quantum$getPersistentData();
    }

    // Gets the radiation value of a specific type for the player.
    public static int get(IEntityDataSaver player, String type) {
        NbtCompound tag = get(player);
        if (tag.contains(type)) {
            return tag.getInt(type);
        }
        return 0;
    }

    // Sets a radiation value of the player.
    public static void set(IEntityDataSaver player, RadiationType type, int value) {
        get(player).putInt(type.getName(), value);
    }
}
