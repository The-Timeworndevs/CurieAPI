package net.timeworndevs.quantum.util;

import net.minecraft.item.Item;
import net.timeworndevs.quantum.radiation.RadiationType;

import java.util.ArrayList;
import java.util.HashMap;


public record ArmorInsulator (ArrayList<Item> armorItems, ArrayList<Float> multipliers, HashMap<RadiationType, Integer> radiations) {

    public boolean containsItem(Item item) {
        return armorItems.contains(item);
    }

    public float getMultiplier(Item item) {
        if (containsItem(item)) {
            return multipliers.get(armorItems.indexOf(item));
        }
        return 0.0f;
    }
    public float getRadiation(RadiationType type) {
        return radiations.get(type);
    }
}
