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

    // Creates a new armor insulator value
    public static ArmorInsulator register(ArrayList<Item> armorItems, ArrayList<Float> multipliers, HashMap<RadiationType, Integer> radiations) {
        ArmorInsulator insulator = new ArmorInsulator(armorItems, multipliers, radiations);
        QuantumConfig.ARMOR_INSULATORS.add(insulator);
        return insulator;
    }

    public float getRadiation(RadiationType type) {
        if (radiations.containsKey(type)) {
            return radiations.get(type);
        }
        return 0.0f;
    }
}
