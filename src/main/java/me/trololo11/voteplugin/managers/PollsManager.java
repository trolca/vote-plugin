package me.trololo11.voteplugin.managers;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.tasks.ChangeHistoricPoll;
import me.trololo11.voteplugin.tasks.PollStopTask;
import me.trololo11.voteplugin.tasks.RemindToVoteTask;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class manages all the polls that are used in this plugin.
 * This is the place where you should get a {@link Poll} object and
 * add them.
 * This class also stores the info about what players saw which poll.
 */
public class PollsManager {
    private LinkedHashMap<String, Poll> activePolls = new LinkedHashMap<>();
    private HashMap<String, Poll> allPolls = new HashMap<>();
    private HashMap<Poll, ArrayList<UUID> > playersPollsSeenHashMap = new HashMap<>();
    private HashMap<Poll, PollStopTask> pollsStopTasks = new HashMap<>();
    private DatabaseManager databaseManager;
    private ArrayList<Poll> historicPolls = new ArrayList<>();
    private ArrayList<Poll> allPollsList = new ArrayList<>();
    private ArrayList<Poll> pollsToUpdate = new ArrayList<>();
    private ArrayList<Poll> recentlyFinishedPolls = new ArrayList<>();

    private ArrayList<String> allCodes = new ArrayList<>();

    private VotePlugin plugin = VotePlugin.getPlugin();


    public PollsManager(DatabaseManager databaseManager) throws SQLException, IOException {
        this.databaseManager = databaseManager;
        ArrayList<Poll> allPolls = databaseManager.getAllPolls();
        Date todayDate = new Date();
        long timeNotLoad = plugin.getPollsNotLoad() == -1 ? -1 : plugin.getPollsNotLoad() * 86400000L;

        allPolls.forEach(poll -> {

            long timeDifference = todayDate.getTime() - poll.getEndDate().getTime();

            if(poll.getEndDate().getTime() > todayDate.getTime()){
                activePolls.put(poll.code, poll);
                createStopTask(poll);
            }else if( (timeNotLoad < 0 || timeDifference < timeNotLoad) && !poll.isActive){
                historicPolls.add(poll);
            }else if(timeNotLoad < 0 || timeDifference < timeNotLoad){
                try {
                    databaseManager.removeEveryPlayerSeenPoll(poll);
                    historicPolls.add(poll);
                    poll.isActive = false;
                    pollsToUpdate.add(poll);
                } catch (SQLException | IOException e) {
                    plugin.logger.severe("Error while trying to remove players seen!");
                }
            }

            try {

                if (todayDate.getTime() - poll.getEndDate().getTime() < 604800000L) {
                    playersPollsSeenHashMap.put(poll, databaseManager.playersSeenPoll(poll));
                }
            }catch (SQLException | IOException e){
                plugin.logger.severe("Error while trying to get the players which saw a poll!");
                e.printStackTrace(System.out);
            }

            allCodes.add(poll.code);


        });

        this.allPolls.putAll(activePolls);
        historicPolls.forEach(poll -> this.allPolls.put(poll.code, poll));

        allPollsList.addAll(activePolls.values());
        allPollsList.addAll(historicPolls);

    }

    /**
     * Gets all the active polls (that players can vote on).
     * @return The active polls
     */
    public Collection<Poll> getAllActivePolls(){
        return activePolls.values();
    }

    public boolean playerSawPoll(UUID uuid, Poll poll){
        return getAllPlayerSawPoll(poll).contains(uuid);
    }

    /**
     * Adds a new poll to the plugin and to the sql database.
     * @param poll The poll to add
     * @throws SQLException While there has been an error while adding the poll to the database
     */
    public void addPoll(Poll poll) throws SQLException, IOException {
        databaseManager.addPoll(poll);
        activePolls.put(poll.code,poll);
        playersPollsSeenHashMap.put(poll, new ArrayList<>());
        allCodes.add(poll.code);
        allPolls.put(poll.code, poll);
        allPollsList.add(poll);
        createStopTask(poll);
    }

    /**
     * This function creates a {@link PollStopTask} for the provided poll. <br>
     * If this poll already has a {@link PollStopTask} then it cancels the old one and creates a new one based on the end
     * time left.
     * @param poll The poll to create the task for
     */
    public void createStopTask(Poll poll){

        PollStopTask oldPollStopTask = pollsStopTasks.get(poll);
        if(oldPollStopTask != null){
            oldPollStopTask.cancel();
        }

        Date endDate = poll.getEndDate();
        long timeBetween = endDate.getTime() - new Date().getTime();
        long delay = (timeBetween/1000)*20;

        PollStopTask newPollStopTask = new PollStopTask(this, poll);
        newPollStopTask.runTaskLater(plugin, delay);

        pollsStopTasks.put(poll, newPollStopTask);

        if(poll.getPollSettings().remindVote && delay > 72000L)
            new RemindToVoteTask(poll).runTaskLater(plugin, delay-72000L);
    }

