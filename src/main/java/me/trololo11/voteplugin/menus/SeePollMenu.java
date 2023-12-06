package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.sql.SQLException;
import java.util.List;

public class SeePollMenu extends Menu {

    private SeePollsMenu seePollsMenu;
    private DatabaseManager databaseManager;
    private Poll poll;

    public SeePollMenu(SeePollsMenu seePollsMenu, Poll poll, DatabaseManager databaseManager){
        this.seePollsMenu = seePollsMenu;
        this.poll = poll;
        this.databaseManager = databaseManager;
    }

    @Override
    public String getMenuName(Player player) {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Poll: "+ Utils.chat("&r"+poll.getTitle());
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void setMenuItems(Player player) {
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");
        ItemStack back = Utils.createItem(Material.RED_DYE, "&c&lBack", "back");
        ItemStack pollIcon = Utils.createItem(poll.getIcon(), "&aPoll icon", "poll-icon");
        ItemStack playerIcon = Utils.createPlayerHead(poll.creator, "&9Poll creator: "+ poll.creator.getName(), "poll-creator");
        ItemStack pollCode = Utils.createItem(Material.NAME_TAG, "&6Poll code: "+ poll.code, "poll-code");
        ItemStack pollEndTime = Utils.createItem(Material.CLOCK, "&eEnds in: "+ poll.getEndDateString(), "end-time");
        Material optionMaterial = Material.OAK_SIGN;

        List<Option> options = poll.getAllOptions();

        int extraI = 0;
        boolean hasVoted = poll.hasVoted(player);
        boolean isPollActive = poll.isActive;
        long allVotes = poll.getTotalVotes();
        Option winningOption = isPollActive ? null : poll.getWinningOption();

        for(int i=0; i < 9; i++){
            inventory.setItem(i, filler);
        }

        for(int i=36; i < 45; i++){
            inventory.setItem(i, filler);
        }

        for(int i=0; i < options.size(); i++){

            if(i % 3 == 0 && i != 0) extraI++;

            int slot =  ( (i+1) * 9 )- 27*extraI + 2 * extraI ; //sets the slot number for options

            Option option = options.get(i);
            boolean playerVotedOnThisOption = hasVoted && option.getPlayersVoted().contains(player.getUniqueId());

            String optionPercentageS = "";
            if(!poll.isActive || poll.showVotes){
                int optionVotes = option.getAmountOfVotes();
                optionPercentageS = ChatColor.GRAY + " ("+((int) ( (double) optionVotes/allVotes )*100) + "%)";
            }

            ItemStack optionItem;

            if(isPollActive) {
                optionItem = Utils.createItem(optionMaterial, "&8Option " + option.getOptionNumber() + ": &f" + option.getName() + optionPercentageS, "option-" + i,
                        hasVoted ? playerVotedOnThisOption ? "&aYou voted on this option" : "&2You've voted on an other option!"
                                : "&eClick to vote for this option!");
            }else{
                optionItem = option.getAmountOfVotes() == winningOption.getAmountOfVotes() ? //Is a winning option
                        Utils.createItem(Material.YELLOW_DYE, "&6&lOption " + option.getOptionNumber() + ": " + option.getName() + optionPercentageS, "option-" + i,
                                "&eThis option won!") :
                        //This option lost
                        Utils.createItem(optionMaterial, "&8Option " + option.getOptionNumber() + ": &f" + option.getName() + optionPercentageS, "option-" + i,
                                playerVotedOnThisOption ? "&aYou voted on this option" : "&2You've voted on another option!" ,"&7&o(This option didn't win!)");

            }

            if(playerVotedOnThisOption)
                optionItem.addUnsafeEnchantment(Enchantment.MENDING, 1);

            inventory.setItem(slot, optionItem);
        }

        inventory.setItem(0, back);
        inventory.setItem(4, pollIcon);
        inventory.setItem(8, playerIcon);
        inventory.setItem(36, pollCode);
        inventory.setItem(44, pollEndTime);

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "back")) return;

                seePollsMenu.open(player);
            }

            case OAK_SIGN -> {
                if(!item.getItemMeta().getLocalizedName().startsWith("option-")) return;

                if (poll.hasVoted(player)) {
                    player.sendMessage(ChatColor.RED + "You've already voted!");
                    return;
                }

                byte optionNum = Byte.parseByte(item.getItemMeta().getLocalizedName().split("-")[1]);
                Option option = poll.getAllOptions().get(optionNum);

                try {
                    option.addVote(player.getUniqueId(), poll, databaseManager);
                    player.sendMessage(ChatColor.GREEN + "Successfully voted!");
                    setMenuItems(player);
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }

            }

        }
    }
}
