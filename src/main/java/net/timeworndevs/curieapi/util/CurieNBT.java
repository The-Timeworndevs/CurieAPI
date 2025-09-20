package net.timeworndevs.curieapi.util;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.timeworndevs.curieapi.radiation.RadiationType;

public class CurieNBT {
    public enum CurieNBTType {
        RADIATION("radiation"),
        EFFECT("effect");

        private final String name;
        CurieNBTType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }
    // Gets the radiation values of the player.
    public static NbtElement get(IEntityDataSaver player, CurieNBTType type) {
        return player.CurieAPI$getPersistentData().get(type.getName());
    }

    // Gets the radiation value of a specific type for the player.
    public static int getRadiation(IEntityDataSaver player, String type) {
        if (get(player, CurieNBTType.RADIATION) instanceof NbtCompound compound) {
            return compound.getInt(type);
        }
        return 0;
    }

    // Sets a radiation value of the player.
    public static void setRadiation(IEntityDataSaver player, RadiationType type, int value) {
        if (get(player, CurieNBTType.RADIATION) instanceof NbtCompound compound) {
            compound.putInt(type.getName(), value);
        }
    }

    public static NbtList getEffectList(IEntityDataSaver player) {
        if (get(player, CurieNBTType.EFFECT) instanceof NbtList list) {
            return list;
        }
        return new NbtList();
    }
}
