package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

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

    @EventHandler
    public void onSpectate(PlayerTeleportEvent event) {
        if (event.getCause() == PlayerTeleportEvent.TeleportCause.SPECTATE && plugin.getGameManager().getState() != GameState.PLAYING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        double multiplier = getTierDamageMultiplier(player);
        event.setDamage(event.getDamage() * multiplier);
    }

    private double getTierDamageMultiplier(Player player) {
        int tier = plugin.getTierManager().getTier(player);

        return switch (tier) {
            case 2 -> 0.9;
            case 3 -> 0.8;
            default -> 1.0;
        };
    }
}
