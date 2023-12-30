package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.PollCreateMenu;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePollCommand implements CommandExecutor {

    private PollsManager pollsManager;
    private DatabaseManager databaseManager;

    public CreatePollCommand(PollsManager pollsManager, DatabaseManager databaseManager){
        this.pollsManager = pollsManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(!player.hasPermission("voteplugin.createpolls")){
            player.sendMessage(ChatColor.RED + "You don't have the permission to use this command!");
            return true;
        }

        new PollCreateMenu(pollsManager, databaseManager).open(player);

        return true;
    }
}
