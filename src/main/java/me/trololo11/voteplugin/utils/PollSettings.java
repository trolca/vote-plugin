package me.trololo11.voteplugin.utils;

public class PollSettings {

    public boolean showVotes;
    public boolean showOnJoin;
    public boolean changeVotes;
    public boolean remindVote;

    public PollSettings(boolean showVotes, boolean showOnJoin, boolean changeVotes, boolean remindVote) {
        this.showVotes = showVotes;
        this.showOnJoin = showOnJoin;
        this.changeVotes = changeVotes;
        this.remindVote = remindVote;
    }
}
