package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabManager {

    private static final int[] HEADER_GRADIENT = {
            0xFF73C6,
            0xFF86B2,
            0xFF9B93,
            0xFFB56D,
            0xFFD447
    };

    private final Main plugin;

    public TabManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateTab, 0L, 20L);
    }

    private void updateTab() {
        Component header = getHeader();
        Component footer = getFooter();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(header, footer);
        }
    }

    private Component getHeader() {
        return buildGradientText("Chill UHC")
                .append(Component.text(" by Nosta", NamedTextColor.AQUA))
                .append(Component.newline());
    }

    private Component getFooter() {
        GameState currentState = plugin.getGameManager().getState();
        BorderManager bm = plugin.getBorderManager();

        if (currentState != GameState.PLAYING) {
            int startRadius = bm.getStartRadius();
            String meetupDuration = TimeUtils.formatToMMSS(bm.getMeetupDuration());
            int targetRadius = bm.getTargetRadius();
            String shrinkDuration = TimeUtils.formatToMMSS(bm.getShrinkDuration());

            return Component.text("\nMeetup: ", NamedTextColor.GRAY)
                    .append(Component.text(startRadius+"x"+startRadius, NamedTextColor.GREEN))
                    .append(Component.text(" ("+meetupDuration+")", NamedTextColor.YELLOW))
                    .append(Component.text("\nShrink: ", NamedTextColor.GRAY))
                    .append(Component.text(targetRadius+"x"+targetRadius, NamedTextColor.GREEN))
                    .append(Component.text(" ("+shrinkDuration+")", NamedTextColor.YELLOW));
        }

        int currentRadius = bm.getCurrentRadius();
        if (bm.isShrinking()) {
            return Component.text("\nBorder: ", NamedTextColor.GRAY)
                    .append(Component.text(currentRadius+"x"+currentRadius, NamedTextColor.GREEN))
                    .append(Component.text("\nMeetup is now !", NamedTextColor.GOLD, TextDecoration.BOLD));
        }

        String time = TimeUtils.formatToMMSS(bm.getMeetupEnd());
        return Component.text("\nBorder: ", NamedTextColor.GRAY)
                .append(Component.text(currentRadius+"x" +currentRadius, NamedTextColor.GREEN))
                .append(Component.text("\nMeetup: ", NamedTextColor.GRAY))
                .append(Component.text(time, NamedTextColor.YELLOW));
    }

    private Component buildGradientText(String text) {
        TextComponent.Builder builder = Component.text();
        int visibleCharCount = (int) text.chars().filter(character -> character != ' ').count();
        int visibleIndex = 0;

        for (char character : text.toCharArray()) {
            if (character == ' ') {
                builder.append(Component.text(" "));
                continue;
            }

            builder.append(Component.text(
                    String.valueOf(character),
                    getGradientColor(visibleIndex, visibleCharCount),
                    TextDecoration.BOLD
            ));
            visibleIndex++;
        }

        return builder.build();
    }

    private TextColor getGradientColor(int index, int length) {
        if (length <= 1) {
            return TextColor.color(HEADER_GRADIENT[0]);
        }

        float progress = (float) index / (length - 1);
        float scaledProgress = progress * (HEADER_GRADIENT.length - 1);

        int startIndex = (int) Math.floor(scaledProgress);
        int endIndex = Math.min(startIndex + 1, HEADER_GRADIENT.length - 1);
        float localProgress = scaledProgress - startIndex;

        return TextColor.color(interpolateColor(HEADER_GRADIENT[startIndex], HEADER_GRADIENT[endIndex], localProgress));
    }

    private int interpolateColor(int startColor, int endColor, float progress) {
        int startRed = (startColor >> 16) & 0xFF;
        int startGreen = (startColor >> 8) & 0xFF;
        int startBlue = startColor & 0xFF;

        int endRed = (endColor >> 16) & 0xFF;
        int endGreen = (endColor >> 8) & 0xFF;
        int endBlue = endColor & 0xFF;

        int red = interpolateChannel(startRed, endRed, progress);
        int green = interpolateChannel(startGreen, endGreen, progress);
        int blue = interpolateChannel(startBlue, endBlue, progress);

        return (red << 16) | (green << 8) | blue;
    }

    private int interpolateChannel(int start, int end, float progress) {
        return Math.round(start + (end - start) * progress);
    }
}
