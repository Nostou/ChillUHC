package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class BorderListener implements Listener {

    private static final double TELEPORT_TRIGGER_OFFSET = 0.0;
    private static final double TELEPORT_SAFE_OFFSET = 2.0;
    private static final long BORDER_CHECK_PERIOD_TICKS = 10L;

    private final Main plugin;

    public BorderListener(Main plugin) {
        this.plugin = plugin;
        startBorderCheckTask();
    }

    private void startBorderCheckTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getGameMode() == GameMode.SPECTATOR) continue;

                    WorldBorder border = player.getWorld().getWorldBorder();
                    Location location = player.getLocation();
                    if (isInsideSafeZone(location, border)) continue;

                    teleportInsideBorder(player, border, location);
                }
            }
        }.runTaskTimer(plugin, BORDER_CHECK_PERIOD_TICKS, BORDER_CHECK_PERIOD_TICKS);
    }

    @EventHandler
    public void onBorderDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.WORLD_BORDER) return;
        if (!(event.getEntity() instanceof Player player)) return;

        event.setCancelled(true);
        teleportInsideBorder(player, player.getWorld().getWorldBorder(), player.getLocation());
    }

    private boolean isInsideSafeZone(Location location, WorldBorder border) {
        Location center = border.getCenter();
        double radius = border.getSize() / 2.0;

        double minX = center.getX() - radius + TELEPORT_TRIGGER_OFFSET;
        double maxX = center.getX() + radius - TELEPORT_TRIGGER_OFFSET;
        double minZ = center.getZ() - radius + TELEPORT_TRIGGER_OFFSET;
        double maxZ = center.getZ() + radius - TELEPORT_TRIGGER_OFFSET;

        return location.getX() > minX
                && location.getX() < maxX
                && location.getZ() > minZ
                && location.getZ() < maxZ;
    }

    private void teleportInsideBorder(Player player, WorldBorder border, Location source) {
        Location center = border.getCenter();
        double radius = border.getSize() / 2.0;

        double minX = center.getX() - radius;
        double maxX = center.getX() + radius;
        double minZ = center.getZ() - radius;
        double maxZ = center.getZ() + radius;

        double safeMinX = minX + TELEPORT_SAFE_OFFSET;
        double safeMaxX = maxX - TELEPORT_SAFE_OFFSET;
        double safeMinZ = minZ + TELEPORT_SAFE_OFFSET;
        double safeMaxZ = maxZ - TELEPORT_SAFE_OFFSET;

        double newX = source.getX();
        double newZ = source.getZ();

        if (newX <= minX + TELEPORT_TRIGGER_OFFSET) newX = safeMinX;
        if (newX >= maxX - TELEPORT_TRIGGER_OFFSET) newX = safeMaxX;
        if (newZ <= minZ + TELEPORT_TRIGGER_OFFSET) newZ = safeMinZ;
        if (newZ >= maxZ - TELEPORT_TRIGGER_OFFSET) newZ = safeMaxZ;

        int surfaceY = player.getWorld().getHighestBlockYAt((int) newX, (int) newZ) + 1;
        Location safeLocation = new Location(player.getWorld(), newX, surfaceY, newZ, source.getYaw(), source.getPitch());

        player.teleport(safeLocation);
    }
}
