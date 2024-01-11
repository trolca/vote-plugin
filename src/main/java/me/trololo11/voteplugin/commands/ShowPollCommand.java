package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.sql.SQLException;

public class ShowPollCommand implements CommandExecutor {

    private PollsManager pollsManager;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public ShowPollCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(args.length == 0){
            sender.sendMessage(ChatColor.RED + "Type the poll code of the poll you want to show to the player");
            return true;
        }

        String code = args[0];

        Poll poll = pollsManager.getPoll(code);
        if(poll == null){
            sender.sendMessage(ChatColor.RED + "The poll doesn't exist!");
            return true;
        }

        if(args.length == 1){
            sender.sendMessage(ChatColor.RED + "Type a player you want to show the poll to!");
            return true;
        }

        if(args[1].equals("*")){

            for(Player player : Bukkit.getOnlinePlayers()){

                try {
                    pollsManager.addPlayerSawPoll(player.getUniqueId(), poll);
                } catch (SQLException | IOException e) {
                    plugin.logger.warning("[VotePlugin] Error while adding player who saw poll "+ poll.code + " to the list");
                }
                Utils.printPollToPlayer(player, poll);

            }

            return true;

        }


        Player player = Bukkit.getPlayer(args[1]);
        if(player == null){
            sender.sendMessage(ChatColor.RED + "The player isn't online!");
            return true;
        }

        try {
            pollsManager.addPlayerSawPoll(player.getUniqueId(), poll);
        } catch (SQLException | IOException e) {
            plugin.logger.warning("[VotePlugin] Error while adding player who saw poll "+ poll.code + " to the list");
        }
        Utils.printPollToPlayer(player, poll);
        sender.sendMessage(ChatColor.GREEN + "Successfully showed poll to "+player.getName());

        return true;
    }
}
