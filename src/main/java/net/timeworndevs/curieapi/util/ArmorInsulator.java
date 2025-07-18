package net.timeworndevs.curieapi.util;

import net.minecraft.item.Item;
import net.timeworndevs.curieapi.radiation.RadiationEntry;
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.util.Map;

import static net.timeworndevs.curieapi.util.CurieAPIConfig.ARMOR_INSULATORS;

public record ArmorInsulator (Map<Item, Float> armorItems, RadiationEntry radiations) {
    public boolean containsItem(Item item) {
        return armorItems.containsKey(item);
    }

    public static ArmorInsulator findSetForItem(Item item) {
        return ARMOR_INSULATORS.stream()
                .filter(set -> set.containsItem(item))
                .findFirst()
                .orElse(null);
    }

    public float getMultiplier(Item item) {
        return this.armorItems.getOrDefault(item, 0.0f);
    }

    // Creates a new armor insulator value
    public static ArmorInsulator register(Map<Item, Float> armorItems, RadiationEntry radiations) {
        ArmorInsulator insulator = new ArmorInsulator(armorItems, radiations);
        ARMOR_INSULATORS.add(insulator);
        return insulator;

    }

    public float getRadiation(RadiationType type) {
        return this.radiations.get(type);
    }
}
