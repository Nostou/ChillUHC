package fr.Nosta.ChillUHC.Tasks;

import fr.Nosta.ChillUHC.Main;
import fr.Nosta.ChillUHC.Utils.*;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Team;

import java.util.List;

public class StartGameTask extends BukkitRunnable {

    public final SimpleEvent<Runnable> onCompleted = new SimpleEvent<>();

    private final Main plugin;

    private final int levitationDuration = 2;
    private final int jumpDuration = 15;
    private int tick;

    public StartGameTask(Main plugin) {
        this.plugin = plugin;
    }

    public void start() {
        runTaskTimer(plugin, 0L, 20L);
    }

    @Override
    public void run() {
        if (tick == 0) startLevitation();
        if (tick == levitationDuration) spreadPlayers();
        if (tick == (levitationDuration+jumpDuration)) finish();
        tick++;
    }

    private void startLevitation() {
        Broadcaster.soundAll(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
        Broadcaster.titleAll("Get Ready !", NamedTextColor.YELLOW, 0, 1000, 1000);

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, levitationDuration * 20, 4, false, false));
        }
    }

    private void spreadPlayers() {
        Location center = plugin.getSpawnLocation();
        int radius = plugin.getBorderManager().getStartRadius();

        int teamCount = getTeamCount();
        boolean isFFA = teamCount == 0;
        int count = isFFA ? Bukkit.getOnlinePlayers().size() : teamCount;

        int minDistance = (int) (radius / Math.sqrt(count));
        minDistance = Math.max(minDistance, 50);

        List<Location> targetList = Spreader.generate(center, radius, count, minDistance);
        plugin.getLogger().warning((isFFA ? "FFA -> " : "TEAM -> " )+"Target count ["+targetList.size()+"]");

        if (isFFA) {
            int index = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                Jumper.jump(plugin, player, targetList.get(index++), 30, jumpDuration * 20);
            }
        }
        else {
            List<Team> teamList = plugin.getTeamManager().getAllTeams();

            int index = 0;
            for (Team team : teamList) {
                List<Player> players = TeamUtils.getPlayers(team);
                if (players.isEmpty()) continue;

                for (Player player : players) {
                    Jumper.jump(plugin, player, targetList.get(index), 30, jumpDuration * 20);
                }
                index++;
            }
        }
    }

    private int getTeamCount() {
        List<Team> teamList = plugin.getTeamManager().getAllTeams();

        int teamCount = 0;
        for (Team team : teamList) {
            if (!TeamUtils.getPlayers(team).isEmpty()) teamCount++;
        }

        return teamCount;
    }

    private void finish() {
        Broadcaster.soundAll(Sound.ENTITY_ENDER_DRAGON_GROWL, 1, 1);
        Broadcaster.titleAll("START!", NamedTextColor.RED, 0, 1000, 1000);

        onCompleted.invoke(this);
        cancel();
    }
}
