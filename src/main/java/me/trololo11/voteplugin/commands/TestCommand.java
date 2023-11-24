package me.trololo11.voteplugin.commands;


import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class TestCommand implements CommandExecutor {

    private PollsManager pollsManager;

    public TestCommand(PollsManager pollsManager){
        this.pollsManager = pollsManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        for(Poll poll : pollsManager.getAllActivePolls()){
            Utils.printPollToPlayer(player, poll);
        }

        return true;
    }
}
