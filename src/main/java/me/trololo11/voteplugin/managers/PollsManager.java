package me.trololo11.voteplugin.managers;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.tasks.PollCountDownTask;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

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
    private ArrayList<String> allCodes = new ArrayList<>();

    private VotePlugin plugin = VotePlugin.getPlugin();


    public PollsManager(DatabaseManager databaseManager) throws SQLException {
        this.databaseManager = databaseManager;
        ArrayList<Poll> allPolls = databaseManager.getAllPolls();
        Date todayDate = new Date();

        allPolls.forEach(poll -> {

            if(!poll.getEndDate().before(todayDate)){
                activePolls.put(poll.code, poll);
                createStopTask(poll);
            }else if(todayDate.getTime() - poll.getEndDate().getTime() < 604800000L){
                historicPolls.add(poll);
            }

            allCodes.add(poll.code);

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
        allCodes.add(poll.code);
        createStopTask(poll);
    }

    /**
     * This function creates a {@link PollCountDownTask} for the provided poll.
     * @param poll The poll to create the task for
     */
    private void createStopTask(Poll poll){

        Date endDate = poll.getEndDate();
        long timeBetween = endDate.getTime() - new Date().getTime();

        //If the time between today's date and  is more that an hour then create a countdown task
        //which counts hours but if it's not then create task which counts minutes
        if(timeBetween > 3600000L) {
            int hoursLeft = (int) Math.floor(timeBetween / 3600000.0);
            long startDelay = ((timeBetween - (hoursLeft * 3600000L)) / 1000) * 20;

            PollCountDownTask pollCountDownTask = new PollCountDownTask(this, poll, hoursLeft + 1, true);
            pollCountDownTask.runTaskTimer(plugin, startDelay, 72000L);
        }else{
            int minutesLeft = (int) Math.floor(timeBetween / 60000.0);
            long startDelay = ((timeBetween - (minutesLeft * 60000L)) / 1000) * 20;

            PollCountDownTask pollCountDownTask = new PollCountDownTask(this, poll, minutesLeft+1, false);
            pollCountDownTask.runTaskTimer(plugin, startDelay, 72000L);
        }

    }

    /**
     * Adds the poll to the historic polls arraylist and displays
     * the results of this poll to every online player
     * @param poll The poll to stop
     */
    public void stopPoll(Poll poll){
        historicPolls.add(poll);
        activePolls.remove(poll.code);

        for(Player player : Bukkit.getOnlinePlayers()){
            Utils.printPollResultsToPlayer(player, poll);
        }
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

    /**
     * Gets all codes of polls that have been used
     * @return An <b>unmodifiable</b> list off all the poll codes
     */
    public List<String> getAllPollCodes(){
        return Collections.unmodifiableList(allCodes);
    }

}
