package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.utils.InputMenu;
import me.trololo11.voteplugin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class OptionNameSetMenu extends InputMenu {

    private OptionEditMenu optionEditMenu;

    public OptionNameSetMenu(OptionEditMenu optionEditMenu){
        this.optionEditMenu = optionEditMenu;
    }

    @Override
    public ItemStack getSearchItem() {
        return Utils.createItem(Material.NAME_TAG, " ", "name");
    }

    @Override
    public String getName() {
        return Utils.chat("&e&lOption name:");
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onQuit(Player player) {
        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() ->  optionEditMenu.open(player) ));
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onSearch(String searchQuery, Player player, AnvilGUI.Builder builder) {
        String newName = Utils.chat(searchQuery).trim();


        if(newName.isBlank()){
            return List.of(AnvilGUI.ResponseAction.replaceInputText(" ") ,AnvilGUI.ResponseAction.replaceInputText("The title is incorrect!"));
        }

        optionEditMenu.setOptionName(newName);

        optionEditMenu.open(player);

        return Collections.emptyList();
    }
}
