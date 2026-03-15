package fr.Nosta.ChillUHC.Managers;

import fr.Nosta.ChillUHC.Inventories.InvSeeInventory;
import fr.Nosta.ChillUHC.Inventories.ScenarioInventory;
import fr.Nosta.ChillUHC.Inventories.TeamInventory;
import fr.Nosta.ChillUHC.Inventories.TierInventory;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.entity.Player;

public class InventoryManager {

    private final Main plugin;
    private ScenarioInventory scenarioInventory;
    private TeamInventory teamInventory;
    private TierInventory tierInventory;

    public InventoryManager(Main plugin) {
        this.plugin = plugin;
    }

    public void openInvSeeInventory(Player player, Player target) {
        new InvSeeInventory(target).open(player);
    }

    public void openTeamInventory(Player player) {
        ensureInventoriesCreated();
        refreshTeamInventory();
        teamInventory.open(player);
    }

    public void openScenarioInventory(Player player) {
        ensureInventoriesCreated();
        refreshScenarioInventory();
        scenarioInventory.open(player);
    }

    public void openTierInventory(Player player) {
        ensureInventoriesCreated();
        refreshTierInventory();
        tierInventory.open(player);
    }

    public void refreshTeamInventory() {
        ensureInventoriesCreated();
        teamInventory.update();
    }

    public void refreshScenarioInventory() {
        ensureInventoriesCreated();
        scenarioInventory.update();
    }

    public void refreshTierInventory() {
        ensureInventoriesCreated();
        tierInventory.update();
    }

    private void ensureInventoriesCreated() {
        if (scenarioInventory == null) {
            scenarioInventory = new ScenarioInventory(plugin);
        }

        if (teamInventory == null) {
            teamInventory = new TeamInventory(plugin.getTeamManager());
        }

        if (tierInventory == null) {
            tierInventory = new TierInventory(plugin);
        }
    }
}
