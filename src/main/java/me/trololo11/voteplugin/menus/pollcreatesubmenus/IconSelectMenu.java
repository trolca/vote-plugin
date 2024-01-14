package me.trololo11.voteplugin.menus.pollcreatesubmenus;

import me.trololo11.voteplugin.menus.EditPollMenu;
import me.trololo11.voteplugin.menus.PollCreateMenu;
import me.trololo11.voteplugin.utils.InputMenu;
import me.trololo11.voteplugin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class IconSelectMenu extends InputMenu {

    private PollCreateMenu pollCreateMenu;
    private EditPollMenu editPollMenu;

    public IconSelectMenu(PollCreateMenu pollCreateMenu){
        this.pollCreateMenu = pollCreateMenu;
    }

    public IconSelectMenu(EditPollMenu editPollMenu){
        this.editPollMenu = editPollMenu;
    }

    @Override
    public ItemStack getSearchItem() {
        return Utils.createItem(Material.CLOCK, " ", "item");
    }

    @Override
    public String getName() {
        return Utils.chat("&e&lThe the name of the item:");
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onQuit(Player player) {

        return Collections.emptyList();
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onSearch(String searchQuery, Player player, AnvilGUI.Builder builder) {
        Material newIcon = Material.getMaterial(searchQuery.trim().toUpperCase());

        if(newIcon == null){
            return List.of(AnvilGUI.ResponseAction.replaceInputText(" ") ,AnvilGUI.ResponseAction.replaceInputText("This item doesn't exists!"));
        }

        if(pollCreateMenu == null){
            editPollMenu.setIcon(newIcon);
            editPollMenu.open(player);
        }else{
            pollCreateMenu.setPollIcon(newIcon);
            pollCreateMenu.open(player);
        }

        return Collections.emptyList();
    }
}
