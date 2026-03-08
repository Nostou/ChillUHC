package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Inventories.InvSeeInventory;
import fr.Nosta.ChillUHC.Inventories.TeamInventory;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.TeamManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;

public class InventoryListener implements Listener {

    private final Main plugin;

    public InventoryListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        if (event.getClickedInventory() == null) return;

        Inventory topInv = event.getView().getTopInventory();
        if (InvSeeInventory.isInvSeeInventory(topInv)) {
            event.setCancelled(true);
            return;
        }

        if (TeamInventory.isTeamInventory(topInv)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null) return;

            TeamManager tm = plugin.getTeamManager();
            if (clickedItem.getType() == Material.BARRIER) {
                tm.setPlayerTeam(player, null);
                player.closeInventory();
                return;
            }

            tm.setPlayerTeam(player, getDisplayName(clickedItem));
            player.closeInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (InvSeeInventory.isInvSeeInventory(topInv)) {
            event.setCancelled(true);
        }
    }

    private String getDisplayName(ItemStack item) {
        Component display = item.getItemMeta().displayName();
        if (display == null) return null;
        return PlainTextComponentSerializer.plainText().serialize(display);
    }
}
