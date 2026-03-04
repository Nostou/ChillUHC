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
    public GameManager getGameManager() { return plugin.getManager(GameManager.class); }
    public BorderManager getWorldBorderManager() { return plugin.getManager(BorderManager.class); }

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
        BorderManager wb = getWorldBorderManager();
        GameState currentState = getGameManager().getState();

        if (currentState != GameState.PLAYING) {
            int startRadius = wb.getStartRadius();
            String meetupDuration = TimeUtils.formatToMMSS(wb.getMeetupDuration());
            int targetRadius = wb.getTargetRadius();
            String shrinkDuration = TimeUtils.formatToMMSS(wb.getShrinkDuration());

            return Component.text("\nMeetup: ", NamedTextColor.GRAY)
                    .append(Component.text(startRadius+"x"+startRadius, NamedTextColor.GREEN))
                    .append(Component.text(" ("+meetupDuration+")", NamedTextColor.YELLOW))
                    .append(Component.text("\nShrink: ", NamedTextColor.GRAY))
                    .append(Component.text(targetRadius+"x"+targetRadius, NamedTextColor.GREEN))
                    .append(Component.text(" ("+shrinkDuration+")", NamedTextColor.YELLOW));
        }

        int currentRadius = wb.getCurrentRadius();
        if (wb.isShrinking()) {
            return Component.text("\nBorder: ", NamedTextColor.GRAY)
                    .append(Component.text(currentRadius+"x"+currentRadius, NamedTextColor.GREEN))
                    .append(Component.text("\nMeetup is now !", NamedTextColor.GOLD, TextDecoration.BOLD));
        }

        String time = TimeUtils.formatToMMSS(wb.getMeetupEnd());
        return Component.text("\nBorder: ", NamedTextColor.GRAY)
                .append(Component.text(currentRadius+"x" +currentRadius, NamedTextColor.GREEN))
                .append(Component.text("\nMeetup: ", NamedTextColor.GRAY))
                .append(Component.text(time, NamedTextColor.YELLOW));
    }
}