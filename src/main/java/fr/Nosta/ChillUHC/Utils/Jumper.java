package fr.Nosta.ChillUHC.Utils;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Jumper {

    private static void onJumpStart(Player player) {
        player.setGameMode(GameMode.SPECTATOR);
        player.setFlying(true);
        player.setAllowFlight(true);
        player.setGravity(false);
        player.setInvulnerable(true);
        player.setWalkSpeed(0f);
        player.setFlySpeed(0f);
    }

    private static void onJumpEnd(Player player) {
        player.setGameMode(GameMode.SURVIVAL);
        player.setFlying(false);
        player.setAllowFlight(false);
        player.setGravity(true);
        player.setInvulnerable(false);
        player.setWalkSpeed(0.2f);
        player.setFlySpeed(0.1f);
    }

    public static void jump(Main plugin, Player player, Location end, double height, int durationTicks) {
        onJumpStart(player);

        Location start = player.getLocation().clone();
        Vector mid = start.toVector().add(end.toVector()).multiply(0.5);
        double peakY = Math.max(start.getY(), end.getY()) + height;
        Location control = new Location(start.getWorld(), mid.getX(), peakY, mid.getZ());

        new BukkitRunnable() {

            int tick = 0;

            @Override
            public void run() {

                if (tick >= durationTicks) {
                    player.setVelocity(new Vector(0,0,0));
                    end.setYaw(player.getLocation().getYaw());
                    end.setPitch(player.getLocation().getPitch());
                    player.teleport(end);

                    onJumpEnd(player);
                    cancel();
                    return;
                }

                double t = (double) tick / durationTicks;
                double tPrev = (double) (tick - 1) / durationTicks;

                t = easeInOutCubic(Math.min(1.0, t));
                tPrev = easeInOutCubic(Math.max(0.0, tPrev));

                Vector prevPoint = bezierPoint(start, control, end, tPrev);
                Vector currentPoint = bezierPoint(start, control, end, t);

                Vector velocity = currentPoint.clone().subtract(prevPoint);
                Vector correction = currentPoint.clone().subtract(player.getLocation().toVector());

                double maxCorrection = 1.5;
                if (correction.length() > maxCorrection) {
                    correction.normalize().multiply(maxCorrection);
                }
                velocity.add(correction.multiply(0.5));

                player.setVelocity(velocity);
                tick++;
            }

        }.runTaskTimer(plugin, 0L, 1L);
    }

    private static Vector bezierPoint(Location p0, Location p1, Location p2, double t) {

        double u = 1 - t;

        Vector v0 = p0.toVector().multiply(u * u);
        Vector v1 = p1.toVector().multiply(2 * u * t);
        Vector v2 = p2.toVector().multiply(t * t);

        return v0.add(v1).add(v2);
    }

    private static double easeInOutCubic(double x) {
        return x < 0.5 ? 4 * x * x * x : 1 - Math.pow(-2 * x + 2, 3) / 2;
    }
}