package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Inventories.InvSeeInventory;
import fr.Nosta.ChillUHC.Inventories.ScenarioInventory;
import fr.Nosta.ChillUHC.Inventories.TeamInventory;
import fr.Nosta.ChillUHC.Inventories.TierInventory;
import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Managers.TeamManager;
import fr.Nosta.ChillUHC.Managers.TierManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

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
        InventoryHolder holder = topInv.getHolder();

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
                plugin.getInventoryManager().refreshTeamInventory();
                player.closeInventory();
                return;
            }

            tm.setPlayerTeam(player, getDisplayName(clickedItem));
            plugin.getInventoryManager().refreshTeamInventory();
            player.closeInventory();
            return;
        }

        if (ScenarioInventory.isScenarioInventory(topInv)) {
            event.setCancelled(true);

            if (plugin.getGameManager().getState() != GameState.WAITING) {
                CustomMessage.error(player, "Scenarios can only be changed before the game starts.");
                return;
            }

            if (!(topInv.getHolder() instanceof ScenarioInventory scenarioInventory)) return;

            ScenarioType scenario = scenarioInventory.getScenario(event.getRawSlot());
            if (scenario == null) return;

            boolean enabled = plugin.getScenarioManager().toggle(scenario);
            plugin.getInventoryManager().refreshScenarioInventory();
            CustomMessage.info(player, scenario.getDisplayName() + " is now " + (enabled ? "enabled." : "disabled."));
            return;
        }

        if (TierInventory.isTierInventory(topInv)) {
            event.setCancelled(true);

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() != Material.PLAYER_HEAD) return;

            SkullMeta meta = (SkullMeta) clickedItem.getItemMeta();
            Player target = meta.getOwningPlayer().getPlayer();

            if (target == null) return;
            TierManager tierManager = plugin.getTierManager();

            if (event.isLeftClick()) tierManager.increaseTier(target);
            if (event.isRightClick()) tierManager.decreaseTier(target);
            plugin.getInventoryManager().refreshTierInventory();
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        Inventory topInv = event.getView().getTopInventory();
        if (InvSeeInventory.isInvSeeInventory(topInv)
                || ScenarioInventory.isScenarioInventory(topInv)
                || TeamInventory.isTeamInventory(topInv)
                || TierInventory.isTierInventory(topInv)) {
            event.setCancelled(true);
        }
    }

    private String getDisplayName(ItemStack item) {
        Component display = item.getItemMeta().displayName();
        if (display == null) return null;
        return PlainTextComponentSerializer.plainText().serialize(display);
    }
}
