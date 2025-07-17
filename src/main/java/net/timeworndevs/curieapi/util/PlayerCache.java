package net.timeworndevs.curieapi.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerCache {
    private static final WeakHashMap<UUID, PlayerCache> cache = new WeakHashMap<>();

    private final PlayerEntity player;
    private Map<Item, Integer> inventory;
    private List<Item> armor;
    private int itemRadiation;
    private float biomeRadiation;
    private float blockRadiation;
    private float armorInsulation;

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

    public void setItemRadiation(int radiation) {
        this.itemRadiation = radiation;
    }
    public int getPrevItemRadiation() {
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
    public void updateArmor(List<Item> armor, float insulation) {
        this.armor = armor;
        this.armorInsulation = insulation;
    }

    public float getArmorInsulation() {
        return this.armorInsulation;
    }

    public float getPrevBiomeRadiation() {
        return this.biomeRadiation;
    }
    public void setBiomeRadiation(float radiation) {
        this.biomeRadiation = radiation;
    }

    public float getPrevBlockRadiation() {
        return this.blockRadiation;
    }
    public void setBlockRadiation(float radiation) {
        this.blockRadiation = radiation;
    }


}
