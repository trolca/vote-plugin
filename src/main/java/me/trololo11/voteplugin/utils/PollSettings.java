package me.trololo11.voteplugin.utils;

/**
 * This class contains all  the settings of a poll
 */
public class PollSettings implements Cloneable {

    /**
     * If show amount of votes when the poll isn't finished
     */
    public boolean showVotes;
    /**
     * If print the poll to a player when they haven't seen it on join
     */
    public boolean showOnJoin;
    /**
     * If player can change their vote after voting.
     */
    public boolean changeVotes;
    /**
     * If it's going to remind players to vote hours before the poll finishes.
     */
    public boolean remindVote;

    public PollSettings(boolean showVotes, boolean showOnJoin, boolean changeVotes, boolean remindVote) {
        this.showVotes = showVotes;
        this.showOnJoin = showOnJoin;
        this.changeVotes = changeVotes;
        this.remindVote = remindVote;
    }
    
    public PollSettings clone(){
        try {
            return (PollSettings) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }

    }
}
