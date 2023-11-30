package me.trololo11.voteplugin.menus.pollcreate;

import me.trololo11.voteplugin.menus.PollCreateMenu;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class EndDateSetMenu extends Menu {

    private int days ;
    private int hours;
    private int minutes;

    private PollCreateMenu pollCreateMenu;

    public EndDateSetMenu(PollCreateMenu pollCreateMenu, int days, int hours, int minutes){
        this.pollCreateMenu = pollCreateMenu;
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    @Override
    public String getMenuName(Player p) {
        return ChatColor.GOLD + ChatColor.BOLD.toString() + "Set end time for poll";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void setMenuItems(Player p) {
        inventory.setMaxStackSize(99);
        ItemStack darkFiller = Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        ItemStack whiteFiller = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack cancel = Utils.createItem(Material.RED_DYE, "&c&lBack", "back");
        ItemStack confirm = Utils.createItem(Material.GREEN_DYE, "&a&lConfirm", "confirm");
        ItemStack customTime = Utils.createItem(Material.NAME_TAG, "&6&lCustom time set", "custom-time");
        ItemStack upTime = Utils.createItem(Material.LIME_STAINED_GLASS_PANE, "&aTime up", "time-up");
        ItemStack downTime = Utils.createItem(Material.RED_STAINED_GLASS_PANE, "&cTime down", "time-down");

        for(int i=0; i < 9; i++){
            inventory.setItem(i, darkFiller);
        }

        for(int i=9; i < getSlots(); i++){
            inventory.setItem(i, whiteFiller);
        }

        inventory.setItem(0, cancel);
        inventory.setItem(4, customTime);
        inventory.setItem(8, confirm);

        inventory.setItem(11, upTime);
        inventory.setItem(13, upTime);
        inventory.setItem(15, upTime);
        inventory.setItem(29, downTime);
        inventory.setItem(31, downTime);
        inventory.setItem(33, downTime);

        updateItemValues();


    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "back")) return;

                pollCreateMenu.open(player);
            }

            case NAME_TAG -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "custom-time")) return;

                new EndDateSetManuallyMenu(this).open(player);
            }

            case LIME_STAINED_GLASS_PANE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "time-up")) return;

                ItemStack itemUnder = inventory.getItem(e.getSlot()+9);

                switch (itemUnder.getType()){

                    case CLOCK -> days++;
                    case REDSTONE -> hours++;
                    case GHAST_TEAR -> minutes++;
                }

                checkTime();
                updateItemValues();
            }

            case RED_STAINED_GLASS_PANE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "time-down")) return;

                ItemStack itemUnder = inventory.getItem(e.getSlot()-9);

                switch (itemUnder.getType()){

                    case CLOCK -> days--;
                    case REDSTONE -> hours--;
                    case GHAST_TEAR -> minutes--;
                }

                checkTime();
                updateItemValues();
            }

            case GREEN_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "confirm")) return;

                if(days == 0 && hours == 0 && minutes == 0)
                    return;

                pollCreateMenu.setTime(days, hours, minutes);

                pollCreateMenu.open(player);
            }



        }

    }

    private void checkTime(){

        //Coll overflowing time stuff
        if(minutes >= 60){
            hours++;
            minutes = 0;
        }
        if(hours >= 24){
            days++;
            hours = 0;
        }
        if(days >= 64) days = 64;

        //making sure the times aren't below zero
        if(days < 0) days = 0;
        if(hours < 0) hours = 0;
        if(minutes < 0) minutes = 0;
    }

    private void updateItemValues(){

        ItemStack daysItem = Utils.createItem(Material.CLOCK, "&e&lDays: "+ days, "days");
        ItemStack hoursItem = Utils.createItem(Material.REDSTONE, "&c&lHours: "+ hours, "hours");
        ItemStack minutesItem = Utils.createItem(Material.GHAST_TEAR, "&f&lMinutes: "+minutes, "minutes");

        daysItem.setAmount(days == 0 ? 1 : days);
        hoursItem.setAmount(hours == 0 ? 1 : hours);
        minutesItem.setAmount(minutes == 0 ? 1 : minutes);

        inventory.setItem(20, daysItem);
        inventory.setItem(22, hoursItem);
        inventory.setItem(24, minutesItem);
    }

    public void setTime(int days, int hours, int minutes){
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
        checkTime();
    }


}
