package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.events.PollCreateEvent;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.pollcreate.EndDateSetMenu;
import me.trololo11.voteplugin.menus.pollcreate.IconSelectMenu;
import me.trololo11.voteplugin.menus.pollcreate.OptionEditMenu;
import me.trololo11.voteplugin.menus.pollcreate.TitleSetMenu;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;

/**
 * The main poll creation menu. Is responsible for allowing players to make polls with gui
 */
public class PollCreateMenu extends Menu {

    private String pollTitle = "New poll";
    private Material pollIcon = Material.PAINTING;
    private LinkedList<String> options = new LinkedList<>();
    private boolean showVotes = true;
    private PollsManager pollsManager;
    private int days, hours, minutes;

    public PollCreateMenu(PollsManager pollsManager){
        options.add("Yes");
        options.add("No");
        this.pollsManager = pollsManager;

        this.days = 0;
        this.hours = 1;
        this.minutes = 0;

    }

    @Override
    public String getMenuName(Player p) {
        return Utils.chat("&a&lCreating poll: &f"+pollTitle);
    }

    @Override
    public int getSlots() {
        return 5*9;
    }

    @Override
    public void setMenuItems(Player p) {
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack iconSelect = Utils.createItem(pollIcon, "&e&lClick to select the icon", "icon-select");
        ItemStack cancel = Utils.createItem(Material.RED_DYE, "&c&lCancel", "cancel");
        ItemStack confirm = Utils.createItem(Material.GREEN_DYE, "&a&lConfirm", "confirm");
        ItemStack showVotesButton = Utils.createItem(showVotes ? Material.GREEN_CANDLE : Material.BARRIER,
                showVotes ? "&aShow votes: &lYes&r&2/No" : "&aShow votes: &r&2Yes/&a&lNo", "show-votes");
        ItemStack titleSelect = Utils.createItem(Material.NAME_TAG, "&6&lSet title", "title-select");
        ItemStack newOption = Utils.createItem(Material.GRAY_DYE, "&7&oCreate a new option", "new-option");
        ItemStack setEndDate = Utils.createItem(Material.CLOCK, "&6&lSet end date", "end-date", "&6This polls will end in:", "&e"+days+"d"+hours+"h"+minutes+"m");
        Material existingOption = Material.OAK_SIGN;

        for(int i=0; i < 9; i++){
            inventory.setItem(i, filler);
        }

        for(int i=9; i < 36; i++){
            inventory.setItem(i, null);
        }

        for(int i=36; i < 45; i++){
            inventory.setItem(i, filler);
        }

        int extraI = 0;

        for(int i=0; i <= options.size(); i++){

            if(i % 3 == 0 && i != 0) extraI++;

            int slot =  ( (i+1) * 9 )- 27*extraI + 2 * extraI ; //sets the slot number for options


            if(i == options.size() && options.size() <= 14){
                inventory.setItem(slot, newOption);
                break;
            }else if (i == options.size())
                break;


            String option = options.get(i);
            int optionNum = i+1;

            ItemStack optionItem = Utils.createItem(existingOption,  "&8Option "+optionNum+": &f" +option, "option-"+i,
                    "&7Left click to edit!", "&7Right click to delete!");

            inventory.setItem(slot, optionItem);
        }

        inventory.setItem(0, cancel);
        inventory.setItem(2, showVotesButton);
        inventory.setItem(4, iconSelect);
        inventory.setItem(6, titleSelect);
        inventory.setItem(8, confirm);
        inventory.setItem(44, setEndDate);

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if(Utils.isLocalizedNameEqual(item.getItemMeta(), "icon-select")) {
            new IconSelectMenu(this).open(player);
            return;
        }

        switch (item.getType()){

            case GREEN_CANDLE, BARRIER -> { //Toggle show votes option
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "show-votes")) return;

                showVotes = !showVotes;
                setMenuItems(player);
            }

            //Change title of poll
            case NAME_TAG -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "title-select")) return;

                new TitleSetMenu(this).open(player);
            }

            //Options editing and deleting
            case OAK_SIGN -> {
                if(!item.getItemMeta().getLocalizedName().startsWith("option-")) return;

                byte num = Byte.parseByte(item.getItemMeta().getLocalizedName().split("-")[1]);

                if(e.getClick() == ClickType.LEFT) { //Left click edit right delete
                    new OptionEditMenu(this, options.get(num), num).open(player);
                }else if(e.getClick() == ClickType.RIGHT){
                    if(options.size() == 2){
                        player.sendMessage(ChatColor.RED + "You have to have at least 2 options!");
                        return;
                    }
                    options.remove(num);
                    setMenuItems(player);
                }
            }

            //New option create
            case GRAY_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "new-option")) return;

                options.add("New option");

                setMenuItems(player);
            }

            //exit
            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "cancel")) return;

                player.closeInventory();
            }

            //Change the end time
            case CLOCK -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "end-date")) return;

                new EndDateSetMenu(this, days, hours, minutes).open(player);
            }

            //Confirm creation
            case GREEN_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "confirm")) return;

                LinkedList<Option> pollOptions = new LinkedList<>();

                //Converts the names of the options to real objects of option class to put in to the poll
                for(int i=0; i < options.size(); i++){
                    pollOptions.add(new Option(new ArrayList<>(), options.get(i), (byte) (i+1) ));
                }

                //Converts the date stored in ints to epoch timestamp, so we can convert it
                //into a date
                long newTime = new Date().getTime();
                newTime += this.days*86400000L; //Converts days to milliseconds
                newTime += this.hours*3600000L; //Converts hours to milliseconds
                newTime += this.minutes*60000L; //Converts minutes to milliseconds


                Poll poll = new Poll(
                        Utils.generateUniqueCode(pollsManager),
                        player,
                        pollOptions,
                        pollTitle,
                        pollIcon,
                        new Date(newTime),
                        showVotes,
                        true
                );

                try {
                    pollsManager.addPoll(poll);
                    Bukkit.getServer().getPluginManager().callEvent(new PollCreateEvent(poll, player));
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

                player.closeInventory();

            }



        }

    }

    public void setTime(int days, int hours, int minutes){
        this.days = days;
        this.hours = hours;
        this.minutes = minutes;
    }

    public LinkedList<String> getOptions(){
        return options;
    }

    public void setPollTitle(String title){
        this.pollTitle = title;
    }

    public void setPollIcon(Material icon){
        this.pollIcon = icon;
    }
}
