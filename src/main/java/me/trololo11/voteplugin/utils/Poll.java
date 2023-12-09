package me.trololo11.voteplugin.utils;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
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
 *     <li>Does the poll show the amount of votes</li>
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

    public Poll(String code, OfflinePlayer creator, LinkedList<Option> options, String title, Material icon, Date endDate, PollSettings pollSettings, boolean isActive) {

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
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public List<Option> getAllOptions(){
        return Collections.unmodifiableList(options);
    }

    @Override
    public String toString(){
        return "poll[code="+code+",creator="+creator +
                ",title="+title+",endDate="+endDate+"]";
    }

}
