package net.timeworndevs.curieapi.radiation;

import java.util.HashMap;
import java.util.Map;

public class RadiationEntry extends AbstractRadiationEntry<Float>{
    public RadiationEntry(Map<RadiationType, Float> entry) {
        super(entry);
    }

    public static RadiationEntry createEmpty() {
        return new RadiationEntry(new HashMap<>());
    }

    public void add(RadiationType type, float value) {
       entry.put(type, entry.get(type) + value);
    }
    public float addAllTypes() {
        return entry.values().stream().reduce(0.0f, Float::sum);
    }

    @Override
    public boolean isWithin(AbstractRadiationEntry<?> other) {
        if (other instanceof RadiationEntry same) {
            for (RadiationType type : entry.keySet()) {
                float value = same.getEntry().getOrDefault(type, 0.0f);
                if (value < entry.get(type)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
}
