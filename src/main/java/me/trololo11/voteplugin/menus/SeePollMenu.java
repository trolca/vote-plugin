package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.managers.PollsManager;
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

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * This menu shows a gui version of a {@link Poll}
 */
public class SeePollMenu extends Menu {

    private Menu menuBack;
    private PollsManager pollsManager;
    private Poll poll;

    /**
     * @param menuBack The menu that will open after clicking the back button (If null it's going to close the inventory)
     * @param pollsManager A poll manager object
     * @param poll The poll that the gui will show
     */
    public SeePollMenu(@Nullable Menu menuBack,PollsManager pollsManager, Poll poll){
        this.menuBack = menuBack;
        this.pollsManager = pollsManager;
        this.poll = poll;
    }

    @Override
    public String getMenuName() {
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
            if(!poll.isActive || poll.getPollSettings().showVotes){
                int optionVotes = option.getAmountOfVotes();
                optionPercentageS = ChatColor.GRAY + " ("+((int) ((optionVotes/(double) allVotes )*100 ) ) + "%)";
            }

            ItemStack optionItem;

            if(isPollActive) {
                ArrayList<String> lore = new ArrayList<>();

                lore.add(Utils.chat(hasVoted ?
                        (playerVotedOnThisOption ? "&aYou voted on this option" :
                                (poll.getPollSettings().changeVotes ? "&2Click to change your vote" : "&2You've voted on an other option!"))
                        : "&eClick to vote for this option!"));
                if(poll.getPollSettings().changeVotes && !hasVoted){
                    lore.add(ChatColor.RED + "This poll doesn't");
                    lore.add(ChatColor.RED + "allow changing votes!");
                }

                optionItem = Utils.createItem(optionMaterial, "&8Option " + option.getOptionNumber() + ": &f" + option.getName() + optionPercentageS, "option-" + i, lore);

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

        if(player.getUniqueId().equals(poll.creator.getUniqueId()) && poll.isActive){
            inventory.setItem(6, Utils.createItem(Material.ORANGE_DYE, "&6&lClick here to edit poll", "edit-poll"));
        }

        if( (player.getUniqueId().equals(poll.creator.getUniqueId()) || player.hasPermission("voteplugin.deletepolls")) && poll.isActive ){
            inventory.setItem(2, Utils.createItem(Material.BARRIER, "&4&lDelete this poll", "delete-poll"));
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

        assert item != null;

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "back")) return;

                if(menuBack == null){
                    player.closeInventory();
                    return;
                }

                menuBack.open(player);
            }

            case OAK_SIGN -> {
                if(!item.getItemMeta().getLocalizedName().startsWith("option-")) return;
                if(item.containsEnchantment(Enchantment.MENDING)) return;
                if(!poll.isActive) return;

                byte optionNum = Byte.parseByte(item.getItemMeta().getLocalizedName().split("-")[1]);

                player.performCommand("vote "+poll.code+" "+ (optionNum+1));

                setMenuItems(player);

            }

            case ORANGE_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "edit-poll")) return;
                if(!player.getUniqueId().equals(poll.creator.getUniqueId())) return;
                if(!poll.isActive) return;

                new EditPollMenu(this, poll, pollsManager).open(player);
            }

            case PLAYER_HEAD -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "poll-creator")) return;

                if(menuBack instanceof SeePlayerPollsMenu)
                    return;

                new SeePlayerPollsMenu(pollsManager, poll.creator, this).open(player);

            }

            case BARRIER -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "delete-poll")) return;
                if(!player.getUniqueId().equals(poll.creator.getUniqueId()) && !player.hasPermission("voteplugin.deletepolls")) return;
                if(!poll.isActive) return;

                new PollDeleteConfirmMenu(menuBack,this, pollsManager, poll).open(player);

            }

        }
    }
}
