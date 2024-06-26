package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Shows all the polls if a specified filter presented by {@link PollType}
 */
public class SeePollsMenu extends Menu {

    private PollsManager pollsManager;
    private PollType pollType;
    private int page;

    /**
     * @param pollType The filter of the polls to show (The types of polls that is going to show)
     * @param pollsManager A poll manager object
     */
    public SeePollsMenu(PollType pollType, PollsManager pollsManager){
        this.pollType = pollType;
        this.pollsManager = pollsManager;
        this.page = 1;
    }

    @Override
    public String getMenuName() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Seeing polls";
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void setMenuItems(Player player) {
        List<Poll> polls = getPollsFromType(pollType);
        int maxPages = (int) Math.ceil(polls.size()/27.0);
        ItemStack darkFiller = Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        ItemStack activePolls = pollType == PollType.ACTIVE ?
                Utils.createEnchantedItem(Material.TORCH, "&e&lActive polls", "active-polls", Enchantment.MENDING) :
                Utils.createItem(Material.TORCH, "&eActive polls", "active-polls");
        ItemStack historicPolls = pollType == PollType.HISTORIC ?
                Utils.createEnchantedItem(Material.REDSTONE_TORCH, "&c&lHistoric polls", "historic-polls", Enchantment.MENDING) :
                Utils.createItem(Material.REDSTONE_TORCH, "&cHistoric polls", "historic-polls");
        ItemStack allPolls = pollType == PollType.ALL ?
                Utils.createEnchantedItem(Material.ENDER_EYE, "&a&lAll polls", "all-polls", Enchantment.MENDING) :
                Utils.createItem(Material.ENDER_EYE, "&aAll polls", "all-polls");
        ItemStack recentlyFinished = pollType == PollType.RECENTLY_FINISHED ?
                Utils.createEnchantedItem(Material.REDSTONE, "&4&lRecently finished polls", "recent-polls", Enchantment.MENDING) :
                Utils.createItem(Material.REDSTONE, "&4Recently finished polls", "recent-polls");
        ItemStack pageLabel = Utils.createItem(Material.STONE_BUTTON, "&b&lPage: &b"+page, "page-label");
        ItemStack back = Utils.createItem(Material.RED_DYE, "&c&lBack", "back");
        ItemStack rightArrow = Utils.createItem(Material.ARROW, "&6Next page", "next-page");
        ItemStack leftArrow = Utils.createItem(Material.ARROW, "&6Previous page", "previous-page");

        for(int i=0; i < 9; i++){
            inventory.setItem(i, darkFiller);
        }

        for(int i=9; i < 36; i++){
            inventory.setItem(i, null);
        }

        for(int i=36; i < 45; i++){
            inventory.setItem(i, darkFiller);
        }


        Utils.generatePollsFromListInInv(polls, page, inventory, player, pollsManager);

        inventory.setItem(0, back);
        inventory.setItem(5, activePolls);
        inventory.setItem(6, historicPolls);
        inventory.setItem(7, recentlyFinished);
        inventory.setItem(8, allPolls);
        if(page < maxPages) inventory.setItem(44, rightArrow);
        if(page > 1) inventory.setItem(36, leftArrow);
        inventory.setItem(40, pageLabel);



    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        if(Utils.getPrivateName(item).startsWith("poll-")){
            String code = Utils.getPrivateName(item).split("-")[1];

            Poll poll = pollsManager.getPoll(code);

            if(!pollsManager.playerSawPoll(player.getUniqueId(), poll)) {
                try {
                    pollsManager.addPlayerSawPoll(player.getUniqueId(), poll);
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            new SeePollMenu(this,pollsManager, poll).open(player);
        }

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "back")) return;

                player.closeInventory();
            }

            case TORCH -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "active-polls")) return;

                pollType = PollType.ACTIVE;
                page = 1;
                setMenuItems(player);
            }

            case REDSTONE_TORCH -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "historic-polls")) return;

                pollType = PollType.HISTORIC;
                page = 1;
                setMenuItems(player);
            }

            case ENDER_EYE -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "all-polls")) return;

                pollType = PollType.ALL;
                page = 1;
                setMenuItems(player);
            }

            case REDSTONE -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "recent-polls")) return;

                pollType = PollType.RECENTLY_FINISHED;
                page = 1;
                setMenuItems(player);
            }

            case ARROW -> {

                if(Utils.isPrivateNameEqual(item.getItemMeta(), "next-page")){
                    page++;
                }else if(Utils.isPrivateNameEqual(item.getItemMeta(), "previous-page")){
                    page--;
                }

                setMenuItems(player);

            }

        }
    }

    private List<Poll> getPollsFromType(PollType pollType){

        switch (pollType){

            case ACTIVE -> {
                return pollsManager.getAllActivePolls().stream().toList();
            }

            case HISTORIC -> {
                return pollsManager.getAllHistoricPolls();
            }
            case RECENTLY_FINISHED -> {
                return pollsManager.getRecentlyFinishedPolls();
            }

            default -> {
                return pollsManager.getAllPolls();
            }

        }

    }

    public enum PollType{
        ACTIVE,
        HISTORIC,
        RECENTLY_FINISHED,
        ALL
    }

}
