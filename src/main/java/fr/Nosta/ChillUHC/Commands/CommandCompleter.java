package fr.Nosta.ChillUHC.Commands;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements TabCompleter {

    private final Main plugin;

    public CommandCompleter(Main plugin) {
        this.plugin = plugin;
    }

    private List<String> result = new ArrayList<String>();

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        result.clear();
        Player player = (Player)sender;

        if (!label.equalsIgnoreCase("hf")) return result;

        if (player.isOp()) HandleOpCommands(player, args);
        HandleNonOpCommands(player, args);

        return result;
    }

    private void HandleOpCommands(Player player, String[] args) {

        if (args.length == 1) {
            if ("start".startsWith(args[0].toLowerCase())) result.add("start");
            if ("stop".startsWith(args[0].toLowerCase())) result.add("stop");
            if ("border".startsWith(args[0].toLowerCase())) result.add("border");
            if ("tier".startsWith(args[0].toLowerCase())) result.add("tier");
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("border")) {
            result.add("<startRadius>");
        }

        else if (args.length == 3 && args[0].equalsIgnoreCase("border")) {
            result.add("[targetRadius]");
        }

        else if (args.length == 4 && args[0].equalsIgnoreCase("border")) {
            result.add("[meetupDuration]");
        }

        else if (args.length == 5 && args[0].equalsIgnoreCase("border")) {
            result.add("[shrinkDuration]");
        }
    }

    private void HandleNonOpCommands(Player player, String[] args) {

        if (args.length == 1) {
            if ("invsee".startsWith(args[0].toLowerCase())) result.add("invsee");
            if ("team".startsWith(args[0].toLowerCase())) result.add("team");
            if ("tiersee".startsWith(args[0].toLowerCase())) result.add("tiersee");
        }

        else if (args.length == 2 && args[0].equalsIgnoreCase("invsee")) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getGameMode() != GameMode.SPECTATOR || p == player) continue;
                result.add(p.getName());
            }
        }
    }
}
