package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;

public class PlayerListener implements Listener {

    private final Main plugin;

    public PlayerListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPortal(PlayerPortalEvent event) {
        World.Environment target = event.getTo().getWorld().getEnvironment();

        if (target == World.Environment.NETHER || target == World.Environment.THE_END) {
            event.setCancelled(true);
            CustomMessage.error(event.getPlayer(), target.name() + " is disabled.");
        }
    }
}
