package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.pollcreatesubmenus.EndDateSetMenu;
import me.trololo11.voteplugin.menus.pollcreatesubmenus.IconSelectMenu;
import me.trololo11.voteplugin.menus.pollcreatesubmenus.PollSettingsMenu;
import me.trololo11.voteplugin.utils.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;

public class EditPollMenu extends Menu {

    private Poll poll;
    private SeePollMenu seePollMenu;
    private PollsManager pollsManager;

    private int days;
    private int hours;
    private int minutes;
    private Material newPollIcon;
    private PollSettings newPollSettings;
    private LinkedList<Option> newOptions;

    private HashMap<Option, Byte> newOptionNumbers = new HashMap<>();

    private boolean isEditingOption = false;
    private Option editingOption = null;

    public EditPollMenu(@Nullable SeePollMenu seePollMenu, Poll poll, PollsManager pollsManager){
        this.poll = poll;
        this.seePollMenu = seePollMenu;
        this.pollsManager = pollsManager;
        this.newOptions = new LinkedList<>(poll.getAllOptions());

        long endTime = poll.getEndDate().getTime() - new Date().getTime();

        days = (int) (endTime/86400000L);
        endTime -= days*86400000L;
        hours = (int) (endTime/3600000L);
        endTime -= hours*3600000L;
        minutes = (int) (endTime/60000L);



        this.newPollIcon = poll.getIcon();
        this.newPollSettings = poll.getPollSettings().clone();
    }

    @Override
    public String getMenuName() {
        return ChatColor.GOLD + ChatColor.BOLD.toString() + "Editing poll: "+ ChatColor.RESET + Utils.chat(poll.getTitle());
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void setMenuItems(Player player) {
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack back = Utils.createItem(Material.RED_DYE, "&c&lCancel", "back");
        ItemStack pollSettingsItem = Utils.createItem(Material.LIGHT_GRAY_DYE, "&7&lEdit settings", "settings");
        ItemStack editIcon = Utils.createItem(poll.getIcon(), "&e&lEdit the icon", "icon-edit");
        ItemStack setEndDate = Utils.createItem(Material.CLOCK, "&6&lEdit end date", "end-date", "&6This polls will end in:",
                "&e" + poll.getEndDateString());
        ItemStack confirm = Utils.createItem(Material.GREEN_DYE, "&a&lConfirm", "confirm");
        Material optionsMaterial = Material.OAK_SIGN;

        for(int i=0; i < 9; i++){
            inventory.setItem(i, filler);
        }

        for(int i=36; i < 45; i++){
            inventory.setItem(i, filler);
        }


        int extraI = 0;

        for(int i=0; i < newOptions.size(); i++){

            if(i % 3 == 0 && i != 0) extraI++;

            int slot =  ( (i+1) * 9 )- 27*extraI + 2 * extraI ; //sets the slot number for options

            Option option = newOptions.get(i);

            ItemStack optionItem;

            if(!isEditingOption || editingOption != option){
                optionItem =  Utils.createItem(optionsMaterial,  "&8Option "+(i+1)+": &f" +option.getName(), "option-"+i,
                        "&7Left click to change the order!");
            }else{
                optionItem = Utils.createEnchantedItem(optionsMaterial, "&6&lSelected option "+(i+1)+": &f" +option.getName(), "selected-option-"+i,
                        Enchantment.MENDING, "&7Click another option to swap this with", "&7Click this option again to cancel");
            }

            inventory.setItem(slot, optionItem);
        }

        inventory.setItem(0, back);
        inventory.setItem(8, confirm);
        inventory.setItem(2, pollSettingsItem);
        inventory.setItem(4, editIcon);
        inventory.setItem(6, setEndDate);


    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if(Utils.isLocalizedNameEqual(item.getItemMeta(), "icon-edit")){
            new IconSelectMenu(this).open(player);
        }

        switch (item.getType()){

            //exit
            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "back")) return;

                seePollMenu.open(player);
            }
            //settings menu
            case LIGHT_GRAY_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "settings")) return;

                new PollSettingsMenu(this, newPollSettings).open(player);
            }

            //end time set
            case CLOCK ->{
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "end-date")) return;

                new EndDateSetMenu(this, days, hours, minutes).open(player);
            }

            //confirm
            case GREEN_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "confirm")) return;

                poll.setPollSettings(newPollSettings);
                poll.setIcon(newPollIcon);
                Date newEndDate = new Date(new Date().getTime() + Utils.convertTimeToMills(days, hours, minutes));
                poll.setEndDate(newEndDate);
                poll.setOptions(newOptions);

                pollsManager.createStopTask(poll);
                pollsManager.addPollToBeUpdated(poll);

                if(seePollMenu != null)
                    seePollMenu.open(player);
                else
                    player.closeInventory();

                player.sendMessage(ChatColor.GREEN + "Successfully edited poll "+ ChatColor.RESET + poll.getTitle());
            }

            //logic of swapping options
            case OAK_SIGN -> {
                String localizedName = item.getItemMeta().getLocalizedName();
                if(item.getItemMeta().getLocalizedName().startsWith("option-")){

                    byte optionNum = Byte.parseByte(localizedName.split("-")[1]);

                    isEditingOption = !isEditingOption;

                    if(editingOption == null){
                        editingOption = newOptions.get(optionNum);
                    }else{
                        Option holdOption = newOptions.get(optionNum);

                        byte editingOptionIndex = (byte) newOptions.indexOf(editingOption);

                        newOptions.set(optionNum, editingOption);
                        newOptions.set(editingOptionIndex, holdOption);

                        editingOption = null;
                    }

                    setMenuItems(player);

                }else if(item.getItemMeta().getLocalizedName().startsWith("selected-option-")){
                    //If the player clicks the already selected option it clears the selection

                    isEditingOption = false;
                    editingOption = null;

                    setMenuItems(player);

                }


            }

        }

    }

    public void setIcon(Material icon){
        this.newPollIcon = icon;
    }

    public void setTime(int days, int hours, int minutes){
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }


}
