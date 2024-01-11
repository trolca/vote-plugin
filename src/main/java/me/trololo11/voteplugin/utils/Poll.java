package me.trololo11.voteplugin.utils;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.DatabaseManager;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

/**
 * This class stores info for every vote that has been created in this plugin. <br>
 * This contains votes which are active and those which are saved for historical purpose. <br>
 * It stores data such as:
 * <ul>
 *     <li>The code of this vote. <br>
 *     <b>The code must always be 6 characters long and it should contain only numbers and english letters!
 *     (ex. 32be1c)</b></li>
 *     <li>The creator of this vote. This is stored as {@link OfflinePlayer} values</li>
 *     <li>An array list of all of the {@link Option} objects that players can vote on in this poll</li>
 *     <li>The title of this poll</li>
 *     <li>The icon of this poll stored in {@link Material}</li>
 *     <li>When the poll comes to an end</li>
 *     <li>An {@link PollSettings} object of the polls settings</li>
 *     <li>Is the poll active</li>
 * </ul>
 * @see Option
 */
public class Poll {

    public final String code;
    public final OfflinePlayer creator;
    private final LinkedList<Option> options;
    public boolean isActive;
    private String title;
    private Date endDate;
    private Material icon;
    private PollSettings pollSettings;
    private DatabaseManager databaseManager;

    public Poll(String code, OfflinePlayer creator, LinkedList<Option> options, String title, Material icon, Date endDate, PollSettings pollSettings, boolean isActive, DatabaseManager databaseManager) {

        if(code.length() != 6){
            throw new IllegalArgumentException("The code of every vote has to be 6 characters long!");
        }

        if(options.size() < 2){
            throw new IllegalArgumentException("There have to be at least 2 options to create a poll!");
        }

        this.code = code;
        this.creator = creator;
        this.options = options;
        this.title = title;
        this.icon = icon;
        this.endDate = endDate;
        this.pollSettings = pollSettings;
        this.isActive = isActive;
        this.databaseManager = databaseManager;
    }


    public String getTitle() {
        return title;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Material getIcon() {
        return icon;
    }

    public void setIcon(Material icon){
        this.icon = icon;
    }

    /**
     * Checks if the player has voted for this poll or not.
     * @param player The player to check
     * @return True if the player has voted false if not
     */
    public boolean hasVoted(Player player){

        for(Option option : options){
            if(option.getPlayersVoted().contains(player.getUniqueId())) return true;
        }

        return false;

    }

    /**
     * Gets the {@link Option} that the provided player voted on already.
     * @param player The player to get
     * @return An {@link Option} that player voted for. If player hasn't voted on anything then returns null
     */
    @Nullable
    public Option getOptionPlayerVoted(Player player){

        for(Option option : getAllOptions()){
            if (option.hasVoted(player.getUniqueId())) return option;
         }

        return null;

    }

    /**
     * Gets the option with the most amount of votes.
     * @return The option with the largest number of votes
     */
    public Option getWinningOption(){

        int largestVotes = 0;
        Option winningOption = getAllOptions().get(0);

        for(Option option : getAllOptions()){
            if(largestVotes < option.getAmountOfVotes()){
                largestVotes = option.getAmountOfVotes();
                winningOption = option;
            }

        }

        return winningOption;

    }

    public PollSettings getPollSettings(){
        return pollSettings;
    }

    public void setPollSettings(PollSettings pollSettings){
        this.pollSettings = pollSettings;
    }


    public String getEndDateString(){
        long timeDifference = endDate.getTime() - new Date().getTime();

        if(timeDifference <= 0) return "Already ended";

        String timeString = Utils.getStringTime(timeDifference/1000, new char[]{'d', 'h', 'm'});

        if(timeString.isBlank())
            timeString = "1m";

        return timeString;
    }

    /**
     * Gets the total amount of votes from all the options.
     * @return The total amount of votes that have been voted in this poll
     */
    public long getTotalVotes(){
        long allVotes = 0;

        for(Option option : getAllOptions()){
            allVotes += option.getAmountOfVotes();
        }

        return allVotes;
    }

    /**
     * Gets all the options of this poll
     * @return An <b>unmodifiable</b> list of all the options for this poll
     */
    public List<Option> getAllOptions(){
        return Collections.unmodifiableList(options);
    }

    /**
     * This should be only used to change the order of the poll options not the amount.
     * <b>The new list with new options shouldn't have the {@link Option#getOptionNumber()} values changed in any way.
     * This function changes it automatically and it synchronizes it with the database!</b>
     * @throws IllegalArgumentException When the list of options is different size from the original one
     * @param newOptions The list of new options. <b>It should be the same size as the original</b>
     */
    public void setOptions(LinkedList<Option> newOptions){

        if(newOptions.size() != this.options.size())
            throw new IllegalArgumentException("The new options list has to be the same size as the already existing option list");

        boolean isGood = false;

        //We do this to check if the options are sorted correctly and if they aren't then we sort them again
        //Ik it's not the most optimized way to do it, but we can have max 15 options, so it's not that big of a problem
        while (!isGood) {

            isGood = true;

            for (int i = 0; i < newOptions.size(); i++) {

                Option newOption = newOptions.get(i);

                if (newOption.getOptionNumber() - 1 != i && options.get(i).getOptionNumber() != newOption.getOptionNumber()) {

                    isGood = false;

                    Option replaceOption = newOptions.get(newOption.getOptionNumber() - 1);

                    options.set(replaceOption.getOptionNumber() - 1, newOption);
                    options.set(newOption.getOptionNumber() - 1, replaceOption);

                    byte replaceNumber = newOption.getOptionNumber();

                    try {
                        databaseManager.changeOptionNumber(newOption.getOptionNumber(), (byte) 21, this);
                        databaseManager.changeOptionNumber(replaceOption.getOptionNumber(), newOption.getOptionNumber(), this);
                        databaseManager.changeOptionNumber((byte) 21, replaceOption.getOptionNumber(), this);
                    } catch (SQLException | IOException e) {
                        VotePlugin.getPlugin().logger.severe("[VotePlugin] Error while swapping options in the database!");
                        VotePlugin.getPlugin().logger.severe("[VotePlugin] The error:");
                        e.printStackTrace(System.out);
                        return;
                    }

                    newOption.setOptionNumber(replaceOption.getOptionNumber());
                    replaceOption.setOptionNumber(replaceNumber);

                }


            }

        }


    }

    @Override
    public String toString(){
        return "poll[code="+code+",creator="+creator +
                ",title="+title+",endDate="+endDate+"]";
    }

}
