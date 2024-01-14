package me.trololo11.voteplugin;

import me.trololo11.voteplugin.commands.*;
import me.trololo11.voteplugin.commands.tabcompleters.SeePollTabCompleter;
import me.trololo11.voteplugin.commands.tabcompleters.ShowPollTabCompleter;
import me.trololo11.voteplugin.commands.tabcompleters.VoteTabCompleter;
import me.trololo11.voteplugin.listeners.CheckPlayerSeenPolls;
import me.trololo11.voteplugin.listeners.MenusManager;
import me.trololo11.voteplugin.listeners.PollCreateListener;
import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.MySqlDatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.managers.YmlDatabaseManager;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public final class VotePlugin extends JavaPlugin {

    public final Properties dbProperties;
    public Logger logger;

    private DatabaseManager databaseManager;
    private PollsManager pollsManager;

    public VotePlugin(){
        super();
        dbProperties = new Properties();
        dbProperties.setProperty("minimumIdle", "1");
        dbProperties.setProperty("maximumPoolSize", "4");
        dbProperties.setProperty("initializationFailTimeout", "20000");
    }

    @Override
    public void onEnable() {
        logger = Bukkit.getLogger();
        getConfig().options().copyDefaults(true);
        saveDefaultConfig();
        boolean useMySqlDatabase = getConfig().getBoolean("use-msql-database");

        if(!useMySqlDatabase) {
            File file = new File(this.getDataFolder() + "/polls-data");
            if (!file.exists()) {
                file.mkdir();
            }
        }

        try {
            databaseManager = useMySqlDatabase ? new MySqlDatabaseManager() : new YmlDatabaseManager();
            pollsManager = new PollsManager(databaseManager);
        } catch (SQLException | IOException e) {
            logger.severe("Error while connecting to the database");
            logger.severe("Make sure the info in config is accurate!");
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new MenusManager(), this);
        getServer().getPluginManager().registerEvents(new PollCreateListener(pollsManager), this);
        getServer().getPluginManager().registerEvents(new CheckPlayerSeenPolls(pollsManager), this);

        getCommand("vote").setExecutor(new VoteCommand(pollsManager, databaseManager));
        getCommand("createpoll").setExecutor(new CreatePollCommand(pollsManager, databaseManager));
        getCommand("seepolls").setExecutor(new SeePollsCommand(pollsManager));
        getCommand("seepoll").setExecutor(new SeePollCommand(pollsManager));
        getCommand("showpoll").setExecutor(new ShowPollCommand(pollsManager));

        getCommand("vote").setTabCompleter(new VoteTabCompleter());
        getCommand("seepoll").setTabCompleter(new SeePollTabCompleter());
        getCommand("showpoll").setTabCompleter(new ShowPollTabCompleter());

    }

    @Override
    public void onDisable() {

        for(Poll poll : pollsManager.getPollsToUpdate()){
            try {
                databaseManager.updatePoll(poll);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        try {
            databaseManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //We close our opened inventories on disable to disallow players to take items from them
        //on reload
        for(Player player : Bukkit.getOnlinePlayers()){
            if(player.getOpenInventory().getTopInventory().getHolder() instanceof Menu)
                player.closeInventory();
        }
    }


    public static VotePlugin getPlugin(){
        return VotePlugin.getPlugin(VotePlugin.class);
    }


}
