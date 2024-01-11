package me.trololo11.voteplugin.commands.tabcompleters;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ShowPollTabCompleter implements TabCompleter {

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 1)
            list.add("<poll code>");

        if(args.length == 2){
            list.add("*");
            for(Player player : Bukkit.getOnlinePlayers())
                list.add(player.getName());
        }

        return list;
    }
}
