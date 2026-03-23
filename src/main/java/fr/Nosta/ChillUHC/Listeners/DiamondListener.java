package fr.Nosta.ChillUHC.Listeners;

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DiamondListener implements Listener {

    private final Main plugin;

    public DiamondListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onArmorChange(PlayerArmorChangeEvent event) {
        if (!isDiamondArmor(event.getNewItem())) return;

        Player player = event.getPlayer();
        if (canWearDiamondArmor(player, event.getOldItem(), event.getNewItem())) return;

        PlayerInventory inventory = player.getInventory();
        inventory.setItem(event.getSlot(), cloneOrNull(event.getOldItem()));
        player.getWorld().dropItem(player.getLocation(), event.getNewItem().clone());

        sendLimitMessage(player);
    }

    private boolean canWearDiamondArmor(Player player, ItemStack oldItem, ItemStack newItem) {
        int diamondPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (isDiamondArmor(item)) diamondPieces++;
        }

        if (isDiamondArmor(oldItem)) {
            diamondPieces--;
        }

        return diamondPieces <= plugin.getTierManager().getMaxDiamondArmorPieces(player);
    }

    private void sendLimitMessage(Player player) {
        int maxPieces = plugin.getTierManager().getMaxDiamondArmorPieces(player);
        CustomMessage.error(player, "Tier " + plugin.getTierManager().getTier(player) + " can only wear " + maxPieces + " diamond armor pieces.");
    }

    private ItemStack cloneOrNull(ItemStack item) {
        return item == null || item.getType().isAir() ? null : item.clone();
    }

    private boolean isDiamondArmor(ItemStack item) {
        if (item == null) return false;

        Material type = item.getType();
        return type == Material.DIAMOND_HELMET
                || type == Material.DIAMOND_CHESTPLATE
                || type == Material.DIAMOND_LEGGINGS
                || type == Material.DIAMOND_BOOTS;
    }
}
