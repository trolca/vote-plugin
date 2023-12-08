package me.trololo11.voteplugin.menus.pollcreatesubmenus;

import me.trololo11.voteplugin.menus.PollCreateMenu;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class OptionEditMenu extends Menu {

    private PollCreateMenu pollCreateMenu;
    private String optionName;
    private final byte startNumber;
    private byte optionNumber;

    public OptionEditMenu(PollCreateMenu pollCreateMenu, String optionName, byte optionNumber){
        this.pollCreateMenu = pollCreateMenu;
        this.optionName = optionName;
        this.startNumber = optionNumber;
        this.optionNumber = optionNumber;
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&7Editing option: &f"+optionName);
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void setMenuItems(Player p) {
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack changeNumber = Utils.createItem(Material.STRING, "&f&lChange the order", "change-number");
        ItemStack changeTitle = Utils.createItem(Material.NAME_TAG, "&e&lChange name", "change-name");
        ItemStack cancel = Utils.createItem(Material.RED_DYE, "&c&lCancel", "cancel");
        ItemStack confirm = Utils.createItem(Material.GREEN_DYE, "&a&lConfirm", "confirm");

        for(int i=0; i < getSlots(); i++){
            inventory.setItem(i, filler);
        }

        inventory.setItem(0, cancel);
        inventory.setItem(3, changeNumber);
        inventory.setItem(5, changeTitle);
        inventory.setItem(8, confirm);

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        if(item.getItemMeta() == null) return;
        Player player = (Player) e.getWhoClicked();

        switch (item.getType()){

            //Cancel changes
            case RED_DYE ->{
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "cancel")) return;

                LinkedList<String> options = pollCreateMenu.getOptions();

                if(optionNumber != startNumber){
                    String optionHolder = options.get(optionNumber);
                    options.set(optionNumber, options.get(startNumber));
                    options.set(startNumber, optionHolder);
                }

                pollCreateMenu.open(player);
            }

            //Change option name
            case NAME_TAG -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "change-name")) return;

                new OptionNameSetMenu(this).open(player);
            }

            //Change option number
            case STRING -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "change-number")) return;

                new ChangeOptionNumMenu(this, pollCreateMenu).open(player);
            }

            //Confirm changes
            case GREEN_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "confirm")) return;
                LinkedList<String> options = pollCreateMenu.getOptions();

                if(optionNumber == startNumber){
                    options.set(optionNumber, optionName);
                }else{
                    options.set(optionNumber, optionName);
                }

                pollCreateMenu.open(player);

            }

        }
    }

    public void setOptionName(String optionName){
        this.optionName = optionName;
    }

    public String getOptionName() {
        return optionName;
    }

    public byte getOptionNumber() {
        return optionNumber;
    }

    public void setOptionNumber(byte optionNumber) {
        this.optionNumber = optionNumber;
    }
}
