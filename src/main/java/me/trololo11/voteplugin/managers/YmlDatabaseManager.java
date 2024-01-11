package me.trololo11.voteplugin.managers;

import me.trololo11.voteplugin.VotePlugin;
import me.trololo11.voteplugin.utils.Option;
import me.trololo11.voteplugin.utils.Poll;
import me.trololo11.voteplugin.utils.PollSettings;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * This class a database which uses YML files to store data.
 * It becomes the main DatabaseManager when the player specifies so in the config
 */
public class YmlDatabaseManager implements DatabaseManager{

    private VotePlugin plugin = VotePlugin.getPlugin();

    /**
     * A hash map of all the polls that we have to update players who voted for them.
     * The key is the poll to update and the value is the options that need to be updated.
     * We do this to not need to encrypt the list every time a player votes and only do it
     * on periodical database saves and when the plugin closes.
     */
    private HashMap<Poll, ArrayList<Option>> pollsVotesUpdate = new HashMap<>();
    /**
     * A hash map of all the polls that new player saw.
     * The key is the poll to update and the value is a list of uuid's of new player who saw it.
     * We do this to not need to encrypt the list every time a player votes and only do it
     * on periodical database saves and when the plugin closes.
     */
    private HashMap<Poll, ArrayList<UUID>> newPlayersSawPoll = new HashMap<>();

    private final String FILES_PATH;

    public YmlDatabaseManager(){
        FILES_PATH = plugin.getDataFolder()+"/polls-data/";
    }


    private File getPollFile(String code) throws IOException{
        File file = new File(FILES_PATH+code+".yml");
        if(!file.exists())
            throw new IOException("The poll code doesn't exist!");

        return file;

    }

    @Override
    public void addPoll(Poll poll) throws IOException {
        File pollFile = new File(FILES_PATH+poll.code+".yml");
        if(pollFile.exists())
            return;

        if(!pollFile.createNewFile())
            throw new IOException("Error while creating the poll file!");

        YamlConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

        //We don't need to store the code in the file cus the title already says it lol

        pollConfig.set("title", poll.getTitle());
        pollConfig.set("creator", poll.creator);
        pollConfig.set("icon", poll.getIcon().toString());
        pollConfig.set("end-date", poll.getEndDate().getTime());
        pollConfig.set("is-active", poll.isActive);

        PollSettings pollSettings = poll.getPollSettings();

        pollConfig.createSection("settings");

        pollConfig.set("settings.show-votes", pollSettings.showVotes);
        pollConfig.set("settings.show-on-join", pollSettings.showOnJoin);
        pollConfig.set("settings.change-votes", pollSettings.changeVotes);
        pollConfig.set("settings.remind-vote", pollSettings.remindVote);

        List<Option> options = poll.getAllOptions();

        pollConfig.set("num-options", options.size());

        pollConfig.createSection("options");

        for(Option option : options){
            pollConfig.createSection("options.option"+option.getOptionNumber());
            pollConfig.set("options.option"+option.getOptionNumber()+".name", option.getName());
            pollConfig.set("options.option"+option.getOptionNumber()+".players-voted", encodePlayersList(option.getPlayersVoted()));
        }

        pollConfig.set("players-saw-poll", "");

        pollConfig.save(pollFile);

    }

    @Override
    public ArrayList<Poll> getAllPolls() throws IOException {
        File pollsPath = new File(FILES_PATH);
        File[] pollsFiles = pollsPath.listFiles();

        if(pollsFiles == null)
            return new ArrayList<>();

        ArrayList<Poll> polls = new ArrayList<>(pollsFiles.length);

        for(File file : pollsFiles){
            FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(file);

            LinkedList<Option> options = new LinkedList<>();
            int numOptions = pollConfig.getInt("num-options");

            for(int i=0; i < numOptions; i++){
                String optionPath = "options.option"+(i+1);
                Option option = new Option(
                        decodePlayerList( pollConfig.getString(optionPath+".players-voted") ),
                        pollConfig.getString(optionPath+".name"),
                        (byte) (i+1)
                );

                options.add(option);
            }

            PollSettings pollSettings = new PollSettings(
                    pollConfig.getBoolean("settings.show-votes"),
                    pollConfig.getBoolean("settings.show-on-join"),
                    pollConfig.getBoolean("settings.change-votes"),
                    pollConfig.getBoolean("settings.remind-vote")
            );

            polls.add(new Poll(
                    file.getName().split("\\.")[0],
                    pollConfig.getOfflinePlayer("creator"),
                    options,
                    pollConfig.getString("title"),
                    Material.getMaterial(Objects.requireNonNull(pollConfig.getString("icon"))),
                    new Date(pollConfig.getLong("end-date")),
                    pollSettings,
                    pollConfig.getBoolean("is-active"),
                    this
            ));

        }

        return polls;
    }

    @Override
    public void updatePoll(Poll poll) throws IOException {
        File pollFile = getPollFile(poll.code);
        FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

        pollConfig.set("icon", poll.getIcon().toString());
        pollConfig.set("end-date", poll.getEndDate().getTime());
        pollConfig.set("is-active", poll.isActive);

        PollSettings pollSettings = poll.getPollSettings();

        pollConfig.set("settings.show-votes", pollSettings.showVotes);
        pollConfig.set("settings.show-on-join", pollSettings.showOnJoin);
        pollConfig.set("settings.change-votes", pollSettings.changeVotes);
        pollConfig.set("settings.remind-vote", pollSettings.remindVote);

        pollConfig.save(pollFile);

    }

