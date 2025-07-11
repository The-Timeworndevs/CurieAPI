package net.timeworndevs.quantum.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.NbtCompound;
import net.timeworndevs.quantum.util.IEntityDataSaver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntity.class)
public abstract class ModEntityDataSaverMixin implements IEntityDataSaver {
    @Unique
    private NbtCompound persistentData;

    @Override
    public NbtCompound getPersistentData() {
        if (this.persistentData == null) {
            this.persistentData = new NbtCompound();
        }
        return persistentData;
    }

    //GENUINELY IDFK WHAT THAT IS :sob:

    /*@Inject(method="writeNbt", at = @At("HEAD"))
    protected void injectWriteMethod(NbtCompound nbt, CallbackInfoReturnable info) {
        if (persistentData!=null) {
            nbt.put(Quantum.MOD_ID + ".fok_data", persistentData);
        }
    }

    @Inject(method="readNbt", at = @At("HEAD"))
    protected void injectReadMethod(NbtCompound nbt, CallbackInfo info) {
        if (nbt.contains(Quantum.MOD_ID+".fok_data", 10)) {
            persistentData = nbt.getCompound(Quantum.MOD_ID + ".fok_data");
        }
    }*/
}
