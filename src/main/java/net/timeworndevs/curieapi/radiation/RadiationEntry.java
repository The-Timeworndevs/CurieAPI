package net.timeworndevs.curieapi.radiation;

import java.util.Map;

public record RadiationEntry(Map<RadiationType, Float> entries) {
    public float get(RadiationType type) {
        return this.entries.getOrDefault(type, 0.0f);
    }

    public float addAllTypes() {
        return this.entries.values().stream().reduce(0.0f, Float::sum);
    }

    public void put(RadiationType type, float value) {
        this.entries.put(type, value + this.entries.getOrDefault(type, 0.0f));
    }

    public boolean containsKey(RadiationType type) {
        return this.entries.containsKey(type);
    }

}
