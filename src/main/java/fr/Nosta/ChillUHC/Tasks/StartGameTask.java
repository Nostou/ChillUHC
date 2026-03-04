package fr.Nosta.ChillUHC.Tasks;

import fr.Nosta.ChillUHC.Utils.Broadcaster;
import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.SimpleEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class StartGameTask extends BukkitRunnable {

    public final SimpleEvent<Runnable> OnCompleted = new SimpleEvent<>();

    private final Main plugin;
    private int countdown = 3;

    public StartGameTask(Main plugin) {
        this.plugin = plugin;
    }

    public void start(long periodTicks) {
        runTaskTimer(plugin, 0L, periodTicks);
    }

    @Override
    public void run() {

        if (countdown > 0) {

            Broadcaster.titleAll(String.valueOf(countdown), NamedTextColor.GREEN, 0, 1000, 0);
            Broadcaster.soundAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, Integer.MAX_VALUE, 1);
            countdown--;
            return;
        }

        Broadcaster.titleAll("GO!", NamedTextColor.RED, 0, 1000, 1000);
        Broadcaster.soundAll(Sound.ENTITY_ENDER_DRAGON_GROWL, Integer.MAX_VALUE, 1);

        OnCompleted.invoke(this);
        cancel();
    }
}