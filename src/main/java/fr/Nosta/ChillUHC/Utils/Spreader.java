package fr.Nosta.ChillUHC.Utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Spreader {

    private static final Random RANDOM = new Random();

    public static List<Location> generate(Location center, int radius, int count, int minDistance) {

        World world = center.getWorld();
        List<Location> result = new ArrayList<>();

        int attempts = 0;
        int maxAttempts = count * 200;

        while (result.size() < count && attempts < maxAttempts) {

            attempts++;

            double angle = RANDOM.nextDouble() * Math.PI * 2;
            double dist = Math.sqrt(RANDOM.nextDouble()) * radius;

            double x = center.getX() + Math.cos(angle) * dist;
            double z = center.getZ() + Math.sin(angle) * dist;

            int blockX = (int) Math.floor(x);
            int blockZ = (int) Math.floor(z);

            int y = world.getHighestBlockYAt(blockX, blockZ);
            Location ground = new Location(world, blockX + 0.5, y, blockZ + 0.5);

            if (!isValidGround(ground)) continue;

            boolean tooClose = false;

            for (Location other : result) {

                double dx = other.getX() - ground.getX();
                double dz = other.getZ() - ground.getZ();

                if (dx * dx + dz * dz < minDistance * minDistance) {
                    tooClose = true;
                    break;
                }
            }

            if (tooClose) continue;

            result.add(ground.add(0, 1, 0));
        }

        return result;
    }

    private static boolean isValidGround(Location ground) {

        Material block = ground.getBlock().getType();

        if (block == Material.WATER || block == Material.LAVA) return false;

        if (!block.isSolid()) return false;

        Location feet = ground.clone().add(0, 1, 0);
        Location head = ground.clone().add(0, 2, 0);

        return feet.getBlock().isPassable() && head.getBlock().isPassable();
    }
}