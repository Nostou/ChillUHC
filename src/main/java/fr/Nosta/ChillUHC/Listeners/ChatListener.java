package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Main plugin;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        NamedTextColor playerColor = plugin.getTeamManager().getColor(player);
        Component message = Component.text(player.getName(), playerColor)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(event.message().color(NamedTextColor.WHITE));

        Bukkit.getServer().sendMessage(message);
    }
}
