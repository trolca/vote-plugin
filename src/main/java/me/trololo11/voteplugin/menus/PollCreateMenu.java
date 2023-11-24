package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PollCreateMenu extends Menu {

    private String pollTitle = "New poll";
    private Material pollIcon = Material.CLOCK;

    @Override
    public String getMenuName(Player p) {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Creating poll: "+ pollTitle;
    }

    @Override
    public int getSlots() {
        return 5*9;
    }

    @Override
    public void setMenuItems(Player p) {
        ItemStack iconSelect = Utils.createItem(pollIcon, "&e&lClick to select the icon", "icon-select");
        ItemStack cancel = Utils.createItem(Material.RED_DYE, "&c&lCancel", "cancel");
        ItemStack confirm = Utils.createItem(Material.GREEN_DYE, "&a&lConfirm", "confirm");

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    public void setPollTitle(String title){
        this.pollTitle = title;
    }

    public void setPollIcon(Material icon){
        this.pollIcon = icon;
    }
}
