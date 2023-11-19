package me.tololo11.voteplugin;

import me.tololo11.voteplugin.commands.TestCommand;
import me.tololo11.voteplugin.managers.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;

public final class VotePlugin extends JavaPlugin {

    public final Properties dbProperties;
    public Logger logger;

    private DatabaseManager databaseManager;

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

        try {
            databaseManager = new DatabaseManager();
        } catch (SQLException e) {
            logger.severe("Error while connecting to the database");
            logger.severe("Make sure the info in config is accurate!");
            throw new RuntimeException(e);
        }

        getCommand("testcommand").setExecutor(new TestCommand(databaseManager));

    }

    @Override
    public void onDisable() {
        databaseManager.turnOffDatabase();
    }


    public static VotePlugin getPlugin(){
        return VotePlugin.getPlugin(VotePlugin.class);
    }

}
