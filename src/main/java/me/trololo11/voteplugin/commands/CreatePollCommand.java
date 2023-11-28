package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.PollCreateMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatePollCommand implements CommandExecutor {

    private PollsManager pollsManager;

    public CreatePollCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        new PollCreateMenu(pollsManager).open(player);

        return true;
    }
}
