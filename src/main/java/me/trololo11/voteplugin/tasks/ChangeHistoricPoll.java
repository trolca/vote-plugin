package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * This class changes an already finished poll from active list to the historic list.
 * We do this because we want player to see for an hours the results of a poll in the active tab.
 */
public class ChangeHistoricPoll extends BukkitRunnable {

    private PollsManager pollsManager;
    private Poll poll;

    public ChangeHistoricPoll(PollsManager pollsManager, Poll poll){
        this.pollsManager = pollsManager;
        this.poll = poll;
    }

    @Override
    public void run() {
        pollsManager.replaceRecentlyFinishedPoll(poll);
    }
}
