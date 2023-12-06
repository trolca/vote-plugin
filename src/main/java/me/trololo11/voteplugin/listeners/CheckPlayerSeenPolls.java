package me.trololo11.voteplugin.listeners;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * This class is responsible for managing which polls player didn't see and
 * showing them to the player. It listens for a player joining and
 * uses {@link PollsManager} to check is player have seen all the polls
 */
public class CheckPlayerSeenPolls implements Listener {

    private PollsManager pollsManager;

    public CheckPlayerSeenPolls(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) throws SQLException {
        Player player = e.getPlayer();
        ArrayList<Poll> unseenPolls = new ArrayList<>();

        for(Poll poll : pollsManager.getAllPolls()){
            List<UUID> playersSeen = pollsManager.getAllPlayerSawPoll(poll);

            if(!playersSeen.contains(player.getUniqueId())){
                unseenPolls.add(poll);
            }


        }

        if(unseenPolls.size() > 1){
            player.sendMessage(ChatColor.GREEN + "There are "+unseenPolls.size() + " polls that you haven't seen!");
            player.sendMessage(ChatColor.GREEN + "Type /seepolls to see them");
        }else if(unseenPolls.size() == 1){
            Poll poll = unseenPolls.get(0);

            if(poll.isActive)
                Utils.printPollToPlayer(player, poll);
            else
                Utils.printPollResultsToPlayer(player, poll);

            pollsManager.addPlayerSawPoll(player.getUniqueId(), poll);
        }




    }
}
