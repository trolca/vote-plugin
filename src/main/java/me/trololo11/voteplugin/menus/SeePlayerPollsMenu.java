package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Shows all the polls that the specified player created.
 */
public class SeePlayerPollsMenu extends Menu {

    private PollsManager pollsManager;
    private OfflinePlayer player;
    private Menu menuBack;
    private int page;

    /**
     * @param pollsManager A poll manager object
     * @param player The player that polls will be shown
     * @param menuBack The menu that shows after clicking the back button (If null then will close the inventory)
     */
    public SeePlayerPollsMenu(PollsManager pollsManager, OfflinePlayer player, @Nullable Menu menuBack){
        this.pollsManager = pollsManager;
        this.player = player;
        this.menuBack = menuBack;
        this.page = 1;
    }


    @Override
    public String getMenuName() {
        return ChatColor.GREEN + ChatColor.BOLD.toString() + "Seeing all polls of "+ player.getName();
    }

    @Override
    public int getSlots() {
        return 45;
    }

    @Override
    public void setMenuItems(Player player) {
        List<Poll> polls = pollsManager.getAllPolls().stream().filter(poll -> poll.creator.getUniqueId().equals(player.getUniqueId())).toList();
        int maxPages = (int) Math.ceil(polls.size()/27.0);
        ItemStack filler = Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        ItemStack back = Utils.createItem(Material.RED_DYE, menuBack == null ? "&c&lExit" : "&c&lBack", "back");
        ItemStack playerHead = Utils.createPlayerHead(player, "&6"+player.getName(), "player-head");
        ItemStack rightArrow = Utils.createItem(Material.ARROW, "&6Next page", "next-page");
        ItemStack leftArrow = Utils.createItem(Material.ARROW, "&6Previous page", "previous-page");
        ItemStack pageLabel = Utils.createItem(Material.STONE_BUTTON, "&b&lPage: &b"+page, "page-label");

        for(int i=0; i < 9; i++){
            inventory.setItem(i, filler);
        }

        for(int i=9; i < 36; i++){
            inventory.setItem(i, null);
        }

        for(int i=36; i < 45; i++){
            inventory.setItem(i, filler);
        }

        Utils.generatePollsFromListInInv(polls, page, inventory, player, pollsManager);

        inventory.setItem(0, back);
        inventory.setItem(8, playerHead);
        if(page < maxPages) inventory.setItem(44, rightArrow);
        if(page > 1) inventory.setItem(36, leftArrow);
        inventory.setItem(40, pageLabel);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player playerInv = (Player) e.getWhoClicked();

        assert item != null;

        if(item.getItemMeta().getLocalizedName().startsWith("poll-")){
            String code = item.getItemMeta().getLocalizedName().split("-")[1];

            Poll poll = pollsManager.getPoll(code);

            if(!pollsManager.playerSawPoll(player.getUniqueId(), poll)) {
                try {
                    pollsManager.addPlayerSawPoll(player.getUniqueId(), poll);
                } catch (SQLException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }

            new SeePollMenu(this,pollsManager, poll).open(playerInv);
        }

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isLocalizedNameEqual(item.getItemMeta(), "back")) return;;

                if(menuBack == null)
                    playerInv.closeInventory();
                else
                    menuBack.open(playerInv);
            }

            case ARROW -> {

                if(Utils.isLocalizedNameEqual(item.getItemMeta(), "next-page")){
                    page++;
                }else if(Utils.isLocalizedNameEqual(item.getItemMeta(), "previous-page")){
                    page--;
                }

                setMenuItems(playerInv);

            }

        }


    }
}
