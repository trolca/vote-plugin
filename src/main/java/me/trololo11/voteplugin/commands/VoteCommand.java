package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.SQLException;

public class VoteCommand implements CommandExecutor {

    private PollsManager pollsManager;
    private DatabaseManager databaseManager;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public VoteCommand(PollsManager pollsManager, DatabaseManager databaseManager){
        this.pollsManager = pollsManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        if(args.length == 0){
            player.sendMessage(ChatColor.RED + "Please type the code of the vote!");
            return true;
        }

        String code = args[0];

        if(code.length() != 6){
            player.sendMessage(ChatColor.RED + "The code needs to be 6 characters long!");
            return true;
        }

        if(args.length == 1){
            player.sendMessage(ChatColor.RED + "Please type the number of the option!");
            return true;
        }

        int optionNum;

        try{
            optionNum = Integer.parseInt(args[1]);
        }catch (NumberFormatException e){
            player.sendMessage(ChatColor.RED + "The option number is incorrect!");
            return true;
        }

        Poll poll = pollsManager.getActivePoll(code);
        if(poll == null){
            player.sendMessage(ChatColor.RED + "The poll code doesn't exist!");
            return true;
        }
        if(optionNum > poll.getAllOptions().size() || optionNum <= 0){
            player.sendMessage(ChatColor.RED + "The poll doesn't have this option!");
            return true;
        }

        if(poll.hasVoted(player)){
            player.sendMessage(ChatColor.RED + "You've already voted for this poll!");
            return true;
        }

        //Adds the vote
        try {
            poll.getAllOptions().get(optionNum-1).addVote(player.getUniqueId(), poll, databaseManager   );
        } catch (SQLException e) {
            plugin.logger.severe("Error while voting for the poll "+ code);
            e.printStackTrace(System.out);
        }

        player.sendMessage(ChatColor.GREEN + "Successfully voted for the option "+ poll.getAllOptions().get(optionNum-1).getName());

        return true;
    }
}
