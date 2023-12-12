package me.trololo11.voteplugin.menus.pollcreatesubmenus;

import me.trololo11.voteplugin.menus.PollCreateMenu;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.PollSettings;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class PollSettingsMenu extends Menu {

    private PollCreateMenu pollCreateMenu;
    private PollSettings pollSettings;

    private final Material SETTING_ON = Material.LIME_DYE;
    private final Material SETTING_OFF = Material.GRAY_DYE;
    private final ChatColor COLOR_ON = ChatColor.GREEN;
    private final ChatColor COLOR_OFF = ChatColor.RED;

    public PollSettingsMenu(PollCreateMenu pollCreateMenu, PollSettings pollSettings){
        this.pollCreateMenu = pollCreateMenu;
        this.pollSettings = pollSettings;
    }

    @Override
    public String getMenuName() {
        return ChatColor.GRAY + ChatColor.BOLD.toString() + "This polls settings";
    }

    @Override
    public int getSlots() {
        return 36;
    }

    @Override
    public void setMenuItems(Player player) {
        ItemStack darkFiller = Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack back = Utils.createItem(Material.RED_DYE, "&c&lBack", "back");
        ItemStack showVotesOption = Utils.createItem(pollSettings.showVotes ? SETTING_ON : SETTING_OFF,
                ( pollSettings.showVotes ? COLOR_ON : COLOR_OFF) + "Show votes", "show-votes", "&7If turned on it's gonna show the percentage amount",
                "&7of how many people voted on each option,",
                "&7and the total amount of votes",
                "&8&o(If turned off the percentage amount", "&8&ois still gonna show when a poll finishes)");

        ItemStack showOnJoinOption = Utils.createItem(pollSettings.showOnJoin ? SETTING_ON : SETTING_OFF,
                ( pollSettings.showOnJoin ? COLOR_ON : COLOR_OFF) + "Show on join", "show-join", "&7If turned on it's gonna show this poll to players",
                "&7on join if they haven't seen it yet.");
        ItemStack changeVotesOption = Utils.createItem(pollSettings.changeVotes ? SETTING_ON : SETTING_OFF,
                ( pollSettings.changeVotes ? COLOR_ON : COLOR_OFF) + "Player can change vote", "change-vote", "&7If turned on players can change their vote to other one");
        ItemStack remindVote = Utils.createItem(pollSettings.remindVote ? SETTING_ON : SETTING_OFF,
                ( pollSettings.remindVote ? COLOR_ON : COLOR_OFF) + "Remind to vote", "remind-vote", "&7If turned on it's gonna remind all of the online players",
                "&7that haven't voted yet to vote for this poll an hour", "&7Before it finishes.");

        for(int i=0; i < 9; i++){
            inventory.setItem(i, darkFiller);
        }

        for(int i=9; i < getSlots(); i++){
            inventory.setItem(i, filler);
        }

        inventory.setItem(19, showVotesOption);
        inventory.setItem(21, showOnJoinOption);
        inventory.setItem(23, changeVotesOption);
        inventory.setItem(25, remindVote);

        inventory.setItem(0, back);

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        String localizedName = e.getCurrentItem().getItemMeta().getLocalizedName();
        Player player = (Player) e.getWhoClicked();

        if(localizedName.equalsIgnoreCase("back")){
            pollCreateMenu.open(player);
            return;
        }else if(localizedName.equalsIgnoreCase("show-votes")) {
            pollSettings.showVotes = !pollSettings.showVotes;

        }else if(localizedName.equalsIgnoreCase("show-join")){
            pollSettings.showOnJoin = !pollSettings.showOnJoin;

        }else if(localizedName.equalsIgnoreCase("change-vote")) {
            pollSettings.changeVotes = !pollSettings.changeVotes;

        }else if(localizedName.equalsIgnoreCase("remind-vote")){
            pollSettings.remindVote = !pollSettings.remindVote;

        }

        setMenuItems(player);


    }
}
