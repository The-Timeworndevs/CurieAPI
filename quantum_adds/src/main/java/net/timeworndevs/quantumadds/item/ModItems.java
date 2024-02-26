package net.timeworndevs.quantumadds.item;

import net.minecraft.item.*;
import net.minecraft.registry.RegistryKey;
import net.timeworndevs.quantumadds.item.GeigerCounter.GeigerCounter;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantumadds.Quantum;
import net.minecraft.util.Rarity;


public class ModItems {

    public static final Item GEIGER_COUNTER = registerItem("geiger_counter", new GeigerCounter(new FabricItemSettings().maxCount(1).rarity(Rarity.RARE)));

    public static final Item BATTERIES = registerItem("batteries", new Item(new FabricItemSettings()));
    public static final Item URANIUM = registerItem("uranium", new Item(new FabricItemSettings()));


    public static final Item ALPHA_SRC = registerItem("alphasrc", new Item(new FabricItemSettings()));

    public static final Item BETA_SRC = registerItem("betasrc", new Item(new FabricItemSettings()));

    public static final Item GAMMA_SRC = registerItem("gammasrc", new Item(new FabricItemSettings()));




    public static Item RUBBER = registerItem("rubber", new Item(new FabricItemSettings()));
    public static Item PVC = registerItem("pvc", new Item(new FabricItemSettings()));

    public static Item HAZMATD_HELMET = registerItem("hazmatd_helmet",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static Item HAZMATD_CHESTPLATE = registerItem("hazmatd_chestplate",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static Item HAZMATD_LEGGINGS = registerItem("hazmatd_leggings",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static Item HAZMATD_BOOTS = registerItem("hazmatd_boots",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.BOOTS, new FabricItemSettings()));


    public static Item HAZMATC_HELMET = registerItem("hazmatc_helmet",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static Item HAZMATC_CHESTPLATE = registerItem("hazmatc_chestplate",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static Item HAZMATC_LEGGINGS = registerItem("hazmatc_leggings",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static Item HAZMATC_BOOTS = registerItem("hazmatc_boots",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    public static Item HAZMATB_HELMET = registerItem("hazmatb_helmet",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static Item HAZMATB_CHESTPLATE = registerItem("hazmatb_chestplate",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static Item HAZMATB_LEGGINGS = registerItem("hazmatb_leggings",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static Item HAZMATB_BOOTS = registerItem("hazmatb_boots",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    public static Item HAZMATA_HELMET = registerItem("hazmata_helmet",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.HELMET, new FabricItemSettings()));
    public static Item HAZMATA_CHESTPLATE = registerItem("hazmata_chestplate",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.CHESTPLATE, new FabricItemSettings()));
    public static Item HAZMATA_LEGGINGS = registerItem("hazmata_leggings",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.LEGGINGS, new FabricItemSettings()));
    public static Item HAZMATA_BOOTS = registerItem("hazmata_boots",
            new ArmorItem(ModArmorMaterials.HAZMATD, ArmorItem.Type.BOOTS, new FabricItemSettings()));

    private static void radiationEntries(FabricItemGroupEntries entries) {

        entries.add(GEIGER_COUNTER);
        entries.add(BATTERIES);
        entries.add(RUBBER);
        entries.add(PVC);
        entries.add(URANIUM);


        entries.add(HAZMATD_HELMET);
        entries.add(HAZMATD_CHESTPLATE);
        entries.add(HAZMATD_LEGGINGS);
        entries.add(HAZMATD_BOOTS);


        entries.add(HAZMATC_HELMET);
        entries.add(HAZMATC_CHESTPLATE);
        entries.add(HAZMATC_LEGGINGS);
        entries.add(HAZMATC_BOOTS);


        entries.add(HAZMATB_HELMET);
        entries.add(HAZMATB_CHESTPLATE);
        entries.add(HAZMATB_LEGGINGS);
        entries.add(HAZMATB_BOOTS);


        entries.add(HAZMATA_HELMET);
        entries.add(HAZMATA_CHESTPLATE);
        entries.add(HAZMATA_LEGGINGS);
        entries.add(HAZMATA_BOOTS);
    }
    private static void quantumEntries(FabricItemGroupEntries entries) {

    }
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(Quantum.MOD_ID, name), item);
    }
    public static void registerItems() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(Quantum.MOD_ID, "radiation"))).register(ModItems::radiationEntries);
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(Quantum.MOD_ID, "quantum"))).register(ModItems::quantumEntries);
    }
}
