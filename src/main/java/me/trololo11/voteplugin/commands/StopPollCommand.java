package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public class StopPollCommand implements CommandExecutor {

    private PollsManager pollsManager;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public StopPollCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!sender.hasPermission("voteplugin.stoppolls")){
            sender.sendMessage(ChatColor.RED + "You don't have the permission for this command!");
        }

        if(args.length == 0){
            sender.sendMessage(ChatColor.RED + "Type the code for the poll you want to finish!");
            return true;
        }

        String code = args[0];
        Poll poll = pollsManager.getActivePoll(code);
        if(poll == null){
            sender.sendMessage(ChatColor.RED + "This poll doesn't exist!");
            return true;
        }

        try {
            pollsManager.stopPoll(poll);
        } catch (SQLException | IOException e) {
            plugin.logger.severe("[VotePlugin] Error while stopping the poll with code "+ poll.code + " with command!");
            e.printStackTrace(System.out);
            return false;
        }

        sender.sendMessage(ChatColor.GREEN + "Successfully finished poll "+ ChatColor.RESET + poll.getTitle());
        return true;
    }
}
