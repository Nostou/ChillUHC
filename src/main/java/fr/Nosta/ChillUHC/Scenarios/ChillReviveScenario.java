package fr.Nosta.ChillUHC.Scenarios;

import fr.Nosta.ChillUHC.Enums.GameState;
import fr.Nosta.ChillUHC.Enums.ScenarioType;
import fr.Nosta.ChillUHC.Main;
import org.bukkit.GameRule;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitTask;

public class ChillReviveScenario implements Scenario, Listener {

    private final Main plugin;
    private BukkitTask meetupTask;

    public ChillReviveScenario(Main plugin) {
        this.plugin = plugin;
        plugin.getGameManager().onGameStart.addListener((ignored) -> onGameStart());
        plugin.getGameManager().onGameStop.addListener((ignored) -> reset());
    }

    @Override
    public ScenarioType getType() {
        return ScenarioType.CHILL_REVIVE;
    }

    private void onGameStart() {
        reset();
        if (!plugin.getScenarioManager().isEnabled(getType())) return;

        World world = plugin.getWorld();
        world.setGameRule(GameRules.KEEP_INVENTORY, true);

        long meetupTicks = plugin.getBorderManager().getMeetupDuration() * 20L;
        meetupTask = plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (plugin.getGameManager().getState() != GameState.PLAYING) return;
            if (!plugin.getScenarioManager().isEnabled(getType())) return;

            world.setGameRule(GameRules.KEEP_INVENTORY, false);
        }, meetupTicks);
    }

    private void reset() {
        if (meetupTask != null) {
            meetupTask.cancel();
            meetupTask = null;
        }

        plugin.getWorld().setGameRule(GameRules.KEEP_INVENTORY, false);
    }
}
