package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This task is responsible for timing the polls and checking if they have ended.
 */
public class PollStopTask extends BukkitRunnable {

    private Poll poll;
    private PollsManager pollsManager;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public PollStopTask(PollsManager pollsManager, Poll poll){
        this.pollsManager = pollsManager;
        this.poll = poll;
    }


    @Override
    public void run() {

       try {
           pollsManager.stopPoll(poll);
       } catch (SQLException | IOException e) {
           plugin.logger.severe("Error with database while stopping the poll "+ poll.code);
           e.printStackTrace(System.out);
       }


    }
}
