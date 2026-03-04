package fr.Nosta.ChillUHC.Commands;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.GameManager;
import fr.Nosta.ChillUHC.Managers.InventoryManager;
import fr.Nosta.ChillUHC.Managers.BorderManager;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GameCommands implements CommandExecutor {

    private final Main plugin;
    private GameManager getGameManager() { return plugin.getManager(GameManager.class); }
    private InventoryManager getInventoryManager() { return plugin.getManager(InventoryManager.class); }
    private BorderManager getWorldBorderManager() { return plugin.getManager(BorderManager.class); }

    public GameCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {

        //Return if console
        if (!(sender instanceof Player player)) return true;
        if (!s.equalsIgnoreCase("hf") || args.length == 0) return true;

        if (player.isOp()) HandleOpCommands(player, args);
        HandleNonOpCommands(player, args);

        return true;
    }

    private void HandleOpCommands(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("start")) {
            if (getGameManager().getState() != GameState.WAITING) {
                CustomMessage.error(player, "A game is already in progress.");
                return;
            }

            getGameManager().startGame();
        }
        else if (args[0].equalsIgnoreCase("stop")) {
            if (getGameManager().getState() == GameState.WAITING) {
                CustomMessage.error(player, "No game is currently running.");
                return;
            }

            getGameManager().stopGame();
        }

        else if (args[0].equalsIgnoreCase("border")) {
            if (args.length < 2) {
                CustomMessage.error(player, "/hf border <startRadius> [targetRadius] [meetupDuration] [shrinkDuration]");
                return;
            }

            BorderManager wbManager = getWorldBorderManager();
            int startRadius = Integer.parseInt(args[1]);
            if (startRadius <= 0) startRadius = 1;
            wbManager.setStartRadius(startRadius);

            if (args.length > 2) {
                int targetRadius = Integer.parseInt(args[2]);
                if (targetRadius <= 0) targetRadius = 1;
                wbManager.setTargetRadius(targetRadius);
            }

            if (args.length > 3) {
                double meetupMinutes = Double.parseDouble(args[3]);
                if (meetupMinutes <= 0) meetupMinutes = 1.0 / 60.0;
                wbManager.setMeetupDuration((long)(meetupMinutes*60));
            }

            if (args.length > 4) {
                double shrinkMinutes = Double.parseDouble(args[3]);
                if (shrinkMinutes <= 0) shrinkMinutes = 1.0 / 60.0;
                wbManager.setShrinkDuration((long)(shrinkMinutes*60));
            }

            CustomMessage.success(player, "WorldBorder has been updated !");
        }
    }

    private void HandleNonOpCommands(Player player, String[] args) {
        if (args[0].equalsIgnoreCase("invsee")) {
            if (!player.isOp() && player.getGameMode() != GameMode.SPECTATOR) {
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

            getInventoryManager().openInvSeeInventory(player, target);
        }
        else if (args[0].equalsIgnoreCase("team")) {
            if (getGameManager().getState() != GameState.WAITING) {
                CustomMessage.error(player, "Team selection is not available.");
                return;
            }

            getInventoryManager().openTeamInventory(player);
        }
    }
}
