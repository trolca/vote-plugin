package me.tololo11.voteplugin.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.tololo11.voteplugin.VotePlugin;
import me.tololo11.voteplugin.utils.Poll;
import org.bukkit.Bukkit;

import java.sql.*;
import java.util.ArrayList;
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

    private void initialize() throws SQLException {
        Connection connection = getConnection();
        Statement statement = connection.createStatement();

        statement.execute("CREATE TABLE IF NOT EXISTS votes(code char(6) primary key unique not null, " +
                "creator char(36) not null, title text, end_date bigint not null);");
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS vote_options(code char(6) not null, option_num tinyint, name varchar(50), primary key(code, option_num) );");

        ResultSet foreginCheck = statement.executeQuery("SELECT * FROM INFORMATION_SCHEMA.REFERENTIAL_CONSTRAINTS WHERE constraint_name = 'vote_options_con'");

        if(!foreginCheck.next()) statement.execute("ALTER TABLE vote_options ADD CONSTRAINT `vote_options_con` FOREIGN KEY (code) REFERENCES votes(code);");

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


    public void addPoll(Poll poll) throws SQLException {
        Connection connection = getConnection();

        String sql = "INSERT INTO votes VALUES (?,?,?,?)";

        PreparedStatement voteDataStatement = connection.prepareStatement(sql);

        voteDataStatement.setString(1, poll.code);
        voteDataStatement.setString(2, poll.creator.getUniqueId().toString());
        voteDataStatement.setString(3, poll.getTitle());
        voteDataStatement.setLong(4, poll.getEndDate().getTime());

        voteDataStatement.executeUpdate();

        ArrayList<String> options = poll.options;
        StringBuilder optionsSql = new StringBuilder("INSERT INTO vote_options VALUES ");

        for(int i=0; i < options.size(); i++){
            String option = options.get(i);
            optionsSql.append("(\"").append(poll.code).append("\",").append(i + 1).append(",\"").append(option).append("\"),");
        }

        sql = optionsSql.substring(0, optionsSql.length()-1);

        PreparedStatement statement = connection.prepareStatement(sql);

        statement.executeUpdate();

        voteDataStatement.close();
        statement.close();
        connection.close();
    }

    public ArrayList<Poll> getAllPolls() throws SQLException {
        String sql = "SELECT * FROM votes";

        Connection connection = getConnection();
        PreparedStatement statement = connection.prepareStatement(sql);

        ResultSet results = statement.executeQuery();
        ArrayList<Poll> allPolls = new ArrayList<>();

        while (results.next()){
            String code = results.getString("code");
            Date endDate = new Date(results.getLong("end_date"));
            ArrayList<String> options = new ArrayList<>();

            PreparedStatement optionsStatement = connection.prepareStatement("SELECT * FROM vote_options WHERE code = ? ORDER BY option_num ASC");
            optionsStatement.setString(1, code);
            ResultSet optionsResults = optionsStatement.executeQuery();

            while(optionsResults.next()){
                options.add(optionsResults.getString("name"));
            }

            Poll poll = new Poll(code,
                    Bukkit.getOfflinePlayer(results.getString("creator")),
                    options,
                    results.getString("title"),
                    endDate,
                    endDate.before(new Date()));

            allPolls.add(poll);

        }

        return allPolls;


    }




}
