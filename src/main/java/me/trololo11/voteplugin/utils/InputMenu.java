package me.trololo11.voteplugin.utils;


import me.trololo11.voteplugin.VotePlugin;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public abstract class InputMenu {

    protected AnvilGUI.Builder builder;
    private VotePlugin plugin = VotePlugin.getPlugin();

    public abstract ItemStack getSearchItem();



    public abstract String getName();
    public void open(Player player){
        builder = new AnvilGUI.Builder();

        builder.plugin(plugin);
        builder.allowConcurrentClickHandlerExecution();
        builder.title(getName());


        ItemStack searchItem = getSearchItem();
        ItemMeta searchMeta = searchItem.getItemMeta();

        if(searchMeta.getDisplayName().isBlank()) builder.text(" ");

        searchMeta.setLocalizedName("search");

        searchItem.setItemMeta(searchMeta);

        builder.itemLeft(searchItem);
        builder.itemOutput(searchItem);

        builder.onClick((integer, stateSnapshot) -> onSearch(stateSnapshot.getText(), player, builder));
        builder.onClose(stateSnapshot -> onQuit(stateSnapshot.getPlayer()));

        builder.open(player);


    }

    protected abstract List<AnvilGUI.ResponseAction> onQuit(Player player);

    protected abstract List<AnvilGUI.ResponseAction> onSearch(String searchQuery, Player player, AnvilGUI.Builder builder);



}