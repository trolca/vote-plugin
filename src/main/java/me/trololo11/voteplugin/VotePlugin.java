package me.trololo11.voteplugin;

import me.trololo11.voteplugin.commands.*;
import me.trololo11.voteplugin.commands.tabcompleters.SeePollTabCompleter;
import me.trololo11.voteplugin.commands.tabcompleters.VoteTabCompleter;
import me.trololo11.voteplugin.listeners.CheckPlayerSeenPolls;
import me.trololo11.voteplugin.listeners.MenusManager;
import me.trololo11.voteplugin.listeners.PollCreateListener;
import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public final class VotePlugin extends JavaPlugin {

    public final Properties dbProperties;
    public Logger logger;

    private int timeKeepPollsLogs;
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

        timeKeepPollsLogs = getConfig().getInt("time-keep-polls-log");

        try {
            databaseManager = new DatabaseManager();
            pollsManager = new PollsManager(databaseManager);
        } catch (SQLException e) {
            logger.severe("Error while connecting to the database");
            logger.severe("Make sure the info in config is accurate!");
            throw new RuntimeException(e);
        }

        getServer().getPluginManager().registerEvents(new MenusManager(), this);
        getServer().getPluginManager().registerEvents(new PollCreateListener(pollsManager), this);
        getServer().getPluginManager().registerEvents(new CheckPlayerSeenPolls(pollsManager), this);

        getCommand("testcommand").setExecutor(new TestCommand(pollsManager));
        getCommand("vote").setExecutor(new VoteCommand(pollsManager, databaseManager));
        getCommand("createpoll").setExecutor(new CreatePollCommand(pollsManager));
        getCommand("seepolls").setExecutor(new SeePollsCommand(pollsManager));
        getCommand("seepoll").setExecutor(new SeePollCommand(pollsManager));

        getCommand("vote").setTabCompleter(new VoteTabCompleter());
        getCommand("seepoll").setTabCompleter(new SeePollTabCompleter());

    }

    @Override
    public void onDisable() {

        for(Poll poll : pollsManager.getPollsToUpdate()){
            try {
                databaseManager.updatePoll(poll);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        databaseManager.turnOffDatabase();
    }


    public static VotePlugin getPlugin(){
        return VotePlugin.getPlugin(VotePlugin.class);
    }

    public int getTimeKeepPollsLogs() {
        return timeKeepPollsLogs;
    }

}
