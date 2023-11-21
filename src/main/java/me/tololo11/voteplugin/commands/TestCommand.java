package me.tololo11.voteplugin.commands;


import me.tololo11.voteplugin.managers.DatabaseManager;
import me.tololo11.voteplugin.managers.PollsManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TestCommand implements CommandExecutor {



    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)) return true;

        return true;
    }
}
