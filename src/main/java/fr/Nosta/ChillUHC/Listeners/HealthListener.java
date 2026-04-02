package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class HealthListener implements Listener {

    private final Main plugin;

    public HealthListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getScoreboardManager().updateHealth(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        scheduleHealthUpdate(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onRegainHealth(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        scheduleHealthUpdate(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        scheduleHealthUpdate(event.getPlayer());
    }

    private void scheduleHealthUpdate(Player player) {
        plugin.getServer().getScheduler().runTask(plugin, () -> plugin.getScoreboardManager().updateHealth(player));
    }
}
