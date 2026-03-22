package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.Material;
import org.bukkit.event.Event;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DiamondListener implements Listener {

    private final Main plugin;

    public DiamondListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack attemptedArmor = getAttemptedArmorFromClick(event, player);
        if (!isDiamondArmor(attemptedArmor)) return;
        if (hasReachedDiamondArmorLimit(player)) return;

        event.setCancelled(true);
        sendLimitMessage(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (!isDiamondArmor(event.getOldCursor())) return;
        if (!targetsArmorSlot(event)) return;
        if (hasReachedDiamondArmorLimit(player)) return;

        event.setCancelled(true);
        sendLimitMessage(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();
        if (!isDiamondArmor(item)) return;
        if (hasReachedDiamondArmorLimit(event.getPlayer())) return;

        event.setUseItemInHand(Event.Result.DENY);
        event.setCancelled(true);
        sendLimitMessage(event.getPlayer());
    }

    private ItemStack getAttemptedArmorFromClick(InventoryClickEvent event, Player player) {
        if (event.isShiftClick()
                && event.getClickedInventory() instanceof PlayerInventory
                && event.getSlotType() != InventoryType.SlotType.ARMOR) {
            return event.getCurrentItem();
        }

        if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
            ItemStack cursor = event.getCursor();
            if (!cursor.getType().isAir()) {
                return cursor;
            }

            if (event.getClick().isKeyboardClick()) {
                int hotbarButton = event.getHotbarButton();
                if (hotbarButton >= 0 && hotbarButton < 9) {
                    return player.getInventory().getItem(hotbarButton);
                }
            }
        }

        return null;
    }

    private boolean hasReachedDiamondArmorLimit(Player player) {
        int diamondPieces = 0;
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (isDiamondArmor(item)) diamondPieces++;
        }

        return diamondPieces < plugin.getTierManager().getMaxDiamondArmorPieces(player);
    }

    private boolean targetsArmorSlot(InventoryDragEvent event) {
        for (int rawSlot : event.getRawSlots()) {
            if (rawSlot >= 5 && rawSlot <= 8) {
                return true;
            }
        }
        return false;
    }

    private void sendLimitMessage(Player player) {
        int maxPieces = plugin.getTierManager().getMaxDiamondArmorPieces(player);
        CustomMessage.error(player, "Tier " + plugin.getTierManager().getTier(player) + " can only wear " + maxPieces + " diamond armor pieces.");
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
