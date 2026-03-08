package fr.Nosta.ChillUHC.Tasks;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.CompassManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassTask extends BukkitRunnable {

    private final Main plugin;
    private final CompassManager manager;

    public CompassTask(Main plugin) {
        this.plugin = plugin;
        this.manager = plugin.getCompassManager();
    }

    public void start() {
        runTaskTimer(plugin, 0L, 10L);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.updatePlayer(player);
        }
    }
}