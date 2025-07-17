package net.timeworndevs.curieapi.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.timeworndevs.curieapi.radiation.RadiationEntry;
import net.timeworndevs.curieapi.radiation.RadiationType;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerCache {
    private static final WeakHashMap<UUID, PlayerCache> cache = new WeakHashMap<>();

    private final PlayerEntity player;
    private Map<Item, Integer> inventory;
    private List<Item> armor;
    private RadiationEntry itemRadiation;
    private RadiationEntry biomeRadiation;
    private RadiationEntry blockRadiation;
    private RadiationEntry armorInsulation;

    public PlayerCache(PlayerEntity player) {
        this.player = player;
        this.armor = this.createArmorMap();
    }

    public static PlayerCache get(PlayerEntity player) {
        return cache.get(player.getUuid());
    }
    public static void add(PlayerEntity player) {
        cache.putIfAbsent(player.getUuid(), new PlayerCache(player));
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

    public void setItemRadiation(RadiationType type, float radiation) {
        this.itemRadiation.put(type, radiation);
    }
    public float getPrevItemRadiation(RadiationType type) {
        return this.itemRadiation.get(type);
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
    public void updateArmor(List<Item> armor, RadiationType type, float insulation) {
        this.armor = armor;
        this.armorInsulation.put(type, insulation);
    }

    public float getArmorInsulation(RadiationType type) {
        return this.armorInsulation.get(type);
    }

    public float getPrevBiomeRadiation(RadiationType type) {
        return this.biomeRadiation.get(type);
    }
    public void setBiomeRadiation(RadiationType type, float radiation) {
        this.biomeRadiation.put(type, radiation);
    }

    public float getPrevBlockRadiation(RadiationType type) {
        return this.blockRadiation.get(type);
    }
    public void setBlockRadiation(RadiationType type, float radiation) {
        this.blockRadiation.put(type, radiation);
    }


}
