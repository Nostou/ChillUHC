package fr.Nosta.ChillUHC.Commands;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.GameManager;
import fr.Nosta.ChillUHC.Managers.InventoryManager;
import fr.Nosta.ChillUHC.Managers.BorderManager;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        switch (subCommand) {
            case "start" -> {
                if (gm.getState() != GameState.WAITING) {
                    CustomMessage.error(player, "A game is already in progress.");
                    return true;
                }

                /*if (plugin.getTierManager().hasUndefinedPlayers()) {
                    CustomMessage.error(player, "Some players have an undefined tier");
                    return true;
                }*/

                gm.startGame();
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
            case "tier" -> {
                plugin.getInventoryManager().openTierInventory(player);
                return true;
            }
            case "scenarios" -> {
                plugin.getInventoryManager().openScenarioInventory(player);
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
            case "tiersee" -> CustomMessage.custom(player, NamedTextColor.YELLOW, plugin.getTierManager().logTierList());
        }
    }

    private void handleBorderCommand(Player player, BorderManager borderManager, String[] args) {
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
            CustomMessage.error(player, "Border values must be valid numbers.");
        }
    }

    private int parsePositiveInt(String value) {
        return Math.max(1, Integer.parseInt(value));
    }

    private long parsePositiveMinutes(String value) {
        double minutes = Math.max(1.0 / 60.0, Double.parseDouble(value));
        return (long) (minutes * 60);
    }
}
