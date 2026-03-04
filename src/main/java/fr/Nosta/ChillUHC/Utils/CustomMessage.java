package fr.Nosta.ChillUHC.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public final class CustomMessage {

    private CustomMessage() {}

    private static Component prefix(NamedTextColor color) { return Component.text("[HF] ", color); }

    private static void send(Player player, NamedTextColor prefixColor, NamedTextColor messageColor, String message) {
        player.sendMessage(prefix(prefixColor).append(Component.text(message, messageColor)));
    }

    private static void send(Player player, NamedTextColor prefixColor, Component message) {
        player.sendMessage(prefix(prefixColor).append(message));
    }

    public static void success(Player player, String message) {
        send(player, NamedTextColor.DARK_GREEN, NamedTextColor.GREEN, message);
    }

    public static void error(Player player, String message) {
        send(player, NamedTextColor.DARK_RED, NamedTextColor.RED, message);
    }

    public static void info(Player player, String message) {
        send(player, NamedTextColor.GOLD, NamedTextColor.YELLOW, message);
    }

    public static void custom(Player player, NamedTextColor prefixColor, Component message) {
        send(player, prefixColor, message);
    }

    public static void successAll(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> success(p, message));
    }

    public static void errorAll(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> error(p, message));
    }

    public static void infoAll(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> info(p, message));
    }

    public static void customAll(NamedTextColor prefixColor, Component message) {
        Bukkit.getOnlinePlayers().forEach(p -> custom(p, prefixColor, message));
    }
}