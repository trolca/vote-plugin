package me.trololo11.voteplugin.commands.tabcompleters;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.List;

public class VoteTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {

        ArrayList<String> list = new ArrayList<>();

        if(args.length == 1) list.add("<poll code>");

        if(args.length == 2) list.add("<option>");

        return list;
    }
}
