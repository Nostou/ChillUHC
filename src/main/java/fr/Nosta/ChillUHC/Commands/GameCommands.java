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

import javax.swing.border.Border;

public class GameCommands implements CommandExecutor {

    private final Main plugin;

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
        BorderManager bm = plugin.getBorderManager();
        GameManager gm = plugin.getGameManager();

        if (args[0].equalsIgnoreCase("start")) {
            if (gm.getState() != GameState.WAITING) {
                CustomMessage.error(player, "A game is already in progress.");
                return;
            }

            /*if (plugin.getTierManager().hasUndefinedPlayers()) {
                CustomMessage.error(player, "Some players have an undefined tier");
                return;
            }*/

            gm.startGame();
        }

        else if (args[0].equalsIgnoreCase("stop")) {
            if (gm.getState() == GameState.WAITING) {
                CustomMessage.error(player, "No game is currently running.");
                return;
            }

            gm.stopGame();
        }

        else if (args[0].equalsIgnoreCase("border")) {
            if (args.length < 2) {
                CustomMessage.error(player, "/hf border <startRadius> [targetRadius] [meetupDuration] [shrinkDuration]");
                return;
            }

            int startRadius = Integer.parseInt(args[1]);
            if (startRadius <= 0) startRadius = 1;
            bm.setStartRadius(startRadius);

            if (args.length > 2) {
                int targetRadius = Integer.parseInt(args[2]);
                if (targetRadius <= 0) targetRadius = 1;
                bm.setTargetRadius(targetRadius);
            }

            if (args.length > 3) {
                double meetupMinutes = Double.parseDouble(args[3]);
                if (meetupMinutes <= 0) meetupMinutes = 1.0 / 60.0;
                bm.setMeetupDuration((long)(meetupMinutes*60));
            }

            if (args.length > 4) {
                double shrinkMinutes = Double.parseDouble(args[3]);
                if (shrinkMinutes <= 0) shrinkMinutes = 1.0 / 60.0;
                bm.setShrinkDuration((long)(shrinkMinutes*60));
            }

            CustomMessage.success(player, "WorldBorder has been updated !");
        }

        else if (args[0].equalsIgnoreCase("tier")) {
            plugin.getInventoryManager().openTierInventory(player);
        }
    }

    private void HandleNonOpCommands(Player player, String[] args) {
        GameManager gm = plugin.getGameManager();
        InventoryManager im = plugin.getInventoryManager();

        if (args[0].equalsIgnoreCase("invsee")) {
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
        else if (args[0].equalsIgnoreCase("team")) {
            if (gm.getState() != GameState.WAITING) {
                CustomMessage.error(player, "Team selection is not available.");
                return;
            }

            im.openTeamInventory(player);
        }
        else if (args[0].equalsIgnoreCase("tiersee")) {
            CustomMessage.custom(player, NamedTextColor.YELLOW, plugin.getTierManager().logTierList());
        }
    }
}
