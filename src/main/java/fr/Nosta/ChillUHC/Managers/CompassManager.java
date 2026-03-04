package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Chill.ChillPlayer;
import fr.Nosta.ChillUHC.Chill.ChillTeam;
import fr.Nosta.ChillUHC.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class CompassManager {

    private final Main plugin;

    public CompassManager(Main plugin) {
        this.plugin = plugin;
    }

    public void updatePlayer(Player player) {

        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Location playerLoc = player.getLocation();
        Location spawn = player.getWorld().getSpawnLocation();

        // ===== CENTER (2D distance) =====
        double centerAngle = computeAngle(playerLoc, spawn, false);
        String centerArrow = getArrow(centerAngle);
        int centerDistance = get2DDistance(playerLoc, spawn);

        Component message = Component.text(centerArrow + " Center ")
                .append(Component.text(centerDistance + "m", NamedTextColor.WHITE));

        // ===== TEAM ALLIES =====
        ChillPlayer chillPlayer = plugin.getManager(PlayerManager.class).getChillPlayer(player);
        ChillTeam team = chillPlayer.getTeam();

        if (team != null) {

            List<Player> allies = new ArrayList<>();
            for (UUID uuid : team.getPlayers()) {
                Player ally = Bukkit.getPlayer(uuid);
                if (ally == null) continue;
                if (!ally.isOnline()) continue;
                if (ally.getGameMode() == GameMode.SPECTATOR) continue;
                if (ally.equals(player)) continue;

                allies.add(ally);
            }

            allies.sort(Comparator.comparing(Player::getName, String.CASE_INSENSITIVE_ORDER));

            if (!allies.isEmpty()) {
                NamedTextColor allyColor = team.getColor();

                for (Player ally : allies) {
                    double allyAngle = computeAngle(playerLoc, ally.getLocation(), true);
                    String allyArrow = getArrow(allyAngle);
                    int allyDistance = (int)playerLoc.distance(ally.getLocation());

                    message = message.append(Component.text(" | ", NamedTextColor.DARK_GRAY))
                            .append(Component.text(allyArrow + " " + ally.getName() + " ", allyColor))
                            .append(Component.text(allyDistance + "m", NamedTextColor.WHITE));
                }
            }
        }

        player.sendActionBar(message);
    }

    private double computeAngle(Location from, Location target, boolean includeY) {

        Vector direction = target.toVector().subtract(from.toVector());

        if (!includeY) direction.setY(0);

        double angleToTarget = Math.toDegrees(Math.atan2(-direction.getX(), direction.getZ()));
        double playerYaw = from.getYaw();

        return normalizeAngle(angleToTarget - playerYaw);
    }

    private double normalizeAngle(double angle) {
        angle %= 360;
        if (angle < -180) angle += 360;
        if (angle > 180) angle -= 360;
        return angle;
    }

    private int get2DDistance(Location a, Location b) {
        double dx = a.getX() - b.getX();
        double dz = a.getZ() - b.getZ();
        return (int) Math.sqrt(dx * dx + dz * dz);
    }

    private String getArrow(double angle) {
        if (angle >= -45 && angle < 45) return "↑";
        if (angle >= 45 && angle < 135) return "→";
        if (angle >= -135 && angle < -45) return "←";
        return "↓";
    }
}