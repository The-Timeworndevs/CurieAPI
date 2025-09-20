package net.timeworndevs.curieapi.radiation;

import java.util.Map;

public abstract class AbstractRadiationEntry<T> {
    protected final Map<RadiationType, T> entry;
    protected AbstractRadiationEntry(Map<RadiationType, T> entry) {
        this.entry = entry;
    }

    public Map<RadiationType, T> getEntry() {
        return entry;
    }

    @Override
    public String toString() {
        return "RadiationEntry{" +
                "entries=" + entry +
                '}';
    }

    public abstract boolean isWithin(AbstractRadiationEntry<?> other);
}
