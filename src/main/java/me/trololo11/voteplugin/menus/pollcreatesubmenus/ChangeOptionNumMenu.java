package me.trololo11.voteplugin.menus.pollcreatesubmenus;

import me.trololo11.voteplugin.menus.PollCreateMenu;
import me.trololo11.voteplugin.utils.Menu;
import me.trololo11.voteplugin.utils.Utils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedList;

public class ChangeOptionNumMenu extends Menu {

    private OptionEditMenu optionEditMenu;
    private PollCreateMenu pollCreateMenu;

    public ChangeOptionNumMenu(OptionEditMenu optionEditMenu, PollCreateMenu pollCreateMenu){
        this.optionEditMenu = optionEditMenu;
        this.pollCreateMenu = pollCreateMenu;
    }

    @Override
    public String getMenuName() {
        return Utils.chat("&2&lClick the option to swap");
    }

    @Override
    public int getSlots() {
        return 4*9;
    }

    @Override
    public void setMenuItems(Player p) {
        ItemStack filler = Utils.createItem(Material.GRAY_STAINED_GLASS_PANE, " ", "filler");
        ItemStack back = Utils.createItem(Material.RED_DYE, "&c&fBack", "back");
        Material optionsMaterial = Material.OAK_SIGN;

        LinkedList<String> options = pollCreateMenu.getOptions();

        for(int i=0; i < 9; i++){
            inventory.setItem(i, filler);
        }

        int extraI = 0;

        for(int i=0; i < options.size(); i++){

            if(i % 3 == 0 && i != 0) extraI++;

            int slot =  ( (i+1) * 9 )- 27*extraI + 2 * extraI;

            String option = options.get(i);
            int optionNum = i+1;

            ItemStack optionItem = i == optionEditMenu.getOptionNumber() ?

                    Utils.createEnchantedItem(optionsMaterial, "&6Editing option "+optionNum + ": &f"+optionEditMenu.getOptionName(), "editing-option-"+i, Enchantment.MENDING) :

                    Utils.createItem(optionsMaterial,  "&8Option "+optionNum+": &f" +option, "option-"+i,
                                    "&7Click to change place to");

            inventory.setItem(slot, optionItem);
        }

        inventory.setItem(0, back);

    }

    @Override
    public void handleMenu(InventoryClickEvent e) {
        ItemStack item = e.getCurrentItem();
        Player player = (Player) e.getWhoClicked();

        switch (item.getType()){

            case RED_DYE -> {
                if(!Utils.isPrivateNameEqual(item.getItemMeta(), "back")) return;

                optionEditMenu.open(player);
            }

            case OAK_SIGN -> {
                if(!Utils.getPrivateName(item).startsWith("option-")) return;

                int num = Integer.parseInt(Utils.getPrivateName(item).split("-")[1]);
                int startNumber = optionEditMenu.getOptionNumber();

                LinkedList<String> options = pollCreateMenu.getOptions();

                String optionHolder = options.get(num);
                options.set(num, options.get(startNumber));
                options.set(startNumber, optionHolder);

                optionEditMenu.setOptionNumber((byte)  num);

                optionEditMenu.open(player);
            }

        }

    }
}
