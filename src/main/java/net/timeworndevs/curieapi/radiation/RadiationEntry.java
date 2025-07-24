package net.timeworndevs.curieapi.radiation;

import java.util.HashMap;
import java.util.Map;

public record RadiationEntry(Map<RadiationType, Float> entries) {
    public static RadiationEntry createEmpty() {
        return new RadiationEntry(new HashMap<>());
    }

    public float get(RadiationType type) {
        return this.entries.getOrDefault(type, 0.0f);
    }
    public void clear() {
        this.entries.clear();
    }

    public void add(RadiationType type, float value) {
        this.put(type, this.get(type) + value);
    }
    public float addAllTypes() {
        return this.entries.values().stream().reduce(0.0f, Float::sum);
    }

    public void put(RadiationType type, float value) {
        this.entries.put(type, value);
    }

    @Override
    public String toString() {
        return "RadiationEntry{" +
                "entries=" + entries +
                '}';
    }

    public boolean containsKey(RadiationType type) {
        return this.entries.containsKey(type);
    }

}
