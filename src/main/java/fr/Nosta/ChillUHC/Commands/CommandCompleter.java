package fr.Nosta.ChillUHC.Commands;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

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
                addSuggestion(result, subCommand, "pvp");
                addSuggestion(result, subCommand, "tier");
                addSuggestion(result, subCommand, "scenarios");
                addSuggestion(result, subCommand, "revive");
            }
            case 2 -> {
                switch (subCommand) {
                    case "start" -> addSuggestion(result, args[1].toLowerCase(), "debug");
                    case "border" -> result.add("<startRadius>");
                    case "revive" -> result.addAll(getPlayers(p -> p.getGameMode() == GameMode.SPECTATOR));
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
                addSuggestion(result, subCommand, "infos");
            }
            case 2 -> {
                if (subCommand.equals("invsee")) {
                    result.addAll(getPlayers(p -> p.getGameMode() == GameMode.SPECTATOR && p != player));
                }
            }
        }
    }

    private List<String> getPlayers(Predicate<Player> condition) {
        List<String> players = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!condition.test(player)) continue;
            players.add(player.getName());
        }
        return players;
    }

    private void addSuggestion(List<String> result, String input, String suggestion) {
        if (suggestion.startsWith(input)) {
            result.add(suggestion);
        }
    }
}
