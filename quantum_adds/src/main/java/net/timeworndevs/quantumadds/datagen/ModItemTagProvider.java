package net.timeworndevs.quantumadds.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.ItemTags;
import net.timeworndevs.quantumadds.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends FabricTagProvider.ItemTagProvider {

    public ModItemTagProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> completableFuture) {
        super(output, completableFuture);
    }

    @Override
    protected void configure(RegistryWrapper.WrapperLookup arg) {




        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.HAZMATD_HELMET, ModItems.HAZMATD_CHESTPLATE,
                        ModItems.HAZMATD_LEGGINGS, ModItems.HAZMATD_BOOTS);


        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.HAZMATC_HELMET, ModItems.HAZMATC_CHESTPLATE,
                        ModItems.HAZMATC_LEGGINGS, ModItems.HAZMATC_BOOTS);


        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.HAZMATB_HELMET, ModItems.HAZMATB_CHESTPLATE,
                        ModItems.HAZMATB_LEGGINGS, ModItems.HAZMATB_BOOTS);


        getOrCreateTagBuilder(ItemTags.TRIMMABLE_ARMOR)
                .add(ModItems.HAZMATA_HELMET, ModItems.HAZMATA_CHESTPLATE,
                        ModItems.HAZMATA_LEGGINGS, ModItems.HAZMATA_BOOTS);

    }
}
