package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.TimeUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class TabManager {

    private final Main plugin;

    public TabManager(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        Bukkit.getScheduler().runTaskTimer(plugin, this::updateTab, 0L, 20L);
    }

    private void updateTab() {
        Component footer = getFooter();
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), footer);
        }
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
}