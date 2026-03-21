package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class BetaZombiesScenario implements Scenario, Listener {

    private final Main plugin;

    public BetaZombiesScenario(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.BETA_ZOMBIES;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!plugin.getScenarioManager().isEnabled(getType())) return;
        if (event.getEntityType() != EntityType.ZOMBIE) return;

        List<ItemStack> drops = event.getDrops();
        for (int i = 0; i < drops.size(); i++) {
            ItemStack drop = drops.get(i);
            if (drop == null || drop.getType() != Material.ROTTEN_FLESH) continue;

            drops.set(i, new ItemStack(Material.FEATHER, drop.getAmount()));
        }
    }
}
