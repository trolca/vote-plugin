package me.trololo11.voteplugin.commands;


import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
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

    private PollsManager pollsManager;

    public TestCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)) return true;

        return true;
    }
}
