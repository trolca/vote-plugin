package me.tololo11.voteplugin.managers;

import me.tololo11.voteplugin.utils.Poll;

import java.sql.SQLException;
import java.util.*;

/**
 * This class manages all the polls that are used in this plugin.
 * This is the place where you should get a {@link Poll} object and
 * add them.
 */
public class PollsManager {
    private HashMap<String, Poll> activePolls = new HashMap<>();
    private DatabaseManager databaseManager;
    private ArrayList<Poll> historicPolls = new ArrayList<>();


    public PollsManager(DatabaseManager databaseManager) throws SQLException {
        this.databaseManager = databaseManager;
        ArrayList<Poll> allPolls = databaseManager.getAllPolls();

        allPolls.forEach(poll -> {
            if(poll.getEndDate().after(new Date())){
                historicPolls.add(poll);
            }else{
                activePolls.put(poll.code, poll);
            }
        });
    }

    /**
     * Gets all the active polls (that players can vote on).
     * @return The active polls
     */
    public Collection<Poll> getAllActivePolls(){
        return activePolls.values();
    }

    /**
     * Adds a new poll to the plugin and to the sql database.
     * @param poll The poll to add
     * @throws SQLException While there has been an error while adding the poll to the database
     */
    public void addPoll(Poll poll) throws SQLException {
        databaseManager.addPoll(poll);
        activePolls.put(poll.code,poll);
    }

    /**
     * Gets the <b>active</b> poll
     * @param code The code of the poll that you want to get
     * @return The active poll
     */
    public Poll getActivePoll(String code){
        return activePolls.get(code);
    }

    /**
     * Gets all the historic polls (That have been already finished)
     * @return An <b>unmodifiable</b> list of all the finished polls
     */
    public List<Poll> getAllHistoricPolls(){
        return Collections.unmodifiableList(historicPolls);
    }


}
