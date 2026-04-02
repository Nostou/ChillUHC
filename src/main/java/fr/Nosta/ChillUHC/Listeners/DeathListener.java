package fr.Nosta.ChillUHC.Listeners;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Scenarios.ChillReviveScenario;
import fr.Nosta.ChillUHC.Utils.Broadcaster;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

public class DeathListener implements Listener {

    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(plugin.getSpawnLocation());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        ChillReviveScenario chillReviveScenario = plugin.getScenarioManager().getScenario(ScenarioType.CHILL_REVIVE, ChillReviveScenario.class);
        boolean automaticRevive = chillReviveScenario != null && chillReviveScenario.isAutomaticReviveActive();
        dropStuff(event, !automaticRevive);

        if (plugin.getGameManager().getState() == GameState.PLAYING) {
            Player player = event.getPlayer();
            player.setGameMode(GameMode.SPECTATOR);
            if (automaticRevive) chillReviveScenario.handleAutomaticDeath(player);
            else plugin.getReviveManager().recordDeath(player);
        }

        Broadcaster.soundAll(Sound.ENTITY_WITHER_SPAWN, 1, 1);
    }

    @EventHandler
    public void onTotemUse(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.getHand() == null) return;

        event.setCancelled(true);
        CustomMessage.error(player, "TOTEM is disabled.");
    }

    private void dropStuff(PlayerDeathEvent event, boolean dropGoldenApple) {
        Player player = event.getEntity();
        Location loc = player.getLocation();

        List<ItemStack> drops = new ArrayList<>(event.getDrops());
        if (dropGoldenApple) drops.add(new ItemStack(Material.GOLDEN_APPLE, 1));
        event.getDrops().clear();

        for (ItemStack item : drops) {
            if (item == null || item.getType().isAir()) continue;
            var dropped = player.getWorld().dropItem(loc, item);
            Vector velocity = new Vector((Math.random() - 0.5), 1, (Math.random() - 0.5)).multiply(0.25);
            dropped.setVelocity(velocity);
            dropped.setPickupDelay(20);
        }
    }
}
