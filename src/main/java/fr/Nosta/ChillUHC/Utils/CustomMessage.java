package fr.Nosta.ChillUHC.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CustomMessage {

    private CustomMessage() {}

    private static Component prefix(NamedTextColor color) {
        return Component.text("[HF] ", color);
    }

    public static Component prefix(String scenarioName) {
        return Component.text("[" + scenarioName + "]", NamedTextColor.GOLD)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY));
    }

    public static void send(Player player, Component message) {
        player.sendMessage(message);
    }

    public static void broadcast(Component message) {
        Bukkit.getOnlinePlayers().forEach(player -> send(player, message));
    }

    private static void sendInternal(Player player, NamedTextColor prefixColor, NamedTextColor messageColor, String message) {
        send(player, prefix(prefixColor).append(Component.text(message, messageColor)));
    }

    public static void success(Player player, String message) {
        sendInternal(player, NamedTextColor.DARK_GREEN, NamedTextColor.GREEN, message);
    }

    public static void error(Player player, String message) {
        sendInternal(player, NamedTextColor.DARK_RED, NamedTextColor.RED, message);
    }

    public static void info(Player player, String message) {
        sendInternal(player, NamedTextColor.GOLD, NamedTextColor.YELLOW, message);
    }

    public static void successAll(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> success(player, message));
    }

    public static void errorAll(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> error(player, message));
    }

    public static void infoAll(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> info(player, message));
    }
}
