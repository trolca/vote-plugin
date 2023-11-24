package me.trololo11.voteplugin.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.entity.Player;


public class Utils {

    public static String chat(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public static void printPollToPlayer(Player player, Poll poll){

        player.sendMessage(ChatColor.GREEN + poll.creator.getName() + " has just created a new poll!");
        player.sendMessage(ChatColor.GRAY + "--------------------------------------");
        player.sendMessage(Utils.chat(poll.getTitle()));
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Options:");

        for(int i=0; i < poll.getAllOptions().size(); i++){
            Option option = poll.getAllOptions().get(i);

            TextComponent optionText = new TextComponent(ChatColor.GRAY.toString() + (i+1) + " - "+ChatColor.RESET+ Utils.chat(option.getName()));
            optionText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote "+poll.code + " " + (i+1) ));
            optionText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to vote!")));

            player.spigot().sendMessage(optionText);
        }

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GRAY + "Ends in: "+ getStringTime(poll.getEndDate().getTime()/1000));
        player.sendMessage(ChatColor.GRAY + "--------------------------------------");
        player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "(Click the option or do /vote "+poll.code+" <option> to vote)");


    }


    /**
     * Returns a time in string based type. <br>
     * For ex. "1h3m3s"
     * @param time The time to format to string. <b>The time must be provided in seconds</b>
     * @return A formated string based from the time param. <br>
     * For a param 63 it would return "1m3s"
     */
    public static String getStringTime(long time){
        long days = time/85400;
        long hours = (time/3600)-(days*24);
        long minutes = (time/60)-(hours*60);
        long seconds = time-(minutes*60);
        StringBuilder stringBuilder = new StringBuilder();

        if(days != 0) stringBuilder.append(days+"d");
        if(hours != 0) stringBuilder.append(hours+"h");
        if(minutes != 0) stringBuilder.append(minutes+"m");
        if(seconds != 0) stringBuilder.append(seconds+"s");

        return stringBuilder.toString();

    }

}
