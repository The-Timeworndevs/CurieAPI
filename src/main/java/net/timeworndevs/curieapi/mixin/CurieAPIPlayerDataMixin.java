package net.timeworndevs.curieapi.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.timeworndevs.curieapi.CurieAPI;
import net.timeworndevs.curieapi.util.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class CurieAPIPlayerDataMixin implements IEntityDataSaver {
    @Unique
    private NbtCompound persistentData = new NbtCompound();

    @Unique
    private final NbtCompound RADIATION = new NbtCompound();

    @Unique
    private final NbtList EFFECTS = new NbtList();

    @Unique
    private static final String radiationData = CurieAPI.MOD_ID;
    @Override
    public NbtCompound CurieAPI$getPersistentData() {
        persistentData.put("radiation", RADIATION);
        persistentData.put("effect", EFFECTS);
        return persistentData;
    }

    @Inject(method="writeCustomDataToNbt", at = @At("TAIL"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfo ci) {
        nbt.put(radiationData, persistentData);
    }

    @Inject(method="readCustomDataFromNbt", at = @At("TAIL"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains(radiationData, NbtElement.COMPOUND_TYPE)) {
            persistentData = nbt.getCompound(radiationData);
        }
    }
}