    /**
     * Add the poll to the recently finished polls list
     * and creates a {@link ChangeHistoricPoll} task and displays
     * the results of this poll to every online player
     * @param poll The poll to stop
     */
    public void stopPoll(Poll poll) throws SQLException, IOException {
        poll.isActive = false;
        activePolls.remove(poll.code);
        pollsToUpdate.add(poll);
        recentlyFinishedPolls.add(poll);
        pollsStopTasks.get(poll).cancel();
        pollsStopTasks.remove(poll);

        new ChangeHistoricPoll(this, poll).runTaskLater(plugin, 72000L);


        databaseManager.removeEveryPlayerSeenPoll(poll);

        ArrayList<UUID> playerSeen = new ArrayList<>();
        for(Player player : Bukkit.getOnlinePlayers()){
            Utils.printPollResultsToPlayer(player, poll);
            playerSeen.add(player.getUniqueId());
        }

        databaseManager.addPlayersSeenPoll(playerSeen, poll);
        playersPollsSeenHashMap.put(poll, playerSeen);

    }

    /**
     * Removes the provided poll from the recently finished polls list and
     * adds it to the historic polls
     * @param poll The poll to replace
     */
    public void replaceRecentlyFinishedPoll(Poll poll){
        recentlyFinishedPolls.remove(poll);
        historicPolls.add(poll);
    }

    /**
     * Gets the <b>active</b> poll from the specified code
     * @param code The code of the poll that you want to get
     * @return The active poll. Null if a poll with the code doesn't exist.
     */
    public Poll getActivePoll(String code){
        return activePolls.get(code);
    }

    /**
     * Gets all the UUID of players that saw the specified poll
     * @param poll The poll to check
     * @return An <b>unmodifiable</b> list UUID of the players who saw the poll
     */
    public List<UUID> getAllPlayerSawPoll(Poll poll){
        return Collections.unmodifiableList(playersPollsSeenHashMap.getOrDefault(poll, new ArrayList<>()));
    }

    /**
     * Adds a player to players that saw the specified poll
     * @param uuid The UUID of player that saw the poll
     * @param poll The poll that they saw
     * @throws SQLException On database error
     */
    public void addPlayerSawPoll(UUID uuid, Poll poll) throws SQLException, IOException {
        ArrayList<UUID> playerSeenPoll = playersPollsSeenHashMap.getOrDefault(poll, new ArrayList<>());
        if(playerSeenPoll.contains(uuid)) return;
        playerSeenPoll.add(uuid);
        databaseManager.addPlayerSeenPoll(uuid, poll);
    }

    /**
     * Adds all the players that saw the poll
     * @param uuids Ann arraylist of all the players that saw the poll
     * @param poll The poll that they saw
     * @throws SQLException On database error
     */
    public void addAllPlayersSawPoll(ArrayList<UUID> uuids, Poll poll) throws SQLException, IOException {
        playersPollsSeenHashMap.getOrDefault(poll, new ArrayList<>()).addAll(uuids);
        databaseManager.addPlayersSeenPoll(uuids, poll);
    }

    /**
     * Adds a poll to the poll to update list. This means that the poll is going to be updated
     * in the sql database when the plugin disables.
     * @param poll The poll to add
     */
    public void addPollToBeUpdated(Poll poll){
        pollsToUpdate.add(poll);
    }

    /**
     * Removes every poll from the list to be updated. <b>This should be used only when the polls were update to the database</b>
     */
    public void clearPollsToBeUpdated(){
        pollsToUpdate.clear();
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

    /**
     * Gets all the polls (active and historical)
     * @return An <b>unmodifiable</b> list aff al polls
     */
    public List<Poll> getAllPolls(){
        return Collections.unmodifiableList(allPollsList);
    }

    /**
     * Gets the poll from the provided code.
     * @param code The code to get the {@link Poll} class to
     * @return <b>ANY</b> type of poll active or unactive
     */
    public Poll getPoll(String code){
        return allPolls.get(code);
    }

    /**
     * Gets all the polls that need to get their info updated in the sql database. <br>
     * @return An array list which should only contain {@link Poll} objects that are to update (that have had their info changed)
     */
    public ArrayList<Poll> getPollsToUpdate() {
        return pollsToUpdate;
    }

    /**
     * Gets all the polls that have been recently finished (to an hour ago)
     * @return An <b>unmodifiable</b> list of all the recently finished polls
     */
    public List<Poll> getRecentlyFinishedPolls(){
        return Collections.unmodifiableList(recentlyFinishedPolls);
    }

    /**
     * Removes a poll from all the arrays in this manager and from the database.
     * @param poll The poll to remove
     * @throws SQLException On database connection error
     */
    public void removePoll(Poll poll) throws SQLException, IOException {
        if(!poll.isActive)
            throw new IllegalArgumentException("You cannot delete a non active poll!");

        allPolls.remove(poll.code);
        activePolls.remove(poll.code);
        allCodes.remove(poll.code);
        playersPollsSeenHashMap.remove(poll);
        pollsToUpdate.remove(poll);
        allPollsList.remove(poll);

        databaseManager.removePoll(poll);

    }


}
