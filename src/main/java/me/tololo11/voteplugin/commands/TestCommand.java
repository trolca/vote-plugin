package me.tololo11.voteplugin.commands;


import me.tololo11.voteplugin.managers.DatabaseManager;
import me.tololo11.voteplugin.utils.Poll;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

public class TestCommand implements CommandExecutor {

    private DatabaseManager databaseManager;

    public TestCommand(DatabaseManager databaseManager){
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player player)) return true;

        return true;
    }
}
