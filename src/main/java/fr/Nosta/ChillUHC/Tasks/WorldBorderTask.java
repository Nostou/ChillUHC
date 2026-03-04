package fr.Nosta.ChillUHC.Tasks;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Managers.BorderManager;
import fr.Nosta.ChillUHC.Utils.CustomMessage;
import fr.Nosta.ChillUHC.Utils.SimpleEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class WorldBorderTask extends BukkitRunnable {

    public final SimpleEvent<Runnable> OnCompleted = new SimpleEvent<>();

    private final Main plugin;

    private long remainingSeconds;

    public WorldBorderTask(Main plugin, BorderManager manager) {
        this.plugin = plugin;
        this.remainingSeconds = manager.getMeetupDuration();
    }

    public void start(long periodTicks) {
        runTaskTimer(plugin, 0L, periodTicks);
    }

    @Override
    public void run() {
        if (remainingSeconds <= 0) {
            CustomMessage.infoAll("WorldBorder is now shrinking!");
            OnCompleted.invoke(this);

            cancel();
            return;
        }

        if (remainingSeconds % 600 == 0) {
            long minutesLeft = remainingSeconds / 60;
            CustomMessage.infoAll("WorldBorder will shrink in " + minutesLeft + " minutes!");
        }

        remainingSeconds--;
    }
}