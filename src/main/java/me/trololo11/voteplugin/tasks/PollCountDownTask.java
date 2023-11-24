package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.scheduler.BukkitRunnable;

public class PollCountDownTask extends BukkitRunnable {

    private int hours;
    private Poll poll;
    private PollsManager pollsManager;

    public PollCountDownTask(PollsManager pollsManager, Poll poll, int hours){
        this.pollsManager = pollsManager;
        this.poll = poll;
        this.hours = hours;
    }


    @Override
    public void run() {
        hours--;

        if(hours <= 0){
            pollsManager.stopPoll(poll);
            this.cancel();

        }

    }
}
