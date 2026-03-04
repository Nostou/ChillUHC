package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Random;
import java.util.Set;

public class AppleDropListener implements Listener {

    private static final double APPLE_DROP_RATE = 0.1;
    private static final Random RANDOM = new Random();

    private static final Set<Material> LEAVES = Set.of(
            Material.ACACIA_LEAVES,
            Material.AZALEA_LEAVES,
            Material.BIRCH_LEAVES,
            Material.CHERRY_LEAVES,
            Material.DARK_OAK_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.JUNGLE_LEAVES,
            Material.MANGROVE_LEAVES,
            Material.OAK_LEAVES,
            Material.PALE_OAK_LEAVES,
            Material.SPRUCE_LEAVES
    );

    private final Main plugin;

    public AppleDropListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeafBreak(BlockBreakEvent event) {
        if (!LEAVES.contains(event.getBlock().getType())) return;

        // Drop custom
        if (RANDOM.nextDouble() <= APPLE_DROP_RATE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
        }
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        if (!LEAVES.contains(event.getBlock().getType())) return;

        if (RANDOM.nextDouble() <= APPLE_DROP_RATE) {
            event.getBlock().getWorld().dropItemNaturally(event.getBlock().getLocation(), new ItemStack(Material.APPLE));
        }
    }
}
