package me.trololo11.voteplugin.menus;

import me.trololo11.voteplugin.utils.InputMenu;
import me.trololo11.voteplugin.utils.Utils;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;

public class TitleSetMenu extends InputMenu {

    private PollCreateMenu pollCreateMenu;

    public TitleSetMenu(PollCreateMenu pollCreateMenu){
        this.pollCreateMenu = pollCreateMenu;
    }

    @Override
    public ItemStack getSearchItem() {
        return Utils.createItem(Material.NAME_TAG, " ", "title-set");
    }

    @Override
    public String getName() {
        return Utils.chat("&6&lType title:");
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onQuit(Player player) {
        return List.of(AnvilGUI.ResponseAction.close(), AnvilGUI.ResponseAction.run(() ->  pollCreateMenu.open(player) ));
    }

    @Override
    protected List<AnvilGUI.ResponseAction> onSearch(String searchQuery, Player player, AnvilGUI.Builder builder) {

        String newTitle = Utils.chat(searchQuery);

        if(newTitle.isBlank()){
            return List.of(AnvilGUI.ResponseAction.replaceInputText(" ") ,AnvilGUI.ResponseAction.replaceInputText("Title cannot be empty!"));
        }

        pollCreateMenu.setPollTitle(newTitle);

        pollCreateMenu.open(player);
        return Collections.emptyList();
    }
}
