package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;

public class ChatListener implements Listener {

    private final Main plugin;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        Team team = plugin.getTeamManager().getTeam(player);
        Component message = Component.text(player.getName(), team.color())
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(event.message().color(NamedTextColor.WHITE));

        Bukkit.getServer().sendMessage(message);
    }
}
