package me.trololo11.voteplugin.tasks;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.managers.DatabaseManager;
import me.trololo11.voteplugin.managers.PollsManager;
import me.trololo11.voteplugin.managers.YmlDatabaseManager;
import me.trololo11.voteplugin.utils.Poll;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.IOException;
import java.sql.SQLException;

public class SynchroniseData extends BukkitRunnable {

    private PollsManager pollsManager;
    private DatabaseManager databaseManager;

    private VotePlugin plugin = VotePlugin.getPlugin();

    public SynchroniseData(PollsManager pollsManager, DatabaseManager databaseManager){
        this.pollsManager = pollsManager;
        this.databaseManager = databaseManager;
    }

    @Override
    public void run() {

        try {
            for (Poll poll : pollsManager.getPollsToUpdate()) {
                databaseManager.updatePoll(poll);
            }
            pollsManager.clearPollsToBeUpdated();

            if(databaseManager instanceof YmlDatabaseManager ymlDatabaseManager){
                ymlDatabaseManager.saveDatabase();
            }

            if(plugin.isAnnounceSaveDatabase())
                plugin.logger.info("[VotePlugin] Synchronized data to the database.");

        }catch (SQLException | IOException e){
            plugin.logger.severe("[VotePlugin] Error while synchronising data to the database!");
            e.printStackTrace(System.out);
        }

    }
}
