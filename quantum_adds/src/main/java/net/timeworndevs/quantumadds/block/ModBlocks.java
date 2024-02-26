package net.timeworndevs.quantumadds.block;


import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;
import net.timeworndevs.quantumadds.Quantum;

public class ModBlocks {

    //public static final Block MICROWAVE = registerBlock("microwave", new ModBlocks (FabricBlockSettings.copyOf(Blocks.IRON_BLOCK).nonOpaque()));

    public static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(Quantum.MOD_ID, name), block);
    }
    /*public static final BlockEntityType<MicrowaveEntity> MICROWAVE_ENTITY = Registry.register(
            Registries.BLOCK_ENTITY_TYPE,
            new Identifier(Quantum.MOD_ID, "microwave"),
            FabricBlockEntityTypeBuilder.create(MicrowaveEntity::new, MICROWAVE).build()
    );*/

    public static final Block METEORITE_ROCK = registerBlock("meteorite_rock", new Block(FabricBlockSettings.create().strength(4.0f)));

    public static final Block NUCLEAR_WASTE = registerBlock("nuclear_waste", new Block(FabricBlockSettings.create().strength(4.0f)));

    public static final Block DEAD_SOIL = registerBlock("dead_soil", new Block(FabricBlockSettings.create().strength(4.0f)));


    public static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(Quantum.MOD_ID, name), new BlockItem(block, new FabricItemSettings()));
    }

    private static void creativeEntries(FabricItemGroupEntries entries) {

        entries.add(METEORITE_ROCK);
        entries.add(NUCLEAR_WASTE);
        entries.add(DEAD_SOIL);

    }
    public static void registerBlockItems() {
        ItemGroupEvents.modifyEntriesEvent(RegistryKey.of(Registries.ITEM_GROUP.getKey(), new Identifier(Quantum.MOD_ID, "building_blocks"))).register(ModBlocks::creativeEntries);
    }
    public static void registerBlocks() {

    }

}