    @Override
    public void addVote(Option option, Poll poll, UUID voter) {
        pollsVotesUpdate.computeIfAbsent(poll, k -> new ArrayList<>());
        pollsVotesUpdate.get(poll).add(option);
    }

    @Override
    public void removeVote(Poll poll,Option option, UUID voter) {
        pollsVotesUpdate.computeIfAbsent(poll, k -> new ArrayList<>());
        pollsVotesUpdate.get(poll).add(option);
    }

    @Override
    public ArrayList<UUID> playersSeenPoll(Poll poll) throws IOException {
        File pollFile = getPollFile(poll.code);
        FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

        return decodePlayerList(pollConfig.getString("players-saw-poll"));
    }

    @Override
    public void removeEveryPlayerSeenPoll(Poll poll) throws IOException {
        File pollFile = getPollFile(poll.code);
        FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

        pollConfig.set("players-saw-poll", "");

        pollConfig.save(pollFile);
    }

    @Override
    public void addPlayersSeenPoll(ArrayList<UUID> listUuid, Poll poll) {
        newPlayersSawPoll.computeIfAbsent(poll, key -> new ArrayList<>());
        newPlayersSawPoll.get(poll).addAll(listUuid);
    }

    @Override
    public void addPlayerSeenPoll(UUID uuid, Poll poll) {
        newPlayersSawPoll.computeIfAbsent(poll, key -> new ArrayList<>());
        newPlayersSawPoll.get(poll).add(uuid);
    }

    @Override
    public void changeOptionNumber(byte oldNumber, byte newNumber, Poll poll) throws IOException {
        File pollFile = getPollFile(poll.code);
        FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

        String oldNumPath = "options.option"+oldNumber;
        String newNumPath = "options.option"+newNumber;

        String optionName = pollConfig.getString(oldNumPath+".name");
        String encryptedStringVoted = pollConfig.getString(oldNumPath+".players-voted");

        pollConfig.set(oldNumPath, null);

        pollConfig.createSection(newNumPath);

        pollConfig.set(newNumPath+".name", optionName);
        pollConfig.set(newNumPath+".players-voted", encryptedStringVoted);

        pollConfig.save(pollFile);
    }

    @Override
    public void removePoll(Poll poll) throws IOException {
        File pollFile = getPollFile(poll.code);

        if(!pollFile.delete())
            throw new IOException("Error while removing the poll "+ poll.code);
    }

    @Override
    public void close() throws IOException {
        saveDatabase();
    }

    /**
     * Encrypts and saves all the players data of player voting or players seeing the poll to the files.
     * We do this to not have to encrypt all the data every time a player votes or sees the poll.
     */
    public void saveDatabase() throws IOException {

        for(Map.Entry<Poll, ArrayList<Option>> entry : pollsVotesUpdate.entrySet()){
            Poll poll = entry.getKey();
            File pollFile = getPollFile(poll.code);
            FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

            for(Option option : entry.getValue()){
                pollConfig.set("options.option"+option.getOptionNumber()+".players-voted", encodePlayersList(option.getPlayersVoted()));
            }

            pollConfig.save(pollFile);
        }

        pollsVotesUpdate.clear();

        for(Map.Entry<Poll, ArrayList<UUID>> entry : newPlayersSawPoll.entrySet()){
            Poll poll = entry.getKey();
            File pollFile = getPollFile(poll.code);
            FileConfiguration pollConfig = YamlConfiguration.loadConfiguration(pollFile);

            String oldString = pollConfig.getString("players-saw-poll");

            pollConfig.set("players-saw-poll", oldString + encodePlayersList(entry.getValue()));

            pollConfig.save(pollFile);
        }

        newPlayersSawPoll.clear();
    }

    /**
     * Encodes the provided list to base64
     * @param playersList The list to encode
     * @return The encoded string in base64
     * @throws IOException On error while writing values
     */
    private String encodePlayersList(List<UUID> playersList) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream bukkitStream = new BukkitObjectOutputStream(outputStream);

        bukkitStream.writeInt(playersList.size());

        for(UUID voter : playersList) {
            bukkitStream.writeObject(voter);
        }

        bukkitStream.close();

        return Base64.getEncoder().encodeToString(outputStream.toByteArray());
    }

    /**
     * Decodes the string of base64 to an uuid list
     * @param encodedString The encoded string in base64
     * @return The decoded list
     * @throws IOException On error while reading objects
     */
    private ArrayList<UUID> decodePlayerList(String encodedString) throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encodedString));
        BukkitObjectInputStream bukkitInputStream = new BukkitObjectInputStream(inputStream);

        int size = bukkitInputStream.readInt();

        ArrayList<UUID> playersList = new ArrayList<>();

        for(int i=0; i < size; i++){
            try {

                playersList.add((UUID) bukkitInputStream.readObject());

            } catch (ClassNotFoundException e) { //Really shouldn't happen so idc
                plugin.logger.severe("[VotePlugin] Error while reading the uuid data for a poll!");
                return playersList;
            }
        }

        bukkitInputStream.close();

        return playersList;
    }
}
