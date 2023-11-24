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

    public abstract String getMenuName(Player p);

    public abstract int getSlots();

    //let each menu decide what items are to be placed in the inventory menu
    public abstract void setMenuItems(Player p);


    public void open(Player p) {

        inventory = Bukkit.createInventory(this, getSlots(), getMenuName(p));

        this.setMenuItems(p);

        p.openInventory(inventory);
    }

    public abstract void handleMenu(InventoryClickEvent e);


    @Override
    public Inventory getInventory() {
        return inventory;
    }


}