package me.tololo11.voteplugin.events;

import me.tololo11.voteplugin.utils.Poll;
import me.tololo11.voteplugin.managers.PollsManager;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Gets called when a new poll has been created . <br>
 * It's called after it has been added to the database and
 * to the {@link PollsManager};
 */
public class PollCreateEvent extends Event {

    private static final HandlerList HANDLERS_LIST = new HandlerList();

    private Poll poll;
    private Player player;

    public PollCreateEvent(Poll poll, Player player){
        this.poll = poll;
        this.player = player;
    }

    /**
     * The poll created
     * @return The poll created
     */
    public Poll getPoll() {
        return poll;
    }

    /**
     * The <b>online</b> player who created this poll
     * @return The creator
     */
    public Player getPlayer() {
        return player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS_LIST;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS_LIST;
    }
}
