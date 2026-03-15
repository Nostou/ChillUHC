package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

public class TimberScenario implements Scenario, Listener {

    private static final int MAX_LOGS_PER_TREE = 256;

    private final Main plugin;

    public TimberScenario(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.TIMBER;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        Player player = event.getPlayer();
        Block origin = event.getBlock();
        Material logType = origin.getType();
        ItemStack tool = player.getInventory().getItemInMainHand();

        if (!isLog(logType) || !isAxe(tool.getType())) return;

        Set<Block> connectedLogs = getConnectedLogs(origin, logType);
        if (!hasNearbyLeaves(connectedLogs, logType)) return;

        breakConnectedLogs(origin, connectedLogs, tool);
    }

    private void breakConnectedLogs(Block origin, Set<Block> connectedLogs, ItemStack tool) {
        int brokenLogs = 0;

        for (Block current : connectedLogs) {
            if (brokenLogs >= MAX_LOGS_PER_TREE) break;

            if (!current.equals(origin)) {
                current.breakNaturally(tool, true);
            }

            brokenLogs++;
        }
    }

    private Set<Block> getConnectedLogs(Block origin, Material logType) {
        ArrayDeque<Block> queue = new ArrayDeque<>();
        Set<Block> visited = new HashSet<>();

        queue.add(origin);

        while (!queue.isEmpty() && visited.size() < MAX_LOGS_PER_TREE) {
            Block current = queue.poll();
            if (current.getType() != logType) continue;
            if (!visited.add(current)) continue;

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        queue.add(current.getRelative(dx, dy, dz));
                    }
                }
            }
        }

        return visited;
    }

    private boolean isLog(Material material) {
        return Tag.LOGS.isTagged(material);
    }

    private boolean hasNearbyLeaves(Set<Block> connectedLogs, Material logType) {
        for (Block log : connectedLogs) {
            for (int dx = -2; dx <= 2; dx++) {
                for (int dy = -2; dy <= 2; dy++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        Block nearby = log.getRelative(dx, dy, dz);
                        Material type = nearby.getType();

                        if (type == logType) continue;
                        if (Tag.LEAVES.isTagged(type)) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    private boolean isAxe(Material material) {
        return material == Material.WOODEN_AXE
                || material == Material.STONE_AXE
                || material == Material.COPPER_AXE
                || material == Material.IRON_AXE
                || material == Material.GOLDEN_AXE
                || material == Material.DIAMOND_AXE
                || material == Material.NETHERITE_AXE;
    }
}
