package me.tololo11.voteplugin.commands;


import me.tololo11.voteplugin.managers.DatabaseManager;
import me.tololo11.voteplugin.managers.PollsManager;
import me.tololo11.voteplugin.utils.Option;
import me.tololo11.voteplugin.utils.Poll;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;


public class TestCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)) return true;

        return true;
    }
}
