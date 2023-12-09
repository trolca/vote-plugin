package me.trololo11.voteplugin.utils;

import com.google.errorprone.annotations.RestrictedApi;
import me.trololo11.voteplugin.managers.DatabaseManager;

import java.sql.SQLException;
import java.util.*;

/**
 * This is an object that stores info for an option for a {@link Poll}.
 * This classes should only be used in connection with a poll. <br>
 * It stores info about:
 * <ul>
 *     <li>The name of the option</li>
 *     <li>The number of the option (stored in byte)</li>
 *     <li>An {@link ArrayList} of players UUIDs that have already voted on this option</li>
 * </ul>
 * @see Poll
 */
public class Option {

    private String name;
    private ArrayList<UUID> playersVoted;
    private byte optionNumber;


    public Option(ArrayList<UUID> playersVoted, String name, byte optionNumber) {
        this.name = name;
        this.playersVoted = playersVoted;
        this.optionNumber = optionNumber;
    }

    /**
     * Gets the name of this option
     * @return The name of this option
     */

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

    /**
     * Adds a vote for this option for the specified player.
     * It adds the uuid of the player to an internal list of all the
     * player that have voted and also uses {@link DatabaseManager#addVote(Option, Poll, UUID)}
     * to add the vote to the database
     * @param voter The uuid of the player that has voted on this option
     * @param poll The poll that the option is from
     * @param databaseManager A {@link DatabaseManager} object to synchronise the vote with the database
     * @throws SQLException On database connection error
     */
    public void addVote(UUID voter,Poll poll, DatabaseManager databaseManager) throws SQLException {
        playersVoted.add(voter);
        databaseManager.addVote(this, poll, voter);
    }


    public void removeVote(UUID uuid,Poll poll, DatabaseManager databaseManager) throws SQLException {
        playersVoted.remove(uuid);
        databaseManager.removeVote(poll, uuid);

    }

    public boolean hasVoted(UUID uuid){
        return playersVoted.contains(uuid);
    }

    public int getAmountOfVotes(){
        return playersVoted.size();
    }

    public byte getOptionNumber() {
        return optionNumber;
    }


}
