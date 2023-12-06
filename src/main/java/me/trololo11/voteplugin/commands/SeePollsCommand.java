package me.trololo11.voteplugin.commands;

import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.menus.SeePollsMenu;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SeePollsCommand implements CommandExecutor {

    private PollsManager pollsManager;
    private DatabaseManager databaseManager;

    public SeePollsCommand(PollsManager pollsManager, DatabaseManager databaseManager){
        this.pollsManager = pollsManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(!(sender instanceof Player)) return true;

        Player player = (Player) sender;

        new SeePollsMenu(SeePollsMenu.PollType.ACTIVE, pollsManager, databaseManager).open(player);

        return true;
    }
}
