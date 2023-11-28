package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This task is responsible for timing the polls and checking if they have ended.
 *
 */
public class PollCountDownTask extends BukkitRunnable {

    private int time;
    private Poll poll;
    private PollsManager pollsManager;
    private boolean countMinutes;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public PollCountDownTask(PollsManager pollsManager, Poll poll, int time, boolean countMinutes){
        this.pollsManager = pollsManager;
        this.poll = poll;
        this.time = time;
        this.countMinutes = countMinutes;
    }


    @Override
    public void run() {
        time--;

        //If the time is 0, and we are counting minutes (so the smallest amount possible) stop the poll
        if(time <= 0 && countMinutes){
            pollsManager.stopPoll(poll);
            this.cancel();
        }else if(time <= 1){
            new PollCountDownTask(pollsManager, poll, 60, true).runTaskTimer(plugin, 1200L, 1200L);
            this.cancel();
        }

    }
}
