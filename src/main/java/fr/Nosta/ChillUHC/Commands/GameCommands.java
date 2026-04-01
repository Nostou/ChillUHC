package fr.Nosta.ChillUHC.Commands;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.GameManager;
import fr.Nosta.ChillUHC.Managers.InventoryManager;
import fr.Nosta.ChillUHC.Managers.BorderManager;
import fr.Nosta.ChillUHC.Managers.ReviveManager;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class GameCommands implements CommandExecutor {

    private final Main plugin;

    public GameCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if (!(sender instanceof Player player)) return true;
        if (!cmd.getName().equalsIgnoreCase("hf") || args.length == 0) return true;

        String subCommand = args[0].toLowerCase();
        if (player.isOp() && handleOpCommands(player, subCommand, args)) {
            return true;
        }

        handleNonOpCommands(player, subCommand, args);

        return true;
    }

    private boolean handleOpCommands(Player player, String subCommand, String[] args) {
        BorderManager bm = plugin.getBorderManager();
        GameManager gm = plugin.getGameManager();
        ReviveManager rm = plugin.getReviveManager();

        switch (subCommand) {
            case "start" -> {
                if (gm.getState() != GameState.WAITING) {
                    CustomMessage.error(player, "A game is already in progress.");
                    return true;
                }

                boolean isDebug = args.length > 1 && args[1].equalsIgnoreCase("debug");

                if (!isDebug && plugin.getTierManager().hasUndefinedPlayers()) {
                    CustomMessage.error(player, "Some players have an undefined tier.");
                    return true;
                }

                gm.startGame(isDebug);
                return true;
            }
            case "stop" -> {
                if (gm.getState() == GameState.WAITING) {
                    CustomMessage.error(player, "No game is currently running.");
                    return true;
                }

                gm.stopGame();
                return true;
            }
            case "border" -> {
                handleBorderCommand(player, bm, args);
                return true;
            }
            case "pvp" -> {
                handlePvpCommand(player, gm, args);
                return true;
            }
            case "tier" -> {
                plugin.getInventoryManager().openTierInventory(player);
                return true;
            }
            case "scenarios" -> {
                plugin.getInventoryManager().openScenarioInventory(player);
                return true;
            }
            case "revive" -> {
                handleReviveCommand(player, gm, rm, args);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void handleNonOpCommands(Player player, String subCommand, String[] args) {
        GameManager gm = plugin.getGameManager();
        InventoryManager im = plugin.getInventoryManager();

        switch (subCommand) {
            case "invsee" -> {
                if (!player.isOp() && (player.getGameMode() != GameMode.SPECTATOR || plugin.getGameManager().getState() != GameState.PLAYING)) {
                    CustomMessage.error(player, "You don't have permission to use this command.");
                    return;
                }

                if (args.length < 2) {
                    CustomMessage.error(player, "/hf invsee <player>");
                    return;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null || target.getGameMode() != GameMode.SURVIVAL || player == target) {
                    CustomMessage.error(player, "Player either not found nor valid.");
                    return;
                }

                im.openInvSeeInventory(player, target);
            }
            case "team" -> {
                if (gm.getState() != GameState.WAITING) {
                    CustomMessage.error(player, "Team selection is not available.");
                    return;
                }

                im.openTeamInventory(player);
            }
            case "infos" -> player.sendMessage(buildInfosMessage());
        }
    }

    private Component buildInfosMessage() {
        return plugin.getTierManager().logTierList()
                .append(Component.text("\n\nScenarios » ", NamedTextColor.AQUA))
                .append(buildScenarioList());
    }

    private Component buildScenarioList() {
        List<ScenarioType> enabledScenarios = plugin.getScenarioManager().getEnabledScenarios();
        if (enabledScenarios.isEmpty()) {
            return Component.text("No active scenarios", NamedTextColor.GRAY);
        }

        Component result = Component.empty();
        Component separator = Component.text("/", NamedTextColor.DARK_GRAY);

        for (int i = 0; i < enabledScenarios.size(); i++) {
            if (i > 0) {
                result = result.append(separator);
            }

            ScenarioType scenario = enabledScenarios.get(i);
            result = result.append(Component.text(scenario.getDisplayName(), NamedTextColor.GREEN)
                    .hoverEvent(HoverEvent.showText(Component.text(scenario.getDescription(), NamedTextColor.GRAY))));
        }

        return result;
    }

    private void handleBorderCommand(Player player, BorderManager borderManager, String[] args) {
        if (plugin.getGameManager().getState() != GameState.WAITING) {
            CustomMessage.error(player, "WorldBorder can only be changed before the game starts.");
            return;
        }

        if (args.length < 2) {
            CustomMessage.error(player, "/hf border <startRadius> [targetRadius] [meetupDuration] [shrinkDuration]");
            return;
        }

        try {
            borderManager.setStartRadius(parsePositiveInt(args[1]));

            if (args.length > 2) {
                borderManager.setTargetRadius(parsePositiveInt(args[2]));
            }

            if (args.length > 3) {
                borderManager.setMeetupDuration(parsePositiveMinutes(args[3]));
            }

            if (args.length > 4) {
                borderManager.setShrinkDuration(parsePositiveMinutes(args[4]));
            }

            CustomMessage.success(player, "WorldBorder has been updated !");
        } catch (NumberFormatException exception) {
            CustomMessage.error(player, "WorldBorder values must be valid numbers.");
        }
    }

    private void handlePvpCommand(Player player, GameManager gameManager, String[] args) {
        boolean pvpEnabled = Boolean.TRUE.equals(plugin.getWorld().getGameRuleValue(org.bukkit.GameRules.PVP));
        gameManager.setPvpEnabled(!pvpEnabled);
    }

    private void handleReviveCommand(Player player, GameManager gameManager, ReviveManager reviveManager, String[] args) {
        if (gameManager.getState() != GameState.PLAYING) {
            CustomMessage.error(player, "A revive is only available while the game is running.");
            return;
        }

        if (plugin.getScenarioManager().isEnabled(ScenarioType.CHILL_REVIVE)) {
            CustomMessage.error(player, "This command is disabled while Chill Revive is enabled.");
            return;
        }

        if (gameManager.getElapsedSeconds() >= plugin.getBorderManager().getMeetupDuration()) {
            CustomMessage.error(player, "This command is disabled at meetup.");
            return;
        }

        if (args.length < 2) {
            CustomMessage.error(player, "/hf revive <player>");
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null || target.getGameMode() != GameMode.SPECTATOR || !reviveManager.hasDeathState(target) || !reviveManager.revive(target)) {
            CustomMessage.error(player, "This player cannot be revived.");
            return;
        }

        CustomMessage.success(player, target.getName() + " has been revived.");
    }

    private int parsePositiveInt(String value) {
        return Math.max(1, Integer.parseInt(value));
    }

    private long parsePositiveMinutes(String value) {
        double minutes = Math.max(1.0 / 60.0, Double.parseDouble(value));
        return (long) (minutes * 60);
    }
}
