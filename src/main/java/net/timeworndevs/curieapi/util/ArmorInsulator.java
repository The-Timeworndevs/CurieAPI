package net.timeworndevs.curieapi.util;

import net.minecraft.item.Item;
import net.timeworndevs.curieapi.radiation.RadiationEntry;
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.util.ArrayList;
import java.util.List;

import static net.timeworndevs.curieapi.util.CurieAPIConfig.ARMOR_INSULATORS;

public record ArmorInsulator (ArrayList<Item> armorItems, ArrayList<Float> multipliers, RadiationEntry radiations) {
    public static List<String> pieceList = List.of("helmet", "chestplate", "leggings", "boots");

    public boolean containsItem(Item item) {
        return armorItems.contains(item);
    }

    public static ArmorInsulator findSetForItem(Item item) {
        return ARMOR_INSULATORS.stream()
                .filter(set -> set.containsItem(item))
                .findFirst()
                .orElse(null);
    }

    public float getMultiplier(Item item) {
        if (this.containsItem(item)) {
            return this.multipliers.get(this.armorItems.indexOf(item));
        }
        return 0.0f;
    }

    // Creates a new armor insulator value
    public static ArmorInsulator register(ArrayList<Item> armorItems, ArrayList<Float> multipliers, RadiationEntry radiations) {
        ArmorInsulator insulator = new ArmorInsulator(armorItems, multipliers, radiations);

        ARMOR_INSULATORS.add(insulator);
        return insulator;

    }

    public int getRadiation(RadiationType type) {
        return this.radiations.get(type);
    }
}
