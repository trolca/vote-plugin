package me.trololo11.voteplugin.utils;

import me.trololo11.voteplugin.managers.PollsManager;
import org.bukkit.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class Utils {

    private static char[] codeGeneratingCharacters = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();


    public static String chat(String s){
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    /**
     * Prints the info about the poll to the player and makes the options clickable for the player to vote
     * @param player The player to print the poll to
     * @param poll The poll to print
     */
    public static void printPollToPlayer(Player player, Poll poll){
        player.sendMessage(ChatColor.GREEN + poll.creator.getName() + " has created a new poll!");
        player.sendMessage(ChatColor.GRAY + "--------------------------------------");
        player.sendMessage(Utils.chat(poll.getTitle()));
        player.sendMessage(" ");
        player.sendMessage(ChatColor.GOLD + "Options:");

        for(int i=0; i < poll.getAllOptions().size(); i++){
            Option option = poll.getAllOptions().get(i);

            TextComponent optionText = new TextComponent(ChatColor.GRAY.toString() + (i+1) + " - "+ChatColor.RESET+ Utils.chat(option.getName()));
            optionText.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/vote "+poll.code + " " + (i+1) ));
            optionText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text("Click to vote!" +
                    (poll.getPollSettings().changeVotes ? "" : "\n" + ChatColor.RED + "This poll doesn't allow changing votes!") )));

            player.spigot().sendMessage(optionText);
        }

        player.sendMessage(" ");
        player.sendMessage(ChatColor.GRAY + "Ends in: "+ poll.getEndDateString());
        player.sendMessage(ChatColor.GRAY + "--------------------------------------");
        player.sendMessage(ChatColor.GRAY + ChatColor.ITALIC.toString() + "(Click the option or do /vote "+poll.code+" <option> to vote)");

    }

    /**
     * Prints the results of a poll for the specified players. <b>Should be used when the poll has ended</b>
     * @param player The player to print to
     * @param poll The poll to print about
     */
    public static void printPollResultsToPlayer(Player player, Poll poll){
        long allVotes = poll.getTotalVotes();
        Option wonOption = poll.getWinningOption();

        player.sendMessage(ChatColor.GREEN + "Poll "+ ChatColor.RESET + poll.getTitle() + ChatColor.GREEN + " has finished!");
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY + ChatColor.BOLD.toString() + "Results:");
        for(Option option : poll.getAllOptions()){
            int amountOfVotes = option.getAmountOfVotes();
            int percentageVotes = ((int) ((amountOfVotes/(double) allVotes )*100 ) );

            if(amountOfVotes == wonOption.getAmountOfVotes()){
                player.sendMessage(ChatColor.GRAY.toString() + option.getOptionNumber() + " - "
                        + ChatColor.GOLD + ChatColor.BOLD + option.getName() + Utils.chat(" &7&o("+percentageVotes+"%)"));
            }else{
                player.sendMessage(Utils.chat("&7"+option.getOptionNumber()+" - &f"+ option.getName() + " &7&o("+percentageVotes+"%)"));
            }
        }
        player.sendMessage("");
        player.sendMessage(ChatColor.GRAY.toString() + poll.getTotalVotes() + " total votes");
    }

    /**
     * Creates a new {@link ItemStack} from the specified parameters
     * @param material The material of this item
     * @param name The display name of this item
     * @param localizedName The localized name of this item
     * @param lore The LOREEE of this item
     * @return The created {@link ItemStack}
     */
    public static ItemStack createItem(Material material, String name, String localizedName, String... lore){
        ItemStack item = new ItemStack(material);

        ArrayList<String> loreArray = new ArrayList<>();

        for(String string : lore){
            loreArray.add(Utils.chat(string));
        }

        item.setItemMeta(getItemMeta(item.getItemMeta(), name, localizedName, loreArray));

        return item;
    }

    /**
     * Creates a new {@link ItemStack} from the specified parameters
     * @param material The material of this item
     * @param name The display name of this item
     * @param localizedName The localized name of this item
     * @param lore The LOREEE of this item
     * @return The created {@link ItemStack}
     */
    public static ItemStack createItem(Material material, String name, String localizedName,List<String> lore){
        ItemStack item = new ItemStack(material);

        ArrayList<String> loreArray = new ArrayList<>();

        for(String string : lore){
            loreArray.add(Utils.chat(string));
        }

        item.setItemMeta(getItemMeta(item.getItemMeta(), name, localizedName, loreArray));

        return item;
    }

    /**
     * Creates a new {@link ItemStack} with the specified enchantment. <br>
     * This function should only be used to create items with the enchantment glint and
     * not for real items to enchant because <b> the enchant will not show on the item</b>
     * @param material The material of this item
     * @param name The name of this item (You can use alternate color codes with the '&' char)
     * @param localizedName The localized name of this item
     * @param enchantment The enchantment to set
     * @param lore The lore (description) of this item
     * @return A new {@link ItemStack} with the specified parameters
     */
    public static ItemStack createEnchantedItem(Material material, String name, String localizedName, Enchantment enchantment, String... lore){
        ItemStack item = new ItemStack(material);

        ArrayList<String> loreArray = new ArrayList<>();

        for(String string : lore){
            loreArray.add(Utils.chat(string));
        }

        item.setItemMeta(getItemMeta(item.getItemMeta(), name, localizedName, loreArray));

        item.addUnsafeEnchantment(enchantment, 1);

        return item;
    }

    public static ItemStack createPlayerHead(OfflinePlayer owner, String name, String localizedName, String... lore){
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);

        ArrayList<String> loreArray = new ArrayList<>();

        for(String string : lore){
            loreArray.add(Utils.chat(string));
        }

        SkullMeta skullMeta = (SkullMeta) getItemMeta(item.getItemMeta(), name, localizedName, loreArray);

        skullMeta.setOwningPlayer(owner);

        item.setItemMeta(skullMeta);

        return item;
    }

    private static ItemMeta getItemMeta(ItemMeta itemMeta, String displayName, String localizedName, List<String> lore){

        itemMeta.setDisplayName(Utils.chat(displayName));
        itemMeta.setLocalizedName(localizedName);
        itemMeta.setLore(lore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_UNBREAKABLE);

        return itemMeta;
    }

    /**
     * Generates a unique 6 character code that can be used by {@link Poll} object
     * which doesn't already exist in the {@link PollsManager} class
     * @param pollsManager A poll manager class
     * @return A unique 6 character code that isn't used anywhere else
     */
    public static String generateUniqueCode(PollsManager pollsManager){
        Random mainRandom = new Random();
        StringBuilder code;

        do{
            code = new StringBuilder();
            for(int i=0; i < 6; i++)
                code.append(codeGeneratingCharacters[mainRandom.nextInt(codeGeneratingCharacters.length)]);

        }while(pollsManager.getAllPollCodes().contains(code.toString()));


        return code.toString();

    }

    /**
     * Returns a time in string based type. <br>
     * For ex. "1h3m3s"
     * @param time The time to format to string. <b>The time must be provided in seconds</b>
     * @return A formatted string based from the time param. <br>
     * For a param 63 it would return "1m3s"
     */
    public static String getStringTime(long time){
        return getStringTime(time, new char[0]);
    }

    /**
     * Returns a time in string based type. <br>
     * For ex. "1h3m3s"
     * @param time The time to format to string. <b>The time must be provided in seconds</b>
     * @param timeRange An array to specify which time frames to return.<br>
     *                  ex. For an array {'d', 'h'} it would only return 1d2h not 1d2h34m43s.
     * @return A formatted string based from the time param. <br>
     * For a param 63 it would return "1m3s"
     */
    public static String getStringTime(long time, char[] timeRange){
        long days = time/86400;
        time -= days * 86400;
        long hours = (time/3600);
        time -= hours * 3600;
        long minutes = (time/60);
        time -= minutes * 60;
        long seconds = time;
        StringBuilder stringBuilder = new StringBuilder();

        if(days > 0 && charInArray(timeRange, 'd')) stringBuilder.append(days).append("d");
        if(hours > 0 && charInArray(timeRange, 'h')) stringBuilder.append(hours).append("h");
        if(minutes > 0 && charInArray(timeRange, 'm')) stringBuilder.append(minutes).append("m");
        if(seconds > 0 && charInArray(timeRange, 's')) stringBuilder.append(seconds).append("s");

        return stringBuilder.toString();

    }

    /**
     * Shows the all the polls from the array in an inventory. Is used to format the {@link me.trololo11.voteplugin.menus.SeePlayerPollsMenu}
     * and {@link me.trololo11.voteplugin.menus.SeePollsMenu}. This function mostly exists to have this code in one place
     * for every menu that will need to show polls in a page menu
     * @param polls A list of polls to show
     * @param page The page that the menu is on.
     * @param inventory The inventory to set the items in
     * @param player The player that is opening the inventory
     * @param pollsManager A polls manager object to see if players saw a poll or not
     */
    public static void generatePollsFromListInInv(List<Poll> polls, int page, Inventory inventory, Player player, PollsManager pollsManager){

        for(int i=0; i < 27; i++){

            int index = i+( (page-1)*27 );

            if(index >= polls.size()) break;

            ArrayList<String> lore = new ArrayList<>(10);
            Poll poll = polls.get( index );
            long allVotes = (!poll.isActive || poll.getPollSettings().showVotes) ? poll.getTotalVotes() : 0;
            boolean playerSawPoll = pollsManager.playerSawPoll(player.getUniqueId(), poll);
            ItemStack pollItem = new ItemStack(poll.getIcon());

            ItemMeta pollMeta = pollItem.getItemMeta();
            pollMeta.setDisplayName(ChatColor.RESET + Utils.chat(poll.getTitle() + "&6&l" + ( playerSawPoll ? "" : poll.isActive ? " NEW" : " FINISHED" )));

            lore.add(ChatColor.GREEN + "Poll creator: "+poll.creator.getName());
            lore.add("");
            lore.add(ChatColor.GOLD + ChatColor.BOLD.toString() + "Options:");
            for(Option option : poll.getAllOptions()){

                String optionPercentageS = "";
                if(!poll.isActive || poll.getPollSettings().showVotes){
                    int optionVotes = option.getAmountOfVotes();
                    optionPercentageS = ChatColor.GRAY + " ("+((int) ( (double) optionVotes/allVotes )*100) + "%)";
                }
                boolean hasVoted = option.getPlayersVoted().contains(player.getUniqueId());
                String optionName = Utils.chat(option.getName());

                lore.add(ChatColor.GRAY.toString() + option.getOptionNumber() + ChatColor.WHITE +" - " + (hasVoted ?
                        ChatColor.GREEN + ChatColor.stripColor(optionName) + " (voted on)" : optionName) + optionPercentageS);

            }
            if(!poll.isActive || poll.getPollSettings().showVotes)
                lore.add(ChatColor.DARK_GRAY + "Total votes: "+ allVotes);
            lore.add(ChatColor.YELLOW + "Ends in: " + poll.getEndDateString());
            lore.add("");
            lore.add(ChatColor.DARK_GRAY + "Poll code: "+ poll.code);

            pollMeta.setLore(lore);
            pollMeta.setLocalizedName("poll-"+poll.code);
            pollMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_DESTROYS);
            if(!playerSawPoll)
                pollMeta.addEnchant(Enchantment.MENDING, 1, true);

            pollItem.setItemMeta(pollMeta);

            inventory.setItem(i+9, pollItem);

        }

    }

    public static long convertTimeToMills(int days, int hours, int minutes){
        long time = 0;

        time += days*86400000L; //Converts days to milliseconds
        time += hours*3600000L; //Converts hours to milliseconds
        time += minutes*60000L; //Converts minutes to milliseconds

        return time;
    }

    public static boolean isLocalizedNameEqual(@Nullable ItemMeta itemMeta, String string){
        if(itemMeta == null) return false;

        return itemMeta.getLocalizedName().equalsIgnoreCase(string);
    }

    public static boolean charInArray(char[] array, char charToCheck){

        for(char character : array){

            if(character == charToCheck) return true;
        }

        return false;
    }

}
