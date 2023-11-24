package me.trololo11.voteplugin.utils;

import me.trololo11.voteplugin.managers.DatabaseManager;

import java.sql.SQLException;
import java.util.*;

public class Option {

    private String name;
    private ArrayList<UUID> playersVoted;
    private byte optionNumber;


    public Option(ArrayList<UUID> playersVoted, String name, byte optionNumber) {
        this.name = name;
        this.playersVoted = playersVoted;
        this.optionNumber = optionNumber;
    }


    public String getName() {
        return name;
    }

    /**
     * Gets all  the players that has voted on this option. <br>
     * @return An unmodifiable list of all the player who voted.
     */
    public List<UUID> getPlayersVoted() {
        return Collections.unmodifiableList(playersVoted);
    }

    public void addVote(UUID voter,Poll poll, DatabaseManager databaseManager) throws SQLException {
        playersVoted.add(voter);
        databaseManager.addVote(this, poll, voter);
    }

    public int getAmountOfVotes(){
        return playersVoted.size();
    }

    public byte getOptionNumber() {
        return optionNumber;
    }


}
