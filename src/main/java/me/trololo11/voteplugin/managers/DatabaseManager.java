package me.trololo11.voteplugin.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.PollSettings;
import org.bukkit.Bukkit;
import org.bukkit.Material;

import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * This class is responsible for connection and modifying the sql database.
 * This class should be used for adding or updating values in the database
 */
public class DatabaseManager {

    private VotePlugin plugin = VotePlugin.getPlugin();
    private HikariDataSource ds;

    public DatabaseManager() throws SQLException {
        initialize();
    }

    /**
     * Gets the connection to the sql database. <br>
     * Also if the main {@link com.zaxxer.hikari.HikariDataSource} is null it creates a new one
     * @return The connection to the SQL database
     * @throws SQLException If there was an error to connect to the sql database
     */
    private Connection getConnection() throws SQLException {
        if (ds != null) return ds.getConnection();

        String host = plugin.getConfig().getString("host");
        String port = plugin.getConfig().getString("port");
        String url = "jdbc:mysql://"+host+":"+port;
        String user = plugin.getConfig().getString("user");
        String password = plugin.getConfig().getString("password");
        String databaseName = plugin.getConfig().getString("database-name");

        if(databaseName == null || databaseName.isEmpty()){
            databaseName = "vote_plugin_database";
        }


        Connection databaseCheck = DriverManager.getConnection(url, user, password);

        Statement databaseStatement = databaseCheck.createStatement();
        databaseStatement.execute("CREATE DATABASE IF NOT EXISTS "+databaseName);
        databaseStatement.close();

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(url + "/" + databaseName);
        config.setUsername(user);
        config.setPassword(password);
        config.setDataSourceProperties(plugin.dbProperties);
        ds = new HikariDataSource(config);

        databaseCheck.close();

        return ds.getConnection();
    }

    /**
     * Creates all the tables and their connections
     * @throws SQLException On database error
     */
    private void initialize() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        statement.execute("CREATE TABLE IF NOT EXISTS polls(code char(6) primary key unique not null, " +
                "creator char(36) not null, title text,icon varchar(50) not null, end_date bigint not null, active bool not null);");

        statement.executeUpdate("CREATE TABLE IF NOT EXISTS polls_options(code char(6) not null, option_num tinyint not null, name varchar(50), primary key(code, option_num) );");
        statement.execute("CREATE TABLE IF NOT EXISTS players_voted(uuid char(36) not null, poll_code char(6) not null, vote_option tinyint not null, primary key(uuid, poll_code))");
        statement.execute("CREATE TABLE IF NOT EXISTS players_seen_poll(uuid char(36) not null, poll_code char(6) not null," +
                "PRIMARY KEY(uuid, poll_code), FOREIGN KEY (poll_code) REFERENCES polls(code))");
        statement.execute("CREATE TABLE IF NOT EXISTS poll_settings(poll_code char(6) primary key not null, show_votes bool, show_on_join bool, change_votes bool, remind_votes bool," +
                "FOREIGN KEY (poll_code) REFERENCES polls(code))");

        statement.execute("CREATE TABLE IF NOT EXISTS has_created_keys(has bool)");

        ResultSet foreignCheck = statement.executeQuery("SELECT * FROM has_created_keys");

        if(!foreignCheck.next()){
            statement.execute("ALTER TABLE polls_options ADD INDEX `option_num_index` (option_num);");
            statement.execute("ALTER TABLE players_voted ADD INDEX `vote_option_index` (vote_option);");
            statement.execute("ALTER TABLE polls_options ADD CONSTRAINT `vote_options_con` FOREIGN KEY (code) REFERENCES polls(code);");
            statement.execute("ALTER TABLE players_voted ADD CONSTRAINT `fk_player_vote` FOREIGN KEY (vote_option) REFERENCES polls_options(option_num);");
            statement.execute("ALTER TABLE players_voted ADD CONSTRAINT `fk_player_vote_code` FOREIGN KEY (poll_code) REFERENCES polls(code);");
            statement.execute("INSERT INTO has_created_keys VALUES(true)");
        }

