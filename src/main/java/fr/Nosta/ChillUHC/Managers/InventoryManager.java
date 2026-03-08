package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Inventories.InvSeeInventory;
import fr.Nosta.ChillUHC.Inventories.TeamInventory;
import fr.Nosta.ChillUHC.Inventories.TierInventory;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.entity.Player;

public class InventoryManager {

    private final Main plugin;

    public InventoryManager(Main plugin) {
        this.plugin = plugin;
    }

    public void openInvSeeInventory(Player player, Player target) {
        new InvSeeInventory(target).open(player);
    }

    public void openTeamInventory(Player player) {
        new TeamInventory(plugin.getTeamManager()).open(player);
    }

    public void openTierInventory(Player player) {
        new TierInventory(plugin).open(player);
    }
}