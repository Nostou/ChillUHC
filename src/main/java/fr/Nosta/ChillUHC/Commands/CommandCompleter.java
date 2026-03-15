package fr.Nosta.ChillUHC.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CommandCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> result = new ArrayList<>();
        if (!(sender instanceof Player player)) return result;
        if (!cmd.getName().equalsIgnoreCase("hf")) return result;

        String subCommand = args.length > 0 ? args[0].toLowerCase() : "";
        if (player.isOp()) handleOpCommands(subCommand, args, result);
        handleNonOpCommands(player, subCommand, args, result);

        return result;
    }

    private void handleOpCommands(String subCommand, String[] args, List<String> result) {
        switch (args.length) {
            case 1 -> {
                addSuggestion(result, subCommand, "start");
                addSuggestion(result, subCommand, "stop");
                addSuggestion(result, subCommand, "border");
                addSuggestion(result, subCommand, "tier");
            }
            case 2 -> {
                if (subCommand.equals("border")) {
                    result.add("<startRadius>");
                }
            }
            case 3 -> {
                if (subCommand.equals("border")) {
                    result.add("[targetRadius]");
                }
            }
            case 4 -> {
                if (subCommand.equals("border")) {
                    result.add("[meetupDuration]");
                }
            }
            case 5 -> {
                if (subCommand.equals("border")) {
                    result.add("[shrinkDuration]");
                }
            }
        }
    }

    private void handleNonOpCommands(Player player, String subCommand, String[] args, List<String> result) {
        switch (args.length) {
            case 1 -> {
                addSuggestion(result, subCommand, "invsee");
                addSuggestion(result, subCommand, "team");
                addSuggestion(result, subCommand, "tiersee");
            }
            case 2 -> {
                if (subCommand.equals("invsee")) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (p.getGameMode() != GameMode.SPECTATOR || p == player) continue;
                        result.add(p.getName());
                    }
                }
            }
        }
    }

    private void addSuggestion(List<String> result, String input, String suggestion) {
        if (suggestion.startsWith(input)) {
            result.add(suggestion);
        }
    }
}
