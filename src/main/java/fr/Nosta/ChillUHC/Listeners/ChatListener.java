package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.PlayerManager;
import fr.Nosta.ChillUHC.Managers.TeamManager;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Main plugin;
    private PlayerManager getPlayerManager() { return plugin.getManager(PlayerManager.class); }

    public ChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        NamedTextColor color = getPlayerManager().getChillPlayer(player).getColor();
        Component message = Component.text(player.getName(), color)
                .append(Component.text(" » ", NamedTextColor.DARK_GRAY))
                .append(event.message().color(NamedTextColor.WHITE));

        Bukkit.getServer().sendMessage(message);
    }
}
