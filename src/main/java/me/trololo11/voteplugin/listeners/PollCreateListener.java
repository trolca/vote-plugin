package me.trololo11.voteplugin.listeners;

import me.trololo11.voteplugin.events.PollCreateEvent;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Listens for poll creation
 */
public class PollCreateListener implements Listener {

    private PollsManager pollsManager;

    public PollCreateListener(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @EventHandler
    public void onCreate(PollCreateEvent e) throws SQLException {

        ArrayList<UUID> playersSeen = new ArrayList<>();

        for(Player player : Bukkit.getOnlinePlayers()){
            Utils.printPollToPlayer(player, e.getPoll());
            playersSeen.add(player.getUniqueId());
        }

        pollsManager.addAllPlayersSawPoll(playersSeen, e.getPoll());

    }


}
