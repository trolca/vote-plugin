package me.tololo11.voteplugin.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tololo11.voteplugin.VotePlugin;
import me.tololo11.voteplugin.utils.Option;
import me.tololo11.voteplugin.utils.Poll;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.Date;

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
                "creator char(36) not null, title text, end_date bigint not null, show_votes bool not null);");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS polls_options(code char(6) not null, option_num tinyint not null, name varchar(50), primary key(code, option_num) );");
        statement.execute("CREATE TABLE IF NOT EXISTS players_voted(uuid char(36) not null, poll_code char(6) not null, vote_option tinyint not null, primary key(uuid, poll_code, vote_option))");

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

        String sql = "INSERT INTO polls VALUES (?,?,?,?,?)";

        PreparedStatement voteDataStatement = connection.prepareStatement(sql);

        voteDataStatement.setString(1, poll.code);
        voteDataStatement.setString(2, poll.creator.getUniqueId().toString());
        voteDataStatement.setString(3, poll.getTitle());
        voteDataStatement.setLong(4, poll.getEndDate().getTime());
        voteDataStatement.setBoolean(5, poll.showVotes);

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
            boolean showVotes = allPollsResult.getBoolean("show_votes");
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

                options.add(new Option(playersVoted, optionsResults.getString("name"), optionNum,this));

                playerVotedResults.close();
                playersVotesStatement.close();
            }

            optionsStatement.close();

            Poll poll = new Poll(code,
                    Bukkit.getOfflinePlayer(creator),
                    options,
                    title,
                    endDate,
                    endDate.before(new Date()),
                    showVotes);

            allPolls.add(poll);

        }

        getPollsStatement.close();
        connection.close();

        return allPolls;
    }

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




}
