package me.trololo11.voteplugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/*
    Defines the behavior and attributes of all menus in our plugin
    Yeah that
    (totaly not copied btw #KodySimpson)
 */
public abstract class Menu implements InventoryHolder {

    protected Inventory inventory;

    public abstract String getMenuName();

    public abstract int getSlots();

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems(Player player);


    public void open(Player player) {

        inventory = Bukkit.createInventory(this, getSlots(), getMenuName());

        player.updateInventory();

        this.setMenuItems(player);

        player.openInventory(inventory);
    }

    public abstract void handleMenu(InventoryClickEvent e);


    @Override
    public Inventory getInventory() {
        return inventory;
    }


}