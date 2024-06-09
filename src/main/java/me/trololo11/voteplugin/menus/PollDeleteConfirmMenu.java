package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class PollDeleteConfirmMenu extends Menu {

    private PollsManager pollsManager;
    private Poll poll;
    private Menu menuBack;
    private SeePollMenu seePollMenu;

    private VotePlugin plugin = VotePlugin.getPlugin();

    public PollDeleteConfirmMenu(@Nullable Menu menuBack,SeePollMenu seePollMenu, PollsManager pollsManager ,Poll poll){
        this.poll = poll;
        this.pollsManager = pollsManager;
        this.menuBack = menuBack;
        this.seePollMenu = seePollMenu;

    }

    @Override
    public String getMenuName() {
        return ChatColor.RED + ChatColor.BOLD.toString() + "Are you sure you want to delete this poll?";
    }

    @Override
    public int getSlots() {
        return 9;
    }

    @Override
    public void setMenuItems(Player player) {
        ItemStack yes = Utils.createItem(Material.GREEN_DYE, "&a&lYes", "yes");
        ItemStack no = Utils.createItem(Material.RED_DYE, "&c&lNo", "no");
        ItemStack filler = Utils.createItem(Material.WHITE_STAINED_GLASS_PANE, " ", "filler");

        for(int i=0; i < getSlots(); i++){
            inventory.setItem(i, filler);
        }

        inventory.setItem(3, no);
        inventory.setItem(5, yes);
    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        switch (item.getType()){

            case RED_DYE ->{
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "no")) return;

                seePollMenu.open(player);
            }

            case GREEN_DYE -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "yes")) return;

                try {
                    pollsManager.removePoll(poll);
                } catch (SQLException | IOException ex) {
                    plugin.logger.severe("[VotePlugin] Error while removing poll "+ poll.code);
                    ex.printStackTrace(System.out);
                }

                if(player.getUniqueId().equals(poll.creator.getUniqueId())){
                    player.sendMessage(ChatColor.GREEN + "Successfully deleted poll "+ ChatColor.RESET + poll.getTitle());
                }else {
                    player.sendMessage(ChatColor.RED + "Your poll "+ ChatColor.RESET + poll.getTitle() + ChatColor.RED + " has just been deleted by an administrator!");
                }


                if(menuBack == null)
                    player.closeInventory();
                else
                    menuBack.open(player);

                for(Option option : poll.getAllOptions()){

                    for(UUID voterUUID : option.getPlayersVoted()){
                        OfflinePlayer voter = Bukkit.getOfflinePlayer(voterUUID);

                        if(voter.isOnline() && !voterUUID.equals(player.getUniqueId())){
                           voter.getPlayer().sendMessage(ChatColor.RED + "Poll "+ ChatColor.RESET + poll.getTitle() + ChatColor.RED + " that you voted in has been deleted!");
                        }

                    }

                }




            }


        }

    }
}
