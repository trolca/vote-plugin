package me.tololo11.voteplugin.utils;

import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Date;

/**
 * This class stores info for every vote that has been created in this plugin. <br>
 * This contains votes which are active and those which are saved for historical purpose. <br>
 * It stores data such as:
 * <ul>
 *     <li>The code of this vote. <br>
 *     <b>The code must always be 6 characters long and it should contain only numbers and english letters!
 *     (ex. 32be1c)</b></li>
 *     <li>The creator of this vote. This is stored as {@link OfflinePlayer} values</li>
 *     <li>An array list of all of the options that players can vote on in this poll</li>
 *     <li>The title of this poll</li>
 *     <li>When the poll comes to an end</li>
 *     <li>Is the poll active</li>
 * </ul>
 */
public class Poll {

    public final String code;
    public final OfflinePlayer creator;
    public final ArrayList<String> options;
    private String title;
    private Date endDate;
    private boolean isActive;

    public Poll(String code, OfflinePlayer creator, ArrayList<String> options, String title, Date endDate, boolean isActive) {

        if(code.length() != 6){
            throw new IllegalArgumentException("The code of every vote should be 6 characters long!");
        }

        this.code = code;
        this.creator = creator;
        this.options = options;
        this.title = title;
        this.endDate = endDate;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
