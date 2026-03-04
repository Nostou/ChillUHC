package fr.Nosta.ChillUHC.Tasks;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.CompassManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CompassTask extends BukkitRunnable {

    private final Main plugin;
    private final CompassManager manager;

    public CompassTask(Main plugin, CompassManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    public void start(long periodTicks) {
        runTaskTimer(plugin, 0L, periodTicks);
    }

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            manager.updatePlayer(player);
        }
    }
}