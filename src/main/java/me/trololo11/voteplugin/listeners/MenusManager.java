package me.trololo11.voteplugin.listeners;

import me.trololo11.voteplugin.utils.Menu;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

public class MenusManager implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent e){

        ItemStack item = e.getCurrentItem();
        InventoryHolder holder = e.getInventory().getHolder();
        if (holder instanceof Menu) {

            e.setCancelled(true);

            if (item == null) return;
            if (!item.hasItemMeta()) return;
            if (!item.getItemMeta().hasLocalizedName()) return;
            if(!(e.getWhoClicked() instanceof Player)){
                e.getWhoClicked().closeInventory();
                return;
            }

            Menu menu = (Menu) holder;
            menu.handleMenu(e);
        }

    }
}
