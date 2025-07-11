package net.timeworndevs.quantum.util;

import net.minecraft.nbt.NbtCompound;

public class RadiationNBT {
    public static NbtCompound get(IEntityDataSaver player) {
        return player.getPersistentData().getCompound("quantum:radiation");
    }

    public static int get(IEntityDataSaver player, String type) {
        NbtCompound tag = get(player);
        if (tag.contains(type)) {
            return tag.getInt(type);
        }
        return 0;
    }
}
