package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class RemindToVoteTask extends BukkitRunnable {

    private Poll poll;

    public RemindToVoteTask(Poll poll){
        this.poll = poll;
    }

    @Override
    public void run() {

        for(Player player : Bukkit.getOnlinePlayers()){

            if(!poll.hasVoted(player)){

                player.sendMessage(ChatColor.GREEN + "There's an hour left to vote for the poll "+ChatColor.RESET + Utils.chat(poll.getTitle()) + ChatColor.GREEN + "!");
                player.sendMessage(ChatColor.GREEN + "Type /seepoll "+poll.code + " to vote!");
            }

        }


    }
}
