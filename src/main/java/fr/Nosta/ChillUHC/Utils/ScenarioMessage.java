package fr.Nosta.ChillUHC.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ScenarioMessage {

    private ScenarioMessage() {}

    public static Component prefix(String scenarioName) {
        return Component.text("["+scenarioName+"]", NamedTextColor.GOLD).append(Component.text(" » ", NamedTextColor.DARK_GRAY));
    }

    private static void send(Player player, String scenarioName, NamedTextColor messageColor, String message) {
        player.sendMessage(prefix(scenarioName).append(Component.text(message, messageColor)));
    }

    public static void info(Player player, String scenarioName, String message) {
        send(player, scenarioName, NamedTextColor.YELLOW, message);
    }

    public static void success(Player player, String scenarioName, String message) {
        send(player, scenarioName, NamedTextColor.GREEN, message);
    }

    public static void error(Player player, String scenarioName, String message) {
        send(player, scenarioName, NamedTextColor.RED, message);
    }

    public static void successAll(String scenarioName, String message) {
        Bukkit.getOnlinePlayers().forEach(p -> success(p, scenarioName, message));
    }

    public static void errorAll(String scenarioName, String message) {
        Bukkit.getOnlinePlayers().forEach(p -> error(p, scenarioName, message));
    }

    public static void infoAll(String scenarioName, String message) {
        Bukkit.getOnlinePlayers().forEach(p -> info(p, scenarioName, message));
    }
}
