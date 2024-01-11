package me.trololo11.voteplugin.managers;

import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public interface DatabaseManager {

    /**
     * Adds a poll info to the sql databse
     * @param poll The poll to add
     */
     void addPoll(Poll poll) throws SQLException, IOException;

    /**
     * Gets all the stored polls from the sql database
     * @return An {@link ArrayList} of all of the {@link Poll}s.
     */
     ArrayList<Poll> getAllPolls() throws SQLException, IOException;

    /**
     * Updates the modifiable data of the poll saved in the polls table in database
     * @param poll The poll you want to update with new values
     */
     void updatePoll(Poll poll) throws SQLException, IOException;

    /**
     * Adds a vote for the player for the specified option in the
     * sql database
     * @param option The option you want to add the vote to
     * @param poll The poll where the option is present
     * @param voter The uuid of the player that voted on this option
     */
     void addVote(Option option, Poll poll, UUID voter) throws SQLException, IOException;

    /**
     * Removes every record of player voted in the players_voted table
     * @param poll The poll to remove the vote from
     * @param voter The player to remove the vote data from
     * @param option The option to remove the option to
     */
     void removeVote(Poll poll, Option option, UUID voter) throws SQLException, IOException;

    /**
     * Gets all the players that already saw the specified poll
     * (Aka the poll was printed for them on chat).
     * @param poll The poll to check who saw
     * @return An ArrayList of UUID of all the players that saw the poll
     */
     ArrayList<UUID> playersSeenPoll(Poll poll) throws SQLException, IOException;

    /**
     * Removes every record of players that saw the specified poll from the database. <br>
     * This should be only used when the poll ends and players need to see the results of the poll.
     * @param poll The poll to remove the players that saw
     */
     void removeEveryPlayerSeenPoll(Poll poll) throws SQLException, IOException;

    /**
     * Adds all the uuid of players from the list to the players that already saw the
     * specified poll.
     * @param listUuid An ArrayList of UUID of all the players that have already seen the poll
     * @param poll The poll that players saw
     */
     void addPlayersSeenPoll(ArrayList<UUID> listUuid, Poll poll) throws SQLException, IOException;

    /**
     * Adds the uuid of the player to the database that have already seen the specified poll.
     * @param uuid The uuid of the player
     * @param poll The poll that they saw
     */
     void addPlayerSeenPoll(UUID uuid, Poll poll) throws SQLException, IOException;

    /**
     * Changes the number of an option in the database to a different one
     * @param oldNumber The old number of an option to change
     * @param newNumber The new number of option to change to option to
     * @param poll The poll that the option is from
     */
     void changeOptionNumber(byte oldNumber, byte newNumber, Poll poll) throws SQLException, IOException;

    /**
     * Removes the specified poll from the database and everything connected to it in the database.
     * @param poll The poll to remove
     */
     void removePoll(Poll poll) throws SQLException, IOException;

     void close() throws IOException;
}
