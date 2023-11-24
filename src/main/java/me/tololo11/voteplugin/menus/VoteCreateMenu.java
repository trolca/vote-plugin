package me.tololo11.voteplugin.menus;

import me.tololo11.voteplugin.utils.Menu;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

public class VoteCreateMenu extends Menu {

    private String pollTitle = "New poll";

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

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {

    }

    public void setPollTitle(String title){
        this.pollTitle = title;
    }
}
