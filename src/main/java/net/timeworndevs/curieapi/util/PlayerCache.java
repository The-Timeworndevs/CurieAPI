package net.timeworndevs.curieapi.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.timeworndevs.curieapi.radiation.RadiationEntry;
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.util.*;
import java.util.stream.Collectors;

import static net.timeworndevs.curieapi.CurieAPI.CACHE;

public class PlayerCache {


    private final PlayerEntity player;
    private Map<Item, Integer> inventory = new HashMap<>();
    private List<Item> armor;
    private RadiationEntry itemRadiation = RadiationEntry.createEmpty();
    private final RadiationEntry biomeRadiation = RadiationEntry.createEmpty();
    private final RadiationEntry blockRadiation = RadiationEntry.createEmpty();
    private RadiationEntry armorInsulation = RadiationEntry.createEmpty();

    public PlayerCache(PlayerEntity player) {
        this.player = player;
        this.armor = this.createArmorMap();
    }

    public static PlayerCache get(PlayerEntity player) {
        UUID uuid = player.getUuid();
        if (!CACHE.containsKey(uuid)) {
            CACHE.put(uuid, new PlayerCache(player));
        }
        return CACHE.get(uuid);
    }
    public static void add(PlayerEntity player) {
        CACHE.putIfAbsent(player.getUuid(), new PlayerCache(player));
    }

    public boolean inventoryEquals(Map<Item, Integer> other) {
        return this.inventory.equals(other);
    }
    public Map<Item, Integer> createInventoryMap() {
        return player.getInventory().main.stream()
                .filter(stack -> !stack.isEmpty())
                .collect(Collectors.groupingBy(
                        ItemStack::getItem,
                        Collectors.summingInt(ItemStack::getCount)
                ));
    }

    public void updateInventory() {
        this.inventory = this.createInventoryMap();
    }

    public void setItemRadiation(RadiationEntry entry) {
        this.itemRadiation = entry;
    }
    public RadiationEntry getPrevItemRadiation() {
        return this.itemRadiation;
    }

    public boolean armorEquals(List<Item> armor) {
        return this.armor.equals(armor);
    }

    public List<Item> createArmorMap() {
        return this.player.getInventory().armor.stream()
                .filter(stack -> !stack.isEmpty())
                .map(ItemStack::getItem)
                .toList();
    }
    public void updateArmor(List<Item> armor, RadiationEntry entry) {
        this.armor = armor;
        this.armorInsulation = entry;
    }

    public RadiationEntry getArmorInsulation() {
        return this.armorInsulation;
    }

    public RadiationEntry getPrevBiomeRadiation() {
        return this.biomeRadiation;
    }

    public void setBiomeRadiation(RadiationType type, float radiation) {
        this.biomeRadiation.put(type, radiation);
    }

    public RadiationEntry getPrevBlockRadiation() {
        return this.blockRadiation;
    }
    public void setBlockRadiation(RadiationType type, float radiation) {
        this.blockRadiation.put(type, radiation);
    }


}
