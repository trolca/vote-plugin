package me.trololo11.voteplugin.listeners;

import me.trololo11.voteplugin.events.PollCreateEvent;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PollCreateListener implements Listener {

    @EventHandler
    public void onCreate(PollCreateEvent e){

        for(Player player : Bukkit.getOnlinePlayers()){
            Utils.printPollToPlayer(player, e.getPoll());
        }

    }


}
