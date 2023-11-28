package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.utils.InputMenu;
import me.trololo11.voteplugin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class EndDateSetManuallyMenu extends InputMenu {

    private EndDateSetMenu endDateSetMenu;

    public  EndDateSetManuallyMenu(EndDateSetMenu endDateSetMenu){
        this.endDateSetMenu = endDateSetMenu;
    }

    @Override
    public ItemStack getSearchItem() {
        return Utils.createItem(Material.IRON_SWORD, " ", "search", "&fTime be should be formatted like this:", "&f<days>d<hours>h<minutes>m");
    }

    @Override
    public String getName() {
        return Utils.chat("&6&lWrite manual time");
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onQuit(Player player) {
        return  List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() ->  endDateSetMenu.open(player) ));
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onSearch(String searchQuery, Player player, AnvilGUI.Builder builder) {
        int days = 0, hours = 0, minutes = 0;

        // We add an s so there's always an extra character so when the string gets split
        // where there  is only 2d it's going to split it only to length 1, and we need at least 2
        searchQuery = searchQuery.trim()+"s";
        try {

            String timeChars = "[dhm]";
            String[] checkString;

            //Check how many days are in string
            checkString = searchQuery.split("d");
            if (checkString.length != 1) {
                checkString = checkString[0].split(timeChars);
                days = Integer.parseInt(checkString[checkString.length-1]);
            }

            //Check how manu hours are in this string
            checkString = searchQuery.split("h");
            if (checkString.length != 1) {
                checkString = checkString[0].split(timeChars);
                hours = Integer.parseInt(checkString[checkString.length-1]);
            }

            //Checks how many minutes are in this string
            checkString = searchQuery.split("m");
            if (checkString.length != 1) {
                checkString = checkString[0].split(timeChars);
                minutes = Integer.parseInt(checkString[checkString.length-1]);
            }
        }catch (NumberFormatException e){
            return List.of(AnvilGUI.ResponseAction.replaceInputText(""), AnvilGUI.ResponseAction.replaceInputText("The numbers are incorrect!"));
        }

        endDateSetMenu.setTime(days, hours, minutes);

        endDateSetMenu.open(player);


        return Collections.emptyList();
    }
}
