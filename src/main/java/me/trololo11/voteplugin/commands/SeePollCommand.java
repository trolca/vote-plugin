package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.SeePollMenu;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SeePollCommand implements CommandExecutor {

    private PollsManager pollsManager;

    public SeePollCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(args.length == 0){
            player.sendMessage(ChatColor.RED + "Type the code of the poll you want to see!");
            return true;
        }

        Poll poll = pollsManager.getPoll(args[0]);

        if(poll == null){
            player.sendMessage(ChatColor.RED + "This poll doesn't exist!");
            return true;
        }

        new SeePollMenu(null, poll).open(player);

        return true;
    }
}
