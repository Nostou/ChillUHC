package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Inventories.InvSeeInventory;
import fr.Nosta.ChillUHC.Inventories.TeamInventory;
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
        TeamManager teamManager = plugin.getManager(TeamManager.class);
        TeamInventory inventory = new TeamInventory(teamManager);
        inventory.open(player);
    }
}