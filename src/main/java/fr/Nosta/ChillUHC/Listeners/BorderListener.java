package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class BorderListener implements Listener {

    private final Main plugin;

    public BorderListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBorderDamage(EntityDamageEvent event) {

        if (event.getCause() != EntityDamageEvent.DamageCause.WORLD_BORDER) return;
        if (!(event.getEntity() instanceof Player player)) return;

        event.setCancelled(true);

        WorldBorder border = player.getWorld().getWorldBorder();
        Location center = border.getCenter();
        double radius = border.getSize() / 2.0;
        double safeOffset = 20.0;

        Location loc = player.getLocation();

        double minX = center.getX() - radius;
        double maxX = center.getX() + radius;
        double minZ = center.getZ() - radius;
        double maxZ = center.getZ() + radius;

        double safeMinX = minX + safeOffset;
        double safeMaxX = maxX - safeOffset;
        double safeMinZ = minZ + safeOffset;
        double safeMaxZ = maxZ - safeOffset;

        double newX = loc.getX();
        double newZ = loc.getZ();

        if (newX <= minX) newX = safeMinX;
        if (newX >= maxX) newX = safeMaxX;
        if (newZ <= minZ) newZ = safeMinZ;
        if (newZ >= maxZ) newZ = safeMaxZ;

        int surfaceY = player.getWorld().getHighestBlockYAt((int)newX, (int)newZ) + 1;
        Location safeLocation = new Location(player.getWorld(), newX, surfaceY, newZ, player.getYaw(), player.getPitch());

        player.teleport(safeLocation);
    }
}