        statement.close();
        connection.close();
    }

    /**
     * Closes the connection to the database.<br>
     * <b>USE IT WHEN THE PLUGIN IS DISABLING TO PREVENT ERRORS</b>
     */
    public void turnOffDatabase(){
        ds.close();
    }

    /**
     * Adds a poll info to the sql databse
     * @param poll The poll to add
     * @throws SQLException On database error
     */
    public void addPoll(Poll poll) throws SQLException {
        Connection connection = getConnection();

        String sql = "INSERT INTO polls VALUES (?,?,?,?,?,?)";

        PreparedStatement voteDataStatement = connection.prepareStatement(sql);

        voteDataStatement.setString(1, poll.code);
        voteDataStatement.setString(2, poll.creator.getUniqueId().toString());
        voteDataStatement.setString(3, poll.getTitle());
        voteDataStatement.setString(4, poll.getIcon().toString());
        voteDataStatement.setLong(5, poll.getEndDate().getTime());
        voteDataStatement.setBoolean(6, poll.isActive);

        voteDataStatement.executeUpdate();

        List<Option> options = poll.getAllOptions();
        StringBuilder optionsSql = new StringBuilder("INSERT INTO polls_options VALUES ");

        for(int i=0; i < options.size(); i++){
            Option option = options.get(i);
            optionsSql.append("(\"").append(poll.code).append("\",").append(i + 1).append(",\"").append(option.getName()).append("\"),");
        }

        sql = optionsSql.substring(0, optionsSql.length()-1);

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.executeUpdate();

        PollSettings pollSettings = poll.getPollSettings();

        PreparedStatement pollSettingsStatement = connection.prepareStatement("INSERT INTO poll_settings VALUES (?,?,?,?,?)");
        pollSettingsStatement.setString(1, poll.code);
        pollSettingsStatement.setBoolean(2, pollSettings.showVotes);
        pollSettingsStatement.setBoolean(3, pollSettings.showOnJoin);
        pollSettingsStatement.setBoolean(4, pollSettings.changeVotes);
        pollSettingsStatement.setBoolean(5, pollSettings.remindVote);

        pollSettingsStatement.executeUpdate();

        pollSettingsStatement.close();
        voteDataStatement.close();
        statement.close();
        connection.close();
    }

    /**
     * Gets all the stored polls from the sql database
     * @return An {@link ArrayList} of all of the {@link Poll}s.
     * @throws SQLException On database error
     */
    public ArrayList<Poll> getAllPolls() throws SQLException {
        String sql = "SELECT * FROM polls";

        Connection connection = getConnection();
        PreparedStatement getPollsStatement = connection.prepareStatement(sql);

        ResultSet allPollsResult = getPollsStatement.executeQuery();
        ArrayList<Poll> allPolls = new ArrayList<>();

        while (allPollsResult.next()){

            String code = allPollsResult.getString("code");
            Date endDate = new Date(allPollsResult.getLong("end_date"));
            String title = allPollsResult.getString("title");
            UUID creator = UUID.fromString(allPollsResult.getString("creator"));
            Material icon = Material.valueOf(allPollsResult.getString("icon"));
            boolean isActive = allPollsResult.getBoolean("active");
            LinkedList<Option> options = new LinkedList<>();

            //We get all the options for this poll and order them in option num because
            //players are voting using these numbers, and it is really important to have them in the same order
            //each time
            PreparedStatement optionsStatement = connection.prepareStatement("SELECT * FROM polls_options WHERE code = ? ORDER BY option_num ASC");

            optionsStatement.setString(1, code);
            ResultSet optionsResults = optionsStatement.executeQuery();

            byte optionNum = (byte) 0;
            while(optionsResults.next()){
                optionNum++;
                //Firstly it gets all the options and how much votes does it had.
                PreparedStatement playersVotesStatement = connection.prepareStatement("SELECT COUNT(*) as `amount_votes`, uuid FROM players_voted WHERE poll_code = ? AND vote_option = ?");
                playersVotesStatement.setString(1, code);
                playersVotesStatement.setByte(2 ,optionsResults.getByte("option_num"));

                ResultSet playerVotedResults = playersVotesStatement.executeQuery();

                ArrayList<UUID> playersVoted;

                //if there is a number of votes counted we create an array list with the predefined
                //size to optimise adding to it
                if(playerVotedResults.next()){
                    playersVoted = new ArrayList<>(playerVotedResults.getInt("amount_votes"));
                    if(playerVotedResults.getString("uuid") != null) playersVoted.add(UUID.fromString(playerVotedResults.getString("uuid")));
                }else{
                    playersVoted = new ArrayList<>(1);
                }

                while (playerVotedResults.next()){
                    playersVoted.add(UUID.fromString(playerVotedResults.getString("uuid")));
                }

                options.add(new Option(playersVoted, optionsResults.getString("name"), optionNum));

                playerVotedResults.close();
                playersVotesStatement.close();
            }

            optionsStatement.close();

            //Getting the poll option object
            PreparedStatement settingsStatement = connection.prepareStatement("SELECT * FROM poll_settings WHERE poll_code = ?");
            settingsStatement.setString(1, code);

            ResultSet settingsResults = settingsStatement.executeQuery();

            settingsResults.next();

            PollSettings pollSettings = new PollSettings(
                    settingsResults.getBoolean("show_votes"),
                    settingsResults.getBoolean("show_on_join"),
                    settingsResults.getBoolean("change_votes"),
                    settingsResults.getBoolean("remind_votes")
            );

            settingsResults.close();
            settingsStatement.close();

            Poll poll = new Poll(code,
                    Bukkit.getOfflinePlayer(creator),
                    options,
                    title,
                    icon,
                    endDate,
                    pollSettings,
                    isActive);

            allPolls.add(poll);

        }

        getPollsStatement.close();
        connection.close();

        return allPolls;
    }

    /**
     * Updates the modifiable data of the poll saved in the polls table in database
     * @param poll The poll you want to update with new values
     * @throws SQLException On connection error
     */
    public void updatePoll(Poll poll) throws SQLException {
        String sql = "UPDATE polls SET title = ?, icon = ?, end_date = ?, active = ? WHERE code = ?";

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, poll.getTitle());
        statement.setString(2, poll.getIcon().toString());
        statement.setLong(3, poll.getEndDate().getTime());
        statement.setBoolean(4, poll.isActive);
        statement.setString(5, poll.code);

        statement.executeUpdate();

        PollSettings pollSettings = poll.getPollSettings();

        PreparedStatement settingsStatement = connection.prepareStatement("UPDATE poll_settings SET show_votes = ?, show_on_join = ?, change_votes = ?, remind_votes = ? " +
                "WHERE poll_code = ?");

        settingsStatement.setBoolean(1, pollSettings.showVotes);
        settingsStatement.setBoolean(2, pollSettings.showOnJoin);
        settingsStatement.setBoolean(3, pollSettings.changeVotes);
        settingsStatement.setBoolean(4, pollSettings.remindVote);
        settingsStatement.setString(5, poll.code);

        settingsStatement.executeUpdate();

        settingsStatement.close();
        statement.close();
        connection.close();
    }

    /**
     * Adds a vote for the player for the specified option in the
     * sql database
     * @param option The option you want to add the vote to
     * @param poll The poll where the option is present
     * @param voter The uuid of the player that voted on this option
     * @throws SQLException On connection error to the sql database
     */
    public void addVote(Option option, Poll poll, UUID voter) throws SQLException {
        String sql = "INSERT INTO players_voted VALUES (?,?,?)";

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, voter.toString());
        statement.setString(2, poll.code);
        statement.setByte(3, option.getOptionNumber());

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    /**
     * Gets all the players that already saw the specified poll
     * (Aka the poll was printed for them on chat).
     * @param poll The poll to check who saw
     * @return An ArrayList of UUID of all the players that saw the poll
     * @throws SQLException On database connection error
     */
    public ArrayList<UUID> playersSeenPoll(Poll poll) throws SQLException {
        String sql = "SELECT uuid FROM players_seen_poll WHERE poll_code = ?";

        ArrayList<UUID> playersSeen = new ArrayList<>();

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, poll.code);

        ResultSet results = statement.executeQuery();

        while (results.next()){
            playersSeen.add(UUID.fromString(results.getString("uuid")));
        }

        statement.close();
        connection.close();

        return playersSeen;
    }

    /**
     * Removes every record of players that saw the specified poll from the database. <br>
     * This should be only used when the poll ends and players need to see the results of the poll.
     * @param poll The poll to remove the players that saw
     * @throws SQLException On database connection error
     */
    public void removeEveryPlayerSeenPoll(Poll poll) throws SQLException {
        String sql = "DELETE FROM players_seen_poll WHERE poll_code = ?";

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, poll.code);

        statement.executeUpdate();

        statement.close();
        connection.close();

    }

    /**
     * Adds all the uuid of players from the list to the players that already saw the
     * specified poll.
     * @param listUuid An ArrayList of UUID of all the players that have already seen the poll
     * @param poll The poll that players saw
     * @throws SQLException On database connection error
     */
    public void addPlayersSeenPoll(ArrayList<UUID> listUuid, Poll poll) throws SQLException {
        if(listUuid.isEmpty()) return;

        StringBuilder sqlBuilder = new StringBuilder("INSERT INTO players_seen_poll VALUES ");

        for(UUID uuid : listUuid){
            sqlBuilder.append("(\"").append(uuid).append("\",\"").append(poll.code).append("\"),");
        }

        String sql = sqlBuilder.substring(0, sqlBuilder.length()-1);


        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }

    /**
     * Adds the uuid of the player to the database that have already seen the specified poll.
     * @param uuid The uuid of the player
     * @param poll The poll that they saw
     * @throws SQLException On database connection error
     */
    public void addPlayerSeenPoll(UUID uuid, Poll poll) throws SQLException {
        String sql = "INSERT INTO players_seen_poll VALUES (?,?)";

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        statement.setString(1, uuid.toString());
        statement.setString(2, poll.code);

        statement.executeUpdate();

        statement.close();
        connection.close();
    }





}
