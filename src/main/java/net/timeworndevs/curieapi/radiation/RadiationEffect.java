package net.timeworndevs.curieapi.radiation;

import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtString;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.timeworndevs.curieapi.util.CurieNBT;
import net.timeworndevs.curieapi.util.IEntityDataSaver;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
@SuppressWarnings("unused")
public abstract class RadiationEffect<T extends AbstractRadiationEntry<?>> {
    protected final Identifier id;
    protected final T entry;
    protected final BiConsumer<ServerPlayerEntity, RadiationEntry> consumer;
    protected RadiationEffect(Identifier id, T entry, BiConsumer<ServerPlayerEntity, RadiationEntry> consumer) {
        this.id = id;
        this.entry = entry;
        this.consumer = consumer;
    }
    protected Identifier getId() {
        return id;
    }

    protected T getEntry() {
        return entry;
    }

    protected BiConsumer<ServerPlayerEntity, RadiationEntry> getConsumer() {
        return consumer;
    }

    public Identifier getIdentifier() {
        return this.id;
    }
    // Applies the effect determined by the class.
    public abstract void applyEffect(ServerPlayerEntity player, RadiationEntry types);

    public void updateEffects(ServerPlayerEntity player) {
        CurieNBT.getEffectList((IEntityDataSaver) player).add(NbtString.of(id.toString()));
    }
    public static Set<Identifier> getCurrentEffects(ServerPlayerEntity serverPlayerEntity) {
        NbtList effectsNbt = CurieNBT.getEffectList((IEntityDataSaver) serverPlayerEntity);
        Set<Identifier> effects = new HashSet<>(effectsNbt.size());
        for (NbtElement effectElement : effectsNbt) {
            if (effectElement instanceof NbtString nbtString) {
                effects.add(Identifier.tryParse(nbtString.asString()));
            }
        }
        return effects;
    }
}
