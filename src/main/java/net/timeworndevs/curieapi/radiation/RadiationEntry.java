package net.timeworndevs.curieapi.radiation;

import java.util.Map;

public record RadiationEntry(Map<RadiationType, Integer> entries) {
    public int get(RadiationType type) {
        return this.entries.getOrDefault(type, 0);
    }

    public boolean containsKey(RadiationType type) {
        return this.entries.containsKey(type);
    }
}
