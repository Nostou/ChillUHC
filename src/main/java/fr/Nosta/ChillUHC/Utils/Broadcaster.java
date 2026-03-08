package fr.Nosta.ChillUHC.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.time.Duration;

public class Broadcaster {

    public static void titleAll(String title, NamedTextColor color, int fadeIn, int stay, int fadeOut) {
        Title t = Title.title(Component.text(title, color), Component.empty(),
                Title.Times.times(Duration.ofMillis(fadeIn), Duration.ofMillis(stay), Duration.ofMillis(fadeOut))
        );

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(t);
        }
    }

    public static void soundAll(Sound sound, int volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}
